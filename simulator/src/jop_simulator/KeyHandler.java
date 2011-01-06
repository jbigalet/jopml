package jop_simulator;

public class KeyHandler extends Gate {
    int keyCode;

    public KeyHandler(int keyCode, int whenPressed, int whenReleased){
        super("KeyHandler", new int[] {}, new int[2]);

        this.keyCode = keyCode;
        this.OutputArray[0] = whenPressed;
        this.OutputArray[1] = whenReleased;
    }

    public boolean[] Simulate(boolean[] Inputs){
        return Inputs.clone();
    }
}
