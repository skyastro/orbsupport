package gaia.addsimbad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class takes the list of HIP objects found with r < 100 pc
 * in SIMBAD where there is no DR2 match and sees if the
 * same SIMBAD name is found in some other entry in the
 * local DR3 table.
 * @author Tom
 */
public class CheckUniqueness {
    public static void main(String[] args) throws Exception {
        Map<String,String> simbad = new HashMap<>();
        BufferedReader br = new BufferedReader(
          new FileReader("f:\\gaia\\simbad\\simbad.hiponly.names"));
        String line;
        int simCount = 0;
        while ( (line = br.readLine()) != null) {
            String[] flds = line.split(",");
            String name = flds[1];
            if (simbad.containsKey(name)) {
                System.out.println("Matched lines on SIMBAD input:");
                System.out.println("  Original: "+simbad.get(name));
                System.out.println("  Secondary:"+line);
            } else {
                simCount += 1;
                simbad.put(name, line);
            }
        }
        Map<String,String> gaia = new HashMap<>();
        
        br.close();
        System.out.println("Number of unique lines:"+simCount);
        
        br = new BufferedReader(
          new FileReader("d:\\programs\\NetBeansProjects\\OrbZ\\public\\data\\galaxy_all.json"));
        int gaiaCount = 0;
        int dupCount = 0;
        while ( (line = br.readLine()) != null) {
            if (line.indexOf("id") < 0) {
                continue;               
            }
            String[] fld1 = line.split(":");
            String frag = fld1[2];
            String[] fld2 = frag.split("\"");
            String name = fld2[1];
            if (gaia.containsKey(name)) {
                System.out.println("Gaia DUP: "+name);
                System.out.println("    "+gaia.get(name));
                System.out.println("    "+line);
                dupCount += 1;
            } else {                                
                gaia.put(name, line);
            }
        }
        System.out.println("Number of glsc stars:"+gaiaCount);
        System.out.println("Duplicated:"+dupCount);
        System.out.println("Size of keyset:"+gaia.keySet().size());
        
        int matchCount = 0;
        int noMatch = 0;
        
        for (String key: simbad.keySet()) {
            if (gaia.containsKey(key)) {
                System.out.println("Match: ");
                System.out.println("  Simbad:  "+simbad.get(key));
                System.out.println("    Gaia: "+gaia.get(key));
                matchCount += 1;
            } else {
                System.out.println(" NoMatch:"+simbad.get(key));
                noMatch += 1;
            }
        }
        System.out.println("Matches:  "+matchCount);
        System.out.println("No match: "+noMatch);                
    }
}
