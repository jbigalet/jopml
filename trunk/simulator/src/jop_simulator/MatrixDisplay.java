package jop_simulator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class MatrixDisplay extends JPanel {

    public int[][] Matrix = new int[0][0];
    public int ppp = 20, ColorBit = 1;

    public MatrixDisplay(){
        super();
    }

    @Override
    public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            //Color White = new Color(255, 255, 255);
            //Color Black = new Color(0,0,0);

            int aBit = 1 << ColorBit;
            int bBit = aBit << ColorBit;
            int cBit = bBit << ColorBit;
            double ColorProp = 255D/(double)(aBit - 1);

            for(int i=0 ; i<Matrix.length ; i++)
                for(int j=0 ; j<Matrix[0].length ; j++){
                    //g2d.setColor(Matrix[i][j]==0 ? White : Black);
                    int toEncode = Matrix[i][j];
                    int a = (int)(ColorProp*(toEncode % aBit));
                    int b = (int)(ColorProp*((toEncode % bBit)/aBit));
                    int c = (int)(ColorProp*((toEncode % cBit)/bBit));
                    g2d.setColor(new Color(a,b,c));
                    g2d.fillRect(i*ppp, j*ppp, ppp, ppp);
                }
    }

    
}
