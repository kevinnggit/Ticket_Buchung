#!/usr/bin/env bash
source local/config.txt || exit 1

JAVAFILES=$(find src -name '*.java')
echo "COMPILE: $(echo $JAVAFILES|wc -w)"
javac  --release 21 -cp 'lib/*' -d build/WEB-INF/classes $JAVAFILES &&
	echo "COMPILE:  success" || { echo "COMPILE:  failure"; exit 1;}

