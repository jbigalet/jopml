#!/bin/sh
python asm/mips2as.py $2 > $2.as
python asm/as.py $1 $2.as > prog.rom
