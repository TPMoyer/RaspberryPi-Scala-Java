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

import org.slf4j.Logger._
import org.slf4j.LoggerFactory._
import org.slf4j.LoggerFactory
import java.time.format._
import java.time.LocalDateTime

import com.pi4j.system._
import com.pi4j.util._
import com.pi4j.util.Console

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.pi4j.io.gpio.PinPullResistance
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.RaspiPin
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital


object BlinkLEDs {
	val appStart=java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss"))
	println("appStart at "+appStart)
	System.setProperty("appStart",appStart)
	/* these two variables are used in the log4j.properties file (defaults as /src/main/resource/log4j.properties when run from eclipse) */
	System.setProperty("invariantLogFileName","myAppLog")
	System.setProperty("uniquePerRunLogFileName","myAppLog_"+appStart)
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def main(args: Array[String]) = {
		if(0<args.length)for((a,ii)<-args.zipWithIndex)(println(f"args[$ii%2d]=$a%s")) else println("no CLI args")
		
		println("<--Pi4J--> GPIO Blink Example ... started .")
		
		// create gpio controller
		val gpio = GpioFactory.getInstance()
		
		// provision gpio pin #01 & #03 as an output pins and blink
		val led0 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29) /* red */
		val led1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28) /* green */
		val led2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27) /* yellow */
		val led3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_26) /* IR */

		// provision gpio pin #02 as an input pin with its internal pull down resistor enabled
		val myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN)

		// create and register gpio pin listener
		myButton.addListener(new GpioPinListenerDigital() {
			override def handleGpioPinDigitalStateChangeEvent(event : GpioPinDigitalStateChangeEvent) {
				// when button is pressed, speed up the blink rate on LED #2
				if(event.getState().isHigh()){
					led2.blink(200)
				} else {
					led2.blink(1000)
				}
			}
		})

		// continuously blink the led every 1/4 second for 15 seconds: it's too bright to leave blinking 
		led0.blink(250, 15000)
		// continuously blink the led s 
		//led0.blink(250)
		led1.blink(500)
		led2.blink(1000)
		led3.blink(2000)
		
		System.out.println(" ... the LEDs will continue blinking until the program is terminated.")
		System.out.println(" ... PRESS <CTRL-C> TO STOP THE PROGRAM.")
				
		// keep program running until user aborts (CTRL-C)
		while(true) {
				Thread.sleep(250)
		}
		
//		/* was not able to achieve reproducable results with the setShutdownOptions */
//		// set shutdown state for these pins
//		led0.setShutdownOptions(false,PinState.HIGH)  
//		led1.setShutdownOptions(false,PinState.HIGH)
//		led2.setShutdownOptions(false,PinState.HIGH)
//		led3.setShutdownOptions(false,PinState.HIGH)
		
		// stop all GPIO activity/threads
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		//gpio.shutdown  /*  implement this method call if you wish to terminate the Pi4J GPIO controller */
		
				

	}
}