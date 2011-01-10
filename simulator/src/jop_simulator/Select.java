package jop_simulator;

public class Select extends Gate {
    public int nBits;

        // Use : select nbits
    public Select( String[] parsedArgs ){
        super("Select", new int[] {}, new int[] {});

        nBits = Integer.parseInt(parsedArgs[1]);
        
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] { ( Inputs[0] ? Inputs[2] : Inputs[1] ) };
    }
}
