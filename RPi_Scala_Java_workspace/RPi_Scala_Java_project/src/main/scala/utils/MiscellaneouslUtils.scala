package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory


import scala.collection.immutable.List
import scala.collection.mutable.ArrayBuffer
//import org.apache.log4j.{Level, Logger, LogManager, PropertyConfigurator}
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
//import org.apache.spark.sql.sqlContext.implicits._
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import com.typesafe.config.Config
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.{MultivariateStatisticalSummary, Statistics}

import utils.FileIO._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem,LocalFileSystem,RawLocalFileSystem,Path,FileStatus}
//import java.io.File
import sys.process._
import org.apache.commons.lang3.exception.ExceptionUtils
import common.FileSystemManager

object MiscellaneousUtils  {
	@transient lazy val log = org.apache.log4j.LogManager.getLogger("MiscellaneousUtils")

	//TODO leave blue mark
	/* repeated my google search on Alvin Alexander's   scala try catch  enough times that I desided to keep a template here */ 
	def tryCatchFinally(){
		import org.apache.commons.lang3.exception.ExceptionUtils
		var retVal=0
		val someValue = "myAppName"
		try {
			val aVal="123-456"
		} catch {
			case e: Exception =>
				retVal=1
				log.error("an exception occurred under  "+someValue+":\n" + ExceptionUtils.getStackTrace(e))
				println("\n\n*************************************************Failed with exception *******************************************\n")
				throw new Exception("Spark Error")
			case unknown : Throwable =>
				retVal=2
				log.error("an exception occurred under  "+someValue+":\n" + ExceptionUtils.getStackTrace(unknown))
				println("\n\n*************************************************Failed with unknown type exception *******************************************\n")
				throw new Exception("Spark Error")      
		} finally {
			/* some final act or acts */
			log.debug("retval="+retVal)
		}
	}
	def printAndLog(msg:String) {println(msg);log.info(msg)}
	def pnl        (msg:String) {println(msg);log.info(msg)}
	/* Am using the hadoop filesystem, after being exposed to it in my big data day job.
	 * https://hadoop.apache.org/docs/r2.7.1/api/index.html?org/apache/hadoop/fs/FileSystem.html
	 * No I do not expect to be making a hadoop cluster with Raspberry Pi's (although I do think it's cool that some are doing so)
	 * My first exposure was when I wanted a single FS able to handle multiple simultanious file systems: hadoop hdfs & the linux local filesystem.
	 * Am retaining it's use here because I forsee RPi's involved in the IOT, and this FS has several intriguing direct known subclasses. 
	 * These subclasses include NativeS3FileSystem, NativeAzureFileSystem, and FTPFileSystem.   
	 * My expectation is that future modification of my code base to include these remote filesystems will be easier from a familiar Apache FileSystem base. 
	 */
	def getFileSystemStuff():String={
		import java.text.NumberFormat
		val sb = new StringBuilder()
		val fs   = FileSystemManager.hdfs
		val rlfs = FileSystemManager.rlfs
		val lfs  = FileSystemManager.lfs
		val conf = FileSystemManager.conf
		
		var msg=""
		val fsStatus=fs.getStatus
		val sd=fs.getServerDefaults
		msg=f"\n\nfs default FileSystem\n Capacity=${NumberFormat.getNumberInstance.format(fsStatus.getCapacity)}%20s"+
				f"\nRemaining=${NumberFormat.getNumberInstance.format(fsStatus.getRemaining)}%20s"+
				f"\n     Used=${NumberFormat.getNumberInstance.format(fsStatus.getUsed)}%20s"+ 
				"\nscheme="+fs.getScheme+
				"\nworkingDirectory="+fs.getWorkingDirectory+
				"\nFileSystem="+fs.getHomeDirectory.getFileSystem(conf)+
				"\nhomeDirectory="+fs.getHomeDirectory.toString+
				"\nBlockSize="+sd.getBlockSize+
				"\nBytesPerChecksum="+sd.getBytesPerChecksum+
				"\nChecksumType="+sd.getChecksumType+
				"\nEncryptDataTransfer="+sd.getEncryptDataTransfer+
				"\nFileBufferSize="+sd.getFileBufferSize+
				"\nReplication="+sd.getReplication+
				"\nTrashInterval="+sd.getTrashInterval+
				"\nWritePacketSize="+sd.getWritePacketSize+
				"\n"
		//log.info(msg)
		sb.append(msg)
		val rlfsStatus= rlfs.getStatus()
		msg=f"\n\nrlfs RawLocalFileSystem\n Capacity=${NumberFormat.getNumberInstance.format(rlfsStatus.getCapacity)}%20s"+
				f"\nRemaining=${NumberFormat.getNumberInstance.format(rlfsStatus.getRemaining)}%20s"+
				f"\n     Used=${NumberFormat.getNumberInstance.format(rlfsStatus.getUsed)}%20s"+
			 "\nworkingDirectory="+rlfs.getWorkingDirectory+
			 "\nFileSystem="+rlfs.getHomeDirectory.getFileSystem(conf)+
			 "\nhomeDirectory="+rlfs.getHomeDirectory.toString+"\n"
		//log.info(msg)
		sb.append(msg)
		val lfsStatus : org.apache.hadoop.fs.FsStatus=lfs.getStatus
		msg=f"\n\nlfs LocalFileSystem\n Capacity=${NumberFormat.getNumberInstance.format(lfsStatus.getCapacity)}%20s"+
				f"\nRemaining=${NumberFormat.getNumberInstance.format(lfsStatus.getRemaining)}%20s"+
				f"\n     Used=${NumberFormat.getNumberInstance.format(lfsStatus.getUsed)}%20s"+ 
				"\nscheme="+lfs.getScheme+"\nworkingDirectory="+lfs.getWorkingDirectory+"\nFileSystem="+lfs.getHomeDirectory.getFileSystem(conf)+
				"\nhomeDirectory="+lfs.getHomeDirectory.toString+"\n"
	  sb.append(msg)
	  sb.toString
	}
	
	def getAsciiAsPrints3WideStrings(
	): Array[String] = {
		//log.info("cme@ getAsciiAsPrints3WideStrings")
		val ascii = Array.fill(267)(" ")
		ascii( 0)="nul"
		ascii( 1)="SOH"
		ascii( 2)="STX"
		ascii( 3)="ETX"
		ascii( 4)="EOT"
		ascii( 5)="ENQ"
		ascii( 6)="ACK"
		ascii( 7)="BEL"
		ascii( 8)=" BS"
		ascii( 9)="tab"
		ascii(10)=" lf"
		ascii(11)=" VT"
		ascii(12)=" FF"
		ascii(13)=" cr"
		ascii(14)=" SO"
		ascii(15)=" SI"
		ascii(16)="DLE"
		ascii(17)="DC1"
		ascii(18)="DC2"
		ascii(19)="DC3"
		ascii(20)="DC4"
		ascii(21)="NAK"
		ascii(22)="SYN"
		ascii(23)="ETB"
		ascii(24)="CAN"
		ascii(25)=" EM"
		ascii(26)="SUB"
		ascii(27)="ESC"
		ascii(28)=" FS"
		ascii(29)=" GS"
		ascii(30)=" RS"
		ascii(31)=" US"
		ascii(32)=" sp"
		for(ii<-33 until 128)ascii(ii)="  "+ii.toChar.toString
		ascii(128)="  €"
		ascii(129)="   "
		ascii(130)="  ‚"
		ascii(131)="  ƒ"
		ascii(132)="  „"
		ascii(133)="  …"
		ascii(134)="  †"
		ascii(135)="  ‡"
		ascii(136)="  ˆ"
		ascii(137)="  ‰"
		ascii(138)="  Š"
		ascii(139)="  ‹"
		ascii(140)="  Œ"
		ascii(141)="   "
		ascii(142)="  Ž"
		ascii(143)="   "
		ascii(144)="   "
		ascii(145)="  ‘"
		ascii(146)="  ’"
		ascii(147)="  “"
		ascii(148)="  ”"
		ascii(149)="  •"
		ascii(150)="  –"
		ascii(151)="  —"
		ascii(152)="  ˜"
		ascii(153)="  ™"
		ascii(154)="  š"
		ascii(155)="  ›"
		ascii(156)="  œ"
		ascii(157)="   "
		ascii(158)="  ž"
		ascii(159)="  Ÿ"
		ascii(160)="  ¡"
		ascii(161)="  ¢"
		val seed =    "  €     ‚  ƒ  „  …  †  ‡  ˆ  ‰  Š  ‹  Œ     Ž        ‘  ’  “  ”  •  –  —  ˜  ™  š  ›  œ     ž  Ÿ  ¡  ¢  £  ¤  ¥  ¦  §  ¨  ©  ª  «  ¬  ­  ®  ¯  °  ±  ²  ³  ´  µ  ¶  ·  ¸  ¹  º  »  ¼  ½  ¾  ¿  À  Á  Â  Ã  Ä  Å  Æ  Ç  È  É  Ê  Ë  Ì  Í  Î  Ï  Ð  Ñ  Ò  Ó  Ô  Õ  Ö  ×  Ø  Ù  Ú  Û  Ü  Ý  Þ  ß  à  á  â  ã  ä  å  æ  ç  è  é  ê  ë  ì  í  î  ï  ð  ñ  ò  ó  ô  õ  ö  ÷  ø  ù  ú  û  ü  ý  þ  ÿ  ÝtooHi"
		for(ii<-128 until 256)ascii(ii)=seed.substring(3*(ii-128),3*(ii-127))
		ascii(256)="2Hi"
		ascii(257)="Ndg"
		ascii(258)="Let"
		ascii(259)="Dig"
		ascii(260)="typ"
		ascii(261)="NTP"
		ascii(262)="NAN"
		ascii(263)="NTS"
		ascii(264)="L- "
		ascii(265)="NL-"
		ascii(266)="LMZ"
		//for(ii<- 0 until 267)log.info(s"%3d %s %s %s %s".format(ii,ascii(ii),ii.toChar.isDigit,ii.toChar.isLetter,ii.toChar.isLetterOrDigit))
		ascii
	}
}