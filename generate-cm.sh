#!/bin/bash
if [ -z "$1" ]
    then
	    echo "No argument supplied"
		exit 0
fi

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

cd $SPARTA_CODE
APKPATH=$(cd $(dirname $1); pwd)/$(basename $1)  
CMPATH="$2"
EPICCOUTPUT=epiccoutput.txt
FILTERSMAPTEMP="$SPARTA_CODE"/filterstemp
FILTERSMAP="$SPARTA_CODE"/src/sparta/checkers/intents/componentmap/filter-map

if [ "$CMPATH" == "" ]; then
	CMPATH=$(dirname ${APKPATH})/component-map
else
	CMPATH=$(cd $(dirname $2); pwd)/$(basename $2) 
fi

TARGETFOLDER_WITH_EXTENSION=$(basename $APKPATH)
TARGETFOLDER=${TARGETFOLDER_WITH_EXTENSION%.apk}
RETARGETEDPATH=./download-libs/epicc/retargeted/"$TARGETFOLDER"
if [ ! -d ./build ]; then
	ant
fi

if [ ! -f ./download-libs/APKParser.jar ]; then
	downloadJars	
fi


#Using DARE
./download-libs/dare/dare -d ../epicc/ "$APKPATH"

#Using Epicc
java -jar ./download-libs/epicc/epicc-0.1.jar -apk "$APKPATH" -android-directory "$RETARGETEDPATH" -cp ./download-libs/epicc/android.jar -icc-study ./download-libs/epicc/ > ./download-libs/epicc/"$EPICCOUTPUT"

#Epicc output generated.

rm -f "$FILTERSMAPTEMP" 
cp "$FILTERSMAP" "$FILTERSMAPTEMP"



if [[ "$APKPATH" == *\.apk ]]
	then
    	rm -f AndroidManifestTemp.xml
		java -jar ./download-libs/APKParser.jar "$APKPATH" > AndroidManifestTemp.xml
		java -cp build/ sparta.checkers.intents.componentmap.ProcessAndroidManifest AndroidManifestTemp.xml "$FILTERSMAPTEMP"
	    rm -f AndroidManifestTemp.xml
	else 
		echo Input is not an .apk file.
		exit 0
fi
#Processing epicc output with filters

java -cp build/ sparta.checkers.intents.componentmap.ProcessEpicOutput ./download-libs/epicc/"$EPICCOUTPUT" "$FILTERSMAPTEMP" "$CMPATH"
rm -f $FILTERSMAPTEMP

