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
public class OIDList {
    public static void main(String[] oidlist) throws Exception {
        BufferedReader br = new BufferedReader(
           new FileReader("f:\\gaia\\comb\simbad.ng.csv")
        );
        br.readLine();
        int cnt = 0;
        String div = "";
        String line;
        System.out.println("(");
        while  ( (line=br.readLine()) != null) {
            String[] flds = line.split(",");
            System.out.print(div+flds[1]);
            cnt += 1;
            if (cnt%10 == 0) {
                System.out.println();               
            }
            div = ",";
        }
        System.out.println(")");
    }    
}
