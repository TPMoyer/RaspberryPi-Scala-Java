package appSpecificStuff


import org.slf4j.Logger._
import org.slf4j.LoggerFactory._
import org.slf4j.LoggerFactory
import com.typesafe.config.Config
import scala.collection.mutable.ArrayBuffer

import common._
import utils.MiscellaneousUtils._
 

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

/* intent is drive a next generation CNC tool platform */
object ToolControler {
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def toolControler(config:Config):Integer={		
		var retCode=0
		pnl("cme@ "+this.getClass.getName.substring(0,this.getClass.getName.length-1))
		
		
		
		
		retCode
	}
}