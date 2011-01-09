package jop_simulator;

import javax.swing.*;

public class Ram_Graphic extends AbstractRam {
    public int VerticalGraphSize, HorizontalGraphSize, ppp, ColorBit;
    public JFrame GraphicFrame;
    public MatrixDisplay GraphicPanel;

    // No debugging for now if the ram is not entered is the right format.
    //
    // The good format is :
    //     ram @Alias WordSize AddressSize RamSize Horizontal_Graph_Size
    //         Vertical_Graph_Size write_flag [addr]#AS [write_data]#WS [read_data]#WS
    // (Ram size = 2^AdressSize words)
    // The first part of the ram is a Matrix Display of the defined size

    public Ram_Graphic(String[] ParsedArray){
        super("Ram_Graphic");

        this.Alias = ParsedArray[1].substring(1);
        WordSize = Integer.parseInt( ParsedArray[2] );
        AddressSize = Integer.parseInt( ParsedArray[3] );
        RamSize = Integer.parseInt( ParsedArray[4] );
        HorizontalGraphSize = Integer.parseInt( ParsedArray[5] );
        VerticalGraphSize = Integer.parseInt( ParsedArray[6] );
        ppp = Integer.parseInt( ParsedArray[7] );
        ColorBit = Integer.parseInt( ParsedArray[8] );

        if(RamSize > (1L<<AddressSize)){
            System.out.println("Error : RamSize of the ram " + Alias + " larger than 2^AdressSize.");
            System.exit(0);
        }

        if(HorizontalGraphSize*VerticalGraphSize > RamSize){
            System.err.println("Error : In the ram " + Alias + " you specified a to large display for the RamSize.");
            System.exit(0);
        }

        this.InputArray = new int[1+AddressSize+WordSize];
        this.OutputArray = new int[WordSize];

        for(int i=0 ; i<this.InputArray.length ; i++)
            this.InputArray[i] = Integer.parseInt( ParsedArray[i+9] );
        for(int j=0 ; j<this.OutputArray.length ; j++)
            this.OutputArray[j] = Integer.parseInt( ParsedArray[j+9+this.InputArray.length] );

        //RamArray = new boolean[1<<AddressSize][WordSize];
        RamArray = new boolean[RamSize][WordSize];

        GraphicFrame = new JFrame(Alias);
        GraphicFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GraphicPanel = new MatrixDisplay();
        GraphicPanel.ppp = ppp;
        GraphicPanel.ColorBit = ColorBit;
        GraphicFrame.add(GraphicPanel);
        GraphicFrame.setSize(16+ppp*HorizontalGraphSize, 40+ppp*VerticalGraphSize);
        GraphicFrame.setVisible(true);
    }

    public int[][] getMatrixDisplay(){
        int[][] Matrix = new int[HorizontalGraphSize][VerticalGraphSize];
        int HorPos = 0, VerPos = 0, graphSize = HorizontalGraphSize*VerticalGraphSize;
        for(int i=0 ; i<graphSize ; i++){
            Matrix[HorPos][VerPos] = BinToDec(RamArray[i], 0, RamArray[i].length-1);
            HorPos++;
            if(HorPos>=HorizontalGraphSize){
                HorPos = 0;
                VerPos++;
            }
        }
        return Matrix;
    }

    @Override
    public boolean[] Simulate(boolean[] Inputs){
        int Address = BinToDec(Inputs,1,AddressSize);
        if(Address >= RamSize)
            return BZero;
        boolean[] toReturn = RamArray[Address].clone();

        if( Inputs[0] ) // write_flag = true
            for(int i=0 ; i<WordSize ; i++)
                RamArray[Address][i] = Inputs[i+AddressSize+1];

        GraphicPanel.Matrix = getMatrixDisplay();
        GraphicPanel.repaint();

        return toReturn;
    }
}
