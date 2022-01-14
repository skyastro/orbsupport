/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author Tom
 */
public class CommaCounter {
    public static void main(String[] args) throws Exception {
        int expect = 10;
        BufferedReader rdr = new BufferedReader(
           new FileReader("src/inputs/simBasic.csv"));
        String line;
        int lc = 0;
        while ( (line = rdr.readLine()) != null) {
            if (line.length() == 0) {
                System.out.println("0len line");
                continue;                
            }
            int off = 0;
            int count = 0;
            while (  (off = line.indexOf(",", off)) >= 0 ) {
                count += 1;
                off += 1;
            }
            if (count != expect) {
                System.out.println("#"+lc+"count: "+count+" : "+line);
            }
        }
    }    
}
