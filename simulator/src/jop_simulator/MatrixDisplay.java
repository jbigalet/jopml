package jop_simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class MatrixDisplay extends JPanel {

    public int[][] Matrix = new int[0][0];
    public int ppp = 20, ColorBit = 1;
    public boolean init = true;
    public boolean isPixToDraw = false;
    public int pixToDrawX, pixToDrawY;

    public MatrixDisplay(){
        super();
    }

    @Override
    public void paintComponent(Graphics g) {
            if(init)
                super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            //Color White = new Color(255, 255, 255);
            //Color Black = new Color(0,0,0);

            int aBit = 1 << ColorBit;
            int bBit = aBit << ColorBit;
            int cBit = bBit << ColorBit;
            double ColorProp = 255D/(double)(aBit - 1);

            if(init){
                for(int i=0 ; i<Matrix.length ; i++)
                    for(int j=0 ; j<Matrix[0].length ; j++){
                        //g2d.setColor(Matrix[i][j]==0 ? White : Black);
                        Color tmpC = getColor( Matrix[i][j], aBit, bBit, cBit, ColorProp );
                        g2d.setColor(tmpC);
                        g2d.fillRect(i*ppp, j*ppp, ppp, ppp);
                    }
            } // end init
            else if(isPixToDraw){
                Color tmpC = getColor( Matrix[pixToDrawX][pixToDrawY], aBit, bBit, cBit, ColorProp );
                g2d.setColor(tmpC);
                g2d.fillRect(pixToDrawX*ppp, pixToDrawY*ppp, ppp, ppp);
            }
            isPixToDraw = false;
    }

    public static Color getColor( int toEncode, int aBit, int bBit, int cBit, double ColorProp ){
        int a = (int)(ColorProp*(toEncode % aBit));
        int b = (int)(ColorProp*((toEncode % bBit)/aBit));
        int c = (int)(ColorProp*((toEncode % cBit)/bBit));
        return new Color(a,b,c);
    }

    
}
