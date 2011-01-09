package jop_simulator;

public class Zero extends Gate {

    public Zero(int var){
        super("Zero", new int[] {}, new int[] {var});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {false};
    }
}
