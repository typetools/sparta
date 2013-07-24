#!/bin/sh

exit 0

files=`hg st | sed -n "s/^[AM]\s\(.*\.java$\)/\1/p" | tr '\n' ' '`
[ -z "$files" ] && exit 0

java -jar checkstyle/checkstyle-5.6-all.jar \
          -c checkstyle/sun_checks.xml \
          $files
if [ $? -ne 0 ]
then
    echo "Committed source code does not comply with style guidelines!"
    exit 1
fi
