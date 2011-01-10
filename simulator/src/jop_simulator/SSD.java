package jop_simulator;

import java.util.*;

    // Seven-segment display
public class SSD extends Gate {

    private List<boolean[]> MemValues = new ArrayList<boolean[]>();
    private boolean isBrief;

    private static int HorSize;
    private static int VerSize;

        // Use ssd HorizontalSize VerticalSiz0 bit0 bit1 ... bit6
    public SSD(String[] ParsedArray, boolean isBrief){
        super("SSD", new int[7], new int[] {});
            
        this.Alias = ParsedArray[1].substring( 1 );
        this.HorSize = Integer.parseInt(ParsedArray[2]);
        this.VerSize = Integer.parseInt(ParsedArray[3]);
        this.isBrief = isBrief;
        
        for(int i=0 ; i<7 ; i++)
            this.InputArray[i] = Integer.parseInt(ParsedArray[4+i]);
    }

    public boolean[] Simulate(boolean[] Inputs){
        if( !isBrief ){
            System.out.println("\tSeven-Segment Display " + Alias + " = \n");
            String[] sT = getSegs(Inputs);
            for(String S : sT)
                System.out.println("\t\t" + S);
            System.out.println();
        }
        else
            MemValues.add( Inputs );

        return new boolean[] {};
    }

    public String[] getSegs(boolean[] T){
        String[] sT = new String[VerSize];
        String emptyS = "";
        String fullS = "";
        for(int i=0 ; i<HorSize ; i++){
            emptyS += " ";
            fullS += "#";
        }
        for(int i=0 ; i<VerSize ; i++)
            sT[i] = new String(emptyS);

        int midVer = VerSize/2;
        if(T[0]) sT[0] = new String(fullS);
        if(T[3]) sT[midVer] = new String(fullS);
        if(T[6]) sT[VerSize-1] = new String(fullS);

        if(T[5])
            for(int i=0 ; i<=midVer ; i++)
                sT[i] = "#" + sT[i].substring(1);
        if(T[4])
            for(int i=midVer ; i<VerSize ; i++)
                sT[i] = "#" + sT[i].substring(1);
        if(T[1])
            for(int i=0 ; i<=midVer ; i++)
                sT[i] = sT[i].substring(0, HorSize-1) + "#";
        if(T[2])
            for(int i=midVer ; i<VerSize ; i++)
                sT[i] = sT[i].substring(0, HorSize-1) + "#";

        return sT;
    }


    public void printAll(){
        System.out.print("Seven-Segment Display " + Alias + " = \n");
        String[] giantST = new String[VerSize];
        for(int i=0 ; i<VerSize ; i++) giantST[i] = "\t";
        for(boolean[] boolT : MemValues ){
            String[] tmp = getSegs(boolT);
            for(int j=0 ; j<VerSize ; j++)
                giantST[j] += "     " + tmp[j];
        }
        for(String S : giantST)
            System.out.println(S);
        System.out.println();
    }
}
