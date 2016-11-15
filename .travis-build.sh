#!/bin/bash
ROOT=$TRAVIS_BUILD_DIR/..

# Fail the whole script if any command fails
set -e

## Build Checker Framework
(cd $ROOT && git clone --depth 1 https://github.com/typetools/checker-framework.git)
# This also builds annotation-tools and jsr308-langtools
(cd checker-framework/ && ./.travis-build-without-test.sh)

## Build
ant jar

## Run tests
ant all-tests
