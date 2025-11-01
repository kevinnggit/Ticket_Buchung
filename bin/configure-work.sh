#!/usr/bin/env bash
echo "webapp=app
manager=manager
dbserver=mariadb
dbuser=dbuser
dbname=dbdemo
dbpassword=complexpassword
redisserver=redis
redispassword=foobared
baseurl=http://localhost:8080" >local/config.txt

echo "machine localhost login manager password einfach
machine tomcat login manager password einfach" > local/netrc
echo "configured for work"
#Hi
