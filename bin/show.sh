#!/usr/bin/env bash

fullline="──────────────────────────────────────────────────────────────────"
fullline+=$fullline
fullline+=$fullline

clear -x
#printf "\033[?25l"
printf "\033[1;2H"
#printf "\033[2;1H${fullline:0:$COLUMNS}"
linestoleave=1

function printvisiblepart {
  local str="$1"
  local left="$2"
  local y="$3"
  local ansi="$4"
  local right=$((left+${#str}))
  if [ $right -ge $COLUMNS ] && [ $left -lt $COLUMNS ];then
    local max=$((COLUMNS-left+1))
    str="${str:0:max}"
  fi
  if [ $left -lt $COLUMNS ]; then
    printf "\033[$y;${left}H${ansi}%s\033[0m" "$str";
  fi
}

function mytree {
  str="$1"
  x=$2
  y=$3
  if test $y -lt $((LINES-linestoleave)); then
    printvisiblepart "[$str]" $x $y "\033[1;34m"
    ((y++))
    ((x+=2))
    dirdepth=0
    if test -d "$1"; then
      walktree "$1"
      cd ..
    fi
  fi
}

function walktree {
  cd "$1";
  shopt -s dotglob
  shopt -s nullglob
  for file in * 
  do
    if test "$file" = "." || test "$file" = ".."; then 
      continue;
    fi
    left=$((x+dirdepth*2))
    ansi=""
    if [ -d "$file" ]
    then
      str="[$file]"
      ansi="\033[1;34m"
    else
      str="$file"
      ansi="\033[1m"
      # if [ .java  = "${str: -5}" ]; then ansi="\033[1;31m"; fi
      # if [ .class = "${str: -6}" ]; then ansi="\033[1;31m"; fi
      if [ .jar   = "${str: -4}" ]; then ansi="\033[1;31m"; fi
      if [ .war   = "${str: -4}" ]; then ansi="\033[1;31m"; fi
      if [ .sh    = "${str: -3}" ]; then ansi="\033[1;32m"; fi
    fi
    if test $y -lt $((LINES-linestoleave)); then
      printvisiblepart "$str" $left $y "$ansi"
      ((y++))
    fi
    ## recurse down
    if [ -d "$file" ]
    then
      ((dirdepth+=1))
      walktree "$file" $x $y
      cd ..
    fi
  done
  ((dirdepth-=1))
}
function dolayout {
  layout="$1"
  max=0
  for dirstruct in $layout; do
    dir=${dirstruct%%:*}
    pospair=${dirstruct#*:}
    left=${pospair%:*}
    top=${pospair#*:}
    if test "$top" = "-"; then top=$((y+1)); fi
    mytree $dir $left $top
    if test $y -gt $max; then max=$y; fi
  done
  printf "\033[$((LINES));1H"
}

dolayout "bin:1:2 local:1:- lib:1:- app:40:2 src:40:- build:80:2 target:80:-"

