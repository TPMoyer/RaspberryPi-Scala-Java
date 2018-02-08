package apps

import org.slf4j.Logger._
import org.slf4j.LoggerFactory._

object HelloWorld {
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def main(args: Array[String]) = {
		println("Hello World  from "+this.getClass.getName.substring(0,this.getClass.getName.length-1)+".scala")
	}
}