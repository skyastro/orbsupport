package gaia.generate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tom
 */
public class SpecVals {
    class Type {
        String type;
        int n;
        double cumMag;
        double cumSqMag;
        double cumDel;
        double cumSqDel;
    }
    
    Map<String, Type> classMap = new HashMap<>();
    public static void main(String[] args) throws Exception {
        new SpecVals().compute();
    }
    
    void compute() throws Exception {
        BufferedReader br = new BufferedReader(
          new FileReader("f:\\gaia\\comb\\SpTypes.sort"));
        
        String line;
        while ( (line=br.readLine()) != null) {
            String[] flds = line.split("\s+");
            if (flds.length == 4 && flds[1].equals(":")  || 
                flds.length == 3 && flds[0].endsWith(":")) {
                String spec = flds[0];
                
                double mag  = Double.parseDouble(flds[flds.length-2]);
                double delt = Double.parseDouble(flds[flds.length-1]);
                String type = spec.substring(0,1);
                Type ty;
                if (!classMap.containsKey(type)) {
                    ty = new Type();
                    ty.type = type;
                    classMap.put(type, ty);
                } else {
                    ty = classMap.get(type);
                }
                ty.n += 1;
                ty.cumMag += mag;
                ty.cumSqMag += mag*mag;
                ty.cumDel += delt;
                ty.cumSqDel += delt*delt;
            }
        }
        
        System.out.printf("%s  %8s %8s %8s %8s %8s\n", "S", "n", "Mag", "DMag", "Delta", "DDelta");
        for (String type: classMap.keySet()) {
            Type ty = classMap.get(type);
            double avgMag = ty.cumMag/ty.n;
            double varMag = Math.sqrt(ty.cumSqMag/ty.n - avgMag*avgMag);
            double avgDel = ty.cumDel/ty.n;
            double varDel = Math.sqrt(ty.cumSqDel/ty.n - avgDel*avgDel);
            System.out.printf("%s: %7d %8.3f %8.3f %8.3f %8.3f\n", ty.type, ty.n, avgMag, varMag, avgDel, varDel);
        }
    }    
}
