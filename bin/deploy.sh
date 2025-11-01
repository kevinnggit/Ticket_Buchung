#!/usr/bin/env bash
source local/config.txt || exit 1

echo "DEPLOY:   target/webapp.war 
DEPLOY:   TO $baseurl/$manager 
DEPLOY:   AS $webapp"
curl --silent --location --netrc-file local/netrc --fail \
  --upload-file "target/webapp.war" "$baseurl/$manager/text/deploy?path=/$webapp&update=true" && \
  echo "DEPLOY:   success" || { echo "DEPLOY:   failure"; exit 1; }

