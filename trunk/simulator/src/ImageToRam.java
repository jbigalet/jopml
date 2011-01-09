import java.io.*;
import java.lang.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ImageToRam {

    public static String DecToBin(int x){
        int[] lB = new int[8];
        for(int i=0 ; i<8 ; i++){
            lB[i] = x % 2;
            x /= 2;
        }
        String S = "";
        for(int i : lB)
            S += "" + i;
        return S;
    }

    public static void main(String args[]) throws IOException{
        File file= new File("C:\\genre-country.jpg");
        BufferedImage image = ImageIO.read(file);

        StringBuilder S = new StringBuilder();
        for(int i=0 ; i<image.getHeight() ; i++)
            for(int j=0 ; j<image.getWidth() ; j++){
                int clr = image.getRGB(j,i);
                int red = (clr & 0x00ff0000) >> 16;
                int green = (clr & 0x0000ff00) >> 8;
                int blue =  clr & 0x000000ff;
                S.append(DecToBin(red));
                S.append(DecToBin(green));
                S.append(DecToBin(blue));
            }

        File f = new File("C:\\test.txt");
        BufferedWriter br = new BufferedWriter(new FileWriter( f ));
        br.write(S.toString(), 0, S.length());
        br.close();
    }
}