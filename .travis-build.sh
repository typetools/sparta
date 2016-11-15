#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

cd $ROOT
export JAVA_HOME=`which javac|xargs readlink -f|xargs dirname|xargs dirname`
export CHECKERFRAMEWORK=$ROOT/checker-framework/
mkdir $ROOT/android-sdk
export ANDROID_HOME=$ROOT/android-sdk

## Get the Android SDK
apt-get update
touch tmp | add-apt-repository ppa:ubuntu-desktop/ubuntu-make
apt-get update
echo "Y" | apt-get install ubuntu-make
umake android android-sdk --accept-license $ANDROID_HOME
export PATH=$ANDROID_HOME/tools/:$PATH

## Get the latests Android tools and API 25
echo "y" | android update sdk -u -t "tool, platform-tool, android-25"

## Build Checker Framework
(cd $ROOT && git clone --depth 1 https://github.com/typetools/checker-framework.git)
# This also builds annotation-tools and jsr308-langtools
(cd checker-framework/ && ./.travis-build-without-test.sh)

cd sparta
ant all-tests-nostubs
