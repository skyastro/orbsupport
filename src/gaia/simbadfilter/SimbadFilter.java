
package gaia.simbadfilter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * We extract all nearby star information from SIMBAD using the query
 * 
select 
  a.oid, a.main_id,b.id,
  a.ra,a.dec,a.plx_value,a.plx_err,
  a.pmra,a.pmdec,a.rvz_radvel,
  a.sp_type

from basic a join ident b on a.oid=b.oidref
where a.plx_value > 9 and a.plx_value > 3*a.plx_err

 * We get magnitude information using the query
select
   oidref, filter, flux from flux
   where 
     filter in ('V', 'B', 'R', 'I') and
     oidref in (select oid from basic where a.plx_value > 9 and a.plx_value > 3*a.plx_err)

 * To do our matching with the EDR3 Nearby Stars catalog
 * we want to slim this down to just a single row for
 * each input object, either using the DR2 ID, or a null
 * value indicating the the DR2 is not available.

 * @author Tom
 */
public class SimbadFilter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new SimbadFilter().filter();        
    }
    static StringBuilder basic = new StringBuilder();
    String basicFile = "src/inputs/simBasic.csv";
    String fluxFile  = "src/inputs/simFlux.csv";
    /* Created by:
curl --url http://simbad.u-strasbg.fr:80/simbad/sim-tap/sync --location  --compressed --form REQUEST=doQuery --form LANG=ADQL-2.0 --form format=csv --form maxrec=2000000 --form query='select  a.oid, a.main_id,b.id,a.ra,a.dec,a.plx_value,a.plx_err,a.pmra,a.pmdec,a.rvz_radvel,a.sp_type from basic a join ident b on a.oid=b.oidref where a.plx_value > 9 and a.plx_value > 3*a.plx_err'
   &
curl --url http://simbad.u-strasbg.fr:80/simbad/sim-tap/sync --location  --compressed --form REQUEST=doQuery --form LANG=ADQL-2.0 --form QUERY="select oidref, filter, flux from flux where filter in ('V', 'B', 'R', 'I') and oidref in (select oid from basic where plx_value > 9 and plx_value > 3*plx_err)" --form format=csv --form maxrec=2000000 
     */
    BufferedReader simbad;
    Map<String,Integer> colNums = new HashMap<>();
    
    // Map of ids -> filter -> flux
    Map<String,Map<String,Double>> fluxes = new HashMap<>();
    
    BufferedWriter out          = new BufferedWriter(
      new FileWriter("outputs/simbad.filtered.csv"));
    int   idCol;
    int   nCol;
    int   raCol;
    int   decCol; 
    int   sptCol;
    int   simidCol;

    SimbadFilter() throws Exception {
        getFluxes();
        simbad = new BufferedReader(
            new FileReader(basicFile)
        );
        readHeader();
        
        idCol    = colNums.get("id");
        raCol    = colNums.get("ra");
        decCol   = colNums.get("dec");
        sptCol   = colNums.get("sp_type");
        simidCol = colNums.get("main_id");            
    }
    
    static String dequote(String s) {
        if (s != null && s.length() > 2  && s.startsWith("\"")  && s.endsWith("\"")) {
            return s.substring(1,s.length()-1);
        } else {
            return s;
        }
    }
    
    void getFluxes() throws Exception {
        BufferedReader rdr = new BufferedReader(new FileReader(fluxFile));
        rdr.readLine();
        String line;
        int count = 0;
        while ( (line = rdr.readLine()) != null) {
            String[] flds = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            flds[1] = dequote(flds[1]);
            Map<String,Double> obj = fluxes.get(flds[0]);
            if (obj == null) {
                obj = new HashMap<String,Double>();
                fluxes.put(flds[0],obj);
            }
            count += 1;
            obj.put(flds[1], Double.parseDouble(flds[2]));            
        }
        System.err.println("Number of fluxes read:"+count);
        rdr.close();        
    }
    
    void readHeader() throws Exception {
        String line = simbad.readLine();
        String[] flds = line.split(",");
        for (int i=0; i<flds.length; i += 1) {
            colNums.put(flds[i], i);
        }
        nCol = flds.length;
        // Add in fluxes
        out.write(line+",V,B,R,I\n");
    } 
    
    boolean logging = false;
    public void filter() throws Exception {
        
        String  lastLine = null;
        boolean isDR2   = false;
        String  lastID   = null;
        String  line;
        
        
        while ( (line=simbad.readLine()) != null) {
            
            String[] flds = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (int i=0; i<flds.length; i += 1) {
                flds[i] = dequote(flds[i]);
            }
            String id = flds[0];
            
            
            if (!id.equals(lastID)) {
                
                emit(lastLine, isDR2);
                lastLine = line;
                lastID   = flds[0];
                isDR2 = checkDR2(flds);
                
            } else {
                
                // Only update the last line
                // if we haven't found a DR2 line.
                if (!isDR2) {
                    lastLine = line;
                    isDR2 = checkDR2(flds);
                }
            }
        }
        emit(lastLine, isDR2);
        System.err.println("Number of DR2 entries:     "+dr2);
        System.err.println("Number of non-DR2 entries: "+notDR2);
    }
    boolean checkDR2(String[] flds) {
        return flds[idCol].startsWith("Gaia DR2 ");
    }
    
    int dr2    = 0;
    int notDR2 = 0;
    
    void emit(String line, boolean isDR2) throws Exception  {
        
        if (line == null) {
            return;
        }
        String[] flds = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i=0; i<flds.length; i += 1) {
            flds[i] = dequote(flds[i]);
        }
        String oid = flds[0];
        
        // Seem to be lots of extra spaces here.
        flds[1]  = flds[1].replaceAll("\\s+", " ");
        
        String div = "";
        for (int i=0; i<flds.length; i += 1) {
            if (i == idCol) {
                if (isDR2) {
                    dr2 += 1;
                    out.write(div+flds[i].substring(9));
                } else {
                    notDR2 += 1;
                    out.write(div);
                }
            } else if (i == raCol || i == decCol) {
                try {
                    double d = Double.parseDouble(flds[i]);
                    String f = String.format("%.5f", d);
                    out.write(div+f);
                } catch (Exception e) {
                    System.err.println("Exception on line: "+line+" "+i);
                    for (int j=0; j<flds.length; j += 1)  {
                        System.err.println("Token is:"+j+" "+flds[j]);
                    }
                    throw e;
                }
            } else if (i == simidCol  || i == sptCol) {
                // Look for embedded commas.
                String simid = flds[i];
                if (simid.indexOf(",") >= 0) {
                    // Double up double quotes, and add double quotes around.
                    simid = simid.replace("\"", "\"\"");
                    simid = '"' + simid + '"';
                    System.out.println("Comma found in:"+line);
                }
                out.write(div+simid);
            } else {
                
                out.write(div+flds[i]);
            }
            div = ",";
        }
       
        Map<String,Double> obj = fluxes.get(oid);
        if (obj == null) {
            out.write(",,,,");
        } else {
            out.write(",");
            if (obj.get("V") != null) {
                out.write(""+obj.get("V"));
            }
            out.write(",");
            if (obj.get("B") != null) {
                out.write(""+obj.get("B"));
            }
            out.write(",");
            if (obj.get("R") != null) {
                out.write(""+obj.get("R"));
            }
            out.write(",");
            if (obj.get("I") != null) {
                out.write(""+obj.get("I"));
            }
        }
        out.write("\n");
        
    }    
}
