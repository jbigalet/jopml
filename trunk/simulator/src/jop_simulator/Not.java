package jop_simulator;

public class Not extends Gate {
    
    public Not(int input, int output){
        super("Not", new int[] {input}, new int[] {output});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {!Inputs[0]};
    }
}
