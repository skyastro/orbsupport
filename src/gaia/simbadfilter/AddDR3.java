
package gaia.simbadfilter;

import gaia.crossid.DeltaChecker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * This class tries to add the DR3 ID to all rows which have a DR2 id.
 * For the ~3,000 objects not in Gaia, we use the negative of the Simbad OID
 * as the base ID.  For objects where there now DR3 id we use the Simbad OID.
 * It also deletes apparent planet lines and converts the names to
 * 'nicer' formats.
 * @author Tom
 */
public class AddDR3 {
    public static void main(String[] args) throws Exception {
        new AddDR3().add();
    }
    NameFixer nf = new NameFixer();
    void add() throws Exception {
        DeltaChecker dc = new DeltaChecker();
        String line;
        // This file should be created by running the following command
        //    cat simbad.filtered.csv|(read -r; printf "%s\n" "$REPLY"; sort -k 3 -n --field-separator=,)  > simbad.srt.csv
        // then moving the header line back up to the top (it will be after the null dr2 lines)
        // The output of this command should be sorted according to the DR3 id using
        //    cat simbad.dr3.csv|(read -r; printf "%s\n" "$REPLY"; sort -k 1 -n --field-separator=,)  > simbad.dr3.srt.csv
        
        BufferedReader br = new BufferedReader(
          new FileReader("outputs/simbad.srt.csv")
        );
        FileWriter fw = new FileWriter("outputs/simbad.dr3.csv");
        // Copy header line;
        line = br.readLine();
        line += "\n";
        fw.write("dr3id,"+line);
        int changes = 0;
        int same    = 0;
        int noDR2   = 0;
        int noDR3   = 0;
        int planets = 0;
        int cnt = 0;
        while ((line=br.readLine()) != null) {
            line += "\n";
            String[] flds = line.split(",");
            String name = flds[1];
            if (name.matches(".*[ 0-9A-Z][b-u]")  &&
                    // Special classes that are really stars.
                    !name.matches(".*Mi")  && 
                    !name.matches(".*CVn")  &&
                    !name.matches("Melotte.*")) {
                System.out.println("Skipping planet: "+name);
                planets += 1;
                continue;
            }
            String upd = nf.processName(name);
            if (!upd.equals(name)) {
                line = line.replace(","+name+"," , ","+upd+","); 
            }
            
            String gid = flds[2];
            if (gid.length() == 0) {
                // Just copy the line with a null
                // for the DR3 ID.
                noDR2 += 1;
                // Use the negative of the OID 
                fw.write("-"+flds[0]+","+line);
            } else {
                long dr2id = Long.parseLong(gid);
                long dr3id = dc.dr3id(dr2id);
                if (dr3id != dr2id) {
                    changes += 1;
                    if (dr3id == -1) {
                        noDR3 += 1;
                        // If we don't seem to have a DR3 ID for this DR2 id,
                        // then we use the negative of the Simbad OID (just
                        // like when we have no DR2).  This seems to be
                        // just a few very bright stars.
                        dr3id = - Long.parseLong(flds[0]);
                    }
                } else {
                    same += 1;
                }
                fw.write(dr3id+","+line);
            }
        }
        fw.close();
        System.out.println("Skipped planets: "+planets);
        System.out.println("NO DR2:          "+noDR2);
        System.out.println("NO DR3:          "+noDR3);
        System.out.println("Same ID:         "+same);
        System.out.println("Changed ID:      "+changes);
    }
}
