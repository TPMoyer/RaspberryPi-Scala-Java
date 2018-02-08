package common

import scala.util.control.Exception
import java.io.StringWriter
import java.io.PrintWriter

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExceptionHandler {
	/*
		* Wraps a call by name function f that returns a value
		* Logs if exception is thrown and exits with errorCode
		* Manually checks if error so the return type is T
		*/
	def callOrExit[T](errorCode: Int, log: Logger, message: Option[String] = None)(f: => T): T = {
		val either: Either[Throwable, T] = Exception.allCatch.either(f) //catches all exceptions
		//log.info("either.isLeft="+ (if(either.isLeft)"left" else "right"))
		
		if (either.isLeft) {
			//log.fatal("we are left with exception")
			val e: Throwable = either.left.get
			message.foreach(log.error(_))
			logStackTraceAndExit(log, e, errorCode)
		}
		either.right.get
	}
	
	def logStackTraceAndExit(log: Logger, e: Throwable, errorCode: Int) = {
		val msg="cme@logStackTraceAndExit with errorCode="+errorCode;println(msg);log.error(msg)
		val stringWriter = new StringWriter //StringWriter does not need to be closed
		e.printStackTrace(new PrintWriter(stringWriter))
		log.error(stringWriter.toString)
		println(stringWriter.toString)
		System.exit(errorCode)
	}
	
	
	/*
		* Auto closes a closable object after use
		* 
		* Ex:
		*  val body: String = ExceptionHandler.usingOrExit(ReadFileErrorCode, log, new BufferedReader(new InputStreamReader(fsdis))){ case br =>
		*  val lines= new ArrayBuffer[String]()
		*  var line = br.readLine
		*  while(line != null){
		*    lines += line
		*    line = br.readLine
		*  }
		*  
		*  lines.mkString("\n")
		* }
		*/
	def usingOrExit[A <: AutoCloseable, B](errorCode: Int, log: Logger, closeable: => A)(f: A => B): B = {
		lazy val _closeable = callOrExit(errorCode, log)(closeable) //instantiate the closable once
		try {
			callOrExit(errorCode, log)(f(_closeable)) //f returns B
		} finally {
			_closeable.close() //unit is ignored / not returned
		}
	}
}