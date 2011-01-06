package jop_simulator;

public class Ram extends AbstractRam {

    // No debugging for now if the ram is not entered is the right format.
    //  
    // The good format is :
    //     ram @Alias WordSize AddressSize RamSize write_flag [addr]#AS [write_data]#WS [read_data]#WS
    // (Ram size = 2^AdressSize words)
    
    public Ram(String[] ParsedArray){
        super("Ram");

        this.Alias = ParsedArray[1].substring(1);
        WordSize = Integer.parseInt( ParsedArray[2] );
        AddressSize = Integer.parseInt( ParsedArray[3] );
        RamSize = Integer.parseInt( ParsedArray[4] );

        if(RamSize > (1<<AddressSize)){
            System.out.println("Error : RamSize of the ram " + Alias + " larger than 2^AdressSize.");
            System.exit(0);
        }

        this.InputArray = new int[1+AddressSize+WordSize];
        this.OutputArray = new int[WordSize];

        for(int i=0 ; i<this.InputArray.length ; i++)
            this.InputArray[i] = Integer.parseInt( ParsedArray[i+5] );
        for(int j=0 ; j<this.OutputArray.length ; j++)
            this.OutputArray[j] = Integer.parseInt( ParsedArray[j+5+this.InputArray.length] );
            
        //RamArray = new boolean[1<<AddressSize][WordSize];
        RamArray = new boolean[RamSize][WordSize];
    }

}
