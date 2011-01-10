
package jop_simulator;


public class AbstractRam extends Gate {

    public boolean[][] RamArray;
    public int WordSize, AddressSize, RamSize;

    public AbstractRam(String Type){
        super(Type, new int[] {}, new int[] {});
    }

    public int BinToDec(boolean[] BinArray, int Start, int End){
        int Dec = 0;
        for(int i=End ; i>=Start ; i--)
            Dec = 2*Dec + ( BinArray[i] ? 1 : 0 );
        return Dec;
    }
    
    public boolean[] Simulate(boolean[] Inputs){
        int Address = BinToDec(Inputs,1,AddressSize);
        if(Address >= RamSize)
            return new boolean[WordSize];
        boolean[] toReturn = RamArray[Address].clone();

        if( Inputs[0] ) // write_flag = true
            for(int i=0 ; i<WordSize ; i++)
                RamArray[Address][i] = Inputs[i+AddressSize+1];

        return toReturn;
    }

    public String getToPrint(){
        String toPrint = "Ram " + this.Alias + " : ";
        for(boolean[] bT : RamArray)
            toPrint += BinToDec(bT, 0, bT.length-1) + ",";
        return toPrint;
    }

    public void printRam(){
        System.out.print("Ram " + this.Alias + " : ");
        for(boolean[] bT : RamArray)
            System.out.print(BinToDec(bT, 0, bT.length-1) + ",");
        System.out.println();
    }
}
