#!/bin/bash 
# the default shell under Ubunto points to dash, which does not support that string substitution syntax. 
# The net is that the hashbang lins is ignored with  sh $myScriptName.sh
# The workaround is to first
# chmod +x *.sh
# and execute scripts with  ./$myScriptName.sh   or    bash ./
# ./9    hit the tab key    hit the enter key
# if the final part of this script gets   bc: command not found
# you have not installed the bc   basic calculator language
#    sudo apt-get install bc



propertiesFid=./z_ScalaApp_Template.properties 
# this next line causes this script to read the .properties file so that values from the file can be used within this script
. $propertiesFid

appStart=$(date  +%s%N | cut -b1-13)
me=`basename "$0"`
echo "me=$me"
meBase=${me:0:${#me}-3}
echo "meBase=$meBase"
appStartDateTime=$(date +'%Y%m%d_%H_%M_%S')
logFid=$1 
if [ ${#logFid} == 0 ]; then 
	
	mkdir -p ./logs 
	logFid=./logs/${meBase}_$appStartDateTime.log 
	#logFid=./logs/${meBase}.log 
	echo "zero Length arg[1] prompted autogeneration of logFid=$logFid" |& tee -a $logFid 
fi 
echo "" 
echo "$me"                                         |& tee -a $logFid 
echo "appStartDateTime=$appStartDateTime"          |& tee -a $logFid 
echo "appStart=$appStart milliseconds sence epoch" |& tee -a $logFid 
echo "logFid=$logFid"                              |& tee -a $logFid 
echo "" |& tee -a $logFid 
retCode=0 
failRetCode=1 
shopt -s nocasematch   # set compare to be case insensitive 

echo "java -Dpi4j.linking=dynamic -cp ${jarFid}.jar $package.$class ./log4j.properties ${propertiesFid}" |& tee -a $logFid 
      java -Dpi4j.linking=dynamic -cp ${jarFid}.jar $package.$class ./log4j.properties ${propertiesFid}  |& tee -a $logFid 

state="FAILED" 
if [ $retCode -eq 0 ]; then 
	state="SUCCESSFULL" 
fi 
appEnd=$(date  +%s%N | cut -b1-13) 
DIFF=$(echo "$appEnd - $appStart" | bc) 
me=`basename "$0"` 
printf "$state retCode=$retCode  $me completed in %.3f seconds\n" $(bc -l <<< "( $DIFF  / 1000)")  |& tee -a $logFid 
echo "" |& tee -a $logFid 
exit $retCode 