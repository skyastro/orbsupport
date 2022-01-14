/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tom
 */
public class HipAdder {
    public static void main(String[] args) throws Exception {
        new HipAdder().run();
    }
    Map<String,String> ids = new HashMap<>();
    HipAdder() throws Exception {
        BufferedReader br = new BufferedReader(
          new FileReader("F:\\gaia\\comb\\hipids.dat"));
        String line;
        while ( (line = br.readLine()) != null) {
            String[] flds = line.split("\\|");
            if (flds.length != 2) {
                System.out.println("Line: "+line+" "+flds.length);
            }
            String hip = flds[0];
            hip = hip.substring(4,hip.length()-1);
            hip = hip.trim();
            String oid = flds[1].trim();
            ids.put(oid, hip);
        }
    }
    void run() throws Exception {
        BufferedReader br = new BufferedReader(
          new FileReader("F:\\gaia\\comb\\simbad.ng.csv"));
        String line;
        int found = 0;
        int missing = 0;
                
        line = br.readLine();
        System.out.println(line);
        while ( (line = br.readLine())!= null) {
            String[] flds = line.split(",");
            String hip = ids.get(flds[1]);
            if (hip == null) {
                hip = "";
                missing += 1;
            } else {
                found += 1;
            }
            if (flds[0].equals("-1")) {
                line=line.substring(2);  // Get rid of -1
            }
            System.out.println(hip+line);
        }
        System.out.println("Found:   "+found);
        System.out.println("Missing: "+missing);
    }    
}