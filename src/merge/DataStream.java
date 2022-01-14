package merge;

import java.io.BufferedReader;

/**
 *
 * @author Tom
 */
public class DataStream {
    int n;
    BufferedReader rdr;
    String currentLine;
    DataStream(int n, BufferedReader rdr, String currentLine) {
        this.n   = n;
        this.rdr = rdr;
        this.currentLine = currentLine;
    }    
}
