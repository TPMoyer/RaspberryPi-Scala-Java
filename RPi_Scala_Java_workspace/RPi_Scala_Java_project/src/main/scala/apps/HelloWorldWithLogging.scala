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
import org.slf4j.LoggerFactory
import java.time.format._
//import java.time._  /* used alternative inLineFullSpecification in first row of object */


object HelloWorldWithLogging {
	val appStart=java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss"))
	//val now=LocalDateTime.now  /* requires import.  copy of same is commented in tof (Top Of File) import section */
	
	println("appStart at "+appStart)
	System.setProperty("appStart",appStart)
	/* these two variables are used in the log4j.properties file (defaults as /src/main/resource/log4j.properties when run from eclipse) */
	System.setProperty("invariantLogFileName","myAppLog")
	System.setProperty("uniquePerRunLogFileName","myAppLog_"+appStart)
	@transient lazy val log = org.slf4j.LoggerFactory.getLogger(this.getClass.getName.substring(0,this.getClass.getName.length-1))
	
	def main(args: Array[String]) = {
		if(0<args.length)for((a,ii)<-args.zipWithIndex)(println(f"args[$ii%2d]=$a%s")) else println("no CLI args")
		
		val msg= "Hello World  from /src/main/scala/apps/"+this.getClass.getName.substring(0,this.getClass.getName.length-1)+".scala"
		log.trace(msg) /* will not display if default logLevelLimit="DEBUG" is retained */ 
		log.debug(msg)
		log.info(msg)
		log.warn(msg)
		log.error(msg)
		println(msg)
		
		withinMethodImportDemo
	}
	def withinMethodImportDemo(){
		/* sometimes when an import is not used often,
		 * or it might be unclear which import is associated with a method or datatype,
		 * or when it might be frequently copy/pasted to multiple methods/classes/object,
		 * Readability can can be enhanced by putting the import within the code instead of tof (Top Of File)
		 */
		import java.text.DateFormatSymbols
		
		/* Goal is a column aligned table from several multiple-width variable columns, using minimum column widths which will adapt if the underlying data changes.
		 * Admittedly this is an attempt to showcase some non-trivial scala formatting.
		 */
		val longestDisplayNameLength   = DateFormatSymbols.getAvailableLocales.map(_.getDisplayName  ).toSeq.reduceLeft((x,y) => if(x.length >= y.length) x else y).length
		val longestCountryLength       = DateFormatSymbols.getAvailableLocales.map(_.getCountry      ).toSeq.reduceLeft((x,y) => if(x.length >= y.length) x else y).length
		val longestDisplayScriptLength = DateFormatSymbols.getAvailableLocales.map(_.getDisplayScript).toSeq.reduceLeft((x,y) => if(x.length >= y.length) x else y).length
		val longestLanguageLength      = DateFormatSymbols.getAvailableLocales.map(_.getLanguage     ).toSeq.reduceLeft((x,y) => if(x.length >= y.length) x else y).length
		val longestISO3LanguageLength  = DateFormatSymbols.getAvailableLocales.map(_.getISO3Language ).toSeq.reduceLeft((x,y) => if(x.length >= y.length) x else y).length
		val alignedFormat0="| %-"+longestDisplayNameLength+"s | %-"+longestCountryLength+"s | %-"+longestDisplayScriptLength+"s | %-"+longestLanguageLength+"s | %-"+longestISO3LanguageLength+"s |"
		log.debug("\n\n\n")
		log.debug("alignedFormat0="+alignedFormat0)
		DateFormatSymbols.getAvailableLocales.foreach(x=>log.info(alignedFormat0.format(x.getDisplayName,x.getCountry,x.getDisplayScript,x.getLanguage,x.getISO3Language)))
	}
}