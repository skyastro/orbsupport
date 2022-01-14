
package gaia.nea;

import gaia.crossid.DeltaChecker;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * This program creates the file exoplanet.csv.  The output needs to be sorted into
 * dr3 order by something like:
   cat exodata.csv|(read -r; printf "%s\n" "$REPLY"; sort -k 1 -n --field-separator=,)  > exodata.srt.csv
 *
 * @author Tom
 */
public class ExoQuery {
    public static void main(String[] args) throws Exception {
        String baseURL="https://exoplanetarchive.ipac.caltech.edu/TAP/sync?format=csv&query=";
        String query =
"""
select 
  gaia_id, hip_name,pl_name, hostname, sy_snum, sy_pnum, sy_dist,
  discoverymethod, pl_orbper, pl_orbsmax, pl_rade, pl_bmasse, pl_eqt
from ps 
where sy_dist < 200
order by pl_name, hostname
""";
        query = URLEncoder.encode(query, "UTF-8");
        System.out.println("URL:\n"+baseURL+query);
        URL nea = new URL(baseURL+query);
        BufferedReader rdr = 
           new BufferedReader(
              new InputStreamReader(nea.openStream()));
        new ExoQuery(rdr).run();
    }
    BufferedReader rdr;
    FileWriter     wtr;
    
    int chngCnt = 0;
    int missCnt = 0;
    
    Planet currPlanet;
    DeltaChecker   dc = new DeltaChecker();
    String output = "outputs/exodata.csv";

    int same = 0;
    int diff = 0;
    int miss = 0;
    
    public ExoQuery(BufferedReader rdr) throws Exception {
        this.rdr = rdr;
        wtr = new FileWriter(output);        
    }
    class Planet {
        long   dr2; 
        long   dr3;
        int    hipid;
        int    nplanet;
        int    nstar;
        double distance;
        int    ndist;
        String system;
        char   planet;
        String type;
        double period;
        int    nperiod;
        double a;
        int    na;
        double radius;
        int    nradius;
        double mass;
        int    nmass;
        double T;
        int    nT;
    }
    
    void writeHeader() throws Exception {
        wtr.write(
          "dr3,dr2,hip,star,planet,ns,np,distance,type,period,a,r,m,T\n"
        );
    }
    void run() throws Exception {
        int count = 0;
        String line;
        
        String last = "";
        
        line = rdr.readLine();
        writeHeader();
                
        while ( (line=rdr.readLine()) != null) {
            
            String[] flds = line.split(",");
            String plan = flds[2];
            if (!plan.equals(last)) {
                if (last.length() > 0) {
                    emitPlanet();
                }
                currPlanet = new Planet();
                last = plan;
                initPlanet(flds);
            }
            
            addToPlanet(flds);
        }
        rdr.close();
        emitPlanet();
        wtr.close();
        System.out.println("Number of same Gaia ids: "+same);
        System.out.println("Number of diff Gaia ids: "+diff);
        System.out.println("Number of miss Gaia ids: "+miss);
    }
    void initPlanet(String[] flds) throws Exception {
        String dr2id = flds[0];
        if (flds[1].length() > 0) {            
            String hip = flds[1].substring(5, flds[1].length()-1);
            hip = hip.replaceAll("\\a", "");
            hip = hip.trim();
            if (hip.endsWith(" A") || hip.endsWith(" B")) {
                hip = hip.substring(0, hip.length()-2);
            }
            if (hip.endsWith(" AB")) {
                hip = hip.substring(0,hip.length()-3);
            }
            
            currPlanet.hipid = Integer.parseInt(hip); 
        }
        if (dr2id.length() > 0) {
            dr2id = dr2id.substring(10, dr2id.length()-1);
            currPlanet.dr2 = Long.parseLong(dr2id);
            currPlanet.dr3 = dc.dr3id(currPlanet.dr2);
            if(currPlanet.dr2 == 5853498713160606720L) {
                System.out.println("**"+currPlanet.dr2);
                System.out.println("**"+currPlanet.dr3);
            }
            if (currPlanet.dr2 == currPlanet.dr3) {
                same += 1;
            } else {
                diff += 1;
            }
            
            if (currPlanet.dr3 != currPlanet.dr2) {                
                chngCnt += 1;
            } else if (currPlanet.dr3 < 0) {
                missCnt += 1;
            }
        } else {
            miss += 1;
            System.out.println("Miss: HIP ID: "+flds[1]);
            System.out.println("Miss: System: "+flds[3]);
            currPlanet.dr2 = 0;
            long oid = getSimbadOid(currPlanet.hipid);
            currPlanet.dr3 = -oid;
        }
        currPlanet.hipid = 0;
        currPlanet.planet = flds[2].charAt(flds[2].length()-2);
        currPlanet.system = flds[3].substring(1,flds[3].length()-1);
        currPlanet.nstar  = Integer.parseInt(flds[4]);
        currPlanet.nplanet = Integer.parseInt(flds[5]);
        currPlanet.type = flds[7].substring(1, flds[7].length()-1);
        String dist = flds[6];
        if (dist.length() > 0) {
            currPlanet.distance = Double.parseDouble(dist);
        }
    }
    void addToPlanet(String[] flds) {
        if (flds.length > 8 && flds[8].length() > 0) {
            double period = Double.parseDouble(flds[8]);
            currPlanet.period += period;
            currPlanet.nperiod += 1;
        }
        if (flds.length > 9 && flds[9].length() > 0) {
            double a = Double.parseDouble(flds[9]);
            currPlanet.a += a;
            currPlanet.na += 1;
        }
        if (flds.length > 10 && flds[10].length() > 0) {
            double radius = Double.parseDouble(flds[10]);
            currPlanet.radius += radius;
            currPlanet.nradius += 1;
        }
        if (flds.length > 11 && flds[11].length() > 0) {
            double mass = Double.parseDouble(flds[11]);
            currPlanet.mass += mass;
            currPlanet.nmass += 1;
        }
        if (flds.length > 12 && flds[12].length() > 0) {
            double T = Double.parseDouble(flds[12]);
            currPlanet.T += T;
            currPlanet.nT += 1;
        }
    }
    
    void emitPlanet() throws Exception {
        // Check that we have a distance that might be
        // used in the 100 PC survey.
        if (currPlanet.distance <= 0  || currPlanet.distance > 150) {
            return;  
        }
        if (currPlanet.nperiod > 0) {
            currPlanet.period /= currPlanet.nperiod;
        }
        if (currPlanet.na > 0) {
            currPlanet.a /= currPlanet.na;
        }
        if (currPlanet.nradius > 0) {
            currPlanet.radius /= currPlanet.nradius;
        }
        if (currPlanet.nmass > 0) {
            currPlanet.mass /= currPlanet.nmass;
        }
        if (currPlanet.nT > 0) {
            currPlanet.T /= currPlanet.nT;
        }
        Planet c = currPlanet;
        wtr.write(c.dr3+","+c.dr2+","+c.hipid+","+c.system+","+c.planet+",");
        wtr.write(c.nstar+","+c.nplanet+",");
        wtr.write(c.distance+",");
        wtr.write(c.type+",");
        
        if (c.nperiod > 0) {
            wtr.write(String.format("%.3f",c.period));
        }
        wtr.write(",");
        if (c.na > 0) {
            wtr.write(String.format("%.3f",c.a));
        }
        wtr.write(",");
        
        if (c.nradius > 0) {
            wtr.write(String.format("%.1f",c.radius));
        }
        wtr.write(",");
        
        if (c.nmass > 0) {
            wtr.write(String.format("%.1f",c.mass));
        }
        wtr.write(",");
        
        if (c.nT > 0) {
            wtr.write(String.format("%.1f",c.T));
        }
        
        wtr.write("\n");
    }
    
    /* We should probably be a bit chary of using the SIMBAD OIDs here since they
     * are not guaranteed to stay constant.  So we should probably update
     * the nearby Simbad stars and nearby planets files at more or less
     * the same time. 
     */
    long getSimbadOid(int hipid) throws Exception {
        if (hipid == 0) {
            return 0;
        }
        String idstr = "HIP" + String.format("%7d", hipid);
        String query = "select oidref from ids where ids like '%|"+idstr+"|%'";
        String base = "http://simbad.u-strasbg.fr/simbad/sim-tap/sync?request=doQuery&lang=adql&format=text&query=";
        String enc = URLEncoder.encode(query, "UTF-8");
        String fullURL = base+enc;
        URL simURL = new URL(fullURL);
        System.out.println("Initiating SIMBAD query\n"+fullURL);
        BufferedReader rdr = new BufferedReader(new InputStreamReader(simURL.openStream()));
        String line, lastLine;
        long ret  = 0;


        int count = 0;
        lastLine = null;
        while ( (line = rdr.readLine()) != null) {
            lastLine = line;
            count += 1;
        }
        
        if (count == 3) {
            ret = Long.parseLong(lastLine);
            
        } else {
            System.err.println("Unexpected return in SIMBAD TAP query: Count:"+count+" last line:\n"+lastLine);
        }
        
        rdr.close();
        System.out.println("Finished SIMBAD query");
        return ret;        
    }    
}
