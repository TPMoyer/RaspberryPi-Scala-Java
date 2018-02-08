package utils

import common.FileSystemManager._
import common.ExceptionHandler._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import com.typesafe.config._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem,RawLocalFileSystem,LocalFileSystem,Path,PathFilter,FileStatus,LocatedFileStatus,FSDataInputStream,FSDataOutputStream,FileUtil}
import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.File
import java.io.FileReader
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileOutputStream
import java.io.IOException
import java.time._
import java.time.format.DateTimeFormatter

import com.opencsv.CSVReader;
import scala.collection.JavaConversions._
import scala.util.Random
import scala.collection.immutable.ListMap
import utils.MiscellaneousUtils._

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import common.ExceptionHandler

object FileIO {
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	val yyyyMMdd_HHmmssFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
	private val ReadFileErrorCode = 304
	private val WriteBytesErrorCode = 303
	
	//TODO leave blue mark
	def fileI(
		 config     : Config
		,fid        : String
		,useLocalFS : Boolean=false
	): ArrayBuffer[String]={
		val fs = fileSystem(config,useLocalFS)
		log.info("about to attemptread on fs.schema="+fs.getScheme+"fileIdentifier="+fid)
		val lines= new ArrayBuffer[String]()
		val exists=fs.exists(new Path(fid))
		log.info("see file exists as "+exists)
		
		if(useLocalFS){
			val bufferedSource = Source.fromFile(fid)
			for(line<- bufferedSource.getLines) {
				lines+=line
			}
			bufferedSource.close
			//log.info("lines has "_lines.size+" rows")
			//lines.foreach { x=> log.info(x)}
		} else {
			val fsdis: FSDataInputStream = ExceptionHandler.callOrExit(ReadFileErrorCode,log,Some(s"Failed opening FSDataInputStream: $fid"))(fs.open(new Path(fid)))
			ExceptionHandler.usingOrExit(ReadFileErrorCode,log,new BufferedReader(new InputStreamReader(fsdis))){ case br =>
				var line = br.readLine
				while(line != null){
					log.info(line)
					lines += line
					line = br.readLine
				}
			}
		}
		if(lines.length <=0)ExceptionHandler.logStackTraceAndExit(log,new IOException(s"$fid is empty"),ReadFileErrorCode)
				
		lines
	}
	//TODO leave blue mark
	def fileO(
		 config     : Config	
		,fid        : String
		,body       : String
		,useLocalFS : Boolean = false
	): Unit = {
		log.info("inside FileO fid="+fid+" and useLocalFS="+(if(useLocalFS)"true"else"false"))
		val fs = fileSystem(config,useLocalFS)
		log.info("about to attempt writeBytes on fs.scheme="+fs.getScheme+" fileIDentifier="+fid)
		val fsdos:FSDataOutputStream = ExceptionHandler.callOrExit(WriteBytesErrorCode,log,Some(s"Failed creating path: $fid"))(fs.create(new Path(fid)))
		val os = new BufferedOutputStream(fsdos)
		os.write(body.toCharArray().map(_.toByte))
		os.close()
		log.info("wrote "+body.length+" bytes"+(if(body.length < 1000)"\n"+body+"\n\n**************** This marks the end of fileO (two rows above) **********\n" else ""))
	}
	//TODO leave blue mark
	/*
	 * Working with the FileStatus can be a bit off-putting at first, as the name of file/directory is down inside.
	 * The path is available as FileStatus.getPath.toString  and the name of the file is FileStatus.getPath.name.toString.
	 * 
	 * This pretty format, which stays aligned even when linux file creation times which happen on 0 milliseconds pull back to only 15 characters
	 * 
	 */
	def exampleFsFormat(
		 config : Config
		,dir    : String
	){
		val afs = getArrayOfFileAndDirFileStatuses(config,dir,true)
		log.info("directory tree below "+dir +" has "+afs.length+" FileStatus's")
		afs.foreach { x=> log.info(fsFormat(x))}
	}
	def fsFormat(
		a : FileStatus
	):String={
		f"${if(a.isDirectory)"dir        " else f"${a.getLen}%12d"}%12s ${LocalDateTime.ofInstant(Instant.ofEpochMilli(a.getModificationTime),ZoneId.systemDefault()).format(yyyyMMdd_HHmmssFormatter)}%s ${a.getPath.toString}%s"
	}
		
	/*
	 * Get a recursive listing of all files underneath th egiven directory.
	 * from https://stackoverflow.com/questions/2637643/how-do-i-list-all-files-in-a-subdirectory-in-scala
	 */
	def getRecursiveListOfFiles(dir: File): Array[File] = {
		val these = dir.listFiles
		these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
	}
	//TODO leave blue mark
	/* this will see both files and directories */
	def getArrayOfFileAndDirFileStatuses(
		 config      : Config
		,dir        : String
		,recursive  : Boolean = true
		,useLocalFS : Boolean=false
	):Array[FileStatus]={
		/**/log.info(f"dirString  getArrayOfFileAndDirFileStatuses $dir%s recursive=$recursive%-5s useLocalFS=$useLocalFS%-5s")
		val abfs=ArrayBuffer[FileStatus]()
		try{
			if(!recursive){
				fileSystem(config,useLocalFS).listStatus(new Path(dir)).foreach(abfs+=_)
			} else {
				val lof=fileSystem(config,useLocalFS).listStatus(new Path(dir))
				var level=0
				val anArray=lof++lof.filter(_.isDirectory).flatMap(getArrayOfFileAndDirFileStatusFileStatuses(config,_,true,useLocalFS,level+1))
				anArray.foreach(abfs+=_)
			}
		} catch {
			case _ : NullPointerException | _ : IllegalArgumentException | _ : java.io.FileNotFoundException =>
				log.info("requested a FileStatus on a non-existant foile or dir "+dir)
			case unknown : Throwable =>
				import org.apache.commons.lang3.exception.ExceptionUtils
				pnl("an exception occurred in a generic unknown : Throwable case\n"+ExceptionUtils.getStackTrace(unknown))
				pnl("\n\n****************************************Failed with unknown type exception *************************************")
				throw new Exception("unKnown exception")
		}
		abfs.toArray
	}
	/*
	 * expect folks to call the version with the much more normal second input (string instead of FileStatus here)
	 * This method exists as I had a bit of bother writing the recursive code (but then again who does'nt
	 * Here the problem was within the recursive call trying to cast _  as _.getPath.toString
	 * A second method seemed like a shameful but workable way out.
	 * Sometimes a programmer's gotta do what a programmemer's gotta do
	 */
	def getArrayOfFileAndDirFileStatusFileStatuses(
		 config     : Config
		,fileStatus : FileStatus
		,recursive  : Boolean
		,useLocalFS : Boolean=false
		,level      : Integer /* level of recursion, for diagnostics only */
	):Array[FileStatus]={
		/**/log.info(f"fileStatus $level%4d getArrayOfFileAndDirFileStatusFileStatuses ${fileStatus.getPath.toString}%s recursive=$recursive%-5s useLocalFS=$useLocalFS%-5s")
		val abfs=ArrayBuffer[FileStatus]()
		if(!recursive){
			fileSystem(config,useLocalFS).listStatus(fileStatus.getPath).foreach(abfs+=_)
		} else {
			val lof=fileSystem(config,useLocalFS).listStatus(fileStatus.getPath)
			var level=0
			val anArray=lof++lof.filter(_.isDirectory).flatMap(getArrayOfFileAndDirFileStatusFileStatuses(config,_,true,useLocalFS,level+1))
			anArray.foreach(abfs+=_)
		}
		abfs.toArray
	}
	//TODO leave blue mark
	/* this method returns only files, it is blind to directories */
	def getListOfFileFileStatuses(
		 config     : Config
		,dirName    : String
		,recurse    : Boolean
		,useLocalFS : Boolean = false
	): List[FileStatus] = {
		pnl(f"getArrayOfFileStatus(pipeBoundDir=|$dirName%s| recurse=$recurse%-5s useLocalFS=$useLocalFS%-5s")
		val alist=ArrayBuffer[FileStatus]()
		val fileStatusListIterator = fileSystem(config,useLocalFS).listFiles(new Path(dirName),recurse)
		while(fileStatusListIterator.hasNext())alist+=fileStatusListIterator.next()
		alist.toList
	}
	//TODO leave blue mark
	def rename(oldFid:String,newFid:String)={
		import util.Try
		Try(new File(oldFid).renameTo(new File(newFid))).getOrElse(false)
	}
	//TODO leave blue mark
	def delete(
		config         : Config
		,fileOrDirName : String
		,recurse       : Boolean = false
		,useLocalFS    : Boolean = false
		) : Boolean = {
		log.info("about to attempt delete on "+fileOrDirName+" recurse="+recurse.toString+" useLocalFS="+useLocalFS.toString())
		fileSystem(config,useLocalFS).delete(new Path(fileOrDirName),recurse)
	}
	//TODO leave blue mark
	def deleteFileStatus (
		 config              : Config
		,fileOrDirFileStatus : FileStatus
		,recursive           : Boolean = false
		,useLocalFS           : Boolean = false
	) : Unit = {
		val fs = fileSystem(config,useLocalFS)
		/**/log.info("pre "+(if(fs.exists(fileOrDirFileStatus.getPath))"true "else "false")+" delete on "+fsFormat(fileOrDirFileStatus)+" recursive="+recursive)
		val rc=fs.delete(fileOrDirFileStatus.getPath,recursive)
		/**/log.info("post "+f"${fs.exists(fileOrDirFileStatus.getPath).toString}%5s with rc reporting $rc%s")
	}
	//TODO leave blue mark
	def copyFile(
		 config         : Config
		,from           : String
		,to             : String
		,useFromLocalFS : Boolean = false
		,useToLocalFS   : Boolean = false
		) : Long = {
		log.info("about to attempt copy of \nfrom="+from+"\n  to="+to+"\nuseFromLocalFS="+useFromLocalFS.toString())
		var bytes = 0L
		FileUtil.copy(
			fileSystem(config,useFromLocalFS)
			,new Path(from)
			,fileSystem(config,useToLocalFS)
			,new Path(to)
			,false
			,false
			,new Configuration
		)
		fileSystem(config,useToLocalFS).listStatus(new Path(to)).map(_.getLen).foreach(bytes += _)
		bytes
	}
	//TODO leave blue mark
	def fullyDeleteContents(
		 config     : Config
		,dir        : String
		,useLocalFS : Boolean = false
	): Boolean = {	
		log.info("cme@ fullyDeleteContents on dir="+dir+" useLocalFS="+useLocalFS)
		/* on a cloudera system, encounterd behavior different from the documentd behavoior 
		/* delete was NOT able to function recursively *?
		 * needed to put ia prissy protocal into place and adoptted this procedure:
		 * First go thru and delete all the files.
		 * Then put the directory names in length order (longest first), and delete them one by one.
		 * This ensured that no non-empty directories have delete attempts */
		 */ 
		val afs=getArrayOfFileAndDirFileStatuses(config,dir,true,useLocalFS)
		afs.filter(_.isFile).foreach(x => {
			//log.info("going for delete on "+fsFormat())
			deleteFileStatus(config,x)
		})
		afs.filter(_.isDirectory()).sortBy(_.getPath.toString.length*(-1)).foreach{x => {
			//log.info("going for delete on "+fsFormat(x))
			deleteFileStatus(config,x)
		}}
		/* have not fleshed out the error handling, so behavior now is to always return true */
		true
	}
	//TODO leave blue mark
	/*within the directory dir, delete any files not in the fileNameArray array */
	def fullyDeleteContentsExcept4(
		 config        : Config
		,dir           : String
		,fileNameArray : Array[String]
		,useLocalFS    : Boolean = false
		,loud          : Boolean = false
) : Boolean ={
		val fs=fileSystem(config,useLocalFS)
		if(loud){
			log.info("cme@ fullyDeleteContentsExcept4 on dir="+dir+" fileNameArray is "+fileNameArray.length+" long and is:")
			for((f,ii)<-fileNameArray.zipWithIndex)log.info(f"$ii%2d $f%s")
			
			val afs=getArrayOfFileAndDirFileStatuses(config,dir,true,useLocalFS)
			log.info("afs is "+afs.length+" long")
			for((a,ii)<-afs.zipWithIndex)log.info(f"$ii%2d $a%s")
			val afsFiltered=afs.filter(f => !fileNameArray.contains(f.getPath.toString().substring(f.getPath.toString.lastIndexOf("/")+1)))
			log.info("afsFiltered is "+afsFiltered.length+" long")
			for((a,ii)<-afsFiltered.zipWithIndex)log.info(f"$ii%2d $a%s")
			afsFiltered.foreach(d => {
				log.info("going for delete on "+fsFormat(d))
				fs.delete(d.getPath,true)
			})
		} else {
			getArrayOfFileAndDirFileStatuses(config,dir,true,useLocalFS)
			.filter(f => !fileNameArray.contains(f.getPath.toString().substring(f.getPath.toString.lastIndexOf("/")+1)))
			.foreach(d=> fs.delete(d.getPath,true))
		}
		/* have not fleshed out the error handling, so behavior now is to always return true */
		true
	
	}
	//TODO leave blue mark
  /*
  * Generates big test file.
  */
  def generateBigFile(fid:String,megabytes: Int) = {
    val kilobytes = megabytes.toLong * 1024
    val buffer = new Array[Byte](1024)
    val file = new File(fid)
    val out = new BufferedOutputStream(new FileOutputStream(file))
    try {
      var idx = 0L
      while (idx < kilobytes) {
        Random.nextBytes(buffer)
        out.write(buffer)
        idx += 1
      }
      file
    } finally {
      out.close()
    }
  }
  //TODO leave blue mark
	def readOpenCsvFile(fid:String, hasHeader:Boolean, header:ArrayBuffer[String]): ArrayBuffer[Array[String]]= {
		val reader = new CSVReader(new FileReader(fid))
		val rows=ArrayBuffer[Array[String]]()
		reader.readAll().foreach { row => rows+=row }
		reader.close
		if(hasHeader){
			rows(0).foreach { col => header+=col}
			rows.remove(0)
		}
		/* It has occured that a manually edited .csv file had a fully empty last row.  
		 * Purge this if it occurs
		 */
		var purge=true
		rows(rows.length-1).foreach { x => if(0<x.length())purge=false }
		if(purge){
			log.info("last line of "+fid+" is completely blank.  Am purging")
			rows.remove(rows.length-1)
		}
		rows
	}
	//TODO leave blue mark
	/* This follows "How to Process a CSV File" from the Scala Cookbook by Alvin Alexander */
	/* as such it is fine for simple csv files, but as soon as anything like an imbedded comma occurs
	 * this parses the lines badly
	 */
	//  def   readCSVFile(fid:String, rows:ArrayBuffer[Array[String]],hasHeader:Boolean, header:ArrayBuffer[String]): Unit = {
	//    if(hasHeader){
	//      using(Source.fromFile(fid)) { source =>
	//        for (line <- source.getLines.take(1)) {
	//          line.split(",",-1).map(_.trim).foreach{ x => header+=x }
	//        }
	//      }
	//    }
	//    using(Source.fromFile(fid)) { source =>
	//      for (line <- source.getLines.drop(if(hasHeader)1 else 0)) {
	//        rows += line.split(",",-1).map(_.trim)
	//      }
	//    }
	//  }
	//def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
	//	try {
	//		f(resource)
	//	} finally {
	//		resource.close()
	//	}
}