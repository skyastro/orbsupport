package gaia.readsrc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the split up identifications file
 * where the files are of the form data-00000.csv
 * and each file is in dr2id order.
 * @author Tom
 */
public class SplitReader {
    static String  dftBase    = "/mnt/d/gaia/sort-";
    String         base;
    int            fileCount  = 0;
    BufferedReader br         = null;
    String         readLine   = null;
    boolean        foundEOF   = false;
    List<String>   rows;
    
    public static void main(String[] args) throws Exception {
        SplitReader sr;
        if(args.length > 0) {
            sr = new SplitReader(args[0]);
        } else {
            sr = new SplitReader();
        }
        long lineCount = 0;
        while (true) {
            String line = sr.nextLine();
            lineCount += 1;
            if (lineCount % 1000000 == 0) {
                System.out.println("  Read line: "+lineCount);
            }
        }        
    }
    
    SplitReader() throws Exception {
        this(dftBase);
    }
    SplitReader(String base) throws Exception {
        this.base = base;
        nextOpen();
    }
    
    /** Open the next element of the split data */
    void nextOpen() throws Exception {
        if (br != null) {
            br.close();
        }
        String suffix = String.format("%05d", fileCount);
        String fn = base + suffix + ".csv";
        System.out.println("Opening file:"+fn);
        fileCount += 1;
        File f = new File(fn);
        if (!f.exists()) {
            System.out.println("File not found.  Signalling EOF");
            throw new EOFException("Extension file not found for count:"+fileCount);
        }        
        br = new BufferedReader(new FileReader(fn));
    }
    
    void skipTo(String id) throws Exception {
        IDComparator idc = new IDComparator();
        // First look to find the file that is past where we want to be.
        System.out.println("Skipping files in SplitReader...");
        while (true) {
            try {
                nextOpen();
            } catch (EOFException e) {
                System.out.println("Breaking from loop on EOF: fileCount="+fileCount);
                break;
            }
            String line = nextLine();
            String[] flds = line.split(",");
            String cID = flds[0];
            if (idc.compare(id, cID) <= 0) {                
                System.out.println("Breaking from loop on "+fileCount+ " "+idc.compare(id, cID));
                break;
            }
        }
       
        fileCount -= 2;
        foundEOF = false;
        nextOpen();
        // Read entries till we read the match (or go beyond it...)
        List<String> packet;
        int zcount = 0;
        while ((packet=nextID()) != null) {
            String[] flds = packet.get(0).split(",");
            String cID = flds[0];
            if (idc.compare(id, cID) <= 0 ) {
                System.out.println("Found Zcount:"+zcount+" "+id+" "+cID);                
                break;
            }            
            zcount += 1;
        }        
    }
    
    /** Read the next line */
    String nextLine() throws Exception {
        if (foundEOF) {
            throw new EOFException("Read past end of file");
        }
        String line = br.readLine();
        if (line == null) {
            try {
                nextOpen();
                return nextLine();
            } catch (EOFException e) {
                foundEOF = true;
                return null;
            }
        }
        if (line.startsWith("dr2")) {
            // Skip header lines.
            return nextLine();
        }
        return line;
    }
    
    /** Get all of the lines associated with the
     *  next ID in the split.
     */
    List<String> nextID() throws Exception {
        if (foundEOF) {
            return null;
        }
        
        rows = new ArrayList<String>();
        if (readLine == null) {
            readLine = nextLine();
        }
        String id = readLine.substring(0, readLine.indexOf(","));
        rows.add(readLine);
        while ( (readLine=nextLine()) != null) {
            String nid = readLine.substring(0, readLine.indexOf(","));
            if (nid.equals(id)) {
                rows.add(readLine);
            } else {
                return rows;
            }            
        }
        return rows;  // Only get here for last entry!        
    }    
}
