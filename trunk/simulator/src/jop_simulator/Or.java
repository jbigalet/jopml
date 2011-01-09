package jop_simulator;

public class Or extends Gate {

    public Or(int in1, int in2, int output){
        super("Or", new int[] {in1, in2}, new int[] {output});
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {Inputs[0] || Inputs[1]};
    }
}
