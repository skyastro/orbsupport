/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Simbad includes planets as separate objects.
 * If everything works, then we will match the planets
 * with the appropriate IDs later on.
 * @author Tom
 */
public class RemovePlanets {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(
          new FileReader("outputs/simbad.filtered.csv"));
        FileWriter fw = new FileWriter("outputs.simbad_np.csv");
        
        String line;
        int nplanets = 0;
        
        line = br.readLine();
        fw.write(line+"\n");
        while ((line=br.readLine()) != null) {
            System.out.println("Line is:"+line);
            String[] flds = line.split(",");
            if (flds[2].matches(".*[a-z]")) {
                nplanets += 1;
            } else {
                fw.write(line+"\n");
            }
        }
        fw.close();
        System.out.println("Number of planet entries deleted:"+nplanets);
    }
}
