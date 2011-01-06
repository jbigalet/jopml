package jop_simulator;

public class Random extends Gate {

    public Random(int output){
        super("Random", new int[] {}, new int[] {output});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] { (Math.random()<0.5) };
    }
}
