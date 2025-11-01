#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
curl -s "$path/assembled.txt"
curl -s "$path/hello"
curl -s "$path/redis"
curl -s "$path/sql"

mkdir -p tmp
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/login?"user=demo&passwd=pwd"
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/pbkdf2?work=10000
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/protected
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/logout
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/protected
rm tmp/cookie-$$.jar
