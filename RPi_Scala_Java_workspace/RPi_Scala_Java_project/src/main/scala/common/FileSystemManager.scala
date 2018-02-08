package common

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{ FileSystem, RawLocalFileSystem, LocalFileSystem}
import com.typesafe.config.Config

object FileSystemManager {
	
	//TODO: Make all file systems private except for the method `fileSystem`
	lazy val conf = new Configuration() /* needed for both hdfs and local FileSystem instancing */
	lazy val hdfs = FileSystem.get(conf)
	lazy val rlfs = {
		val rlfs = new RawLocalFileSystem() /* If we are not on a cluster, fs and lfs will be equal */
		rlfs.setConf(conf);
		rlfs
	}
	
	lazy val lfs = new LocalFileSystem(rlfs)
	
	val exitCodeFid = {
		if (System.getProperty("os.name").startsWith("Windows")){
			"c:\\logs\\ExitCode.txt"
		}else{
			"./logs/exitCode.txt"
		}
	}
	
	/* Get the correct type of file system at runtime
	 * Uses the SharedPropties class (based upon typesafe's config)
	 * 
	 * On cluster use defaults to hdfs. 
	 * useLocalFs: optional parameter which allows direction to the edge node local file system.
	 */
	def fileSystem(config : Config, useLocalFs : Boolean = false) = {
		if(  (   config.getBoolean("onCluster") 
					 &&config.getBoolean("useOnlyLocalFileSystem")
				  )
				|| useLocalFs	
			)	{
			lfs
		}else{
			hdfs
		}
	}
}