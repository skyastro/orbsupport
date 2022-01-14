/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.readsrc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

/**
 *
 * @author Tom
 */
public class Comparer {
    
    public static void main(String[] args) throws Exception {
        
        String latest  = null;
        if (args[0].equals("restart")) {
            Comparer cmp = new Comparer();
            // Get the last ID which has been written 
            BufferedReader br = new BufferedReader(new FileReader("crossmiss.dat"));
            System.out.println("Looking for lastCross:");
            String line, lastCross=null;
            while ( (line = br.readLine()) != null) {
                lastCross = line;
            }
            br.close();
            System.out.println("Last cross is:"+lastCross);
            System.out.println("Looking for last delta:");
            
            long flen = new File("deltas.dat").length();
            FileInputStream fis = new FileInputStream("deltas.dat");
            fis.skip(flen-1000);
            
                    
            br = new BufferedReader(new InputStreamReader(fis));
            String lastDelta=null;
            while ( (line = br.readLine()) != null) {
                String[] flds = line.split(" ");
                lastDelta = flds[0];                
            }
            br.close();
            System.out.println("LastDelta is:"+lastDelta);
            System.out.println("lastDelta:"+lastDelta+" lastCross:"+lastCross);
            latest = lastCross;
            if (new IDComparator().compare(lastCross, lastDelta) == 1) {
                latest = lastDelta;
            }
            System.out.println("Last is..."+latest);
        }
        new Comparer().run(latest);
    }
    

    String startID = null;
    SplitReader sr;
    FileLooper  fl;
    IDComparator cmp = new IDComparator();
    BufferedWriter cm;
    BufferedWriter idm;
    BufferedWriter deltas;
    
    String getCrossID(List<String> list) {
        return list.get(0).substring(0, list.get(0).indexOf(","));
    }
    
    String dr2id;
    String crossID;
    List<String> crossList;
    long[] counts = new long[12];
    // 0: Unary no change.
    // 1: Unary changed
    // 2: Missing Cross
    // 3: Missing DR2
    // 4: Acceptable match nochange    
    // 5: Acceptable match change
    // 6: Single nearby no change
    // 7: Single nearby change
    // 8: Much closer no change
    // 9: Much closer change
    // 10: FOM no change
    // 11: FOM change
    
    void run(String init) throws Exception {
        boolean append = init != null;
        sr = new SplitReader();
        fl = new FileLooper();
        System.out.println("Do we append:"+init+" "+append);
        if (append) {
            sr.skipTo(init);
            fl.setStart(init);
        }
        
        fl.init();
        
        cm = new BufferedWriter(new FileWriter("crossmiss.dat", append));
        idm = new BufferedWriter(new FileWriter("idmiss.dat", append));
        deltas = new BufferedWriter(new FileWriter("deltas.dat", append));
    
        crossList = sr.nextID();
        dr2id     = fl.nextID();
        crossID   = getCrossID(crossList);
        long matches = 0;
        long dr2miss = 0;
        long crossMiss = 0;
        long count = 0;
        long deltaCount = 0;
        while (true) {
            count += 1;
            if (count % 100000 == 0) {
                System.out.println("At "+count+" "+dr2id);
                for (int i=0; i<counts.length; i += 1) {
                    System.out.print("   "+counts[i]);
                }
                System.out.println();
                cm.flush();
                idm.flush();
                deltas.flush();
            }
            if (dr2id == null && crossID == null) {
                break;
            }
            if (dr2id == null) {
                counts[3] += 1;
                System.out.println("Unmatched late crossID:"+crossID);
                nextCross();
            } else if (crossID == null) {
                counts[2] += 1;
                System.out.println("Unmatched late dr2id:"+dr2id);
                nextDR2();
            } else {
                int indic = cmp.compare(dr2id, crossID);
                if (indic == 0) {
                    matches += 1;
                    deltaCount += checkMatch(crossList, dr2id);
                    nextCross();
                    nextDR2();
                } else if (indic < 0) {
                    showCrossMiss(dr2id);
                    crossMiss += 1;
                    counts[2] += 1;
                    nextDR2();
                } else {
                    counts[3] += 1;
                    showIDMiss(crossID);
                    nextCross();
                }                                
            }
        }
        System.out.println("At "+count);
        for (int i=0; i<counts.length; i += 1) {
            System.out.print("   "+counts[i]);
        }
        System.out.println();
        deltas.flush();
        cm.flush();
        idm.flush();
        cm.close();
        idm.close();
        deltas.close();
    }
    
    void showCrossMiss(String id) throws Exception  {
        cm.write(id+"\n");
    }
    void showIDMiss(String id) throws Exception  {
        idm.write(id+"\n");
    }
    
    void showDelta(String dr2id, String dr3id) throws Exception {
        deltas.write(dr2id+" "+dr3id+"\n");
    }
    
    class CrInfo {
        String id2;
        String id3;
        double dr = 20;
        double dm = 1;
        boolean pm;
    }
    
    CrInfo parseCross(String line) {
        String[] flds = line.split(",");
        CrInfo x = new CrInfo();
        x.id2 = flds[0];
        x.id3 = flds[1];
        try {
            x.dr = Double.parseDouble(flds[2]);
            x.dm = Double.parseDouble(flds[3]);
            x.pm = Boolean.parseBoolean(flds[4]);
        } catch (Exception e) {
  //          System.out.println("Parse error: "+line);
        }
        return x;
    }
    
    int checkMatch(List<String> cross, String dr2id) throws Exception {
        
        // Only one possible match...
        if (cross.size() == 1) {
            String dr3id = parseCross(cross.get(0)).id3;
            if (!dr3id.equals(dr2id)) {
                showDelta(dr2id, dr3id);
                counts[1] += 1;
                return 1;
            } else {
                counts[0] += 1;
                return 0;
            }
        }
        
        // If there is a match with only small position/magnitude offsets... Go for it.
        int smallIndex = -1;
        int largeOffsets = 0;
        for (int i=0; i<cross.size(); i += 1) {
            CrInfo cand = parseCross(cross.get(i));
            if (cand.id3.equals(dr2id)  && Math.abs(cand.dr) < 30 && Math.abs(cand.dm) < 0.1) {
                counts[4] += 1;
                return 0;
            }
            if (Math.abs(cand.dr)  < 20) {
                if (smallIndex >= 0) {
                    // Already found another, so can't use this.
                    smallIndex = -1;
                } else {
                    smallIndex = i;
                }
            } else if (Math.abs(cand.dr) > 100) {
                largeOffsets += 1;
            }
        }
                
        // If there is an object within 20 mas and no other with 100;
        if (smallIndex >= 0 && (largeOffsets == (cross.size() - 1) )) {
            String dr3id = parseCross(cross.get(smallIndex)).id3;
            if (dr3id.equals(dr2id)) {
                counts[6] += 1;
                return 0;
            } else {
                showDelta(dr2id, dr3id);
                counts[7] += 1;
                return 1;
            }
        }
        double minD  = 1.e9;
        double minD2 = 2.e9;
        int    iD = -1;
        int    iD2 = -1;
        double best = -1;
        int    iBest = -1;
        for (int i=0; i<cross.size(); i += 1) {
            CrInfo cand = parseCross(cross.get(i));
            double merit = Math.pow(100/(cand.dr+20), 2) * Math.pow(.3/(cand.dm+0.1), 2);
            if (merit > best) {
                best = merit;
                iBest = i;
            }
            if (cand.dr < minD) {
                // Insert and shift.
                iD2 = iD;
                iD  = i;
                minD2 = minD;
                minD = cand.dr;
            } else if (cand.dr < minD2) {
                // Replace second lowest.
                minD2 = cand.dr;
                iD2 = i;
            }
        }
        
        // If the distance to the second nearest object is more than twice the
        // distance to the nearest.  Otherwise use the figure of merit.
        
        int use;
        int cb=8;
        if (minD2 > 2*minD) {
            use = iD;
        } else {
            cb = 10;
            use = iBest;
        }
        
        
        String dr3id = parseCross(cross.get(use)).id3;
        if (dr3id.equals(dr2id)) {
            counts[cb] += 1;
            return 0;
        } else {
            counts[cb+1] += 1;
            showDelta(dr2id, dr3id);
            return 1;
        }
        
        // Find the best figure of merit defined as (100/(dr+20))^2  * .2/(dm+.05)**2
    }
    
    void nextDR2() throws Exception {
        dr2id = fl.nextID();
        if (dr2id == null) {
            System.out.println("Got null dr2\n");
        }
    }
    void nextCross() throws Exception {
        crossList = sr.nextID();
        if (crossList != null) {
            crossID   = getCrossID(crossList);
        } else {
            crossID = null;
            System.out.println("Got null cross");
        }
    }
}
