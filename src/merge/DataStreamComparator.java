
package merge;

import java.util.Comparator;

/**
 *
 * @author Tom
 */
public class DataStreamComparator implements Comparator<DataStream> {
    public int compare(DataStream a, DataStream b) {
        int p1 = a.currentLine.indexOf(",");
        int p2 = b.currentLine.indexOf(",");
        if (p1 < p2) {
            return -1;
        } else if (p1 > p2) {
            return 1;
        } else {
            return a.currentLine.compareTo(b.currentLine);
        }
    }    
}
