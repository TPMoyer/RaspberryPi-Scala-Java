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
such as         dev on 32bit windows for 64 bit hadoop    
and the current    dev on 64bit windows for 32bit ARM linux (the RaspberryPi)

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

Raspberry Pi side:
=================
**RTFM**  If you're new to the raspberryPi an excellent resource is the [https://www.raspberrypi.org/help/](https://www.raspberrypi.org/help/)  
To get full function from this repo, you should enable I2C, and SSH through the  
     RPi_Icon...   Preferences...   Raspberrry Pi COnfiguration...   interfaces tab...    
I have a camera on my Pi, and use arduinos through the USB , and like to connect to my pi with VNC, so I also have the Camera, SPI, and VNC interfaces enabled on this same dialog tab.
 
**Download/install the following:**  
* wiringPi   [https://projects.drogon.net/raspberry-pi/wiringpi/](https://projects.drogon.net/raspberry-pi/wiringpi/) is his official site.  
        Toward the bottom of this page you'll find his [download and install](https://projects.drogon.net/raspberry-pi/wiringpi/download-and-install/)  
You DO need his library, not the one packaged with NOOBS, because Gordon updated his code Dec 2017 to recognize the now current RPi_3B, (the NOOBS WiringPi version says "not recognized" on a 3B)   
* bc language   to allow calculations within bash scripts  
      sudo apt-get install bc  
* Scala...   not needed.  Everything is compiled to java before it gets to the Pi

**bring the bash scripts from the RaspberryPi-Scala-Java repo**  
Create a "working" directory at a location of your choice
copy the dozen-or-so files in your workstation ``/RPi_Scala_Java_workspace/RPi_Scala_Java_project/src/main/resources/Linux_Files/*``  
into this new RPi directory.   I use MobaXTerm for this, but WinSCP is a viable choice.

  
Development Workstation
=======================
**Download the following:**

*    ScalaIDE   [http://scala-ide.org/download/sdk.html](http://scala-ide.org/download/sdk.html)    big green button    
*    Java     google Java SDK...     click on the oracle link for the latest (currently 1.9)
*    MobaXTerm   [https://mobaxterm.mobatek.net/](https://mobaxterm.mobatek.net/)   or alternately WinSCP  https://winscp.net/eng/download.php
*    A Good Editor   [https://www.sublimetext.com/](https://www.sublimetext.com/)   This one works on Windows, Linux (but not the RPi ARM linux) and OSX
*    commons-csv.jar   [https://commons.apache.org/proper/commons-csv/download_csv.cgi](https://commons.apache.org/proper/commons-csv/download_csv.cgi)   
* this repo.   Put it in any location of your choosing    
   
**Workspace and project setup**   on the development workstation occurs in a couple of steps.  
Opening the IDE and getting the project imported is first.  
Next is to set up some of the IDE parameters to match the maven pom.xml provided  
Open the blink.java and run it as a java application (it will fail)  
Compile the project into a jar
Copy the jar and the _lib to the "working" directory you created on your RPi.
Run one of the scripts in the RPi working directory.    
   
* Open and Import  
Start your ScalaIDE... wait for the Eclipse Launcher dialog... 
 point the ``workspace entryfield`` to the ``RPi_Scala_Java_workspace`` directory...   
ok...    File...    import...    Maven...    Existing Maven Project...    next...     browse...   
point toward the directory  ``RPI_Scala_Java_project``...     ok...  
smile if the Import Maven Projects dialog has an entry in the projects: field and there is a checkmark in the box next to /pom.xml...  
finish...  
  It may take some time for eclipse to download all the resourses in the pom.xml file.  
  We're talking coffie break time.  
After the downloading activity ends I showed a square red x splorch in the Package Explorer panel on the collapsed ``RPi_Scala_java_project``,  and the Problems panel showed a red x circle of collapsed Erros(37 items)  
  You now have a Scala_Java project.  :)  
* IDE parameters: create a location for our compiled runnable jar  
Expand the Package Explorer ``RPi_Scala_Java_project``... 
right-mouse-click on the Package Explorer ``RPi_Scala_Java_project``...  new...  folder...  in the Folder Name field type  ``target``...  
Finish
* IDE parameters: create a location for our 2 external jars  
Expand the Package Explorer ``RPi_Scala_Java_project``... 
right-mouse-click on the Package Explorer ``RPi_Scala_Java_project``...  new...  folder...  in the Folder Name field type  ``external_jars``...  
Finish   
* IDE parameters: put our 2 external jars in that new location.  
Now go out to your OS and copy-or-move commons-csv.jar to the newly created
``$myLocation/RPi_Scala_Java_workspace/RPi_Scala_Java_project/external_jars`` directory.  
Similarly find a scala-library-2.11.8.jar and copy it to that same directory.
* IDE parameters: get the Eclipse file system interface to recognize these 2 external jars.    
right-mouse-click on the Package Explorer ``RPi_Scala_Java_project``...  refresh...
* IDE parameters: get Eclipse to treat the two files as external jars.  
 right-mouse-click on the Package Explorer ``RPi_Scala_Java_project``...  build path...  configure build path...  click on the Libraries tab...  
Add External Jars...    navigate to your external_jars directory...  select both the commons-csv.jar and the scala-library-2.11.8.jar...  open...  
* IDE parameters: change the Scala version to match the one provisioned in the maven pom.xml file  
 (you should still be in libraries tab of the java build path dialog from the last step).  
 double-click on the  Scala Library container[2.12.2]...   click on the ``Fixed Scala Library container 2.11.8``...  finish...
* IDE parameters: tell eclipse to put a version of java into the jar that matches the 1.8 on the RPi  
 double-click on the JRE System Library...     Environments...     JavaSE-1.8...    click on the best match in the Compatible JREs; seciton...  
ok...   finish...   ok
* Let all that crunch through  
 Look in the lower right corner of the IDE and you should see "building" progress blue bars for some 10's of seconds.
When buildin is complete, you should have no red splorch on the Package Explorer RPi_Scala_Java_project
and the Problems panel should have only yellow warnings.
* Open  BlinkLEDs.java  
 What we are really doing here is causing eclipse to create a run configuration. We can't really run blink on a non-RPi platform because only the RPi has GPIO.  
click on the package explorer ``RPi_Scala_Java_project`` src/main/Java to uncollapse it...  
click on the myJavaStuff package... double-click on the BlinkLEDs.java java-class... It should open in the large editor panel. 
* "Run"  BlinkLEDs.java
 On the icon bar (at the top of eclipse is the title bar, below that is the menu bar, below that is a bar full of icons: the icon bar) find the biggest green circle (it has a a beige triangle inside the green circle).  Click on the black twisty (triangle) to the right of the green circle... run as... java application   
A Progress dialog will appear and then a console panel, and then a lot of red text.  

**All the stuff above is one time only.  Editing/developing code in either java or Scala do not require any of the above steps to be repeated.**

* compile a runnable jar  
 On the menu bar    File... Export... click on the boxed + next to Java... Runnable Jar file... In the newly opened Runnable JAR File Export dialog, click on the expander on the right side of the empty Launch configuration entryfield... Click on the BlinkLEDs... click on the Browse button and navigate to the ``$myLocation/RPi_Scala_Java_workspace/RPi_Scala_Java_project/target`` directory... in the File name entryfield type ``RPi_Scala_Java.jar``...  Save... change the Library handling to select ``Copy required libraries into a sub-folder next ot hte generated JAR``... Finish... Click on the ``do not show this message again``... ok... yes... ok  
If it completes with warnings you're still good to go. 
* copy the Jar and the _lib to the RPi
 Open your mobaXTerm or WinSCP and copy paste the ``RPI_Scala_Java_lib`` directory (about 130MB) and the ``RPI_Scala_Java.jar`` (about 300KB) into the RPi working directory.
 
**switch over to the RPi**  
That can be done with either mobaXTerm, putty, VNC, or moving over to the actual RPI (if you have a keyboard and display).  
 Open a terminal and cd (change directory) to your working directory.   
chmod +x *.sh 
type the following 5 keys:   ./0  the TAB key   the Enter key
Text should appear on the console, and voltage should appear on GPIO pins 29,28,27 and 26.  and If you have connected the RPI to LED's according to [https://www.sunfounder.com/learn/Super_Kit_V2_for_RaspberryPi/lesson-1-blinking-led-super-kit-for-raspberrypi.html](https://www.sunfounder.com/learn/Super_Kit_V2_for_RaspberryPi/lesson-1-blinking-led-super-kit-for-raspberrypi.html) the LED's should blink.  (yes I changed which pins are used)


The Several Apps
=========

``0_run_jar.sh``  is a demo of running a runnable jar, as a runnable jar.   Whatever main class
is pointed to, within the runnable jar will be executed.

``1_run_blink.sh``  is a demo of selecting a (possibly different) package.class from that same
runnable jar.

``2_run_TSL2561.sh``  runs the java code from the Pi4J supplied I2CExample.java 
My inital goal was to be able to use another device (the MMA8451) as an electronic inclinometer.
The MMA8451 uses the I2C interface and my initial web searches suggested that the RPI had troubles
with I2C.  It turns out that the early versions of the RPi seem to have had the dificulties,
but the current RPI_3B with a 2018 vintage NOOBS has no problems with I2C (if you enable it 
in the Preferences...    configuration...     interfaces )
Having code for the TSL3561 from the Pi4J project, I bought a unit from Adafruit, and it worked
in short order.  

``3_run_SysInfo.sh``  runs the java code form the Pi4J supplied SystemInfoExample.java  
Wanted a superset of info to select for inclusion with the MMA8451 output

``4_run_MMA8451.java.sh``  A second aspect of the MMA8451 which the inital web searches suggested was
troublesome was "repeated start I2C support" required for this module.  The existance of accel.py
at https://github.com/massixone/mma8451/blob/master/accel.py told me that the RPi could function
with this module.   Was able to function with Grodon's I2C.wiringPiI2CSetup after initial 
difficulty with the I2CExample I2CFactory and I2CBus.getDevice methods.  
   Implemented one of the extreems of the behaviors available for the MMA8451: highest resolution.
This ment slow reads (1.56Hz), and highest power (oversampling enabled).  Did not implement any
of the wake on X modes:  1) movement  2) shock  3) freefall or  4) tilt

``5_run_MMA8451_scala.sh``  A classic first implementation in a new language.   Confirmed that Scala
developed on a 64 bit Windows box woudl function on the RPI after being compiled into JVM code
in a jar. 

``6_run_blink_scala.sh``  converted the java blink code to Scala.

``7_run_MMA8451_scala``  converted the java MMA8451 code to Scala.   Did make the coding more "Scala-ish"
than I might have the first couple months into scala.  Folks who are not familiar with the language
can compare (rosetta stone fashion) the same app from Massimo's original python, java and scala.

``8_run_HelloWorld_scala_logging.sh``  added slf4j log4j logging to the HelloWorld code.   My 
development style relies heavily on logging, so this was a key extension of RPI Scala capabilities.

``9_run_ScalaApp_Template.sh``  The (so far empty shell of a) toolControl application.   Incorporated
typesafe configuration, logging, hadoop.fs filesystem, and some of the enterprise application
programming architecture suggestions which have been directed at me.
