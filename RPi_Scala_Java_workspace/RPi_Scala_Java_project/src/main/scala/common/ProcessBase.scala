package common

import org.apache.spark._
import org.apache.spark.sql._
import org.apache.spark.sql.types._
import com.typesafe.config.{ConfigFactory, Config}
import java.util.Properties
import utils._
import java.time._
import utils.MiscellaneousUtils._
import common.PropertiesFileHandler._
import scala.collection.mutable.ArrayBuffer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait ProcessBase {
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def run(config: Config): Integer
	
	def	setupConfig(
		 args                    : Array[String]
		,appStart                : String
		,invariantLogFileName    : String
		,uniquePerRunLogFileName : String
		,appClassName            : String
	):Config = {
		System.setProperty("appStart",appStart)
		System.setProperty("invariantLogFileName"   ,invariantLogFileName)
		System.setProperty("uniquePerRunLogFileName",uniquePerRunLogFileName)
		val sb = new StringBuilder()
		var msg="cme@ ProcessBase.setupConfig";sb.append(msg+"\n")
		var gotAnArgFidProperties=false
		val target0=".config"
		val target1=".properties"
		var argFidT2IMD=""
		var config = ConfigFactory.empty()
		
		val targets = Seq(".config", ".properties")
		val numPropertiesFiles=targets.map{t=> args.map { x => if((x.length>t.length)&&(0==x.substring(x.length()-t.length()).compareToIgnoreCase(t)))1 else 0 }.foldLeft(0)(_+_)}.foldLeft(0)(_+_)
		msg=appClassName+" started. "+LocalDateTime.now.toString.replace("T"," ")+"  saw "+numPropertiesFiles+" properties files"
		println(msg)
		sb.append(msg+"\n")
		for ((arg,ii)<-args.zipWithIndex){msg=f"arg($ii%2d)=$arg%s";println(msg);sb.append(msg+"\n")}
			
		//println(System.getProperty("os.name")+" "+System.getProperty("user.dir"))
		if(0==numPropertiesFiles) {
			var haveLog4JDotProperties = false
			val fidArray=new ArrayBuffer[String]
			fidArray+=System.getProperty("user.dir")+"/src/main/resources/"+(if(System.getProperty("os.name").startsWith("Window"))"Windows" else "Linux")+"_Files/log4j_windows.properties"
			fidArray+="./log4j.properties"
			fidArray+="./log4j_linux.properties"
			fidArray+="./log4j_windows.properties"
			fidArray.filter(fid =>new java.io.File(fid).exists).foreach{fid =>
				msg="no .config or .properties recieved as CLI args.  Eclipse log4j.properties detected, and is being applied\nfid="+fid
				println(msg)
				sb.append(msg)
				config=PropertiesHandler.propertiesHandler(fid,config,numPropertiesFiles)
				haveLog4JDotProperties=true
			} 
			/* if the user has not provisioned any log4j.properties variant, write one ourselves*/
			if(false==haveLog4JDotProperties){
				msg="no .config or .properties recieved as CLI args, and the default eclipse log4j.properties could not be found.\nImplementing default logging to current location.\n"+System.getProperty("user.dir")+"/"+appClassName+".log"
				println(msg)
				sb.append(msg)
				val sbF=new StringBuilder()
				sbF.append("log4j.rootLogger=INFO,fileout0\n")
				sbF.append("# notes:\n")
				sbF.append("#  Threshold takes precedence over programatic log.setLevel \n")
				sbF.append("#  The $ variables are set within PropertiesFileHandler.scala prior to calling the PropertyConfigurator.configure\n")
				sbF.append("\n")
				sbF.append("# Direct log messages to file name which does not change run to run\n")
				sbF.append("log4j.appender.fileout0=org.apache.log4j.FileAppender\n")
				sbF.append("log4j.appender.fileout0.File=./"+appClassName+".log\n")
				sbF.append("log4j.appender.fileout0.ImmediateFlush=true\n")
				sbF.append("log4j.appender.fileout0.Threshold=debug\n")
				sbF.append("log4j.appender.fileout0.Append=false\n")
				sbF.append("log4j.appender.fileout0.layout=org.apache.log4j.PatternLayout\n")
				sbF.append("log4j.appender.fileout0.layout.conversionPattern=%-5p %8r %3L %c{1} - %m%n\n")
				import java.io._
				val fid="./log4j.properties"
				val bw = new BufferedWriter(new FileWriter(new File(fid)))
				bw.write(sbF.toString())
				bw.close
				config=PropertiesHandler.propertiesHandler(fid,config,numPropertiesFiles)				
			}
			//val log4jFid=(if(System.getProperty("os.name").startsWith("Window")
		}else {
			println("")
		}
		
		args.foreach { arg =>
			var handled=false
			if(  (  (arg.length>target0.length)
					   &&(0==arg.substring(arg.length()-target0.length()).compareToIgnoreCase(target0))
					  )
					||(  (arg.length>target1.length)
					   &&(0==arg.substring(arg.length()-target1.length()).compareToIgnoreCase(target1))
					  )
				){
				//this.logDebug("see properties "+arg)
				config=PropertiesHandler.propertiesHandler(arg,config,numPropertiesFiles)
				gotAnArgFidProperties=true
				handled=true
			}
			if(  arg.toLowerCase().endsWith(".log")){
				val props= new Properties()
				props.setProperty("logFid",arg)
				config=ConfigFactory.parseProperties(props).withFallback(config)
				handled=true
			}
			if(!handled){
				printAndLog("argument not handled:"+arg+"\n")
			}
		}
		
		config=PropertiesHandler.tackonSysEnv(config)
		log.info(sb.toString())
		return config;  
	}
	
	def go4it(config: Config,printFileSystemStuff: Boolean): Int = {
		log.info("cme@ go4it")
		
		val sb = new StringBuilder()
		var ranWithErrors = 0
		
		val fss=getFileSystemStuff()
		log.info(fss)
		if(printFileSystemStuff)println(fss)
		
		run(config)
		
		log.info("atEndOf go4it ranWithErrors=" + ranWithErrors)
		ranWithErrors
	}	
	
	def printAndLogTaskMsg(retVal:Long,beginTime:Long,task:String) {
		val msg="\n"+(if(0==retVal)"Success"else"FAIL")+f" retVal="+retVal+ " "+task +f"  "
		printAndLog(msg)
	}
}