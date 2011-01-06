package jop_simulator;

public class Xor extends Gate {

    public Xor(int in1, int in2, int output){
        super("Xor", new int[] {in1, in2}, new int[] {output});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {Inputs[0] ^ Inputs[1]};
    }
}
