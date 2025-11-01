#!/usr/bin/env bash
source local/config.txt || exit 1
mkdir -p build target
echo "PREPARE"
cp -r app/* build
sed  "s/REDISSERVER/${redisserver}/g" app/WEB-INF/web.xml |sed  "s/REDISPASSWORD/${redispassword}/g" > build/WEB-INF/web.xml
sed  -e "s/DBSERVER/${dbserver}/g" -e "s/DBUSER/${dbuser}/g" -e "s/DBPASSWORD/${dbpassword}/g" -e "s/DBNAME/${dbname}/g" app/META-INF/context.xml  > build/META-INF/context.xml
