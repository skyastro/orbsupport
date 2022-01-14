package orbsupport.galaxies;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This class reads a file generated by downloading the HEASARC version of the 
 * Karachentsev, Nearby Galaxies catalog in the pure text format and transforms
 * it into the JSON format used in E&IB.
 * @author Tom
 */
public class GalaxyParser {
    public static void main(String[] args) throws Exception {
        String output = "outputs/galaxies.js";
        if (args.length == 1) {
            output = args[0];
        } else if (args.length > 0) {
            System.out.println("Usage:\n  GalaxyParser output  \n");
            return;
        }
        new GalaxyParser(output).parse();
    }
    String mainQuery = "https://heasarc.gsfc.nasa.gov/xamin/CLIServlet?table=neargalcat&fields=all";
    String posAngs   = "https://heasarc.gsfc.nasa.gov/xamin/CLIServlet?table=neargalcat,rc3&fields=a.name,b.position_angle&offset=a:b:1&constraint=not+position_angle+is+null";
    String simbad    = "src/inputs/simbadBibcodeQuery.txt";
    private BufferedReader inp;    
    private BufferedWriter out;
    private String sep = "";
    private String xsep = "";
    Map<String, Double> angs = new HashMap<>();
    Map<String,Integer> indices = new HashMap<>();
    Map<String, String> simbadNames = new HashMap<>();
    
    public GalaxyParser(String output) throws Exception {
        getPosangs();
        getSimbad();
        inp = new BufferedReader(
                new InputStreamReader(
                  new URL(mainQuery).openStream()));
        out = new BufferedWriter(new FileWriter(output));
    }
    
    public void getSimbad() throws Exception {
        System.out.println("Parsing SIMBAD names");
        inp = new BufferedReader(
                new FileReader(simbad));
        
        String line;
        int state = 0;
        while ((line = inp.readLine()) != null) {
            if (state > 1) {
                break;
            }
            switch(state) {
                case 0:
                    if (line.startsWith(" # |")) { // Found header line
                        inp.readLine(); // Skip divider line.
                        state = 1;
                    }
                    break;
                        
                case 1:
                    if (line.startsWith("==")) {
                        state = 2;
                        break;
                    }
                    String[] fields = line.split("\\s*\\|\\s*");
                    String catName = fields[3];
                    String simName = fields[4];
                    catName = catName.replace("catalog:Name= ", ""); 
                    if (simName.startsWith("NAME ")) {
                        // Want just name, but we need to check this when
                        // we look to match for base name in SIMBAD.
                        simName = simName.substring(5);  // Get rid of NAME prefix                                                           
                    }
                    simbadNames.put(catName, simName);
                
            }
        }
        inp.close();
    }
    
    void getPosangs() throws Exception {
        System.out.println("Finding orientations using RC3");
        inp = new BufferedReader(
                new InputStreamReader(
                  new URL(posAngs).openStream()));
        String line;
        inp.readLine();  //Skip first line which is header.
        while ( (line = inp.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                System.out.println("Found "+angs.size()+" position angles");
                return;
            }
            String[] fields = line.split("\\|");
            if (fields.length != 2) {
                System.out.println("Invalid line:"+line);
                continue;
            }
            angs.put(fields[0].trim(), Double.parseDouble(fields[1].trim()));
        }
    }
    
    void parse() throws Exception {
        System.out.println("Reading and parsing Karachensev catalog");
        parseFirstLine(inp);
        writeHeader(out);
        String line;
        while (true) {
            line = inp.readLine();
            
            // Check for last line.  Note that there may be some stuff at the end
            // of the file that we just ignore.
            if (line == null || line.trim().length() == 0) {
                break;
            }
            String[] flds = line.split("\\|");
            processFields(flds);
        }
        inp.close();
        writeFooter(out);
        out.close();
    }
    
    void writeHeader(BufferedWriter out) throws Exception {
        out.write("let karachentsev = [\n");        
    }
    void writeFooter(BufferedWriter out) throws Exception {
        out.write("\n];\nexport default karachentsev;\n");
    }
    
    void parseFirstLine(BufferedReader inp) throws Exception {
        String line = inp.readLine();
        String[] flds = line.split("\\|");
        for (int i=0; i<flds.length; i += 1) {
            indices.put(flds[i].trim(), i);
        }                
    }
    
    void processFields(String[] fields) throws Exception {
        
        String name = fields[indices.get("name")].trim();
        String simName = simbadNames.get(name);
                
        double b = Double.parseDouble(fields[indices.get("bii")]);
        double l = Double.parseDouble(fields[indices.get("lii")]);
        double d = Double.parseDouble(fields[indices.get("distance")]);
        b = Math.toRadians(b);
        l = Math.toRadians(l);
        double x = Math.cos(l)*Math.cos(b)*d;
        double y = Math.sin(l)*Math.cos(b)*d;
        double z = Math.sin(b)*d;
        String xs = String.format("%.2f", x);
        String ys = String.format("%.2f", y);
        String zs = String.format("%.2f", z);
        
        // Handle missing diameters Milky Way -> 20 kpc, other missing -> 100 pc;
        double dia;
        try {
            dia = Double.parseDouble(fields[indices.get("linear_diameter")]);
        } catch (Exception e) {
            if (name == "Milky Way") {
                dia = 30;
            } else {
                dia = 0.10;
            }
        }
        out.write(sep + "  {\n");
        sep = ",\n";
        xsep = "    ";
        
        // Use SIMBAD name as primary since it's
        // usually 'nicer'.
        if (simName != null) {
            emit("name", simName, true);
            emit("orig_name", name, true);
        } else {
            emit("name", name, true);
        }
        
        
        emit("x", xs);
        emit("y", ys);
        emit("z", zs);
        emit("d", fields[indices.get("distance")]);
        emit("l", fields[indices.get("lii")]);
        emit("b", fields[indices.get("bii")]);
        emit("diameter", ""+dia);        
        emit("bmag", fields[indices.get("abs_bmag")]);
        emit("morph_type", fields);
        emit("dwarf_morph_type", fields, true);
        emit("axial_ratio", fields);
        emit("inclination", fields);
        if (angs.containsKey(name)) {
            emit("pa", ""+angs.get(name));
        }
        
        out.write("\n  }");
    }
    
    void emit(String fld, String[] fields) throws Exception {
        emit(fld, fields, false);
    }
    
    void emit(String fld, String[] fields, boolean isStr) throws Exception {
        emit(fld, fields[indices.get(fld)], isStr);
    }
    
    void emit(String fld, String val) throws Exception {
        emit(fld, val, false);
    }
    
    void emit(String fld, String val, boolean isStr) throws Exception {
        fld = fld.trim();
        val = val.trim();
        out.write(xsep+ "\""+fld+"\": ");
        String q = "";
        if (isStr) {
            q = "\"";
        }
        out.write(q+val+q);
        xsep = ",\n    ";        
    }
}
