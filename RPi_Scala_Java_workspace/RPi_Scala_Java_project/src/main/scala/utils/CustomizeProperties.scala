package utils

import scala.collection.mutable.ArrayBuffer
import com.typesafe.config._
import java.io.File
import java.util.Properties
import scala.io.Source
import org.apache.log4j.{Level, Logger, LogManager, PropertyConfigurator}
import java.text.SimpleDateFormat;
import scala.collection.JavaConversions._
import java.util.Date;

object CustomizeProperties {
	@transient lazy val log = org.apache.log4j.LogManager.getLogger("CustomizeProperties")
	
	def customizeProperties(
			config  : Config,
			props : Properties
		) {
		log.info("cme@ customizeProperties")
		
		props.setProperty("aws_access_key_id"                       ,"") /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("aws_secret_access_key"                   ,"") /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("bailOnAnalysisOfMostFrequent"           ,"false")
		props.setProperty("current_date_override"                   ,"")
		
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
//    props.setProperty(""                   ,"") /* setting default so as to avoid exception for missing configuration setting */
	
	}
}