#!/bin/bash


function downloadJars {
    mkdir download-libs
    cd download-libs
	mkdir epicc
	mkdir dare

#Dare.jar
	echo Downloading Dare
	echo ===========================================
	if [[ "$OSTYPE" == "linux-gnu" ]]; then
		curl -o dare.tgz -L	https://github.com/dare-android/platform_dalvik/releases/download/dare-1.1.0/dare-1.1.0-linux.tgz
		tar zxvf dare.tgz -C dare/
		cp -R dare/dare-1.1.0-linux/. dare/ 
		rm -rf dare/dare-1.1.0-linux/*
		rmdir dare/dare-1.1.0-linux
	elif [[ "$OSTYPE" == "darwin"* ]]; then
		curl -o dare.tgz -L	https://github.com/dare-android/platform_dalvik/releases/download/dare-1.1.0/dare-1.1.0-macos.tgz
		tar zxvf dare.tgz -C dare/
		cp -R dare/dare-1.1.0-macos/. dare/
		rm -rf dare/dare-1.1.0-macos/*
		rmdir dare/dare-1.1.0-macos
	else
    	echo "This script only runs in linux or mac OS"
    	exit 0
	fi
	echo ===========================================
#Epicc.jar
    echo Downloading Epicc
	echo ===========================================
	curl http://siis.cse.psu.edu/epicc/downloads/epicc-0.1.tgz -o epicc.tgz
	echo ===========================================
    tar zxvf epicc.tgz -C epicc/

#APKParser.jar
	echo Downloading APKParser
	echo ===========================================
	curl https://xml-apk-parser.googlecode.com/files/APKParser.jar -o APKParser.jar
    echo ===========================================
	cd ..

}


APKPATH=$1
CMPATH=$2
APPSFOLDER=$3
EPICCOUTPUT=epiccoutput.txt
FILTERS=filters

if [ "$CMPATH" == "" ]; then
	CMPATH=$(dirname ${APKPATH})/component-map
fi

TARGETFOLDER_WITH_EXTENSION=$(basename $APKPATH)
TARGETFOLDER=${TARGETFOLDER_WITH_EXTENSION%.apk}
RETARGETEDPATH=./download-libs/epicc/retargeted/"$TARGETFOLDER"

if [ ! -d ./build ]; then
	echo Please build SPARTA first.
	exit 0
fi

if [ ! -f ./download-libs/APKParser.jar ]; then
	downloadJars	
fi


#Using DARE
./download-libs/dare/dare -d ../epicc/ "$APKPATH"

#Using Epicc
java -jar ./download-libs/epicc/epicc-0.1.jar -apk "$APKPATH" -android-directory "$RETARGETEDPATH" -cp ./download-libs/epicc/android.jar -icc-study ./download-libs/epicc/ > ./download-libs/epicc/"$EPICCOUTPUT"

#Epicc output generated.

#Creating filters file. Intent Filters -> Components
rm -f "$FILTERS"
#For every .apk file in $APPSFOLDER: 

for apkFile in "$APKPATH" "$APPSFOLDER"/*
do
	if [[ "$apkFile" == *\.apk ]]
	then
		echo $apkFile
    	java -jar ./download-libs/APKParser.jar "$apkFile" > AndroidManifestTemp.xml
		java -cp build/ sparta.checkers.intents.componentmap.ProcessAndroidManifest AndroidManifestTemp.xml "$FILTERS"
	    rm -f AndroidManifestTemp.xml
	fi
done

#Processing epicc output with filters

java -cp build/ sparta.checkers.intents.componentmap.ProcessEpicOutput ./download-libs/epicc/"$EPICCOUTPUT" "$FILTERS" "$CMPATH"

