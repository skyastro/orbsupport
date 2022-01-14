package gaia.generate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tom
 */
public class IndexedCSVReader {
    BufferedReader br;
    String     currentLine;
    String[]   currentTokens;
    long       currentIndex;
    int        columnIndex;
    Map<String, Integer> colNums;
    String[]  colNames;
    boolean   foundNaN = false;
    
    public IndexedCSVReader(String file) throws Exception {
        this(file,0);
    }
    
    public IndexedCSVReader(String file, int column) throws Exception {
        this.columnIndex = column;
        br = new BufferedReader(
          new FileReader(file));
        colNums     = new HashMap<String,Integer>();
        currentLine = br.readLine();
        colNames    = currentLine.split(",");
        currentTokens = colNames;  // Just after reading header.
        for (int i=0; i<colNames.length; i += 1) {
            colNums.put(colNames[i], i);
        }
        currentIndex = Long.MIN_VALUE;
    }
    
    boolean next() throws Exception {
        currentLine = br.readLine();
        if (currentLine == null) {
            currentIndex = Long.MAX_VALUE;
            currentTokens = null;
            return false;
        } else {
            // Might be commas embedded in quotes.
            currentTokens = currentLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            String id = currentTokens[columnIndex];
            if (id.length() == 0) {
                currentIndex = -1;
            } else {
                currentIndex = Long.parseLong(id);
            }
            return true;
        }
    }
    
    public String getLine() {
        return currentLine;
    }
    
    public String[] getTokens() {
        return currentTokens;
    }
    
    public String get(String name) {
        int index = colNums.get(name);
        if (index >= currentTokens.length) {
            // Terminal empty strings are discarded.
            return "";
        }
        String res = currentTokens[index];
        return res;
    }
    
    public double getDouble(String name) {
        String token = null;
        if (foundNaN) {
            return Double.NaN;  // Need to clear before we can do anything.
        }
        try {
            token = get(name);
            if (token == null || token.length() == 0) {
                foundNaN = true;
                return Double.NaN;
            } else {
                return Double.parseDouble(token);
            }
        } catch (Exception e) {
            System.err.println("Got exception :"+e+" name,value:"+name+" "+token+" "+currentLine);
            throw e;
        }
    }
    
    /** Have we created a NaN since last we checked? */
    public boolean checkNaN() {
        boolean ret = foundNaN;
        foundNaN = false;
        return ret;
    }
    
    public long getIndex() {
        return currentIndex;
    }
}
