#!/bin/sh
SDIR=$0
SORIG=`pwd`
S=$1
cd `dirname $SDIR`
echo "> Building demo: $S"
make clean > /dev/null
make > /dev/null
echo "> Generating netlist."
shift
circuits/$S netlists/$S.nl $*
echo "> Netlist: netlists/$S.nl"
echo "> Input (netlists/$S.nl.input) :"
cat netlists/$S.nl.input | tail -n +2
echo "> Simulation:"
java -jar simulator/dist/JOP_Simulator.jar -brief -f netlists/$S.nl.input netlists/$S.nl
cd $SORIG
