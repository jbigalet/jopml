#!/bin/sh
SDIR=$0
SORIG=`pwd`
S=$1
cd `dirname $SDIR`
echo "> Building proc"
make clean > /dev/null
make > /dev/null
echo "> Generating netlist."
circuits/proc $* netlists/proc.nl
echo "> Netlist: netlists/$S.nl"
cd $SORIG
