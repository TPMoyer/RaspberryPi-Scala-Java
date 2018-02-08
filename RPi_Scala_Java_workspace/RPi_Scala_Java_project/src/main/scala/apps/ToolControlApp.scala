package apps

/* Expand this comment for suggested program CLI argument and MV arguments
 * 
 * CLI and VM arguments can be supplied to eclipse urns by first establishing a "run configuration"
 * by clicking on the icon bar Run icon pulldown... run as...   scala application
 * 
 * THereafter icon bar Run iconpulldown... run configuratons... arguments tab
 * shows two sections:   the upper for CLI and the lower for VM
 * In the upper (program argument:) box is for CLI arguments
 * 
 * The lower (VM:) entryField has arguments fo the JVM
 * These come into play frequently in spark applications but are not expected to have
 * many applications on the RaspberryPi
 */

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.typesafe.config.Config
import scala.collection.mutable.ArrayBuffer

import common._
import utils.MiscellaneousUtils._
import appSpecificStuff.ToolControler.toolControler

import java.time.format._
import java.time.LocalDateTime
import org.apache.commons.lang3.exception.ExceptionUtils

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException
import com.pi4j.platform.PlatformAlreadyAssignedException
import java.io.IOException
import com.pi4j.system._
import com.pi4j.util._
import com.pi4j.util.Console
import com.pi4j.wiringpi._

/* the expectation is that this code can be largely reused, 
 * with changes limited to the application launched,
 * and the names of the log files
 */
object ToolControlApp extends ProcessBase {
	@transient lazy override val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	override def run(config:Config):Integer={
		var retCode=0
		pnl("confirm run within "+this.getClass.getName.substring(0,this.getClass.getName.length-1))
		
		/* launch into my application function with all the .properties file information in the typesafe config object */
		retCode+=toolControler(config)
		
		retCode
	}
	 
	def main(args: Array[String]) {
		var retVal = 0 /* used like the standard linux return value 0==good, anything else is a fail */
		val appBeginTime = System.nanoTime
		val appStart=java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss"))
		println("appStart at "+appStart)
		
		/* these 2 val's defineing the log file names could have come in with the .properties file,
		 * but some users want to run with no CLI argument.
		 * Log file locations are set in the log4j.properties file (eclipse default is /src/main/resources/log4j.properties )
		 * Linux run-from-a-jar usually has this log4j.properties as a CLI argument.
		 */
		val invariantLogFileName    = "myAppLog"
	  val uniquePerRunLogFileName = "myAppLog_"+appStart
	  val config=setupConfig(
			 args
			,appStart
			,invariantLogFileName
			,uniquePerRunLogFileName
			,this.getClass.getName.substring(0,this.getClass.getName.length-1)
		)	
		val printFileSystemStuff=false /* fileSystemStuff gets logged,  His toggles only the console output */
		retVal+=go4it(config,printFileSystemStuff) /*does some of the boilerplate processing.  Is in ProcessBase as a trait */
		printAndLogTaskMsg(retVal,appBeginTime,this.getClass.getName.substring(0,this.getClass.getName.length-1))
		System.exit(retVal)
	}
}