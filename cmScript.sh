#!/bin/bash


function downloadJars {
    mkdir cmTemp
    cd cmTemp
	mkdir epicc
	mkdir dare

#Dare.jar
	if [[ "$OSTYPE" == "linux-gnu" ]]; then
		curl -o dare.tgz -L	https://github.com/dare-android/platform_dalvik/releases/download/dare-1.1.0/dare-1.1.0-linux.tgz
		cp -R dare/dare-1.1.0-linux/. dare/ 
		rm -rf dare/dare-1.1.0-linux/*
		rmdir dare/dare-1.1.0-linux
	elif [[ "$OSTYPE" == "darwin"* ]]; then
		curl -o dare.tgz -L	https://github.com/dare-android/platform_dalvik/releases/download/dare-1.1.0/dare-1.1.0-macos.tgz
		cp -R dare/dare-1.1.0-macos/. dare/
		rm -rf dare/dare-1.1.0-macos/*
		rmdir dare/dare-1.1.0-macos
	else
    	echo "This script only runs in linux or mac OS"
    	exit 0
	fi
	tar zxvf dare.tgz -C dare/
#Epicc.jar
	curl http://siis.cse.psu.edu/epicc/downloads/epicc-0.1.tgz -o epicc.tgz
	tar zxvf epicc.tgz -C epicc/

#APKParser.jar
	curl https://xml-apk-parser.googlecode.com/files/APKParser.jar -o APKParser.jar

	cd ..

}


APKPATH=$1
CMPATH=$2
APPSFOLDER=$3
EPICCOUTPUT=epiccoutput.txt
FILTERS=filters

TARGETFOLDER_WITH_EXTENSION=$(basename $APKPATH)
TARGETFOLDER=${TARGETFOLDER_WITH_EXTENSION%.apk}
RETARGETEDPATH=./cmTemp/epicc/retargeted/"$TARGETFOLDER"

if [ ! -d ./bin ]; then
	echo Please build SPARTA first.
	exit 0
fi

if [ ! -f ./cmTemp/APKParser.jar ]; then
	downloadJars	
fi


#Using DARE
./cmTemp/dare/dare -d ../epicc/ "$APKPATH"

#Using Epicc
java -jar ./cmTemp/epicc/epicc-0.1.jar -apk "$APKPATH" -android-directory "$RETARGETEDPATH" -cp ./cmTemp/epicc/android.jar -icc-study ./cmTemp/epicc/ > ./cmTemp/epicc/"$EPICCOUTPUT"

#Epicc output generated.

#Creating filters file. Intent Filters -> Components
rm -f "$FILTERS"
#For every .apk file in $APPSFOLDER: 

for apkFile in "$APKPATH" "$APPSFOLDER"/*
do
	if [[ "$apkFile" == *\.apk ]]
	then
		echo $apkFile
    	java -jar ./cmTemp/APKParser.jar "$apkFile" > AndroidManifestTemp.xml
		java -cp bin/ sparta.checkers.intents.componentmap.ProcessAndroidManifest AndroidManifestTemp.xml "$FILTERS"
	    rm -f AndroidManifestTemp.xml
	fi
done

#Processing epicc output with filters

java -cp bin/ sparta.checkers.intents.componentmap.ProcessEpicOutput ./cmTemp/epicc/"$EPICCOUTPUT" "$FILTERS" "$CMPATH"

