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

/*
__author__ = "Massimo Di Primio"
__copyright__ = "Copyright 2016, dpmiictc"
__credits__ = ["Massimo Di Primio", "Dario Dalla Libera"]
__license__ = "GNU GENERAL PUBLIC LICENSE Version 3"
__version__ = "0.0.1"
__deprecated__ = "None so far"
__date__ = "2017-01-03"
__maintainer__ = "Massimo Di Primio"
__email__ = "massimo@diprimio.com"
__status__ = "Testing"

//
// See 'LICENSE'  for copying
//
// Revision history
// Date			Author					Version		Details
// ----------------------------------------------------------------------------------
// 2016-12-31	Massimo Di Primio		V.0.04		Fixed some basic functionality
//
// 2017-01-03	Massimo Di Primio		0.05		Added Interrut handler
//
// 2018-01-28    Thomas P. Moyer    0.06  converted to java and Scala
__java_and_scala_maintainer__ = "Thomas P. Moyer"
__email__ = "tpmoyer006@gmail.com"
// 

"""Simple code example for Adafruit MMA8452 3-axis Accelerometer

This experimental code is intended for measuring gravity acceleration trough Adafruit(c) MMA8451, connected
to a Raspberry Pi Model 2A, 2B, 2B+ or 3 (not yet tested with RPi Zero).
Through this code we will demonstrate the ability of the 3-axis sensor MMA8451 to efficiently measure
gravity acceleration, so that we can identify the spatial orientation of the device.
It is hoped that further and even more useful application can start from this minimal enabling code.
Please share if you do so.
*/


import org.slf4j.Logger._
import org.slf4j.LoggerFactory._
import org.slf4j.LoggerFactory
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


object MMA8451 {
	val appStart=java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss"))
	println("appStart at "+appStart)
	System.setProperty("appStart",appStart)
	/* these two variables are used in the log4j.properties file (defaults as /src/main/resource/log4j.properties when run from eclipse) */
	System.setProperty("invariantLogFileName","myAppLog")
	System.setProperty("uniquePerRunLogFileName","myAppLog_"+appStart)
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
		// MMA8451  I2C address
	// My board has no connection to the A pin... Adifruit documentation says this should be address 0x1c BUT...
	val MMA8451_ADDR =  0x1D /* 0x1C is said to be default, but Massixon's code uses 0x1D.  You can connect the A pin to the 3Vo pin for an address of 0x1D */
	val earthGravityMS2 = 9.80665f
	val range2G = 0 /*  +/- 2g (default value)   value==0 */
	val range4G = 1 /*  +/- 4g                   value==1 */
	val range8G = 2 /*  +/- 8g                   value==2 */
	val rangeDivider= Array(4096.0,2048.0,1024.0,64.0,32.0,16.0) /* (default)2G, 4G, 8G at 14 bit resolution, followed by ranges for default 2G, 4G, and 8G at 8 bit resolution */
	
	/* change the range variable to accept the range you want to use */
	val range=range2G
	val correctionFactor = 1.0;
	//val correctionFactor = 1.037
	
	/*
	 *  information on  registers and flags is available at https://www.nxp.com/docs/en/data-sheet/MMA8451Q.pdf
	 *  For read frequency vs resolution vs power consumption,
	 *  wake upon event, using the FIFO buffer, advanced features,
	 *  and descriptions of the cryptic register bit names:
	 *  it's your key resource. 
	 */
	
	/* a note on data types used in this java code
	 * bytes are 8 bits with values from -127 to 128    
	 * char  are 8 bits with values from 0 to 255 
	 * Will use char, in order to not have to deal with the two's compliment representation when the msb (bit7) is 1 
	 */
	
	/*  Register Address */
	val REG_STATUS       = 0x00  /*  Read-Only  */
	val REG_WHOAMI       = 0x0d  /*  Read-Only  */
	val REG_DEVID        = 0x1a  /*  Read-Only  */
	val REG_OUT_X_MSB    = 0x01  /*  Read-Only  */
	val REG_OUT_X_LSB    = 0x02  /*  Read-Only  */
	val REG_OUT_Y_MSB    = 0x03  /*  Read-Only  */
	val REG_OUT_Y_LSB    = 0x04  /*  Read-Only  */
	val REG_OUT_Z_MSB    = 0x05  /*  Read-Only  */
	val REG_OUT_Z_LSB    = 0x06  /*  Read-Only  */
	val REG_F_SETUP      = 0x09  /*  Read/Write */
	val REG_SYSMOD       = 0x0b  /*  Read-Only  */
	val REG_XYZ_DATA_CFG = 0x0e  /*  Read/Write */
	val REG_PL_STATUS    = 0x10  /*  Read-Only  */
	val REG_PL_CFG       = 0x11  /*  Read/Write */
	val REG_CTRL_REG1    = 0x2a  /*  Read/Write */
	val REG_CTRL_REG2    = 0x2b  /*  Read/Write */
	val REG_CTRL_REG3    = 0x2c  /*  Read/Write */
	val REG_CTRL_REG4    = 0x2d  /*  Read/Write */
	val REG_CTRL_REG5    = 0x2e  /*  Read/Write */
	val REG_OFF_X        = 0x2f  /*  Read/Write */
	val REG_OFF_Y        = 0x30  /*  Read/Write */
	val REG_OFF_Z        = 0x31  /*  Read/Write */
	
	@throws(classOf[InterruptedException])
	@throws(classOf[PlatformAlreadyAssignedException])
	@throws(classOf[IOException])
	@throws(classOf[UnsupportedBusNumberException])
	def main(args: Array[String]) = {
		if(0<args.length)for((a,ii)<-args.zipWithIndex)(println(f"args[$ii%2d]=$a%s")) else println("no CLI args")
		var calibrateGForceReading=false
		var calibrateReadingDuration=false
		val sysInfo=getSysInfoString()
		
		
		
		// create Pi4J console wrapper/helper
		// (This is a utility class to abstract some of the boilerplate code)
		/* Scala note: was able to use console within this method, but was not able to pass it as a variable to another method */
		val console = new Console()
		console.println("cme@ MMA8451.main")
		
		// print program title/header
		console.title("<-- Java Pi4J MMA841 Example -->", "V0.1")
		
		// allow for user to exit program using CTRL-C
		console.promptForExit()
		val i2cHandle = mma8451Setup() 
		
		//calibrateGForceReading=true
		//calibrateReadingDuration=true
		if(calibrateGForceReading){
			val numReadings=10
			val normal= Array(1.234567,1.234567,1.234567)
			var magSum=0.0
			var startTime = System.nanoTime()
			var mag=1.234567
			var endTime = 0L
			var baseLineDuration=0.641
			if(calibrateReadingDuration){
				println("collecting baseline")
				
				println("printing to the console can be a very expensive operation")
				for(ii <- 0 until numReadings){
					magSum+=mag
					/* the formatting done more java'esque */
					println("%4d %8.6f G's normal to (%9.6f,%9.6f,%9.6f)".format(ii,mag,normal(0),normal(1),normal(2)))
					Thread.sleep(641) 
				}
				endTime = System.nanoTime()
				baseLineDuration = ((endTime - startTime)/1000000000.0)/numReadings
				println("")
			}
			magSum=0.0
			startTime = System.nanoTime()
			for(ii <- 0 until numReadings){
				val magN=read14Bits(i2cHandle)
				magSum+=magN._1
				println("%4d %8.6f G's normal to (".format(ii,magN._1)+magN._2.map(axis => f"$axis%9.6f").mkString(",")+")")
				Thread.sleep(641) 
			}
			endTime = System.nanoTime()
			val duration = ((endTime - startTime)/1000000000.0)/numReadings
		  val measuredGMag=magSum/numReadings
			val correctionFactor2B=1/measuredGMag
			println("Average G force measured was %12.9f which would make the correctionFactor=%9.6f".format(measuredGMag,correctionFactor2B))
			if(calibrateReadingDuration)println("baseLineDuration was %8.6f seconds/loop,   %8.6f seconds/loop without sleep".format(baseLineDuration,baseLineDuration-.641))
			println("        Duration was %8.6f seconds/loop,   %8.6f seconds/reading".format(duration,duration-baseLineDuration))
		} else {
			while(true) {
				try{
					console.println("\n"+LocalDateTime.now()+"\n"+sysInfo+" cpuTemp="+SystemInfo.getCpuTemperature()+"C")
				} catch {
					case e: UnsupportedOperationException => {}
					case unknown : Throwable =>
						log.error("an exception occurred under  "+this.getClass.getName.substring(0,this.getClass.getName.length-1)+":\n" + ExceptionUtils.getStackTrace(unknown))
						println("\n\n*************************************************Failed with unknown type exception *******************************************\n")
						throw new Exception("unknown Error")      
				}
				debugShowRegisters(i2cHandle)
				prettyReadNPrint14Bits(i2cHandle)
				//prettyReadNPrint8Bits(i2cHandle)
				/* having set things up for 1.56Hz operation, this many milliseconds sleep will insure each loop pass will be a new reading */
				Thread.sleep(641) 
			}
		}
	}
	//TODO :provision this with a number of input selections to enable other other modes
	/* Setup the MMA8451.
	 * Currentl only a single mode is available: The stable readings possible,
	 * Which is achieved by 14 Bit, oversampling and reading at the slowest rate 1.56Hz at high power consumption.
	 */
	def mma8451Setup() : Int = {
		val i2cHandle = I2C.wiringPiI2CSetup(MMA8451_ADDR)
		//debugShowRegisters(console,i2cHandle)
		
		/* reset the device, which clears all buffers.   reset bit is special... it can be written both active and standby mode */
		/* 0  1  0  0  0  0  0  0    SelfTest(7):0 no effect  for any input, reset(6):1 RESET, unused(5):0, sleepPowerMode(4:3):0 no effect for any input  ,  autoSleepEnabled(2):0 no effect for any input, acitvePowerMode(1:0):00 no effect for any input */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG2, 64) /* this immediately puts all the regesters to all bits 0 */
		//debugShowRegisters(console,i2cHandle)
		
		/* Set bits for selected profile, and set active/standby to standby so that other REG_CTRL_REG# bits can be set */
		/* For this demo, am opting for highest resolution, lowest noise readings (this implies both slowest, and highest power usage) */
		/* 1  1  1  1  1  1  0  0   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)0 no  active(0)0 standby */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,252)
		
		///* for 8 bit resolution (use the matching prettyRead8Bits with this)*/
		///* 1  1  1  1  1  1  1  0   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)1 yes  active(0)0 standby */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,254)
		
		/* 0  0  0  1  0  0  1  0    SelfTest(7):0 off, reset(6):0 no, unused(5):0, sleepPowerMode(4:3):10 hi res,  autoSleepEnabled(2):0 no, acitvePowerMode(1:0):10 hi res  */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG2, 18)
		//debugShowRegisters(console,i2cHandle)
		
		/* It is cool that the MMA8451 can wake from sleep upon a jolt, freefall, single/double tap, or tilt,
		 * but in this demo will not be implementing any of those. 
		 * so will not need to set control registers 3, 4 or 5.
		 * The three rows below are commented, to leave the allBits == 0 state from the reset, instead of sending 0 three times 
		 */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG3, 0)
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG4, 0)
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG5, 0)
		
		/* have not bent my board, nor mounted it askiew, so will not set the offset registers */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_X, 0)
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_Y, 0)
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_Z, 0)
		
		/* 0  0  0  0  0  0  0  0  unused(7:5)0 does not matter,  highPassFilter(4)0 off, unused(3:2)0 does not matter, fullScaleRange(1:0):00   2G  */
		/* with highPassFilter (bit4) off, the fullscale range 4G woudl be 1, and fullscale range 8G woudl be 2 */ 
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_XYZ_DATA_CFG,  0+range)
		 
		/* 1  1  0  0  0  0  0  0   debounce counter(7)1 clear upon condition no longer valid {1 is default}   Portrait/Landscape enable(6)1 enabled   unused(5:1) does not matter */ 
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_PL_CFG,192)
		
		// Finally, Activate the sensor
		/* 1  1  1  1  1  1  0  1   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)0 no  active(0)1 active */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,253)
		i2cHandle
	}
	
	def getSysInfoString(): String={
		val sb = new StringBuilder()
		var sysInfo=""
		try {
			sysInfo=
				SystemInfo.getBoardType().name()+" "+
				SystemInfo.getHardware()+"_Rev_"+
				SystemInfo.getRevision()+" java="+
				SystemInfo.getJavaVersion()
		} catch {
			case e: IOException =>
				log.error("an exception occurred under  "+this.getClass.getName.substring(0,this.getClass.getName.length-1)+":\n" + ExceptionUtils.getStackTrace(e))
				println("\n\n*************************************************Failed with exception *******************************************\n")
				throw new Exception("IOException from getSysInfoString")
			case e: InterruptedException =>
				log.error("an exception occurred under  "+this.getClass.getName.substring(0,this.getClass.getName.length-1)+":\n" + ExceptionUtils.getStackTrace(e))
				println("\n\n*************************************************Failed with exception *******************************************\n")
				throw new Exception("IOException from getSysInfoString")	
			case unknown : Throwable =>
				log.error("an exception occurred under  "+this.getClass.getName.substring(0,this.getClass.getName.length-1)+":\n" + ExceptionUtils.getStackTrace(unknown))
				println("\n\n*************************************************Failed with unknown type exception *******************************************\n")
				throw new Exception("unknown Error")      
		}
		sysInfo
	}
	
	@throws(classOf[IOException])	
	def debugShowRegisters(i2cHandle: Int){
		var reg=0
		showReg(i2cHandle,"REG_WHOAMI",REG_WHOAMI)
		reg=I2C.wiringPiI2CReadReg8(i2cHandle,REG_SYSMOD          )
		println("REG_SYSMOD       (0x%02X):%3d 0x%02X %s SYSMOD[1:0] 00:Standby  01:Wake  10:Sleep".format(reg,reg,reg,(String.format("%8s",Integer.toBinaryString(reg)).replace(' ','0'))))
		showReg(i2cHandle,"REG_CTRL_REG1",REG_CTRL_REG1)
		showReg(i2cHandle,"REG_CTRL_REG2",REG_CTRL_REG2)
		showReg(i2cHandle,"REG_CTRL_REG3",REG_CTRL_REG3)
		showReg(i2cHandle,"REG_CTRL_REG4",REG_CTRL_REG4)
		showReg(i2cHandle,"REG_CTRL_REG5",REG_CTRL_REG5)
		showReg(i2cHandle,"REG_STATUS",REG_STATUS)	
		showReg(i2cHandle,"REG_F_SETUP",REG_F_SETUP)
		showReg(i2cHandle,"REG_XYZ_DATA_CFG",REG_XYZ_DATA_CFG)
		showReg(i2cHandle,"REG_PL_STATUS",REG_PL_STATUS)
	}	
	def showReg(i2cHandle: Int ,sinReg: String,inReg: Int){
		val reg=I2C.wiringPiI2CReadReg8(i2cHandle,inReg)
		println("%-16s (0x%02X):%3d 0x%02X %s".format(sinReg,reg,reg,reg,(String.format("%8s",Integer.toBinaryString(reg)).replace(' ','0'))))
	}
	def prettyReadNPrint14Bits(i2cHandle: Int){
		/* The MMA8451 uses a pair of 8 bit registers for each axis.
		 * The raw number is expressed as a single integer in two's compliment form, shifted two bits toward the MSB.
		 * Was able to get java to see the two's compliment nature of the register pair by bitShifting to have the data 
		 * MSB align with an integer type MSB.   By choosing to shift 8 bits and cast as short, I avoided bitShifting the lower byte. 
		 * Retained java's knowledge of negative/positive by doing the the reduction of the 16 bit short to the 14 bits actually
		 * present, by dividing by a power of 2, rather than bit shifting.   The second advantage of having chosen my intermediate
		 * type as "short" is that this division was by a small integer (4) to shift 2 bits.
		 * An intermediate 32 bit integer type would have required division by 262144 to shift 18 bits, a needless expensive.
		 */
		/* These are the raw readings from the registers, decoded into an integer type  */
		val xyz=Array(0,1,2).map(reg => (((I2C.wiringPiI2CReadReg8(i2cHandle,(reg*2)+1))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,(reg*2)+2)).toShort/4)
		
		/* A vector normal to the plane of the device, with an amplitude of the measured force in G's  */ 
		val xyzG=xyz.map(_/rangeDivider(range))
		
		/* normalized to SI units,   meters per second squared */
		val xyzAccel=xyzG.map(_*earthGravityMS2)
		
		val mag = Math.sqrt(xyzG.foldLeft(0.0)( _ + Math.pow(_, 2)))
		val xyzN = xyzG.map(_/mag)
		val radius = Math.sqrt((xyzN(0)*xyzN(0))+(xyzN(1)*xyzN(1)))
		val angleXYDeg = Math.asin(radius ) * Math.PI/180.0
		val angleXDeg  = Math.asin(xyzN(0)) * Math.PI/180.0
		val angleYDeg  = Math.asin(xyzN(1)) * Math.PI/180.0
		
		/* LEMA:  for more fun with geometry... check out
		 *    http://www.webgltutorials.appspot.com/WebGLTutorial06
		 * And remember,  3 points do define a plane, but use 9 floating point numbers to define an area on a plane.
		 * It is better to see a plane as a point and a normal, using only 6 numbers. 
		 */
		
		/* each row of the console output will get a different axis, set up an array of characters (aka a String) for labeling them */
		val c="xyz"
		
		println("module is measuring %8.6f G's normal to (%9.6f,%9.6f,%9.6f)".format(mag,xyzN(0),xyzN(1),xyzN(2)))
		println("this represents a tilt of %10.6f degrees from vertical, %10.6f degrees in X, and %10.6f degrees in Y".format(angleXYDeg,angleXDeg,angleYDeg))
		for((a,ii)<- xyz.zipWithIndex)println(f"${c(ii)}%sRaw=${xyz(ii)}%5d ${c(ii)}%sG's=${xyzG(ii)}%9.6f  ${c(ii)}%sAccel=${xyzAccel(ii)}%6.3f")  
	}
	def prettyReadNPrint8Bits(i2cHandle:Int){
		val xyz=Array(1,3,5).map(reg => (I2C.wiringPiI2CReadReg8(i2cHandle,reg)).toByte)
		val xyzG=xyz.map(_/rangeDivider(range+3))
		val xyzAccel=xyzG.map(_*earthGravityMS2)
		val c="xyz"
		for((a,ii)<- xyz.zipWithIndex)println(f"${c(ii)}%sRaw=${xyz(ii)}%5d ${c(ii)}%sG's=${xyzG(ii)}%9.6f  ${c(ii)}%sAccel=${xyzAccel(ii)}%6.3f")
	}
	/* output is  magnitude & the normal vector.
	 * Set them up as a tuple as a sort-of ultra-light-weight class
	 */
	def read14Bits(i2cHandle : Int) : (Double,Array[Double]) = {
		val xyz=Array(0,1,2).map(reg => (((I2C.wiringPiI2CReadReg8(i2cHandle,(reg*2)+1))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,(reg*2)+2)).toShort/4)
		/* alternate formulation for the same xyz, format more java'ish */
		//var xyz=Array(
		//	(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_LSB)).toShort/4,
		//	(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_LSB)).toShort/4,
		//	(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_LSB)).toShort/4
		//)
		
		val mag = Math.sqrt(xyz.foldLeft(0.0)( _ + Math.pow(_, 2)))
		/* the outside parenthises and the comma between the members,  make this a tupple */
		(correctionFactor*mag/rangeDivider(range) , xyz.map(_/mag))
		
	}
}