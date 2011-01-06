// -brief -i -tick 1000 C:\\Users\\Mystic\\Documents\\Nouveau_Dossier\\netlists\\primegen.nl

zero 10
one 11

	// Initialisation for the i-counter
reg 1059 1058
nreg 1053 1052 
reg 1043 1042 
reg 1032 1031 
reg 1020 1019 

	// Reg-s to keep adding - Making a counter with step at i (see bottom to check init at 2*i)
	// If '765887' (thus be the j initialisation), we set these reg-s at i, i.e. mux using 765887 usualReg bits_of_i
	// '765887' is the switch from statej = 0 to statej = 1, i.e. (!jstate && !5th-i-nit && !ramread)

reg 765886 765887
//output @JInit 765887

reg 76515 76529
reg 765115 76539
reg 765215 76549
reg 765315 76559
reg 765415 76569
reg 765515 76579
reg 765615 76589
reg 765715 76599
mux 765887 76529 1058 76522
mux 765887 76539 1052 76532
mux 765887 76549 1042 76542
mux 765887 76559 1031 76552
mux 765887 76569 10 76562
mux 765887 76579 10 76572
mux 765887 76589 10 76582
mux 765887 76599 10 76592


	///////////////////////////////////////////////////////////////////////
	// 9 bits adder, adding a 8 bit number to a 4 bit one, for j counter //
	///////////////////////////////////////////////////////////////////////

	// There, @b=i, to make steps on counter.
	// input @b 1058 1052 1042 1031

xor 76522 1058 76515
and 76522 1058 76511

xor 76532 1052 765119 
and 765119 76511 765114 
and 76532 1052 765118 
xor 765119 76511 765115 
or 765114 765118 765111 

xor 76542 1042 765219 
and 765219 765111 765214 
and 76542 1042 765218 
xor 765219 765111 765215 
or 765214 765218 765211 

xor 76552 1031 765319 
and 765319 765211 765314 
and 76552 1031 765318 
xor 765319 765211 765315 
or 765314 765318 765311 

and 76562 765311 765411
xor 76562 765311 765415 

and 76572 765411 765511
xor 76572 765411 765515 

and 76582 765511 765611
xor 76582 765511 765615 

and 76592 765611 765711
xor 76592 765611 765715 

	// (Not usefull anymore) //
	// To push 2*i instead of i for j initialisation, we got a 'set' reg, then mux-s.
	// (To get it usefull back, replace some 76515 with 76515 (etc))
	//
	//nreg 10 765887
	//mux 765887 76515 10 76510
	//mux 765887 765115 76515 765110
	//mux 765887 765215 765115 765210
	//mux 765887 765315 765215 765310
	//mux 765887 765415 765315 765410
	//

//output @j 76515 765115 765215 765315 765415 765515 765615 765715 765711


	//
	// For the ram, mux for each adress bits (related to i and to j), write_data=1 because we only get to write '1'.
	// The adresses are either of i, if we're checking for isNotPrime[i], or either of j, if we are writing
	//   on the ram isNotPrime[j]=1. This make write_flag = Mux_Switch. Thus beeing, = jstate && !9th-j-bit.
	// 


not 765711 765710
and 1070 765710 10707
//output @Write_flag 10707

mux 10707 1058 76515 651
mux 10707 1052 765115 652
mux 10707 1042 765215 653
mux 10707 1031 765315 654
mux 10707 10 765415 655
mux 10707 10 765515 656
mux 10707 10 765615 657
mux 10707 10 765715 658

ram @iNP 1 8 10707 651 652 653 654 655 656 657 658 11 660

//output @Ram_read 660

	///////////////////////////
	// 5 Bits Counter, for i //
	///////////////////////////	

	// Incrementation gate (for i++)
	// If i > sqrt then incrForI = 0, nothing in done anymore. (i > sqrt <=> 5-th i bit = 1 <=> '1019'=1
	// If j > N when we are on incrementing j state, then incrForI=1 and we get out of incr j state.
	// If isNotPrime[i] when checking, then we incr i again, so incrForI = 1
	// When incr j state, if j <= N then incrForI = 0
	// Finally, incrForI = 1 at the start of the simulation. (Helloooooooo nreg !)
	// Need for ram value, so to check the rest, go just after the ram definition
	// [ incrForI = (NOT 5th-i-bit) AND (((NOT jstate) AND ramread ) OR ( jstate AND 9th-j-bit )) ]
	//     1061   = (NOT   1019   ) AND (((NOT  1070 ) AND   660   ) OR (  1070  AND   765711  ))
	//     1061   =      10190      AND ((   10700     AND   660   ) OR         10701          )
	//     1061   =      10190      AND (             10702          OR         10701          )
	//     1061   =      10190      AND                             10703

not 1019 10190
not 1070 10700
and 1070 765711 10701
and 10700 660 10702
or 10702 10701 10703
and 10190 10703 1061

//output @incrForI 1061

	// And jstate :
	// If i > sqrt then nothing done, everything is killed, jstate = 0
	// If jstate = 0 and ramread = 0, then !isNotPrime[i], so jstate = 1
	// So it's simply either 0 if i > sqrt, either !incrForI
	// i.e. jstate = MUX 5-th-i-bit !incrForI 0

reg 1071 1070
//output @jstate 1070

	// Now we know our incrForI, we can incr (or not) i
and 1061 1058 1057 
and 1057 1052 1051 
and 1051 1042 1041 
and 1041 1031 1030 
xor 1030 1019 1020 
xor 1041 1031 1032 
xor 1051 1042 1043 
xor 1057 1052 1053 
xor 1061 1058 1059 
//output @i 1058 1052 1042 1031 1019

	// [ jstate = MUX 5-th-i-bit !incrForI 0 ]
	//    1071  = MUX    1019   (NOT 1061) 10 

not 1061 10610
mux 1019 10610 10 1071

	// Switching from jstate=0 to jstate=1 (to initialize j value)
	// 765886 = (!jstate && !5th-i-nit && !ramread)
	// 765886 =   10700  &&    10190   &&  NOT 660
	// 765886 =        10711           &&   6600

and 10700 10190 10711
not 660 6600
and 10711 6600 765886