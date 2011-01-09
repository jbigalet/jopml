package jop_simulator;

public class One extends Gate {

    public One(int var){
        super("One", new int[] {}, new int[] {var});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {true};
    }
}
