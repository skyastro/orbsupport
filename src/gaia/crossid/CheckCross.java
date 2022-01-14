
package gaia.crossid;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author Tom
 */
public class CheckCross {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int lineCount = 0;
        int diffCount = 0;
        String line;
        while ( (line=br.readLine()) != null) {
            if (line.startsWith("dr2")) {
                continue;
            }
            String[] flds = line.split(",");
            if (flds.length != 5) {
                System.out.println("Bad line:"+line);
                continue;
            }
            lineCount += 1;
            if (!flds[0].equals(flds[1])) {
                diffCount += 1;
            }
            
        }
        System.out.println("Number of lines: "+lineCount);
        System.out.println("Number of diffs: "+diffCount);
    }
    
}
