package jop_simulator;

public class Mux extends Gate {

    public Mux(int Switch, int A, int B, int output){
        super("Mux", new int[] {Switch, A, B}, new int[] {output});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] { ( Inputs[0] ? Inputs[2] : Inputs[1] ) };
    }
}
