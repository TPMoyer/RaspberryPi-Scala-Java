package myJavaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;


/* Config has advantages over a simple list of Strings, 
 * because the TypeSafe ConfigValue class has some useful attributes.  
 * Prominent among these is that the ConfigValues can be multiple different classes, 
 * ie a String, Number, Boolean,Map<String,Object>, List<Object>, or null,
 *  
 * One caution on Lists.   none of them are empty. 
 * The smallest List has 1==listNameHere.size() and  0==listNameHere.get(0).length()
 *  
 * Have added a few extras beyond the default typesafe implementation
 * All of these are based on case sensitive property names:
 *  
 *  property name clue    example           action
 *  ___________________   _________        _____________________________________________________ 
 *  ends in List          zList            stores it as a LIST and creates an outrider property zListType
 *                                         zListType will have values Integer|Double|String
 *                                         This type needs to be specified when you instance the value,
 *                                         and is available for anyone who performs the TODO: dynamic initialization.
 *                                         ArrayList<String> zListInstance = (ArrayList<String>)myConfig.get("zList").unwrapped();
 *                                         
 * ends in CapList        zCapList         Same LIST value created, but all values are passed through .toUpperCase()
 *                                         
 * ends in PasswordFidHostOS    userPasswordFidHostOS  Fid is a File IDentifier.  This handler stores the userPassword property
 *                                                     not the userPasswordFid.  It goes to the indicated Fid, and reads the single
 *                                                     word entry as the indicated password.  
 *                                                     These passwords are masked as XXXXXXX in the log output
 * ends in PasswordFidHDFS      userPasswordFidHDFS  Fid is a File IDentifier.  This handler stores the userPassword property
 *                                                   not the userPasswordFid.  It goes to the indicated Fid, and reads the single
 *                                                   word entry as the indicated password.  
 *                                                   These passwords are masked as XXXXXXX in the log output *                                              
 */
public class PropertiesFileHandler {
	private static final transient Logger log = LogManager.getLogger(PropertiesFileHandler.class);
	
	/* stuff which would be convenient to have in the config, but which do not come directly from the .properties file
	 * can be instanced here.   Catenations of properties, and formatting of time stuff fit into this.
	 * Things added here WILL be part of what gets logged (unless your .properties has suppressLoggingTheseProperties = true)
	 */
	/* the config argument to CustomizeProperties is not modified within this method,
	 * the configIn in the argument list as the config entries may be needed building blocks for the properties created therein */  
	private static void CustomizeProperties(
		Properties props, 
		Config config
	){
		LocalDate load_ts = LocalDate.now();
		props.setProperty("load_ts",load_ts.toString());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		props.setProperty("dateStringForLoadTsPartition",load_ts.format(formatter));
		
		String lndDB=config.getString("databaseNameAbbreviation4Landing")+"_"+config.getString("project")+"_"+config.getString("projectPhase");
		String rawDB=config.getString("databaseNameAbbreviation4Raw"    )+"_"+config.getString("project")+"_"+config.getString("projectPhase");
		String rfdDB=config.getString("databaseNameAbbreviation4Refined")+"_"+config.getString("project")+"_"+config.getString("projectPhase");
		log.debug("the three databases are being put into the config as \nlndDB = "+lndDB+"\nrawDB = "+rawDB+"\nrfdDB = "+rfdDB);
		props.setProperty("lndDB", lndDB);
		props.setProperty("rawDB", rawDB);
		props.setProperty("rfdDB", rfdDB);
		
		String lndLocation=config.getString("nameservice")+config.getString("directoryTreeAboveLandingRawTempAndRefined")+config.getString("dirForLanding");
		String rawLocation=config.getString("nameservice")+config.getString("directoryTreeAboveLandingRawTempAndRefined")+config.getString("dirForRaw");
		String rfdLocation=config.getString("nameservice")+config.getString("directoryTreeAboveLandingRawTempAndRefined")+config.getString("dirForRefined");
		props.setProperty("lndLocation", lndLocation);
		props.setProperty("rawLocation", rawLocation);
		props.setProperty("rfdLocation", rfdLocation);
		log.debug("the three database locations are being put into the config as \nlndLocation = "+lndLocation+"\nrarLocation = "+rawLocation+"\nrfdLocation = "+rfdLocation);
	}
	static Config setDefaultProperties(){
		Properties props = new Properties();
		props.setProperty("suppressLoggingTheseProperties"      ,"false"); /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("suppressLoggingEnvironmentProperties","true" ); /* setting default so as to avoid exception for missing configuration setting */
		props.setProperty("suppressLoggingSystemProperties"     ,"true" ); /* setting default so as to avoid exception for missing configuration setting */
		//props.setProperty("","");
		//props.setProperty("","");
		//props.setProperty("","");
		//props.setProperty("","");
		//props.setProperty("","");
		
		ArrayList<String> list0 = new ArrayList<String>();
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		ArrayList<String> list3 = new ArrayList<String>();
		
		/* legacy config values from years of doing java database & big data work... some default values */
		Config config=ConfigFactory.parseProperties(props)
			.withValue("project"                                ,ConfigValueFactory.fromAnyRef(""))
			.withValue("subProject"                             ,ConfigValueFactory.fromAnyRef(""))
			.withValue("subSubProject"                          ,ConfigValueFactory.fromAnyRef(""))
			.withValue("hiveTableNamePrepender"                 ,ConfigValueFactory.fromAnyRef(""))
			.withValue("convertHostTimestampsToStrings"         ,ConfigValueFactory.fromAnyRef("false"))
			.withValue("limitHiveTypesToAvroParquetConvertables",ConfigValueFactory.fromAnyRef("false"))
			.withValue("limitSchemasPulledToThisList"           ,ConfigValueFactory.fromAnyRef(list0))
			.withValue("limitTablesPulledToThisList"            ,ConfigValueFactory.fromAnyRef(list1))
			.withValue("excludedSchemas"                        ,ConfigValueFactory.fromAnyRef(list2))
			.withValue("excludedTables"                         ,ConfigValueFactory.fromAnyRef(list3))
		;
		return config;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static  Config Handler(String [] args){
		Config config = setDefaultProperties(); 
		String target0 = ".properties";
		String target1 = ".conf";
		String target2 = ".config";
		String response="";
		Properties props= new Properties();
		String configPathlessFid="";
		if(0<args.length){
			if(   (args[0].endsWith(target0))
					||(args[0].endsWith(target1))
					||(args[0].endsWith(target2))
				){
				//log.debug("see file. Using "+args[0]+" as configuration properties file");
				String fid=args[0];
				File file =new File(fid);
				if(file.exists()){
					@SuppressWarnings("unused")
					long bytes = file.length();
					//log.debug("length of "+fid+" ="+bytes);
				} else {
					response=".properties File does not exist!";
					log.debug(response);
					System.out.println(response);
					System.exit(1);
				}
				config = ConfigFactory.parseFile(new File(args[0])).withFallback(config);
				configPathlessFid=args[0].substring(1+args[0].lastIndexOf(System.getProperty("file.separator")));
				//log.debug("configPathlessFid="+configPathlessFid);
				props.setProperty("configPathlessFid",configPathlessFid);
			} else {
				response="jar was called with a single argument, but that argument did not end in one of the 3 endings required for properties files.\nThe three ending accepted are  .properties   .conf   or .config\n";
				log.debug(response);
				System.out.println(response);
				System.exit(1);
			}
		} else {
			StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
			StackTraceElement main = stack[stack.length - 1];
			String [] mainClassTokens = main.getClassName().split("\\.");
			String appName=mainClassTokens[0];
			response="using default "+appName+".properties as configuration file.";
			String fid=appName+".properties";
			File file =new File(fid);
			if(file.exists()){
				long bytes = file.length();
				log.debug("length of "+fid+" ="+bytes);
			}else{
				 System.out.println("File does not exists!");
			}
			log.debug(response);
			System.out.println(response);
			config = ConfigFactory.load().withFallback(config); 
		}
		for (Entry<String,ConfigValue> entry : config.entrySet()) {
			String key=entry.getKey();
			ConfigValue value=entry.getValue();
			//log.debug(String.format("key=%-40s %s",key,key.toLowerCase().endsWith("passwordhostosfid")));
			//String value=entry.getValue().unwrapped().toString();
			if(key.equals("hiveTableNamePrepender")){
				String bare=entry.getValue().render().toString().toLowerCase().replace("\"","").trim();
				log.debug("key="+key+" bare="+bare+" bare.length()="+bare.length()+" bare.endsWith(\"_\")="+bare.endsWith("_")+" tackon=|"+(((bare.length()==0)||(bare.endsWith("_")))?"":"_")+"|");
				String pre=bare+(((bare.length()==0)||(bare.endsWith("_")))?"":"_");
				log.debug("piped pre=|"+pre+"|");
				props.setProperty(key, pre);
			} else
			if(   key.toLowerCase().endsWith("databasepasswordfidhostos")
			/*   || (  key.toLowerCase().endsWith("databasepasswordfidhdfs")   bailed on this, because only sqoop needs to get the password from within hdfs */ 
			/*       && (0< value.unwrapped().toString().length()) */
			/*      )                                              */
				 ||(  key.toLowerCase().endsWith("databasepasswordfid")
						 && (0< value.unwrapped().toString().length())
						)
			){
				log.debug("going for password at "+value);
				try{
					BufferedReader bufferReader = new BufferedReader(new FileReader(new File(value.unwrapped().toString())));
					String line;
					if ((line = bufferReader.readLine()) != null)   { /* expect only one line, so go with if, instead of multiline would use a while */
						String pwFromPwFid=key.substring(0,key.length()-9);
						/* uncommenting this next line will cause the databasePassword to be printed to the .log file */
						//log.debug("from "+key+" see line as "+line+" so will instantiate configKey "+pwFromPwFid);
						props.setProperty("databasePassword",line);
					}
					bufferReader.close();
				}catch(Exception e){
					System.out.println("Error while reading file "+value.unwrapped().toString()+" e.getMessage=\n" + e.getMessage());
				}
			} else 
			if(key.endsWith("List")){
				//log.debug("key="+key);
				String val=value.unwrapped().toString().trim();
				//for(int ii=0;ii<val.length();ii++){
				//  log.debug(String.format("%2d %3d %c",ii,((int)val.charAt(ii)),val.charAt(ii)));
				//}
				if('['==val.charAt(0)){
					val=val.substring(1);
				}
				if(']'==val.charAt(val.length()-1)){
					val=val.substring(0,val.length()-1);
				}
				//log.debug("val="+val);
				String [] tokens = val.split(",");
				//log.debug("tokens.length="+tokens.length);
				if(0==tokens.length){
					log.debug("properties value for key="+key+" is blank.  Defaulting to ArrayList<String>");
				} else {
					ArrayList myList = null;
					String listType="";
					try {
						@SuppressWarnings("unused")
						Integer anInt=Integer.parseInt(tokens[0]);
						myList= new ArrayList<Integer>();
						for(int jj=0;jj<tokens.length;jj++){
							myList.add(Integer.parseInt(tokens[jj].trim()));
							//log.debug(key+" adding <Integer> "+tokens[jj].trim());
						}
						listType="Integer";
					} catch (NumberFormatException e) {
						try {
							@SuppressWarnings("unused")
							Double aDub = Double.parseDouble(tokens[0]);
							myList= new ArrayList<Double>();
							for(int jj=0;jj<tokens.length;jj++){
								myList.add(Double.parseDouble(tokens[jj].trim()));
								//log.debug(key+" adding <Double> "+tokens[jj].trim());
							}
							listType="Double";
						} catch (NumberFormatException e1) {  
							myList= new ArrayList<String>();
							boolean caps=key.endsWith("CapList");
							for(int jj=0;jj<tokens.length;jj++){
								myList.add(caps?tokens[jj].toUpperCase().trim():tokens[jj].trim());
								//log.debug(key+" adding <String> "+(caps?tokens[jj].toUpperCase().trim():tokens[jj].trim()));
							}
							listType="String";
						}
					}
					config=config.withValue(key,ConfigValueFactory.fromAnyRef(myList));
					//log.debug("key="+key+" length of list="+config.getStringList(key).size());
					props.setProperty(key+"Type",listType);
				}
			} 
		}
		CustomizeProperties(props,config); 
		//log.debug("pre0Config has "+pre0Config.entrySet().size()+" keys");
		TreeMap<String,String> myConfigTree = new TreeMap<String,String>();
		config=ConfigFactory.parseProperties(props).withFallback(config);
		int longestKeyLength=12; /* one of the environment or system properties was 38 char long */
		String len="12";
		if(false==config.getBoolean("suppressLoggingTheseProperties")){
			Iterator<Entry<String, ConfigValue>> iteratorPC0 = config.entrySet().iterator();
			while(iteratorPC0.hasNext()) {
				Entry entry = iteratorPC0.next();
				if(longestKeyLength < entry.getKey().toString().length()){
					longestKeyLength =entry.getKey().toString().length();
				} 
			}
			log.debug("longestKey.length()="+longestKeyLength);
			len=String.format("%d",longestKeyLength);
			Iterator<Entry<String, ConfigValue>> iteratorPC = config.entrySet().iterator();
			while(iteratorPC.hasNext()) {
				Entry entry = iteratorPC.next();
				
				String value=((ConfigValue)entry.getValue()).unwrapped().toString();
				String key=String.format("App %-"+len+"s %3d ",entry.getKey(),value.length());
				if(  key.contains("Password")
					 && !key.contains("PasswordFid")  
					){
					value="XXXXXXXX";
				} 
				//else if(entry.getKey().toString().endsWith("List")){
				//  log.debug(key+"  "+entry.getValue().toString());
				//  
				//} 
				myConfigTree.put(key, value);
				/* this will show that strings show up as "Quoted() and lists show up as "SimpleConfigList" when you forget to do the .unwrapped() */
				//log.debug(((ConfigValue) entry.getValue()).toString());
			}
		}
		Config configE=ConfigFactory.systemEnvironment();
		if(false==config.getBoolean("suppressLoggingEnvironmentProperties")){
			Iterator<Entry<String, ConfigValue>> iteratorE = configE.entrySet().iterator();
			while(iteratorE.hasNext()) {
				Entry entry = iteratorE.next();
				String value=((ConfigValue) entry.getValue()).unwrapped().toString();
				String key=String.format("Env %-"+len+"s %3d ",entry.getKey(),value.length());
				myConfigTree.put(key, value);
			}
		}
		Config configS=ConfigFactory.systemProperties();  
		if(false==config.getBoolean("suppressLoggingSystemProperties")){
			Iterator<Entry<String, ConfigValue>>  iteratorS = configS.entrySet().iterator();
			while(iteratorS.hasNext()) {
				Entry entry = iteratorS.next();
				String value=((ConfigValue) entry.getValue()).unwrapped().toString();
				String key=String.format("Sys %-"+len+"s %3d ",entry.getKey(),value.length());
				if(entry.getKey().toString().equals("line.separator")){
					value=String.format("ascii character %d",(int)(((ConfigValue) entry.getValue()).unwrapped().toString().charAt(0)));
				} 
				myConfigTree.put(key, value);
			}
		}
		config=config.withFallback(configE).withFallback(configS);
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for(String s:myConfigTree.keySet()){
			sb.append(s+myConfigTree.get(s)+"\n");
		}
		log.debug(sb.toString());
		log.debug("myConfig has "+config.entrySet().size()+" keys");
		return config;
	}
}
