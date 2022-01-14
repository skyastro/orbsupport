/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package orbsupport.smb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Tom
 */
public class GetSMB {
    
    static final String[] URLs =
    {
     "https://www.minorplanetcenter.net/Extended_Files/mpcorb_extended.json.gz",
     "https://www.minorplanetcenter.net/Extended_Files/cometels.json.gz"
    };
    

    BufferedReader URLReader(String theURL) throws Exception {
        return new BufferedReader(
            new InputStreamReader(
                new GZIPInputStream(
                    new URL(theURL).openStream()
                 )));
    }
    public static void main(String[] args) throws Exception {
        int nTotal = 50000;
        if (args.length > 0) {
            nTotal  = Integer.parseInt(args[0]);  // Total number of objects to write out
        }
        int minType = 3000;
        if (args.length > 1) {
            minType = Integer.parseInt(args[1]);  // Number to be included before filtering in a given class
        }
        GetSMB proc = new GetSMB(nTotal, minType);
        proc.process();
    }
    
    private int      total;
    private int      minType;
    private int      left;
    private int      fileCount = 0;
    private Set<String> fields = new HashSet<>(Arrays.asList(new String[]{
        "H","G", "Name", "Number", "M", "Peri", "Node","i", "e","n", "a", "Orbit_type",
        "Year_of_perihelion", "Month_of_perihelion", "Day_of_perihelion",
        "Perihelion_dist", "Designation_and_name", "Ref", "Epoch"}));
    
    GetSMB(int total, int minType) {
        this.total   = total;
        this.minType = minType; 
        this.left    = 0;
    }
    
    private Map<String, Integer> typeCount;
    void process() throws Exception {
        typeCount = getCount();
        int useCount = 0;
        for (String key: typeCount.keySet()) {
            System.out.println(key+": "+typeCount.get(key));
            int tCnt = Math.min(this.minType, typeCount.get(key));
            if (tCnt < typeCount.get(key)) {
                typeCount.put(key, tCnt);  
            }
            useCount += tCnt;
        }
        this.left = this.total - useCount;
        System.out.println("UseCount is:     "+useCount);
        System.out.println("Unassigned count:"+this.left);
        if (left < 0) {
            System.out.println("Cannot get all requested data into size requested.");
            System.exit(-1);
        }
        copy();
    }
    void writeHeader(BufferedWriter bw) throws Exception {
        bw.write("{\"mpcdata\":[\n");
    }
    void writeFooter(BufferedWriter bw) throws Exception {
        bw.write("]}\n");
    }
    
    void copy() throws Exception {        
        BufferedWriter bw = new BufferedWriter(
          new FileWriter("comb.json"));
        writeHeader(bw);
        int count = 0;
        boolean first = true;
        for (fileCount=0; fileCount<URLs.length; fileCount += 1) {
            System.out.println("Starting: "+URLs[fileCount]);
            BufferedReader rdr = URLReader(URLs[fileCount]);
            rdr.readLine();  // Skip first line.
            List<String> entry;
            while ( (entry = readEntry(rdr)) != null) {
                count += 1;
                writeEntry(entry, bw, first);
                first = false;
            }
            rdr.close();
            System.out.println("Count after "+URLs[fileCount]+": "+count);
        }
        
        System.out.println("Got total count:"+count);
        writeFooter(bw);
        bw.close();
    }
    
    void writeEntry(List<String> entry, BufferedWriter bw, boolean first) throws Exception {
        // Parse the entry to get the type and see if it has a name.
        String name = null;
        String type = null;
        for (String line: entry) {
            line = line.trim();
            if (line.startsWith("\"Name\"")) {
                name = lineValue(line);
            } else if (line.startsWith("\"Orbit_type\"")) {
                type = lineValue(line);                
            }
        }
        
        int remainder = typeCount.get(type);
        // We will write the entry if
        //    remainder > 0:  first minType of this class of objects (e.g., 3000)
        //    this.left > 0:  within total count for the output (e.g., 50,000 entries)
        //    name is not null:  want to make sure that all explicitly named objects are included.
        if (remainder > 0 || this.left > 0 || name != null) {
            spitOut(entry, bw, first);
            remainder -= 1;
            typeCount.put(type, remainder);
            if (remainder > 0) {
                // Nothing
            } else if (remainder == 0) {
                System.out.println("Exhausted class:"+type);
            } else {
                // Remainder is < 0, used other criteria for writing
                this.left -= 1;
                if (this.left > 0) {
                    // nothing
                } else if (this.left == 0) {
                    System.out.println("Exhausted total limit.");
                } else  { 
                    // Must have been name.
                    System.out.println("Write "+name+" beyond limit");
                }                                
            }
        }
    }
    
    void spitOut(List<String> entry, BufferedWriter bw, boolean first) throws Exception {
        if (first) {
           bw.write(" {\n");
        } else {
           bw.write (",\n{\n");
        }
        
        String prefix = " ";
        for (String line: entry) {
            line = line.trim();
            if (line.endsWith(",")) {
                line = line.substring(0,line.length()-1);                
            }
            // Can have array of values terminated on new line so lines
            // can start with " or ]
            if (line.charAt(0) == ']') {
                continue;
            }
            if (line.charAt(0) != '"') {
                System.out.println("Unexpected character starts line:"+line);
                continue;
            }
            int epos = line.indexOf('"', 1);
            String field = line.substring(1,epos);
            if (fields.contains(field)) {
                bw.write(prefix+line);
                prefix = ",\n ";
            }
        }
        bw.write("\n}");
    }
    
    String lineValue(String line) {
        String[] flds = line.split(":", 2);
        String val = flds[1].trim();
        val = val.replace("\"", "");
        if (val.endsWith(",")) {
            val=val.substring(0,val.length()-1);
        }
        return val;
    }
    
    List<String> readEntry(BufferedReader rdr) throws Exception  {
        
        List<String> entry = new ArrayList<>();
        String line;
        boolean foundBracket = false;
        while ( (line = rdr.readLine()) != null) {
//            if (fileCount == 1) {
//                System.out.println("Readentry reading:"+line+" "+foundBracket);
//            }
            if (!foundBracket) {
                if (line.indexOf("{") >= 0) {
                    foundBracket = true;
                }
            } else {
                if (line.indexOf("}")  >= 0) {
                    return entry;
                }
                entry.add(line);
            }
        }
        System.out.println("EOF reached");
        return null;
    }
    
    Map<String,Integer> getCount() throws Exception {
        typeCount = new HashMap<String,Integer>();
        for (int i=0; i<URLs.length; i += 1) {
            BufferedReader rdr = URLReader(URLs[i]);
            String line;
            while ( (line = rdr.readLine()) != null) {
                if (line.indexOf("Orbit_type") >= 0) {
                    String[] flds = line.split(":");
                    if (flds.length != 2) {
                        System.out.println("Strange line in count:"+line);
                        continue;
                    }
                    String type = flds[1].trim();
                    if (type.endsWith(",")) {
                        type = type.substring(0, type.length()-1);
                    }
                    type = type.replace("\"", "");
                    if (!typeCount.containsKey(type)) {
                        typeCount.put(type, 1);
                    } else {
                        typeCount.put(type, typeCount.get(type)+1);
                    }
                }
            }
            rdr.close();
        }
        return typeCount;        
    }
}
