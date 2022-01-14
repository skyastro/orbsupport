
package gaia.readsrc;

import java.util.Comparator;

/**
 *
 * @author Tom
 */
class IDComparator implements Comparator<String> {

    public int compare(String a, String b) {
        if (a.length() < b.length()) {
            return -1;
        } else if (a.length() > b.length()) {
            return 1;
        } else {
            return a.compareTo(b);
        }
    }
}
