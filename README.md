# RaspberryPi-Scala-Java
GPIO and Pi4J and homebrew for the RaspberryPi, written in Scala and/or Java

This is an eclipse maven workspace for RaspberryPi GPIO control using Scala and/or Java.
Working code is shared which  
 1) Blink a few LED's (unrepentent grab of the Pi4J Java, port to Scala was first success)
 2) Read a TSL2561 lux sensor 
 3) Read an MMA8451 3 axis 14 bit accelerometer

Background:    I develop on my fastest local machine and ssh the compiled jar to my target
environment for execution.   My targets are remote hadoop clusters at my day job, and 
RaspberryPi at night.  Have experienced success with java development on different platforms
such as           dev on 32bit windows for 64 bit hadoop  
and the current   dev on 64bit windows for 32bit ARM linux (the RaspberryPi)
    Eclipse, and the ScalaIDE version of Eclipse, offer a time-saveing option for this use case. 
In the dialog for creating a runnable jar, one can opt for library handling:
"copy required libraries into a sub-folder next to the generated JAR"
By selecting this option, the bulk (100ish MB of library jars) of the data in the $myJarName_lib 
directory can be copied to the remote platform only once. The $myJarName.jar has only 0.3MB and
contains all the code from my .java and .scala programming.   This makes the 
copy-jar-from-development-platform-to-app-platform portion of the programmers life shorter. 
How I spend my days and nights:
   ( think it's fixed,
     compile, 
     copy-jar-from-development-platform-to-app-platform,
     execute,
     fail,
     copy-diagnostics-log-to-development-platform,
     figure out what went wrong,
     program the fix
    )
     repeat
Making any of these steps shorter is a always desireable.  

If, on the other hand,  you are one of the programmers who work by setting breakpoints 
within their IDE, instead of using a log file centric approach: you will need to either switch to logging,
or restrict your application platforms to those which can support your IDE of choice.
My exploration suggests that eclipse does not run well on a 1GB 1GHz ARM platform like 
the current RaspberryPi.

This readme will be expanded as time and requests occur.
The initial short form is:
 Raspberry Pi side:
    General setup and I2C specifics are well documented in several sources.  I like the Adafruit series
       https://learn.adafruit.com/adafruits-raspberry-pi-lesson-4-gpio-setup/configuring-i2c
    Scala... not needed.  Everything is compiled to java before it gets to the Pi
  
  Development Workstation... Download and install
    ScalaIDE   http://scala-ide.org/download/sdk.html    big green button    
    Java    google Java SDK... click on the oracle link for the latest (currently 1.9)
    MobaXTerm    https://mobaxterm.mobatek.net/     or alternately WinSCP  https://winscp.net/eng/download.php
    A Good Editor   https://www.sublimetext.com/    This one works on Windows, Linux (but not the RPi ARM linux) and OSX
    commons-csv.jar  https://commons.apache.org/proper/commons-csv/download_csv.cgi   
    
   
ScalaIDE... workspace and project setup   on the development workstation  
Clone or Download/unzip this project to a location of your choice
    


0_run_jar.sh   is a demo of running a runnable jar, as a runnable jar.   Whatever main class
is pointed to, within the runnable jar will be executed.

1_run_blink.sh   is a demo of selecting a (possibly different) package.class from that same
runnable jar.

2_run_TSL2561.sh  runs the java code from the Pi4J supplied I2CExample.java 
My inital goal was to be able to use another device (the MMA8451) as an electronic inclinometer.
The MMA8451 uses the I2C interface and my initial web searches suggested that the RPI had troubles
with I2C.  It turns out that the early versions of the RPi seem to have had the dificulties,
but the current RPI_3B with a 2018 vintage NOOBS has no problems with I2C (if you enable it 
in the Preferences...  configuration...   interfaces )
Having code for the TSL3561 from the Pi4J project, I bought a unit from Adafruit, and it worked
in short order.  

3_run_SysInfo.sh  runs the java code form the Pi4J supplied SystemInfoExample.java
Wanted a superset of info to select for inclusion with the MMA8451 output

4_run_MMA8451.java.sh   A second aspect of the MMA8451 which the inital web searches suggested was
troublesome was "repeated start I2C support" required for this module.  The existance of accel.py
at https://github.com/massixone/mma8451/blob/master/accel.py told me that the RPi could function
with this module.   Was able to function with Grodon's I2C.wiringPiI2CSetup after initial 
difficulty with the I2CExample I2CFactory and I2CBus.getDevice methods.  
   Implemented one of the extreems of the behaviors available for the MMA8451: highest resolution.
This ment slow reads (1.56Hz), and highest power (oversampling enabled).  Did not implement any
of the wake on X modes:  1) movement  2) shock  3) freefall or  4) tilt

5_run_MMA8451_scala.sh  A classic first implementation in a new language.   Confirmed that Scala
developed on a 64 bit Windows box woudl function on the RPI after being compiled into JVM code
in a jar. 

6_run_blink_scala.sh  converted the java blink code to Scala.

7_run_MMA8451_scala  converted the java MMA8451 code to Scala.   Did make the coding more "Scala-ish"
than I might have the first couple months into scala.  Folks who are not familiar with the language
can compare (rosetta stone fashion) the same app from Massimo's original python, java and scala.

8_run_HelloWorld_scala_logging.sh     added slf4j log4j logging to the HelloWorld code.   My 
development style relies heavily on logging, so this was a key extension of RPI Scala capabilities.

9_run_ScalaApp_Template.sh   The (so far empty shell of a) toolControl application.   Incorporated
typesafe configuration, logging, hadoop.fs filesystem, and some of the enterprise application
programming architecture suggestions which have been directed at me.
