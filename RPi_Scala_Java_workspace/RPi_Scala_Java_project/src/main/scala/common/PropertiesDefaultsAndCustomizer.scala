package common

import com.typesafe.config._
import java.util.Properties
import scala.collection.JavaConversions._

object PropertiesDefaultsAndCustomizer {
	@transient lazy val log = org.apache.log4j.LogManager.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def customizeProperties(
		config  : Config,
		props : Properties
	) {
		log.info("cme@ customizeProperties")

		
		/* setting defaultS 
			* These will be overwritten if the .config or .properties file contains these values 
			*/
		
		props.setProperty("aws_access_key_id"        ,"") 
		props.setProperty("aws_secret_access_key"    ,"") 
		props.setProperty("s3SourceBucketName"       ,"") 
		props.setProperty("s3SourcePrefixOmit"       ,"") 
		props.setProperty("s3SourceRegion"           ,"us-east-1") 
		props.setProperty("s3SourceRootPrefix"       ,"") 
		props.setProperty("s3TargetBucketName"       ,"") 
		props.setProperty("s3TargetPrefixPrefix"     ,"") 
		props.setProperty("s3TargetPrefixPrefix"     ,"") 
		props.setProperty("s3TargetRegion"           ,"us-east-1") 
		
		props.setProperty("csvFid"                   ,"")
		props.setProperty("inputFid"                 ,"") 
		props.setProperty("outputLocation"           ,"") 
		props.setProperty("exitCodeFid"              ,"./logs")
		props.setProperty("useOnlyLocalFileSystem"   ,"false")
//		props.setProperty("winutilsDotExeLocation"   ,"")
		
//		props.setProperty(""                   ,"")
//		props.setProperty(""                   ,"")
//		props.setProperty(""                   ,"")
//		props.setProperty(""                   ,"")
//		props.setProperty(""                   ,"")
		
		/* code to customize the properties has been called from this location is several apps */
		//buildUpLandingRawRefined(props,config)
	}

	
	/* Additional configuration properties constructed by compositing input properties */
//  def buildUpLandingRawRefined(props:Properties, config:Config) ={
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
}