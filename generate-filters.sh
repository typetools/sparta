#!/bin/bash
APPSFOLDER=$1 #Path to .apks folder
FILTERMAP=src/sparta/checkers/intents/componentmap/filter-map

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


