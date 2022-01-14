/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.readsrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * @author Tom
 */
public class ReadCol {
   
    BufferedReader br;
    int index;
    
    ReadCol(File src, int index) throws Exception {
        this.index = index;
        br = new BufferedReader(
                new InputStreamReader(
                    new GZIPInputStream(
                        new FileInputStream(src)
                    )
                )
        );
        // Skip the first header line.
        String hdr = br.readLine();
    }
    
    String nextRow() throws Exception {
        String line = br.readLine();
        if (line == null) {
            br.close();
            return null;
        }
        line = line.trim();
        
        if (line.length() == 0) {
            return nextRow();  // In prinicple we allow interior blank lines.
                               // But mostly guarding for them at the end.
        }
        
        String[] flds = line.split(",", index+2);
        if (flds.length < index) {
            System.err.println("Too few fields in line:"+line);
        }
        return flds[index];
    }    
}
