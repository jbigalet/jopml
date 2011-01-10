package jop_simulator;

public class MouseHandler extends Gate {
    int nBits;

        // MouseHandler nBits isPressed x[0] .. x[nBits-1]
    public MouseHandler(String[] ParsedArgs){
        super("MouseHandler", new int[] {}, new int[] {});

        this.nBits = Integer.parseInt(ParsedArgs[1]);

        this.OutputArray = new int[1+nBits];
        for(int i=0 ; i<1+nBits ; i++)
            this.OutputArray[i] = Integer.parseInt(ParsedArgs[i+2]);
    }

    public boolean[] Simulate(boolean[] Inputs){
        return Inputs.clone();
    }
}
