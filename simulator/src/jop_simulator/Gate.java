package jop_simulator;

public abstract class Gate {
    public String GateType;
    public int[] InputArray;
    public int[] OutputArray;
    public String Alias = null;

    public Gate(String GateType, int[] InputArray, int[] OutputArray){
        this.GateType = GateType;
        this.InputArray = InputArray.clone();
        this.OutputArray = OutputArray.clone();
    }

    public abstract boolean[] Simulate(boolean[] Inputs);

    public void print(){
        System.out.println(GateType + " :");
        if(InputArray.length != 0){
            System.out.print("\tInput" + (InputArray.length > 1 ? "s" : "") + " : ");
            for(int i=0 ; i<InputArray.length ; i++)
                System.out.print(InputArray[i] + (i!=InputArray.length-1 ? ", " : ""));
            System.out.println();
        }
        if(OutputArray.length != 0){
            System.out.print("\tOutput" + (OutputArray.length > 1 ? "s" : "") + " : ");
            for(int i=0 ; i<OutputArray.length ; i++)
                System.out.print(OutputArray[i] + (i!=OutputArray.length-1 ? ", " : ""));
            System.out.println();
        }
    }
}
