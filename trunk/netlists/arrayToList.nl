// -brief -i -tick 1000 -ram iNP=C:\\Users\\Mystic\\Documents\\Nouveau_Dossier\\netlists\\arrayToList.ram C:\\Users\\Mystic\\Documents\\Nouveau_Dossier\\netlists\\arrayToList.nl

zero 10
one 11

	// If everything is done, we can start to put our prime numbers in a ram composed of integers.
	// We got a flag (PutInList=1019) which is equal to !incrForI && !jstate, or equal to 5th-i-bit (1019).
	// "read_data" is not used, so we put some dont-care-numbers on.

	// We got two counters, one (i1) to browse the 'isNotPrime' ram and one (i2) to see were we are on the 'Prime_List' ram.
	// Both are 8 bits counter :


	// For i1 :

reg 30107 30106 
reg 30101 30100 
reg 3091 3090 
reg 3080 3079 
reg 3068 3067 
reg 3055 3054 
reg 3041 3040 
reg 3026 3025 

	// We incr i1 as soon as the program start, and we stop it when i1=255.
	// So, we start to check if i1 = 255 :

and 30106 30100 5001
and 3090 3079 5002
and 3067 3054 5003
and 3040 3025 5004
and 5001 5002 5005
and 5003 5004 5006
and 5005 5006 5007

not 5007 30108
//output @incr_i_1 30108

//input @incr 30108 

and 30108 30106 30105 
and 30105 30100 3099 
and 3099 3090 3089 
and 3089 3079 3078 
and 3078 3067 3066 
and 3066 3054 3053 
and 3053 3040 3039 
and 3039 3025 3024 
xor 3039 3025 3026 
xor 3053 3040 3041 
xor 3066 3054 3055 
xor 3078 3067 3068 
xor 3089 3079 3080 
xor 3099 3090 3091 
xor 30105 30100 30101 
xor 30108 30106 30107 
//output @i1 30106 30100 3090 3079 3067 3054 3040 3025

	// Again, isNotPrime ram, with a never_used write_flag and write_data, adress = i1 and read_data in 660.
ram @iNP 1 8 10 30106 30100 3090 3079 3067 3054 3040 3025 11 660

	// If read_data (660) == 0, that means i1 is a prime, so we incr i2 and we add on the 2nd ram i1.

	// For i2 :

not 660 40108
//input @incr 40108 

nreg 40107 40106 
nreg 40101 40100 
nreg 4091 4090 
nreg 4080 4079 
nreg 4068 4067 
nreg 4055 4054 
nreg 4041 4040 
nreg 4026 4025 
and 40108 40106 40105 
and 40105 40100 4099 
and 4099 4090 4089 
and 4089 4079 4078 
and 4078 4067 4066 
and 4066 4054 4053 
and 4053 4040 4039 
and 4039 4025 4024 
xor 4039 4025 4026 
xor 4053 4040 4041 
xor 4066 4054 4055 
xor 4078 4067 4068 
xor 4089 4079 4080 
xor 4099 4090 4091 
xor 40105 40100 40101 
xor 40108 40106 40107 
//output @i2 40106 40100 4090 4079 4067 4054 4040 4025

	// Of course, the adress is i2, write_data is i1, and the write_flag is incr_i2 (40108)

ram @Prime_List 8 8 40108 40106 40100 4090 4079 4067 4054 4040 4025 30106 30100 3090 3079 3067 3054 3040 3025 801 802 803 804 805 806 807 808