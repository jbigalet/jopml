#!/bin/sh
echo "> Netlist: netlists/proc.nl"
echo "> Input (netlists/proc.nl.input) :"
cat netlists/proc.nl.input | tail -n +2
echo "> Simulation:"
java -jar simulator/dist/JOP_Simulator.jar $* -brief -printram -f netlists/proc.nl.input -ram program_rom=./prog.rom netlists/proc.nl