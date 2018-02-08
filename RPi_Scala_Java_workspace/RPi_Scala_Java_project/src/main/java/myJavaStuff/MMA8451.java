package myJavaStuff;

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


//!/usr/bin/env python
// -*- coding:utf-8, indent=tab, tabstop=4 -*-
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
// 2018-01-28    Thomas P. Moyer        0.06        converted to java and scala
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

import java.io.IOException;
import java.time.LocalDateTime;

//import com.pi4j.io.i2c.I2CBus;
//import com.pi4j.io.i2c.I2CDevice;
//import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.system.SystemInfo;
import com.pi4j.util.Console;
import com.pi4j.wiringpi.*;
//import com.pi4j.wiringpi.I2C;




public class MMA8451 {	
	/* MMA8451  I2C address
	 * My board has no connection to the A pin... Adifruit documentation says this should be address 0x1c  BUT
	 * Massixon's python code works, and it uses 0x1D.
	 * went to the https://learn.sparkfun.com/tutorials/raspberry-pi-spi-and-i2c-tutorial and used 
	 * sudo apt-get install -y i2c-tools
	 * i2cdetect -y 1
	 * This showed my address is 1d
	 */
	public static final char  MMA8451_ADDR = 0x1d;
	public static final float earthGravityMS2 = 9.80665f;
	public static final float correctionFactor = 1.0f;
	//public static final float correctionFactor = 1.037f; 
	
	public static final int range2G = 0; /*  +/- 2g (default value)   value==0 */
	public static final int range4G = 1; /*  +/- 4g                   value==1 */
	public static final int range8G = 2; /*  +/- 8g                   value==2 */
	public static final float[] rangeDivider= new float[]{4096.f,2048.f,1024.f,64.f,32.f,16.f}; /* (default)2G, 4G, 8G at 14 bit resolution, followed by ranges for default 2G, 4G, and 8G at 8 bit resolution */
	
	/* change the range variable to accept the range you want to use. Same range values is used for 14 bit and 8 bit resolution */
	public static final int range=range2G;
	
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
	public static final char REG_STATUS       = 0x00;  /*  Read-Only  */
	public static final char REG_WHOAMI       = 0x0d;  /*  Read-Only  */
	public static final char REG_DEVID        = 0x1a;  /*  Read-Only  */
	public static final char REG_OUT_X_MSB    = 0x01;  /*  Read-Only  */
	public static final char REG_OUT_X_LSB    = 0x02;  /*  Read-Only  */
	public static final char REG_OUT_Y_MSB    = 0x03;  /*  Read-Only  */
	public static final char REG_OUT_Y_LSB    = 0x04;  /*  Read-Only  */
	public static final char REG_OUT_Z_MSB    = 0x05;  /*  Read-Only  */
	public static final char REG_OUT_Z_LSB    = 0x06;  /*  Read-Only  */
	public static final char REG_F_SETUP      = 0x09;  /*  Read/Write */
	public static final char REG_SYSMOD       = 0x0b;  /*  Read-Only  */
	public static final char REG_XYZ_DATA_CFG = 0x0e;  /*  Read/Write */
	public static final char REG_PL_STATUS    = 0x10;  /*  Read-Only  */
	public static final char REG_PL_CFG       = 0x11;  /*  Read/Write */
	public static final char REG_CTRL_REG1    = 0x2a;  /*  Read/Write */
	public static final char REG_CTRL_REG2    = 0x2b;  /*  Read/Write */
	public static final char REG_CTRL_REG3    = 0x2c;  /*  Read/Write */
	public static final char REG_CTRL_REG4    = 0x2d;  /*  Read/Write */
	public static final char REG_CTRL_REG5    = 0x2e;  /*  Read/Write */
	public static final char REG_OFF_X        = 0x2f;  /*  Read/Write */
	public static final char REG_OFF_Y        = 0x30;  /*  Read/Write */
	public static final char REG_OFF_Z        = 0x31;  /*  Read/Write */
	 
	/*
	 * Program Main Entry Point
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws PlatformAlreadyAssignedException
	 * @throws IOException
	 * @throws UnsupportedBusNumberException
	 */
	public static void main(String[] args) throws InterruptedException, PlatformAlreadyAssignedException, IOException, UnsupportedBusNumberException {
		Boolean calibrateGForceReading=false;
		Boolean calibrateReadingDuration=false;
		String sysInfo=getSysInfoString();
		
		// create Pi4J console wrapper/helper
		// (This is a utility class to abstract some of the boilerplate code)
		final Console console = new Console();
		console.println("cme@ MMA8451.main");
		
		// print program title/header
		console.title("<-- Java Pi4J MMA841 Example -->", "V0.1");
		
		// allow for user to exit program using CTRL-C
		console.promptForExit();
		
		int i2cHandle = mma8451Setup(); 
		
		//calibrateGForceReading=true;
		//calibrateReadingDuration=true;
		if(calibrateGForceReading){
			int numReadings=10;
			double[] normal=new double[]{1.234567,1.234567,1.234567};
			double magSum=0.0;
			long startTime = System.nanoTime();
			double mag=1.234567;
			long endTime = 0L;
			double baseLineDuration=0.641;
			if(calibrateReadingDuration){
				console.println("collecting baseline");
				console.println("printing to the console can be a very expensive operation");
				for(int ii=0;ii<numReadings;ii++){
					magSum+=mag;
					console.println("%4d %8.6f G's normal to (%9.6f,%9.6f,%9.6f)",ii,mag,normal[0],normal[1],normal[2]);
					Thread.sleep(641); 
				}
				endTime = System.nanoTime();
				baseLineDuration = ((endTime - startTime)/1000000000.)/numReadings; 
				console.println("");
			}
			magSum=0.0;
			startTime = System.nanoTime();
			for(int ii=0;ii<numReadings;ii++){
				mag=read14Bits(i2cHandle,normal);
				magSum+=mag;
				console.println("%4d %8.6f G's normal to (%9.6f,%9.6f,%9.6f)",ii,mag,normal[0],normal[1],normal[2]);
				Thread.sleep(641); 
			}
			endTime = System.nanoTime();
			double duration = ((endTime - startTime)/1000000000.)/numReadings;
			double measuredGMag=magSum/numReadings;
			double correctionFactor2B=1/measuredGMag;
			console.println("Average G force measured was %12.9f which would make the correctionFactor=%9.6f",measuredGMag,correctionFactor2B);
			if(calibrateReadingDuration)console.println("baseLineDuration was %8.6f seconds/loop,   %8.6f seconds/loop without sleep",baseLineDuration,baseLineDuration-.641);
			console.println("        Duration was %8.6f seconds/loop,   %8.6f seconds/reading",duration,duration-baseLineDuration);
		} else {
			while(true) {
				try{
					console.println("\n"+LocalDateTime.now()+"\n"+sysInfo+" cpuTemp="+SystemInfo.getCpuTemperature()+"C");
				}
				catch(UnsupportedOperationException ex){}
				debugShowRegisters(console,i2cHandle);
				prettyReadNPrint14Bits(console,i2cHandle);
				//prettyReadNPrint8Bits(console,i2cHandle);
				/* having set things up for 1.56Hz operation, this many milliseconds sleep will insure each loop pass will be a new reading */
				Thread.sleep(641); 
			}
		}
	}
	//TODO :provision this with a number of input selections to enable other other modes
	/* Setup the MMA8451.
	 * Currentl only a single mode is available: The stable readings possible,
	 * Which is achieved by 14 Bit, oversampling and reading at the slowest rate 1.56Hz at high power consumption.
	 */
	public static int mma8451Setup(){
		int i2cHandle=I2C.wiringPiI2CSetup(MMA8451_ADDR);
		//debugShowRegisters(console,i2cHandle);
		
		/* reset the device, which clears all buffers.   reset bit is special... it can be written both active and standby mode */
		/* 0  1  0  0  0  0  0  0    SelfTest(7):0 no effect  for any input, reset(6):1 RESET, unused(5):0, sleepPowerMode(4:3):0 no effect for any input  ,  autoSleepEnabled(2):0 no effect for any input, acitvePowerMode(1:0):00 no effect for any input */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG2, 64); /* this immediately puts all the regesters to all bits 0 */
		//debugShowRegisters(console,i2cHandle);
		
		/* Set bits for selected profile, and set active/standby to standby so that other REG_CTRL_REG# bits can be set */
		/* For this demo, am opting for highest resolution, lowest noise readings (this implies both slowest, and highest power usage) */
		/* 1  1  1  1  1  1  0  0   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)0 no  active(0)0 standby */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,252);
		
		///* for 8 bit resolution (use the matching prettyReadNPrint8Bits with this)*/
		///* 1  1  1  1  1  1  1  0   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)1 yes  active(0)0 standby */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,254);
		
		/* 0  0  0  1  0  0  1  0    SelfTest(7):0 off, reset(6):0 no, unused(5):0, sleepPowerMode(4:3):10 hi res,  autoSleepEnabled(2):0 no, acitvePowerMode(1:0):10 hi res  */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG2, 18);
		//debugShowRegisters(console,i2cHandle);
		
		/* It is cool that the MMA8451 can wake from sleep upon a jolt, freefall, single/double tap, or tilt,
		 * but in this demo will not be implementing any of those. 
		 * so will not need to set control registers 3, 4 or 5.
		 * The three rows below are commented, to leave the allBits == 0 state from the reset, instead of sending 0 three times 
		 */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG3, 0);
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG4, 0);
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG5, 0);
		
		/* have not bent my board, nor mounted it askiew, so will not set the offset registers */
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_X, 0);
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_Y, 0);
		//I2C.wiringPiI2CWriteReg8(i2cHandle,REG_OFF_Z, 0);
		
		/* 0  0  0  0  0  0  0  0  unused(7:5)0 does not matter,  highPassFilter(4)0 off, unused(3:2)0 does not matter, fullScaleRange(1:0):00   2G  */
		/* with highPassFilter (bit4) off, the fullscale range 4G woudl be 1, and fullscale range 8G woudl be 2 */ 
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_XYZ_DATA_CFG,  0+range);
		 
		/* 1  1  0  0  0  0  0  0   debounce counter(7)1 clear upon condition no longer valid {1 is default}   Portrait/Landscape enable(6)1 enabled   unused(5:1) does not matter */ 
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_PL_CFG,192);
		
		// Finally, Activate the sensor
		/* 1  1  1  1  1  1  0  1   autoWakeSampleFrequency(7:6)11 1.56Hz DataRate(5:3)111 1.56Hz  lowNoise(2)1 yes  fastRead(1)0 no  active(0)1 active */
		I2C.wiringPiI2CWriteReg8(i2cHandle,REG_CTRL_REG1,253);
		
		return(i2cHandle);
	}
	public static String getSysInfoString(){
		String sysInfo="";
		try {
			sysInfo=
				SystemInfo.getBoardType().name()+" "+
				SystemInfo.getHardware()+"_Rev_"+
				SystemInfo.getRevision()+" java="+
				SystemInfo.getJavaVersion();
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return(sysInfo);
	}
	public static void debugShowRegisters(Console console,int i2cHandle) throws IOException{
		int reg=0;
		showReg(console,i2cHandle,"REG_WHOAMI",REG_WHOAMI);
		reg=I2C.wiringPiI2CReadReg8(i2cHandle,REG_SYSMOD          );
		console.println("REG_SYSMOD       (0x%02X):%3d 0x%02X %s SYSMOD[1:0] 00:Standby  01:Wake  10:Sleep",reg,reg,reg,(String.format("%8s",Integer.toBinaryString(reg)).replace(' ','0')));
		showReg(console,i2cHandle,"REG_CTRL_REG1",REG_CTRL_REG1);
		showReg(console,i2cHandle,"REG_CTRL_REG2",REG_CTRL_REG2);
		showReg(console,i2cHandle,"REG_CTRL_REG3",REG_CTRL_REG3);
		showReg(console,i2cHandle,"REG_CTRL_REG4",REG_CTRL_REG4);
		showReg(console,i2cHandle,"REG_CTRL_REG5",REG_CTRL_REG5);
		showReg(console,i2cHandle,"REG_STATUS",REG_STATUS);	
		showReg(console,i2cHandle,"REG_F_SETUP",REG_F_SETUP);
		showReg(console,i2cHandle,"REG_XYZ_DATA_CFG",REG_XYZ_DATA_CFG);
		showReg(console,i2cHandle,"REG_PL_STATUS",REG_PL_STATUS);
	}	
	public static void showReg(Console console,int i2cHandle,String sinReg,int inReg){
		int reg=I2C.wiringPiI2CReadReg8(i2cHandle,inReg);
		console.println("%-16s (0x%02X):%3d 0x%02X %s",sinReg,reg,reg,reg,(String.format("%8s",Integer.toBinaryString(reg)).replace(' ','0')));
	}
	public static void prettyReadNPrint14Bits(Console console,int i2cHandle){
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
		int x=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_LSB)))/4;
		int y=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_LSB)))/4;
		int z=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_LSB)))/4;
		
		/* A vector normal to the plane of the device, with an amplitude of the measured force in G's  */ 
		float xG=x/rangeDivider[range];
		float yG=y/rangeDivider[range];
		float zG=z/rangeDivider[range];
		
		/* normalized to SI units,   meters per second squared */
		float xAccel = xG*earthGravityMS2;
		float yAccel = yG*earthGravityMS2;
		float zAccel = zG*earthGravityMS2;
	
		double mag = Math.sqrt((xG*xG)+(yG+yG)+(zG*zG));
		double xN=xG/mag;
		double yN=yG/mag;
		double zN=zG/mag;
		double radius = Math.sqrt((xN*xN)+(yN+yN));
		double angleXYDeg = Math.asin(radius) * Math.PI/180.;
		double angleXDeg  = Math.asin(xN    ) * Math.PI/180.;
		double angleYDeg  = Math.asin(yN    ) * Math.PI/180.;
		
		/* LEMA:  for more fun with geometry... check out
		 *    http://www.webgltutorials.appspot.com/WebGLTutorial06
		 * And remember,  3 points do define a plane, but use 9 floating point numbers to define an area on a plane.
		 * It is better to see a plane as a point and a normal, using only 6 numbers. 
		 */
		
		console.println("module is measuring %8.6f G's normal to (%9.6f,%9.6f,%9.6f)",mag,xN,yN,zN);
		console.println("this represents a tilt of %10.6f degrees from vertical, %10.6f degrees in X, and %10.6f degrees in Y",angleXYDeg,angleXDeg,angleYDeg);
		console.println("xRaw=%5d xG's=%9.6f xAccel=%6.3f",x,xG,xAccel);
		console.println("yRaw=%5d yG's=%9.6f yAccel=%6.3f",y,yG,yAccel);
		console.println("zRaw=%5d zG's=%9.6f zAccel=%6.3f",z,zG,zAccel);
	}
	public static void prettyReadNPrint8Bits(Console console,int i2cHandle){
		/* byte is a signed 8 bit integer, so the two's compliment decription occurs without any need for bitShifting */
		byte x=(byte)(I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_MSB));
		byte y=(byte)(I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_MSB));
		byte z=(byte)(I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_MSB));
		
		float xG=x/rangeDivider[range+3];
		float yG=y/rangeDivider[range+3];
		float zG=z/rangeDivider[range+3];
		
		float xAccel = xG*earthGravityMS2;
		float yAccel = yG*earthGravityMS2;
		float zAccel = zG*earthGravityMS2;
		
		console.println("xRaw=%5d xG's=%9.6f xAccel=%6.3f",x,xG,xAccel);
		console.println("yRaw=%5d yG's=%9.6f yAccel=%6.3f",y,yG,yAccel);
		console.println("zRaw=%5d zG's=%9.6f zAccel=%6.3f",z,zG,zAccel);
	}
	public static double read14Bits(int i2cHandle,double[] normal){
		int x=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_X_LSB)))/4;
		int y=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Y_LSB)))/4;
		int z=((short)(((I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_MSB))<<8)+I2C.wiringPiI2CReadReg8(i2cHandle,REG_OUT_Z_LSB)))/4;
	
		double mag = Math.sqrt((x*x)+(y+y)+(z*z));
		normal[0]=x/mag;
		normal[1]=y/mag;
		normal[2]=z/mag;
		return(correctionFactor*mag/rangeDivider[range]);
	}
}