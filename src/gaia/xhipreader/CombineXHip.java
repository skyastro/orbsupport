package gaia.xhipreader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.*;


public class CombineXHip {
    // Greek alphabet is represented by three character fields,
    // but not always what you expect...
    static Map<String,String> greek = new HashMap<String,String>();
    static {
        greek.put("alf", "Alpha");
        greek.put("bet", "Beta");
        greek.put("gam", "Gamma");
        greek.put("del", "Delta");
        greek.put("eps", "Epsilon");
        greek.put("zet", "Zeta");
        greek.put("eta", "Eta");
        greek.put("tet", "Theta");
        greek.put("iot", "Iota");
        greek.put("kap", "Kappa");
        greek.put("lam", "Lambda");
        greek.put("mu.", "Mu");
        greek.put("nu.", "Nu");
        greek.put("ksi", "Xi");
        greek.put("omi", "Omicron");
        greek.put("pi.", "Pi");
        greek.put("rho", "Rho");
        greek.put("sig", "Sigma");
        greek.put("tau", "Tau");
        greek.put("ups", "Upsilon");
        greek.put("phi", "Phi");
        greek.put("chi", "Chi");
        greek.put("psi", "Psi");
        greek.put("ome", "Omega");
    }
    
    static Map<String, String> constell = new HashMap<>();
    static String[] constells = {
    "And", "Andromedae",
    "Ant", "Antliae",
    "Aps", "Apodis",
    "Aqr", "Aquarii",
    "Aql", "Aquilae",
    "Ara", "Arae",
    "Ari", "Aries",
    "Aur", "Aurigae",
    "Boo", "Bootis",
    "Cae", "Caeli",
    "Cam", "Camelopardalis",
    "Cnc", "Cancri",
    "CVn", "Canam Venaticorum",
    "CMa", "Canis Majoris",
    "CMi", "Canis Minoris",
    "Cap", "Capricorni",
    "Car", "Carinae",
    "Cas", "Cassiopeiae",
    "Cen", "Centauri",
    "Cep", "Cephei",
    "Cet", "Ceti",
    "Cha", "Chamaeleontis",
    "Cir", "Circini",
    "Col", "Columba",
    "Com", "Coma Berenices",
    "CrA", "Coronae Australis",
    "CrB", "Coronae Borealis",
    "Crv", "Corvi",
    "Crt", "Crateris",
    "Cru", "Crucis",
    "Cyg", "Cygni",
    "Del", "Delphini",
    "Dor", "Doradus",
    "Dra", "Draconis",
    "Equ", "Equulei",
    "Eri", "Eridani",
    "For", "Fornacis",
    "Gem", "Geminorum",
    "Gru", "Gruis",
    "Her", "Herculis",
    "Hor", "Horologii",
    "Hya", "Hydrae",
    "Hyi", "Hydri",
    "Ind", "Indi",
    "Lac", "Lacertae",
    "Leo", "Leonis",
    "LMi", "Leonis Minoris",
    "Lep", "Leporis",
    "Lib", "Librae",
    "Lup", "Lupus",
    "Lyn", "Lyncis",
    "Lyr", "Lyrae",
    "Men", "Mensae",
    "Mic", "Microscopii",
    "Mon", "Monocerotis",
    "Mus", "Muscae",
    "Nor", "Normae",
    "Oct", "Octantis",
    "Oph", "Ophiuchi",
    "Ori", "Orionis",
    "Pav", "Pavonis",
    "Peg", "Pegasi",
    "Per", "Persei",
    "Phe", "Phoenicis",
    "Pic", "Pictoris",
    "Psc", "Piscium",
    "PsA", "Piscis Austrini",
    "Pup", "Puppis",
    "Pyx", "Pyxidis",
    "Ret", "Reticuli",
    "Sge", "Sagittae",
    "Sgr", "Sagittarii",
    "Sco", "Scorpii",
    "Scl", "Sculptoris",
    "Ser", "Serpentis",
    "Sex", "Sextantis",
    "Tau", "Tauri",
    "Tel", "Telescopii",
    "Tri", "Trianguli",
    "TrA", "Trianguli Australis",
    "Tuc", "Tucanae",
    "UMa", "Ursae Majoris",
    "UMi", "Ursae Minoris",
    "Vel", "Velorum",
    "Vir", "Virginis",
    "Vol", "Volantis",
    "Vul", "Vulpeculae"
    };
    static {
        for (int i=0; i<constells.length; i += 2) {
            constell.put(constells[i], constells[i+1]);
        }
    }
    
    static Map<String, Integer> useGreek    = new HashMap<>();
    static Map<String, Integer> useConstell = new HashMap<>();
    static Map<Integer, String> names = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        String idFile   = "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\hipids.txt";
        String starFile   = "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\nearby40pc.js";
        String updFile   =  "D:\\Programs\\react\\XHipReader\\src\\xhipreader\\id_nearby40pc.js";
        if (args.length > 0) {
            idFile = args[0];            
        }
        if (args.length > 1) {
            starFile = args[1];
        }
        BufferedReader br = new BufferedReader(new FileReader(idFile));
        String line;
        while ( (line = br.readLine() ) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            int hip_id  = Integer.parseInt(line.substring(8,15).trim());            
            String name = line.substring(17,51).trim();
            
            String updName = processName(name);
            names.put(hip_id, updName);
        }
              
        
        for (String gl:   greek.keySet()) {
            System.out.println(gl+" "+useGreek.get(gl));            
        }
        for (String cnstl: constell.keySet()) {
            System.out.println(cnstl+" "+useConstell.get(cnstl));
        }
        br.close();
        updateJSON(starFile, updFile, names);
    }
    
    public static String processName(String name) {
        if (name.startsWith("* ")  || name.startsWith("V* ")) {
            name = name.substring(2).trim();
        }

        if (name.startsWith("NAME ")) {
            name=name.substring(5);
        }

        String updName = null;
        if (name.length() < 3) {
            updName = name;
        } else {
            String letterCheck = name.substring(0,3);
            if (greek.containsKey(letterCheck)) {
                if (useGreek.get(letterCheck) == null) {
                    useGreek.put(letterCheck,1);
                } else {
                    useGreek.put(letterCheck, useGreek.get(letterCheck)+1);
                }
                updName = greek.get(letterCheck)+" "+checkForNumber(name.substring(3));
            } else {
                updName = checkForConstell(name);
            }
        }
        updName = updName.replaceAll("\s+", " ");
        return updName;
    }
    
    static void updateJSON(String input, String output, Map<Integer, String> names) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(input));
        Writer         wr = new FileWriter(output);
        // We process the lines between the []'s and just copy the others.
        boolean copy = true;
        String line = null;
        while ( (line = br.readLine()) != null) {
            
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
    
    static void processLine(Writer wr, String line, Map<Integer, String> names) throws Exception {
       // Line stars with ,{id:
       String idFrag = line.substring(6, line.indexOf(',', 4));
       int id = Integer.parseInt(idFrag);
       String name = names.get(id);
       wr.write(line.substring(0,3)+"name:\""+name+"\", "+line.substring(3)+"\n");               
    }
    
    static String checkForNumber(String input) {
        int prefixLen = 0;
        while (prefixLen < input.length() &&
               input.charAt(prefixLen) >= '0'  && input.charAt(prefixLen) <= '9') {
            prefixLen += 1;
        }
        if (prefixLen == 0) {
            return checkForConstell(input);
        } else {
            return Integer.parseInt(input.substring(0,prefixLen))+" "+checkForConstell(input.substring(prefixLen));            
        }
    }
    static String checkForConstell(String input) {
        for(String cnstl: constell.keySet()) {
            int p = input.indexOf(cnstl);
            
            char c = 0;
            if (p+3 < input.length()) {
                c = input.charAt(p+3);
            }
            if (p >= 0 ) {
                // Check that either the constellation is at the end of the string,
                // or that that next character in the string is not alphabetic.
                // In these cases we match the constellation.
                
                if (p+3 == input.length() 
                         ||
                    !Character.isAlphabetic(c) ) {
                    input = input.replace(cnstl, constell.get(cnstl));
                    if (useConstell.get(cnstl) == null) {
                        useConstell.put(cnstl, 1);
                    } else {
                        useConstell.put(cnstl, useConstell.get(cnstl)+1);
                    }
                    
                    return input;
                }
            }
        }
        return input;
    }
}
