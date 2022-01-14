/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Simbad includes planets as separate objects.
 * If everything works, then we will match the planets
 * with the appropriate IDs later on.
 * @author Tom
 */
public class DistanceFilt {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(
          new FileReader("F:\\gaia\\comb\\simbad.ng_np.csv"));
        String line;
        int tooFar = 0;
        int noDist = 0;
        
        line = br.readLine();
        System.out.println(line);
        while ((line=br.readLine()) != null) {
            String[] flds = line.split(",");
            if (flds[6].length() > 0) {
                double dist = Double.parseDouble(flds[6]);
                if (dist < 100) {
                    System.out.println(line);
                } else {
                    tooFar += 1;
                }
            } else {
                noDist += 1;
            }
        }
        System.out.println("Too distant:"+tooFar);
        System.out.println("No distance:"+noDist);
    }    
}
