#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

cd $ROOT
export JAVA_HOME=`which javac|xargs readlink -f|xargs dirname|xargs dirname`
export CHECKERFRAMEWORK=$ROOT/checker-framework/
export ANDROID_HOME=$ROOT/android-sdk-linux
mkdir $ANDROID_HOME

apt-get update
apt-get -f install
apt-get install wget


## Get the Android SDK
# download android sdk
cd $ANDROID_HOME
wget -q https://dl.google.com/android/repository/tools_r25.2.3-linux.zip
unzip tools_r25.2.3-linux.zip
export PATH=$ANDROID_HOME/tools/bin:$PATH

## Get the latests Android tools and API 24
#echo y\ry\ry | sdkmanager "tool;platform-tool;android-25"
echo y | tools/bin/sdkmanager "build-tools;25.0.2"
echo y | tools/bin/sdkmanager "platforms;android-25"

## Build Checker Framework
(cd $ROOT && git clone --depth 1 -b issue1456 https://github.com/Bohdankm22/checker-framework.git)
# This also builds annotation-tools and jsr308-langtools
(cd $CHECKERFRAMEWORK && ./.travis-build-without-test.sh downloadjdk)

cd $ROOT/sparta
ant all-tests-nostubs
