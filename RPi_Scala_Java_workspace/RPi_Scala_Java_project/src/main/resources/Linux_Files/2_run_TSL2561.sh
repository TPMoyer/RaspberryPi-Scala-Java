#!/bin/bash	
# calls a specific class, even if the jar is runnable for another class
java -Dpi4j.linking=dynamic -cp RPi_Scala_Java.jar myJavaStuff.I2CExample