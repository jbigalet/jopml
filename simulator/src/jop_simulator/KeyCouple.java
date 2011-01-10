package jop_simulator;

import java.awt.event.*;

public class KeyCouple {
    public boolean isPressed;
    public KeyEvent event;

    public KeyCouple(boolean isPressed, KeyEvent event){
        this.isPressed = isPressed;
        this.event = event;
    }
}
