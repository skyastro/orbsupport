package gaia.simbadfilter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tom
 */
public class NameFixer {
    Map<String,String> greek = new HashMap<String,String>();
    {
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
    
    Map<String, String> constell = new HashMap<>();
    String[] constells = {
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
    {
        for (int i=0; i<constells.length; i += 2) {
            constell.put(constells[i], constells[i+1]);
        }
    }
    
    Map<String, Integer> useGreek    = new HashMap<>();
    Map<String, Integer> useConstell = new HashMap<>();
    Map<Integer, String> names = new HashMap<>();


    public String processName(String name) {
        
        String upd = name;
        if (upd.startsWith("* ")) {
            upd = upd.substring(2);
        } else if (upd.startsWith("V* ")) {
            upd = upd.substring(3);
        } else if (upd.startsWith("NAME ")) {
            upd = upd.substring(5);
        }
 
        if (upd.length() >  2) {
            String letterCheck = upd.substring(0,3);
            if (greek.containsKey(letterCheck)) {
                if (useGreek.get(letterCheck) == null) {
                    useGreek.put(letterCheck,1);
                } else {
                    useGreek.put(letterCheck, useGreek.get(letterCheck)+1);
                }
                upd = greek.get(letterCheck)+" "+checkForNumber(upd.substring(3));
            } else {
                upd = checkForConstell(upd);
            }
        }
        upd = upd.replaceAll("\s+", " ");
        return upd;
    }
    
    String checkForNumber(String input) {
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
        
    String checkForConstell(String input) {
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
