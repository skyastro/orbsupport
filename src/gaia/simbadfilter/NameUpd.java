/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 *
 * @author Tom
 */
public class NameUpd {
    public static void main(String[] arsg) throws Exception {
        NameFixer nf = new NameFixer();
        BufferedReader br = new BufferedReader(
          new FileReader("f:\\gaia\\simbad\\simbad.hiponly"));
        String line;
        while ( (line=br.readLine()) != null) {
            String[] flds = line.split(",", 11);
            String prefix = "";
            for (int i=0; i<11; i += 1) {
                if (i == 1) {
                    flds[i] = nf.processName(flds[i]);                    
                }
                System.out.print(prefix+flds[i]);
                prefix = ",";
            }
            System.out.println();
        }
        
    }    
}
