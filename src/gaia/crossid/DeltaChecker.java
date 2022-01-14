package gaia.crossid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class to see how DR2 IDs translate into DR3
 * using delta and crossmiss files.
 * @author Tom
 */
public class DeltaChecker {
    BufferedReader missing;
    BufferedReader deltas;
//    String fileDir = "/mnt/c/users/tom/gaia/";
    String fileDir = "c:\\users\\tom\\gaia\\";
    long currMiss = -1;
    long currDelta = -1;
    long currDr3 = -1;
    Set<Long> misses   = new HashSet<>();    
    long[][] changes  = new long[2][]; 
    public DeltaChecker() throws Exception {
        System.out.println("Initializing DeltaChecker");
        missing = new BufferedReader(
          new FileReader(fileDir + "crossmiss.unq"));
        deltas = new BufferedReader(
          new FileReader(fileDir + "deltas.filt.dat"));
        
        String line;
        int mcount = 0;
        while ( (line = missing.readLine()) != null) {
            long m = Long.parseLong(line);
            misses.add(m);
            mcount += 1;
        }
        missing.close();
        System.out.println("   Finished reading missing dr2s");
        int ccount = 0;
        while ( (line = deltas.readLine()) != null) {
            ccount += 1;
            if (ccount % 5000000 == 0) {
                System.out.println("   Init:"+ccount);
            }
        }
        deltas.close();
        System.out.println("   Finished first pass for changes.  Total: "+ccount);
        changes[0] = new long[ccount];
        changes[1] = new long[ccount];
        
        deltas = new BufferedReader(
          new FileReader(fileDir + "deltas.filt.dat"));
        
        int total = ccount;
        ccount = 0;
        while ( (line = deltas.readLine()) != null) {
            String[] flds = line.split(" ");
            changes[0][ccount] = Long.parseLong(flds[0]); 
            changes[1][ccount] = Long.parseLong(flds[1]);
            
            ccount += 1;
            if (ccount % 5000000 == 0) {
                System.out.println("   Second pass: "+ccount+"/"+total);
            }
        }
        deltas.close();
        System.out.println("Finished DeltaChecker initialization");
    }
    
    public static void main(String[] args) throws Exception {
        DeltaChecker dc = new DeltaChecker();
        if (args.length == 0) {
            args = new String[] {"5853498713160606720"};
        }
        for (int i=0; i<args.length; i += 1) {
            long lng = Long.parseLong(args[i]);
            System.out.println("Input:  "+lng);
            System.out.println("Output: "+dc.dr3id(lng));            
        }
    }
    
    
    /** Check to see if the given dr2id is in either the
     * crossmiss, or deltas file.  If in the crossmiss file
     * then return -1: there is no counterpart in the DR3.
     * If in the deltas files, then return the corresponding
     * DR3 id.  Otherwise just return the input.
     */
    public long dr3id(long dr2id) throws Exception {
        if (misses.contains(dr2id)) {
            return -1;
        }
        int offset = Arrays.binarySearch(changes[0], dr2id);
        if (offset < 0 || offset >= changes[0].length) {
            return dr2id;
        }
        if (changes[0][offset] == dr2id) {
            return changes[1][offset];
        } else {
            return dr2id;
        }
    }
}