package common

import scala.collection.mutable.ArrayBuffer
import com.typesafe.config._
import java.io.File
import java.util.Properties
import scala.io.Source
import org.apache.log4j.{Level, Logger, LogManager, PropertyConfigurator}
import java.text.SimpleDateFormat;
import scala.collection.JavaConversions._
import java.util.Date;

import utils.CustomizeProperties._

object PropertiesFileHandler  {
	@transient lazy val log = org.apache.log4j.LogManager.getLogger("PropertiesFileHandler")
	
	def propertiesFileHandler(fid:String, confIn:Config, numPropertiesFiles:Int): Config={
		//println("inside propertiesFileHandler fid="+fid)
		var msg=""
		val pre0Config=ConfigFactory.parseFile(new File(fid)).resolve().withFallback(confIn)
		val propA = new Properties()
		propA.setProperty("log4jPropertiesHasBenSet","false")
		if(pre0Config.isEmpty()){
			val errorOut="no properties found in "+fid
			log.error(errorOut)
			println(errorOut)
			return pre0Config
		} else {
			//println("pre0Config is not empty")
			try{
				pre0Config.getBoolean("log4jPropertiesHasBeenSet")
			} catch {
				// more specific cases first 
				case ce:  ConfigException=>  
					/* go thru the propterties and if there are log4j properties, configure the logger */
					var seeLog4j=false
					val iteratorPC = pre0Config.entrySet().iterator();
					while(iteratorPC.hasNext()) {
						val entry = iteratorPC.next()
						if(entry.getKey().startsWith("log4j")){
							seeLog4j=true
							//println("see log4j property "+entry.getKey() +" = "+entry.getValue().render())
						}
					}
					if(seeLog4j){
						
						var sjc=System.getProperty("sun.java.command")
						//println("seeLog4j with fid="+fid+" "+System.getProperty("os.name") + "\n|"+sjc+"|")
						val fsRoot=System.getProperty("fsRoot")
						//println("System.getProperty(\"fsRoot\")="+System.getProperty("fsRoot"))
						if(sjc.contains(" "))sjc=sjc.substring(0,sjc.indexOf(' ')) /* when the CLI has arguments, want to have the log file name use only the initial app name */
						//println(System.getProperty("os.name") + "|"+sjc+"|")
						if(null==fsRoot)try{System.setProperty("fsRoot",pre0Config.getString("fsRoot"))}catch{
							case ce: ConfigException=> msg="attempted to set the optional fsRoot, but property not present"
							println(msg)
						}
						if(  ("Linux"==System.getProperty("os.name"))
							 &&(sjc.startsWith("./"))
							){ 
								sjc=sjc.substring(2);
						} else 
						if(sjc.contains("\\")){
							sjc=sjc.substring(1+sjc.lastIndexOf("\\"))
						}   
						if("org.apache.spark.deploy.SparkSubmit"==sjc){
							/* */println("demurring from having bla.bla.bla.SparkSubmit as the log file name.  will use the spark.app.name instead")
							var s2t=""
							try{
								 s2t=pre0Config.getString("source2TargetCsvFid")
								 s2t=s2t.substring((1+s2t.lastIndexOf(if(s2t.contains("/")) "/" else "\\")),s2t.lastIndexOf("_"))
								 println("s2t="+s2t)
							} catch{
								case ce: ConfigException=>
									val msg="desired property not present: source2TargetCsvFile"
									println(msg)
							}
							sjc=System.getProperty("spark.app.name")+(if(0<s2t.length)"_"+s2t else "")
						}
						try{sjc=sjc+"_"+pre0Config.getString("logNameAppender")}catch{case ce: ConfigException=> msg="attempted to set the optional logNameAppender, but  property is not present"; println(msg)}
						/**/println("first part of log file name shown as bounded by pipes=|"+sjc+"|")
						System.setProperty("package.class",sjc)
						val dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
						System.setProperty("appStart.yyyyMMddHHmmss", dateFormat.format(new Date()));
						PropertyConfigurator.configure(fid)
						
						val log=LogManager.getRootLogger()
						propA.setProperty("log4jPropertiesHasBenSet","true")
						
						//log.setLevel(Level.TRACE)
						log.setLevel(Level.INFO)
						log.trace  ("logger level check main trace") /* trace will not output, when the .properties Threshold is set higher than trace, even if setLevel IS set to trace */
						log.debug  ("logger level check main debug")
						log.info   ("logger level check main info")
						log.warn   ("logger level check main warn")
						log.error  ("logger level check main error")
					}
				case e: Exception => 
					log.info("e Exception. non-specific-handled error ") 
					e.printStackTrace()
			}
			//  println("size of the properties file config.entrySet()="+pre0Config.entrySet().size()
		}
		
		log.info("setting default properties")
		val props= new Properties()
		props.setProperty("onCluster",Option(System.getProperty("SPARK_SUBMIT")).getOrElse("false"))
		props.setProperty("suppressLoggingEnvironmentProperties"    ,"true" ) /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("suppressLoggingPropertiesUntillFinalFile","true") /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("suppressLoggingSystemProperties"         ,"true" ) /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("suppressLoggingTheseProperties"          ,"false") /* setting default so as to avoid exception for missing configuration setting */
		
		customizeProperties(pre0Config,props)
		
		try{
			if(  (0<pre0Config.getString("hiveTableNamePrepender").length())
				 &&(! pre0Config.getString("hiveTableNamePrepender").endsWith("_")) 
				){
				propA.setProperty("hiveTableNamePrepender",pre0Config.getString("hiveTableNamePrepender")+"_")
			}
		} catch {
			// more specific cases first 
			case ce:  ConfigException=>  
				//log.info("encountered case of no hiveTableNamePrepender.  This is fine.. is defaulted to \"\"\n"+ce.getLocalizedMessage())
			case e: Exception => 
				log.info("e Exception. non-specific-handled error ") 
				e.printStackTrace()
		}
		
		log.info("os="+System.getProperty("os.name"))
		
		try{
			if(  (0<pre0Config.getString("userPasswordFidOnWindows4Linux").length())
				 &&("Linux" != System.getProperty("os.name"))
			){
				val bufferedSource = Source.fromFile(pre0Config.getString("userPasswordFidOnWindows4Linux"))
				for(line<- bufferedSource.getLines){
					propA.setProperty("userPassword4Linux",line)
				}
			}
		} catch {
			// more specific cases first 
			case ce:  ConfigException=>  
				//log.info("encountered case of no hiveTableNamePrepender.  This is fine.. is defaulted to \"\"\n"+ce.getLocalizedMessage())
			case e: Exception => 
				log.info("e Exception. non-specific-handled error ") 
				e.printStackTrace()
		}
		/* For use in bash scripts, need to have the beelineOptions and the impalaOptions have leading and trailing double-quotes.
		 * For direct LinuxControlFromWindows, need to have no bounding double-quotes.
		 */
		try{
			if(0<pre0Config.getString("beelineOptions").length()){
				val blo=pre0Config.getString("beelineOptions")
				log.info("see beelineOptions="+blo+" equals  "+('"'==blo.charAt(0)) )
				if('"'==blo.trim.charAt(0)){
					val reblo=blo.trim.substring(1,blo.trim.length()-1)
					propA.setProperty("beelineOptions",reblo)
				}
			}
		} catch {
			// more specific cases first 
			case ce:  ConfigException=>  
				//log.info("encountered case of no hiveTableNamePrepender.  This is fine.. is defaulted to \"\"\n"+ce.getLocalizedMessage())
			case e: Exception => 
				log.info("e Exception. non-specific-handled error ") 
				e.printStackTrace()
		}
		/* For use in bash scripts, need to hav ethe beelineOptions and the impalaOptions have leading and trailing double-quotes.
		 * For direct LinuxControlFromWindows, need to have no bounding double-quotes.
		 */
		try{
			if(0<pre0Config.getString("impalaOptions").length()){
				val blo=pre0Config.getString("impalaOptions")
				log.info("see impalaOptions ="+blo+" equals  "+('"'==blo.charAt(0)) )
				if('"'==blo.trim.charAt(0)){
					propA.setProperty("impalaOptions",blo.trim.substring(1,blo.trim.length()-1))
				}
			}
		} catch {
			// more specific cases first 
			case ce:  ConfigException=>  
				//log.info("encountered case of no hiveTableNamePrepender.  This is fine.. is defaulted to \"\"\n"+ce.getLocalizedMessage())
			case e: Exception => 
				log.info("e Exception. non-specific-handled error ") 
				e.printStackTrace()
		}
		
		try{
			val soFar=confIn.getInt("numPropertiesFilesHandled")
			propA.setProperty("numPropertiesFilesHandled",(soFar+1).toString())
		} catch {
			// more specific cases first 
			case ce:  ConfigException=>  
				//log.info("first properties file trips this ConfigException case \n"+ce.getLocalizedMessage())
				props.setProperty("numPropertiesFiles",f"$numPropertiesFiles%d")
				props.setProperty("numPropertiesFilesHandled","1")
			case e: Exception => 
				log.info("e Exception. non-specific-handled error ") 
				e.printStackTrace()
		}
		
		//customizeProperties(props,pre0Config) /* additional configuration properties are constructed by compositing input properties */ 
		
		var config=ConfigFactory.parseProperties(propA).withFallback(pre0Config.withFallback(ConfigFactory.parseProperties(props)))
		val ab = new ArrayBuffer[String]()
		
		
		//log.info("110 got config.getInt(\"numPropertiesFilesHandled\")="+config.getInt("numPropertiesFilesHandled")+" vs config.getInt(\"numPropertiesFiles\")="+config.getInt("numPropertiesFiles"))
		if(  (false==config.getBoolean("suppressLoggingTheseProperties"))
			 &&(  (false==config.getBoolean("suppressLoggingPropertiesUntillFinalFile"))
					||(config.getInt("numPropertiesFilesHandled")>=config.getInt("numPropertiesFiles"))
				 ) 
		){
			val iteratorPC = config.entrySet().iterator();
			while(iteratorPC.hasNext()) {
				val entry = iteratorPC.next()
				val in=entry.getValue().render().trim()
				val inl=entry.getKey().trim().toLowerCase()
				//log.info("inl="+inl)
				ab+=String.format("App %-38s %s",entry.getKey(),(
					if(  (  (inl.contains("password"))
								||(inl.contains("access_key"))
							 ) 
						 &&(!inl.contains("passwordfid"))
					 ){"XXXXXXXX"}else {in}
				))
			}
		}
		
		log.info("properties after "+fid+" config now has "+config.entrySet().size()+" "+ab.size+" entries "+config.getBoolean("suppressLoggingTheseProperties"))
		if(!config.getBoolean("suppressLoggingTheseProperties")){
			ab.sortWith(_.toLowerCase()<_.toLowerCase()).foreach { x => log.info(x) }
		}
		if(System.getProperty("os.name").startsWith("Win")){
			/* http://www.srccodes.com/p/article/39/error-util-shell-failed-locate-winutils-binary-hadoop-binary-path */
			if(!new File(config.getString("winutilsDotExeLocation")).exists){
				log.info("\n\nFAIL at attempt to not get   Shell - Failed to locate the winutils binary in the hadoop binary path\nIt seems the path to a winutils.exe has not been set correctly in the .properties file.\ncurrently shows as "+config.getString("winutilsDotExeLocation")+"\n\n")
			} else {
				log.info("pre-empting    Failed to locate the winutils binary in the hadoop binary path.")
				log.info("succesful with .properties value of     winutilsDotExeLocation="+config.getString("winutilsDotExeLocation")) 
				System.setProperty("hadoop.home.dir", config.getString("winutilsDotExeLocation"))
			}
		}
		
		//log.info("exiting propertiesFileHandler.propertiesFileHandler")
		config
	}
	/* Additional configuration properties are constructed by compositing input properties */
//  def customizeProperties(props:Properties, config:Config) ={
//    try{
//      val lndDB=
//        config.getString("databaseNameAbbreviation4Landing")+"_"+
//        config.getString("project")+"_"+
//        config.getString("projectPhase")
//      props.setProperty("lndDB",lndDB)
//      val lndPath = 
//        //config.getString("nameservice")+
//        config.getString("directoryTreeAboveLandingRawTempAndRefined")+
//        config.getString("dirForLanding")+"/"+
//        lndDB+"/"
//      props.setProperty("lndPath",lndPath)
//      val rawDB=
//        config.getString("databaseNameAbbreviation4Raw")+"_"+
//        config.getString("project")+"_"+
//        config.getString("projectPhase")
//      props.setProperty("rawDB",rawDB)
//      val rawPath = 
//        //config.getString("nameservice")+
//        config.getString("directoryTreeAboveLandingRawTempAndRefined")+
//        config.getString("dirForRaw")+"/"+
//        rawDB+"/"
//      props.setProperty("rawPath",rawPath)
//      val tmpDB=
//        config.getString("databaseNameAbbreviation4Temp")+"_"+
//        config.getString("project")+"_"+
//        config.getString("projectPhase")
//      props.setProperty("tmpDB",tmpDB)
//      val tmpPath = 
//        //config.getString("nameservice")+
//        config.getString("directoryTreeAboveLandingRawTempAndRefined")+
//        config.getString("dirForTemp")+"/"+
//        tmpDB+"/"
//      props.setProperty("tmpPath",tmpPath)
//      val rfdDB=
//        config.getString("databaseNameAbbreviation4Refined")+"_"+
//        config.getString("project")+"_"+
//        config.getString("projectPhase")
//      props.setProperty("rfdDB",rfdDB)
//      val rfdPath = 
//        //config.getString("nameservice")+
//        config.getString("directoryTreeAboveLandingRawTempAndRefined")+
//        config.getString("dirForRefined")+"/"+
//        rfdDB+"/"
//      props.setProperty("rfdPath",rfdPath)
//      if(0<config.getString("impalaOptions").length){
//        log.info("see nonZeroLength impalaOptions")
//        props.setProperty("clusterHasImpala","true")
//      }
//      //config.getString("notHere") /* uncomment to pro{mpt a non-fatal config ConfigException missing property  catch case ce   */
//    } catch {
//      // more specific cases first 
//      case ce:  ConfigException=>  
//        log.info("ConfigException one or more of the inpath outpath precursors not present tripped at\n"+ce.getLocalizedMessage())
//      case e: Exception => 
//        log.info("e Exception. non-specific-handled error ") 
//        e.printStackTrace()
//    }
//  }
	

	
	def tackonSysEnv(configIn:Config): Config={
		val ab = new ArrayBuffer[String]()
		val configE=ConfigFactory.systemEnvironment()
		if(false==configIn.getBoolean("suppressLoggingEnvironmentProperties")){
			val iteratorE = configE.entrySet().iterator();
			while(iteratorE.hasNext()) {
				val entry = iteratorE.next()
				ab+=String.format("Env %-38s %s",entry.getKey(),entry.getValue().render())
			}
		}
		val configS=ConfigFactory.systemProperties()
		if(false==configIn.getBoolean("suppressLoggingSystemProperties")){
			val iteratorS = configS.entrySet().iterator();
			while(iteratorS.hasNext()) {
				val entry = iteratorS.next()
				ab+=String.format("Sys %-38s %s",entry.getKey(),entry.getValue().render())
			}
		}
		val config=configIn.withFallback(configE).withFallback(configS)
		if(0==ab.length){
			log.info("system and environment properties both suppressed from logging")
		} else {
			//log.error("pre ab")
			ab.sortWith(_<_).foreach { x => log.info(x) }
			//log.error("post ab")
		}
		//log.info("exiting propertiesFileHandler.tackonSysEnv")
		config;
	}
}