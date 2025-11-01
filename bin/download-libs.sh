#!/usr/bin/env bash

function doget {
  local dst=$1; local version=$2; local fullpath="$3"; local libtype="$4"; local overridename="$5"
  local name=$dst-$version.jar
  local realname=${overridename:=$name}
  local url="https://repo1.maven.org/maven2/$fullpath/$dst/$version/$dst-$version.jar"
  curl -f -s -o "tmp/$realname" "$url"
  exitcode="$?"
  echo "$exitcode $realname $libtype"
  if test "$exitcode" = "0"; then
    if test "$libtype" = "compile"; then mv tmp/$realname lib/; fi
    if test "$libtype" = "runtime"; then mv tmp/$realname app/WEB-INF/lib/; fi
    if test "$libtype" = "compile+runtime"; then 
      mv tmp/$realname lib
      cp lib/$realname app/WEB-INF/lib/
    fi
  fi
}

rm -f lib/*
rm -f app/WEB-INF/lib/*
mkdir -p lib tmp

doget jakarta.jakartaee-api 10.0.0 jakarta/platform compile
doget json 20240205 org/json compile+runtime
doget jedis 5.2.0 redis/clients compile+runtime
doget commons-pool2 2.12.0 org/apache/commons compile+runtime
doget slf4j-api 2.0.16 org/slf4j runtime
doget HikariCP 6.2.1 com/zaxxer runtime

rm -rf tmp
