package jop_simulator;

public class Reg extends Gate {

    boolean startingValue;

    public Reg(int input, int output, boolean startingValue){
        super("Reg", new int[] {input}, new int[] {output});
        this.startingValue = startingValue;
    }

    public boolean[] Simulate(boolean[] Inputs){
        return new boolean[] {Inputs[0]};
    }
}
