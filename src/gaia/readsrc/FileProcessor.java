/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gaia.readsrc;

import java.io.File;
import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author Tom
 */
public class FileProcessor {
    ReadCol rc;
    int lineCount;
    TreeSet<String> ids = new TreeSet<String>(new IDComparator());            
    FileProcessor(File src) throws Exception {
        rc = new ReadCol(src,2);
    }
    TreeSet<String> process() throws Exception  {
        String idStr;
        
        while (  (idStr=rc.nextRow()) != null) {
            ids.add(idStr);
            lineCount += 1;
        }
        return ids;
    }
}
