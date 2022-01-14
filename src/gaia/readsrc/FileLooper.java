package gaia.readsrc;

import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Loop over all of the CSV files in the DR2 GaiaSource catalog in
 * order of ID.
 * @author Tom
 */
public class FileLooper {
    String base = "/mnt/f/gaia/gaia_source/cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv";
    
    File    currFile = null;
    boolean atEOF       = false;
    Iterator<File>   fileIterator;
    Iterator<String> idIterator;
    IDComparator idc = new IDComparator();
    String idStart = null;
        
    class FileComparator implements Comparator<File> {
        public int compare(File a, File b) {
            String an = a.getName();
            String bn = b.getName();
            String aStart = getStart(an);
            String bStart = getStart(bn);
            return idc.compare(aStart, bStart);
        }        
    }
    
    public static void main(String[] args) throws Exception {
        FileLooper fl = new FileLooper();
        fl.testRun();
    }
    
    void testRun() throws Exception {
        init(null);
        long idCnt = 0;
        long errs  = 0;
        String lastID = "";
        while (true) {
            String id = nextID();
            idCnt  += 1;
            if (idCnt%100000 == 0) {
                System.out.println("  "+idCnt+" "+id+"  "+errs+" "+currFile.getName());
            }
            if (idc.compare(lastID, id) >= 0) {
                System.out.println("Misordering:"+lastID+" "+id);
                errs += 1;
            }
            lastID = id;
        }
    }
    
    
    TreeSet<File> fileTree = new TreeSet<File>(new FileComparator());
    
    static String getStart(String name) {
        String[]  flds = name.split("_");
        if (flds.length != 3) {
            return null;
        }
        return flds[1];
    }
    
    void setStart(String id) {
        idStart = id;
    }
    void init() throws Exception {
        init(null);
    }
    
    void init(String[] args) throws Exception {
        System.out.println("In FileLooperInit");
        if (args != null && args.length > 0) {
            base = args[0];
        }
        File f = new File(base);
        if (!f.isDirectory()) {
            System.err.println("Base not directory...");
            return;            
        }
        File[] fileList = f.listFiles();
        System.out.println("Got FileList:"+fileList.length+" "+idStart);
        fileTree = new TreeSet<File>(new FileComparator());
        System.out.println("Created fileTree");
        for(File curr: fileList) {
            if (curr.getName().startsWith("GaiaS")) {
                fileTree.add(curr);
            }
        }
        System.out.println("Sorted file tree");
        if (idStart != null) {
            // Prune the tree of all entries until we reach the one that contains
            // the needed id.
            
            int dcount = 0;
            System.out.println("Looking for data..."+idStart);
            while (true) {
                f = fileTree.first();
                String name = f.getName();
                String[] flds = name.split("_");
                String last = flds[2].substring(0, flds[2].indexOf("."));
                if (idc.compare(idStart, last) <= 0) {
                    break;
                } else {
                    fileTree.remove(f);
                    dcount += 1;
                    if (dcount % 100 == 0) {
                        System.out.println("Removing:"+dcount);
                    }
                }
            }
            System.out.println("Deleted:"+dcount+" files");
            
            int zcount = 0;
            String id;
            while ( (id=nextID()) != null) {
                if (zcount % 1000 == 0) {
                    System.out.println("zcount:"+zcount+" "+idStart+" "+id);
                }
                if (id.equals(idStart)) {
                    System.out.println("Match at:"+zcount);
                    break;
                }
                zcount += 1;
            }            
        }
    }
    
    String nextID() throws Exception {
        if (atEOF) {
            return null;            
        }
        if (currFile == null) {
            fileIterator = fileTree.iterator();
            nextFile();            
        }
        
        if (idIterator.hasNext()) {
            return idIterator.next();
        } else {
            if (fileIterator.hasNext()) {
                nextFile();
                return nextID();
            } else {
                atEOF = true;
                return null;
            }
        }
    }
    
    void nextFile() throws Exception {
        currFile = fileIterator.next();
        System.out.println("Opening file:"+currFile.getName());
        TreeSet<String> idTree = new FileProcessor(currFile).process();
        idIterator = idTree.iterator();
    }
}