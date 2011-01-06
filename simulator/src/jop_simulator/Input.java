package jop_simulator;

public class Input extends Gate {

    public Input(String AliasWithAt, String[] Vars){
        super("Input", new int[] {}, new int[Vars.length-2]);
        for(int i=2 ; i<Vars.length ; i++)
            this.OutputArray[i-2] = Integer.parseInt( Vars[i] );
        this.Alias = AliasWithAt.substring(1);
    }

    public boolean[] Simulate(boolean[] Inputs){
        return Inputs.clone();
    }
}
