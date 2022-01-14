package gaia.translate;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Tom
 */
public class IDMaps {
    
    String base = "/mnt/f/gaia/gaia_source/cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv";

    public static void main(String[] args) throws Exception {
        new IDMaps().generate();
        
    }
    
    void generate() throws Exception {
        Set<Long>      missing = generateMissing();
        Map<Long,Long> deltas  = generateChanged();
        writeObjects(missing, deltas);
    }
    
    void writeObjects(Set<Long> missing, Map<Long,Long> deltas) throws Exception {
        ObjectOutputStream os = new ObjectOutputStream(
            new FileOutputStream(base+"/data.obj"));
        os.writeObject(missing);
        os.writeObject(deltas);
        os.close();
    
    }
    
    Set<Long> generateMissing() throws Exception  {
        String file = base+"/crossmiss.dat";
        System.out.println("Reading crossmiss file: "+file);
        TreeSet<Long> ts = new TreeSet<>(); 
        BufferedReader br = new BufferedReader(
           new FileReader(file)
        );
        String line;
        
        while ((line=br.readLine()) != null) {
            ts.add(Long.parseLong(line));
        }
        br.close();
        System.out.println("Read crossmiss: count="+ts.size());
        return ts;
    }
    
    Map<Long,Long> generateChanged() throws Exception {
        String file = base+"/deltas.dat";
        System.out.println("Reading deltas file: "+file);
        Map<Long,Long> chngs = new TreeMap<>(); 
        BufferedReader br = new BufferedReader(
           new FileReader(file)
        );
        String line;
        
        long cnt = 0;
        while ((line=br.readLine()) != null) {
            String[] flds = line.split(" ");
            chngs.put(Long.parseLong(flds[0]), Long.parseLong(flds[1]));
            cnt += 1;
            if (cnt % 100000 == 0) {
                System.out.println("Deltas count: "+cnt);
            }
        }
        br.close();
        
        System.out.println("Deltas: count ="+chngs.size());
        return chngs;         
    }
}
