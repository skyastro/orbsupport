package gaia.generate;
/**
 *
 * @author Tom
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import gaia.xhipreader.CombineXHip;

public class GenerateGalaxy {
   
    String gnscFile    = "outputs/gnsc.srt.csv";
    String planetsFile = "outputs/exodata.srt.csv";
    String simbadFile  = "outputs/simbad.dr3.srt.csv";
    
    IndexedCSVReader gnsc, planets, simbad;
    BufferedWriter jw, ww;
    
    public static void main(String[] args) throws Exception {
        new GenerateGalaxy().generate();
    }
    
    int[] counts = new int[8];
    int warn = 0;
    
    /** Initialize the file which the Web application will
     *  read with information about nearby stars.
     *  
     * @throws Exception 
     */
    void initJS()  throws Exception {
        jw   = new BufferedWriter(new FileWriter("outputs/galaxy2.json"));
        ww   = new BufferedWriter(new FileWriter("outputs/galaxy.warn")); 
        jw.write("{\"StarList\": [\n");
    }
    void generate() throws Exception {
        gnsc    = new IndexedCSVReader(gnscFile);
        planets = new IndexedCSVReader(planetsFile);
        simbad  = new IndexedCSVReader(simbadFile);
        gnsc.next();
        planets.next();
        simbad.next();
        initJS();
        long gi = gnsc.getIndex();
        long pi = planets.getIndex();
        long si = simbad.getIndex();
        long sitCount = 0;
        System.out.println("Merge initiated");
        while (gi < Long.MAX_VALUE ||
               pi < Long.MAX_VALUE ||
               si < Long.MAX_VALUE) {
            
            situation(gi,pi,si);
            
            sitCount += 1;
            if (sitCount % 10000 == 0) {
                System.out.println("Situation count: "+sitCount);
            }
            
            gi = gnsc.getIndex();
            pi = planets.getIndex();
            si = simbad.getIndex();
        }
        logError("Exhaustion count:"+sitCount);
        System.out.println("Situation counts:");
        for (int i=0; i<counts.length; i += 1) {
             System.out.printf("  %6d", counts[i]);
        }
        System.out.println();
        closeJW();
        System.out.println("Total warnings:"+warn);
    }
    
    void addPlanet(StringBuilder line, String name, double period, double a, double r, double m, double T) {
        app(line, "", "planet", enq(name));
        app(line, ",", "period", ""+period);
        app(line, ",", "a", ""+a);
        app(line, ",", "r", ""+r);
        app(line, ",", "T", ""+T);
    }
    
    String addSun() throws Exception {
        
        StringBuilder line = new StringBuilder();
        line.append("{");
        
        app(line, "", "id", ""+0);
        app(line, ",", "name", enq("Sol"));
        app(line, ",", "spt",  enq("G2V"));
        app(line, ",", "x", "[0,0,0]");
        app(line, ",", "v", "[0,0,0]");
        StringBuilder mags = new StringBuilder();
        
        app(mags, "",  "B", "5.44");
        app(mags, ",", "V", "4.81");
        app(mags, ",", "R", "4.43");
        app(mags, ",", "I", "4.10");
        app(line, ",", "mags", "{"+mags.toString()+"}");
        
        app(line, ",", "np", "9");
        
        String[] names = {"Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"};
        double[] period ={87.97,     224.7,   365.26, 686.,    4332,      10761,    30685,    60191,     90797};
        double[] a      ={0.3871,    0.7233,  1.000,  1.5273,  5.2028,    9.5388,   19.1914,  164.79,    39.5294}; 
        double[] m      ={0.0553,    0.815,   1.,     0.107,   317.8,     95.2,     14.5,     17.1,      0.0022};
        double[] r      ={0.383,     0.949,   1.,     0.532,   11.21,     9.45,     4.01,     3.88,      0.18};
        double[] T      ={700,       740,     288,    244,     164,       134,      77,       71,        39};  
  
        String pstr = "[";
        String sep = "";
        for (int p=0; p<9; p += 1) {
            StringBuilder pl = new StringBuilder();
            addPlanet(pl, names[p], period[p], a[p], r[p], m[p], T[p]);
            pstr += sep + "{"+pl.toString()+"}";
            sep = ",";            
        }
        pstr += "]";
        app(line, ",", "planets", pstr);        
        line.append("}");
        return line.toString();
        
    }
    
    void closeJW() throws Exception {
        String sun = addSun();
        System.out.println("Sun is:\n"+sun);
        jw.write(",\n  "+sun);
        jw.write("\n]}\n");
        jw.flush();
        jw.close();
    }
    
    
    void situation(long gi, long pi, long si) throws Exception {
        if (si < gi && si < pi) {
            if (simbad.get("plx_value") != null  && simbad.getDouble("plx_value") >= 10) {
                // More than 100 pc away, so we exclude this.  We keep these in
                // in case we have a match with a (presumably measured as nearer) Gaia
                // object;            
                counts[0] += 1;
                processData(false, true, false);
            } else {
                simbad.next();
                counts[7] += 1;
            }
        } else if (pi < gi && pi < si) {
            counts[1] += 1;
            processData(false, false, true);
        } else if (gi < pi && gi < si) {
            counts[2] += 1;
            processData(true, false, false);
        } else if (gi == si && gi < pi) {        
            counts[3] += 1;
            processData(true, true, false);
        } else if (gi == pi && gi < si) {
            counts[4] += 1;
            processData(true, false, true);
        } else if (si == pi && si < gi) {
            counts[5] += 1;
            processData(false, true, true);
        } else {
            counts[6] += 1;
            processData(true, true, true);
        }
    }
    
    void logError(String msg) throws Exception {
        ww.write(msg+"\n"); 
    }
    
    // Calculate fractional offset
    double offset(double[] a, double[] b) {
        double[] avg = a.clone();
        double sum = 0;
        for (int i=0; i<a.length; i += 1) {
            double d = a[i]-b[i];
            sum    += d*d;
            avg[i] += b[i];
            avg[i] /= 2;
        }
        double nrm = gaia.geom.Geometry.norm(avg);
        return Math.sqrt(sum)/nrm;
    }
    
    void validate() throws Exception  {
        double[] xg = getGNSCPos();
        double[] xs = getSimbadPos();
        if (xg == null || xs == null) {
            warn += 1;
            logError("Unexpected missing data: "+gnsc.get("source_id"));
        } else {
            if (simbad.checkNaN() || gnsc.checkNaN()) {
                logError("**** Missing expected data:"+gnsc.get("source_)id"+" "+simbad.get("oid")));
            } else {
                double off = offset(xg,xs);
                if (off > 0.1) {
                    String id = gnsc.get("source_id");
                    logError(String.format("Warning: Large offset for: %s offset/dist= %8.3f",id, off));
                }
            }

            xg = getGNSCVel();
            xs = getSimbadVel();
            if (xg != null && xs != null) {
                double off = offset(xg,xs);
                if (off > 0.20) {
                    warn += 1;
                    String id = gnsc.get("source_id");
                    logError(String.format("Warning: Large velocity offset for: %s offset/dist= %8.3f",id, off));
                }
            }
        }
    
        
        // Clear the NaN flags.
        gnsc.checkNaN();
        simbad.checkNaN();
        // Don't read doubles from planets yet.        
    }
    String dbl(double x, int prec) {
        return String.format("%."+prec+"f", x);
    }
    String vec(double[] x, int prec) {
        return "["+dbl(x[0],prec)+","+dbl(x[1],prec)+","+dbl(x[2],prec)+"]";
    }

    static String key(String k) {        
        return enq(k)+":";
    }
    
    static String enq(String i) {
        i = i.replace("\"", "'");
        return "\""+i+"\"";
    }
    static void app(StringBuilder line, String sep, String key, String value) {
        line.append(sep + key(key)+value);
    }
    
    String linePrefix = "  ";
    void writeJSON(boolean g, boolean s, boolean p) throws Exception {
        StringBuilder line = new StringBuilder();
        line.append(linePrefix);
        linePrefix = ",\n  ";
        line.append("{");
        app(line, "", "id", enq(getID(g,s,p)));
        
        String name = getName(g,s,p);
        app(line, ",", "name", enq(name));
        
        String mag = getMags(g,s,p);
        if (mag.length() > 0) {
            app(line, ",", "mags", mag);
        }
        String spt = getSpType(g,s,p);        
        if (spt != null) {
            app(line, ",", "spt", enq(spt));
        }
        
        app(line, ",", "x", vec(getPosition(g,s,p),2));
        
        double[] v = getVelocity(g,s,p);
        if (v != null) {
            app(line, ",", "v",vec(v,2));
        }        
        
        String np = getNPlanet(g,s,p);
        if (np != null) {
            app(line, ",", "np", np);
            app(line, ",", "planets", getPlanets());
        }
        
        line.append("}");
        jw.write(line.toString());        
    };
    
    String getMags(boolean g, boolean s, boolean p) {
        StringBuilder mag = new StringBuilder();
        boolean magFound = false;
        if (s) {
            magFound |= magAdd(mag, simbad, "V", magFound);
            magFound |= magAdd(mag, simbad, "B", magFound);
            magFound |= magAdd(mag, simbad, "R", magFound);
            magFound |= magAdd(mag, simbad, "I", magFound);
        }
        if (g) {
            magFound |= magAdd(mag, gnsc, "phot_g_mean_mag", magFound);
            magFound |= magAdd(mag, gnsc, "phot_bp_mean_mag", magFound);
            magFound |= magAdd(mag, gnsc, "phot_rp_mean_mag", magFound);
        }
        return "{"+mag.toString()+"}";
    }
    
    boolean magAdd(StringBuilder mag, IndexedCSVReader r, String band, boolean magFound) {
        if (!Double.isNaN(r.getDouble(band))) {            
            String sep = magFound? "," : "";
            String uband = band;
            if (band.matches("phot_.*_mean_mag")) {
                // phot_g_mean_mag -> g
                uband = band.substring(5, band.length()-9);
            }
            app(mag, sep, uband, String.format("%.2f",r.getDouble(band)) );
            return true;            
        } else {
            r.checkNaN();
            return false;
        }
    }
    
    String getPlanets() throws Exception {
        String system = planets.get("star");
        String sep = "";
        String res = "[";
        do {
            StringBuilder pln = new StringBuilder();
            
            pln.append("{");
            app(pln, "", "planet", enq(planets.get("planet")));
            addField(pln, "period");
            addField(pln, "a");
            addField(pln, "r");
            addField(pln, "m");
            addField(pln, "T");            
            res +=      sep + pln.toString() + "}";
            sep = ",";  // preceded further planets with ,
            if (!planets.next()) {
                break; // Reached EOF.
            }
                    
        } while (planets.get("star").equals(system));
        
        res += "]";
        return res;        
    }
    
    void addField(StringBuilder pln, String name) {
        if (!Double.isNaN(planets.getDouble(name))) {
            app(pln, ",", name, ""+planets.getDouble(name));
        } else {
            planets.checkNaN();
        }
    }
    
    void processData(boolean g, boolean s, boolean p) throws Exception {
        if (g && s) {
            validate();
        }
        if (g || s) {
            writeJSON(g,s,p);
        }
        
        // Read next entry where used.
        if (g) {
            gnsc.next();
        }
        if (s) {
            simbad.next();
        }
        // We no longer usually get the next planet here, becuase
        // we did that earlier as we read multiple planets.
        if (p  && !s && !g) {
            planets.next();
        }
    }
    
    double[] getSimbadPos() {
        double ra  = simbad.getDouble("ra");
        double dec = simbad.getDouble("dec");
        double parallax = simbad.getDouble("plx_value");
        if (!simbad.checkNaN()) {
            double[] gc   = gaia.geom.Geometry.galactic(ra,dec);
            return gaia.geom.Geometry.position(gc[0], gc[1], parallax);
        } else {
            System.err.println("Unexpected missing simbad position: oid="+simbad.get("oid"));
            return null;            
        }
    }
    
    double[] getGNSCPos() {
        double[] gaiaG = {gnsc.getDouble("xcoord_50"),
                          gnsc.getDouble("ycoord_50"),
                          gnsc.getDouble("zcoord_50")};
        if (gnsc.checkNaN()) {
            return null;
        } else {
            return gaiaG;
        }
    }
    
    double[] getSimbadVel() {
        double ra  = simbad.getDouble("ra");
        double dec = simbad.getDouble("dec");
        double parallax = simbad.getDouble("plx_value");
        if (simbad.checkNaN()) {
            return null;
        }
        double[] simE = gaia.geom.Geometry.position(ra,dec,parallax);
        double pmra  = simbad.getDouble("pmra");
        double pmdec = simbad.getDouble("pmdec");
        double rv    = simbad.getDouble("rvz_radvel");
        // Should we check velocities.
        if (simbad.checkNaN()) {
            return null;
        }
        double[] simVEq = gaia.geom.Geometry.velocity(
          ra,dec,parallax,
          simE, // Use equatorial position. 
          pmra, pmdec, rv
        );
        // Now rotate to Galactic coordinates
        double[] simVG = gaia.geom.Geometry.galVector(simVEq);
        return simVG; 
    }
    
    double[] getGNSCVel() {
        double[] gaiaV = {gnsc.getDouble("uvel_50"),
                          gnsc.getDouble("vvel_50"),
                          gnsc.getDouble("wvel_50")};
        if (gnsc.checkNaN()) {
            return null;
        } else {
            return gaiaV;
        }
    }
    
    String getID(boolean g, boolean s, boolean p) {
        
        // Use the GAIA DR3 id, or an Hipparcos number
        // inserted, or if all else fails the SIMBAD oid (but return
        // the negative to eliminate change of redundancy
        if (g) {
            return gnsc.get("source_id");            
        } else {
            String id = simbad.get("dr3id");  // If not null presumably HIP id.
            if (id.length() == 0) {
                return "-"+simbad.get("oid");
            } else {
                return id;
            }
        }
    }
    
    String getName(boolean g, boolean s, boolean p) {
        
        String name;
        if (s) {
            name = simbad.get("main_id");
            name = CombineXHip.processName(name);             
        } else {
            name = "Gaia DR3 "+gnsc.get("source_id");
        }
        return name;
    }
    
    String getNPlanet(boolean g, boolean s, boolean p) {
        
        if (!p) {
            return null;
        } else {
            return planets.get("np");
        }
    }
    
    double[] getPosition(boolean g, boolean s, boolean p) {
        
        if (g) {
            return getGNSCPos();
        } else {
            return getSimbadPos();
        }
    }
    
    double[] getVelocity(boolean g, boolean s, boolean p) {        
        if (g) {
            double[] v = getGNSCVel();
            if (v != null) {
                return v;
            }
        }
        if (s) {
            double[] v = getSimbadVel();
            if (v != null) {
                return v;
            }
        }
        return null;
    }
    
    double[] getCoords(boolean g, boolean s, boolean p) {
        double[] coords = new double[2];
        if (g) {
           coords[0] = gnsc.getDouble("ra");
           coords[1] = gnsc.getDouble("dec");
           gnsc.checkNaN();
        } else {
           coords[0] = simbad.getDouble("ra");
           coords[1] = simbad.getDouble("dec");
           simbad.checkNaN();
        }
        return coords;
    }
    
    String getSpType(boolean g, boolean s, boolean p) {
        String spt = null;
        if (s) {
            spt = simbad.get("sp_type");
            if (spt != null && spt.length() > 0) {
                return spt;
            }
        }
        if (g) {
            double delta = gnsc.getDouble("phot_bp_mean_mag") -
                           gnsc.getDouble("phot_rp_mean_mag");
            if (gnsc.checkNaN()) {
                return null;
            }
            if (delta < 0.07) {
                return "Bex";
            } else if (delta < 0.44) {
                return "Aex";
            } else if (delta < 0.74) {
                return "Fex";
            } else if (delta < 1.05) {
                return "Gex";
            } else if (delta < 2.0) {
                return "Kex";
            } else {
                return "Mex";
            }
        }
        return null;
    }
    
    double getBMag(boolean g, boolean s, boolean p) {
        if (g) {
            double mg = gnsc.getDouble("phot_bp_mean_mag");
            if (gnsc.checkNaN()) {
                return 99;               
            } else {
                return mg;
            }
        } else {
            return 99;
        }
    }
}
