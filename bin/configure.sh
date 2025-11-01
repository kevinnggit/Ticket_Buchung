#!/usr/bin/env bash
if [ "$(uname -n)" = hopper ]; then
  YOURUSER=$(whoami)
  DEPLOYPASSWORD=$(grep password ~/.my.cnf|cut -d '=' -f2)
else
  ssh hopper whoami || { echo "ssh hopper *must* work if hopperuser and mycnf password not provided" >&2; exit 1; }
  YOURUSER="$(ssh hopper whoami)"
  DEPLOYPASSWORD=$(ssh hopper grep password .my.cnf|cut -d '=' -f2)
fi


if test "$DEPLOYPASSWORD" = ""; then
  echo "could not get password from .my.cnf on hopper" >&2
  exit 1
fi
mkdir -p local
echo "user=$YOURUSER
webapp=docker-$YOURUSER-java
manager=docker-$YOURUSER-manager
dbserver=mysql-server
dbuser=$YOURUSER
dbname=${YOURUSER}_db
dbpassword=$DEPLOYPASSWORD
redisserver=localhost
redispassword=$DEPLOYPASSWORD
baseurl=https://informatik.hs-bremerhaven.de" > local/config.txt

echo "machine informatik.hs-bremerhaven.de login manager password $DEPLOYPASSWORD
machine localhost login manager password $DEPLOYPASSWORD" >local/netrc

echo "config.txt angelegt ($YOURUSER)"

