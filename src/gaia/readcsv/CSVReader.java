package gaia.readcsv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Read a CSV file with content of the GAIA DR3
 * Nearby Stars Catalogue and reformat into
 * JSON.
 * @author Tom
 */
public class CSVReader {
    Map<String,String> fields = new HashMap<>();
    {
         fields.put("source_id","dr3id");
         fields.put("ra", "ra");
         fields.put("dec", "dec");
         fields.put("parallax", "p");
         fields.put("pmra", "pmra");
         fields.put("pmdec", "pmdec");
         fields.put("phot_g_mean_mag", "gm");
         fields.put("phot_bp_mean_mag", "bm");
         fields.put("phot_rp_mean_mag", "rm");
         fields.put("xcoord_50", "x");
         fields.put("ycoord_50", "y");
         fields.put("zcoord_50", "z");
         fields.put("uvel_50", "u");
         fields.put("vvel_50", "v");
         fields.put("wvel_50", "w");
    }
    Map<String,Integer> fieldLocations = new HashMap<String,Integer>();
    String[] want = {
        "source_id", "ra", "dec", "parallax", "parallax_error",
        "pmra", "pmdec", 
        "phot_g_mean_mag", "phot_bp_mean_mag", "phot_rp_mean_mag",
        "xcoord_50", "ycoord_50", "zcoord_50",
        "uvel_50","vvel_50","wvel_50" 
    };
    
    public static void main(String[] args) throws Exception {
        String file = "table.csv";
        if (args.length > 0) {
            file = args[1];
        }
        new CSVReader().run(file);
    }
    
    void run(String file) throws Exception {
        String outfile = file.replace(".csv", ".json");
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        // Get the order of the files from the first line
        String line = br.readLine();
        String[] names = line.split(",");
        for (int i=0; i<names.length; i += 1) {
            fieldLocations.put(names[i], i);            
        }

        int count = 0;
        while ( (line=br.readLine()) != null) {
            openEntry(bw);
            String[] values = line.split(",");
            String prefix = " ";
            for (String key: want) {
                int index = fieldLocations.get(key);
                bw.write(prefix+fields.get(key)+":"+values[index]);
                prefix = ",";
            }
            closeEntry(bw);
            count += 1;
            if (count % 10000 == 0) {
                System.out.println("Wrote "+count+" records");
            }          
        }
        br.close();
        bw.close();
    }
    boolean first;
    void openEntry(Writer wr) throws Exception {
        if (first) {
            wr.write("  {");
        } else {
            wr.write(" ,{");
        }
        first = false;    
    }
    void closeEntry(Writer wr) throws Exception {
        wr.write("}\n");
    }
}
