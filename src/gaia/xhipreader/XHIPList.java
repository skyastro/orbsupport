package gaia.xhipreader;

import java.util.HashMap;
import java.util.Map;
import nom.tam.fits.*;
/**
 * Read the XHIP catalog FITS files and produce JSON files suitable for use in the Orrery
 * @author Tom
 */
public class XHIPList {    

    public static void main(String[] args) throws Exception {
        new XHIPList().process("c:\\users\\tom\\downloads\\xhip_astrom.fits", "c:\\users\\tom\\downloads\\xhip_photom.fits");
    }
    float dmin =  (float)1.e10;
    float dmax =(float)  -1.e10;
    int   countP= 0;
    int   countV= 0;
    void process(String astrom, String photom) throws Exception {    
        Fits f    = new Fits(astrom);
        TableHDU bhdu = (TableHDU) f.getHDU(1);
        int[] ids = (int[]) bhdu.getColumn("HIP");
        float[] ls = (float[]) bhdu.getColumn("GLon");
        float[] bs = (float[]) bhdu.getColumn("GLat");
        float[] xs = (float[]) bhdu.getColumn("X");
        float[] ys = (float[]) bhdu.getColumn("Y");
        float[] zs = (float[]) bhdu.getColumn("Z");
        float[] us = (float[]) bhdu.getColumn("U");
        float[] vs = (float[]) bhdu.getColumn("V");
        float[] ws = (float[]) bhdu.getColumn("W");
        float[] ds = (float[]) bhdu.getColumn("Dist");
        String[] types = (String[]) bhdu.getColumn("SpType");
        Fits g    = new Fits(photom);
        bhdu = (TableHDU) g.getHDU(1);
        int[] ids2 = (int[]) bhdu.getColumn("HIP");
        Map<Integer, Float> Bmags = new HashMap<>();
        Map<Integer, Float> Vmags = new HashMap<>();
        float[] bms = (float[]) bhdu.getColumn("Bmag");
        float[] vms = (float[]) bhdu.getColumn("Vmag");
        for (int i=0; i<ids2.length; i += 1) {
            Bmags.put(ids2[i], bms[i]);
            Vmags.put(ids2[i], vms[i]);
        }
        for (int i=0; i<ids.length; i += 1) {
            writeData(ids[i], 
                      ls[i],bs[i], 
                      types[i],
                      xs[i],ys[i],zs[i], 
                      us[i],vs[i],ws[i],
                      ds[i], 
                      Bmags.get(ids[i]),Vmags.get(ids[i]));
        }
                              
    }
    void writeData(int id, float l, float b, String spt, float x, float y, float z,
                 float u, float v, float w, float d,
                 float bm, float vm) {
        if (d <= 0   || d > 40) {
            return;
        }
        System.out.println("HIP "+id);
    }    
}
