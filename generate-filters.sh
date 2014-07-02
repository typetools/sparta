#!/bin/bash
#This script should only be used to generate the file filter-map-base,
#containing a mapping between intent filters of Android built-in apps and
#their components. This script should be executed for each new Android version.
#Its parameter is a folder containing all Android built-in apps .apk files.

#Modify line below every time the version changes:
#Current Version of filter-map-base: Google APIs - API 18.

#A common way to obtain the .apk files for a new Android version is the following:
#1. Open the Eclipse that comes in the Android ADT bundle:
#http://developer.android.com/sdk/index.html#download
#2. Go to Window -> Android Virtual Device Manager
#3. Create a new emulator with the desired Android API. Preferably using the
#Google APIs, which will install in the emulator some common built-in Android
#apps (e.g. Google maps). Make sure to create it with 1024 or more RAM memory.
#4. Start the emulator in Eclipse with the desired Android version and wait for
#it to start (it might take a while in the first time... 10+ minutes).
#5. Open the DDMS view in Eclipse. Select the running emulator in the devices tab.
#6. Open the File Explorer tab. Select all .apk files in the folder system/app/.
#7. There is a hidden option in one of the buttons on the top right of the file
#explorer tab which allows you to pull all selected files. Pull them to a folder
#you created.
#8. The path of this created folder can be used as an argument for this script.

APPSFOLDER=$1 #Path to .apks folder
FILTERMAP=src/sparta/checkers/intents/componentmap/filter-map-base
if [ -z "$1" ]
	then
	    echo No folder containing .apks supplied
		exit 0
fi
cd $SPARTA_CODE
rm -f $FILTERMAP

for apkFile in "$APPSFOLDER"/*
do
	if [[ "$apkFile" == *\.apk ]]
	then
		echo Adding "$apkFile" information into filter-map
    	java -jar ./download-libs/APKParser.jar "$apkFile" > AndroidManifestTemp.xml
		java -cp build/ sparta.checkers.intents.componentmap.ProcessAndroidManifest AndroidManifestTemp.xml "$FILTERMAP"
	    rm -f AndroidManifestTemp.xml
	fi
done


