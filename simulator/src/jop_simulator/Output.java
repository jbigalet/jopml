package jop_simulator;

import java.util.*;

public class Output extends Gate {

    private List<Integer> MemValues = new ArrayList<Integer>();
    private boolean isBrief;

    public Output(String[] ParsedArray, boolean isBrief){
        super("Output", new int[] {}, new int[] {});
        this.isBrief = isBrief;

            // If there is an Alias, then we start to explore the
            // array for values from 2, else we start from 1.
            // The Alias comes to be without the @, or it is
            // the first variable to be in the array.

        int iStart = ( ParsedArray[1].startsWith("@") ) ? 2 : 1;
        Alias = ParsedArray[1].substring( iStart - 1 );

        int[] finalO = new int[ParsedArray.length - iStart];
        for(int i=iStart ; i<ParsedArray.length ; i++)
            finalO[i - iStart] = Integer.parseInt(ParsedArray[i]);

        this.InputArray = finalO.clone();
    }

    // Now LSBF instead of MSBF.
    private int BinToDec(boolean[] BinArray){
        int Dec = 0;
        for(int i=BinArray.length-1 ; i>=0 ; i--)
            Dec = 2*Dec + ( BinArray[i] ? 1 : 0 );
        return Dec;
    }

    public boolean[] Simulate(boolean[] Inputs){
        if( !isBrief )
            System.out.println("Output " + Alias + " = " + BinToDec( Inputs ));
        else
            MemValues.add( BinToDec(Inputs) );

        return new boolean[] {};
    }

    public String getOutput(boolean[] Inputs){
        return Alias + " : " + BinToDec(Inputs);
    }

    public void printAll(){
        System.out.print("Output " + Alias + " = \n\t");
        for(int i : MemValues )
            System.out.print( i + " " );
        System.out.println();
    }
}
