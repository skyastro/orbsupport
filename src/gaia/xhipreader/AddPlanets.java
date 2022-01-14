package gaia.xhipreader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.*;


public class AddPlanets {
    
    public static void main(String[] args) throws Exception {
        String pFile     = "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\NearbyPlanetsNoDups.csv";
        String updFile   =  "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\id_nearby40pc.js";
        String updFile2  =  "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\idp_nearby40pc.js";
        
        BufferedReader br = new BufferedReader(new FileReader(pFile));
        String line;
        Map<Integer, Integer> planets = new HashMap<>();
        
        line = br.readLine();  // Skip headers        
        while ( (line = br.readLine() ) != null) {
            String[] flds = line.split(",");
            if (flds.length != 3) {
                System.out.println("Error line:"+line);
            }
            String ids = flds[0].trim().substring(4);
            ids = ids.replaceAll("[A-Z]", "");
            ids = ids.trim();
            System.out.println("flds:"+ids+','+flds[1]+","+flds[2]);
            int hip_id = Integer.parseInt(ids);
            int np     = Integer.parseInt(flds[2].trim());
            System.out.println("Looking at hip_id, np: "+hip_id+" "+np);
            planets.put(hip_id, np);
        }
        updateJSON(updFile, updFile2, planets);
    }
    static void updateJSON(String input, String output, Map<Integer, Integer> names) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(input));
        Writer         wr = new FileWriter(output);
        // We process the lines between the []'s and just copy the others.
        boolean copy = true;
        String line = null;
        while ( (line = br.readLine()) != null) {
            if (line.trim().length() == 0) {
                break;
            }
            
            if (!copy) {
                copy = line.indexOf(']') >= 0;
                if (copy) {
                    System.out.println("Got end of process, back to copy");
                }
            } 
            if (copy) {
                wr.write(line+"\n");
            }
            if (!copy)  {
                processLine(wr, line, names);
            }
                    
            if (copy) {
                copy = line.indexOf('[') < 0;
            }
        }
        wr.flush();
        wr.close();
    }
    
    static void processLine(Writer wr, String line, Map<Integer, Integer> planets) throws Exception {
       System.out.println(" Looking at line:"+line);
       // Line stars with ,{id:
       String idFrag = line.substring(6, line.indexOf(',', 4));
       int np = 0;
       
       int pos = line.indexOf("id:");
       int end = line.indexOf(",", pos);
       System.out.println("pos, end:"+pos);
       int id  = Integer.parseInt(line.substring(pos+3,end));
       if (planets.containsKey(id)) {
           np = planets.get(id);
       }
       wr.write(line.substring(0,3)+"np:"+np+", "+line.substring(3)+"\n");               
    }
}
