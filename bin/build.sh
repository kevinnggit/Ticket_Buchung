#!/usr/bin/env bash
bin/clean.sh &&
bin/prepare.sh &&
bin/compile.sh &&
bin/assemble.sh &&
bin/deploy.sh 
#bin/check.sh 
