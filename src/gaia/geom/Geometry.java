package gaia.geom;
import static java.lang.Math.*;

/**
 *
 * @author Tom
 */
public class Geometry {
    
    static final double PARSEC = 30.856775e12; // km
    static final double YEAR   = 365.25*86400; // s
    // RA and Dec of Galactic pole
    static final double RA_GP = Math.toRadians(192.85948);
    static final double DEC_GP   = Math.toRadians(27.12825);
    // Galactic latitude of Equatorial pole
    static final double L_EQP    = Math.toRadians(122.93192);
    static final double[][] EQTOGAL = {
        {-0.054876,-0.873437,-0.483835},
        {0.494109, -0.444830, 0.746982},
        {-0.867666,-0.198076, 0.455984}
    };
    
/*    static final double[][] GALTOEQ = {
        {-0.054876,
         0.494109, 
         -0.867666},
        {
            -0.873437,
            -0.444830, 
            -0.198076},
        {
                -0.483835,
                0.746982,
                0.455984}
    };
    */
    public static double[] position(double ra, double dec, double parallax) {
        ra  = toRadians(ra);
        dec = toRadians(dec);
        double r = 1000/parallax;
        return new double[] {
            r*cos(ra)*cos(dec),
            r*sin(ra)*cos(dec),
            r*        sin(dec)
        };
    }
    
    public static double[] velocity(double ra, double dec, double parallax, 
                                    double[] pos,
                                    double pmra, double pmdec, double radVel) {
        double r = 1000/parallax; // In parsecs.
        
        // Radial velocity... We have position in parsec units.
        double[] v = {radVel*pos[0]/r,
                      radVel*pos[1]/r,
                      radVel*pos[2]/r};
        double[] tst = galVector(pos);
        tst = galVector(v);
        // Convert r to km so that units of velocity ar
        // km/s
        
        r  *= PARSEC;
        ra  = toRadians(ra);
        dec = toRadians(dec);        
        
        double vd = Math.toRadians(pmdec/1000/3600);
        
        vd = vd*r / YEAR;
        v[0] -= vd*cos(ra)*sin(dec);
        v[1] -= vd*sin(ra)*sin(dec);
        v[2] += vd*cos(dec);
        
        double vra = Math.toRadians(pmra/1000/3600);
        vra  = vra*r / YEAR;
        double dvra = vra/cos(dec);  // Now this is the angular rate of cnage of the RA.
        
        
        v[0] -= dvra*sin(ra)*cos(dec);
        v[1] += dvra*cos(ra)*cos(dec);
        return v;
    }
    
    public static double[] galactic(double ra, double dec) {
        ra  = toRadians(ra);
        dec = toRadians(dec);
        double sinb =
           sin(dec)*sin(DEC_GP)+cos(dec)*cos(DEC_GP)*cos(ra-RA_GP);
        if (sinb > 1) {
            sinb = 1;
        } else if (sinb < -1) {
            sinb = -1;
        }
        double cosb  = Math.sqrt(1-sinb*sinb);
        double cosld = (sin(dec)*cos(DEC_GP)-cos(dec)*sin(DEC_GP)*cos(ra-RA_GP))/cosb;
        double sinld = (cos(dec)*sin(ra-RA_GP))/cosb;
        double ld = Math.atan2(sinld, cosld);
        double l = L_EQP - ld;
        if (l < 0)  {
            l += 2*PI;
        } else if (l > 2*PI) {
            l -= 2*PI;
        }
        double b = asin(sinb);
        b = toDegrees(b);
        l = toDegrees(l);
        return new double[]{l, b};
    }
    public static double dot(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            throw new IllegalArgumentException("dot product of null or unequal length vectors");
        }
        double sum = 0;
        for (int i=0; i<a.length; i += 1) {
            sum += a[i]*b[i];
        }
        return sum;
    }
    public static double[] galVector(double[] eqVector) {
        return new double[] {
            dot(EQTOGAL[0], eqVector),
            dot(EQTOGAL[1], eqVector),
            dot(EQTOGAL[2], eqVector)
        };        
    }
    
    public static double norm(double[] x) {
        return sqrt(dot(x,x));
    }
}