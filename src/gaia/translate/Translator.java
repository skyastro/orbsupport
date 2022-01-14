
package gaia.translate;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;

/**
 * This class uses the knowledge of which DR2 IDs have no corresponding
 * DR3 object and which have different names to translate.  All names
 * which are not in those two classes are returned unchanged.  This includes
 * the 97% of the DR2 IDs that are unchanged in the DR3.  However if an invalid
 * DR2 ID is supplied this class is blissfully unaware.
 * @author Tom
 */
public class Translator {
    String base = "/mnt/f/gaia/gaia_source/cdn.gea.esac.esa.int/Gaia/gdr2/gaia_source/csv/data.obj";
    Set<Long>        missing;
    Map<Long,Long> changed;
    Translator() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(base));
        missing = (Set<Long>)ois.readObject();
        changed = (Map<Long,Long>)ois.readObject();        
    }
    
    /** Return the dr3id given a valid dr2id input.  If the input is not a valid
     *  DR2 id, it will be returned unchanged.
     * @param dr2id  A valid DR2 ID
     * @return Null if there is no corresponding object, or the corresponding ID in the DR3.
     */
    public String translate(String dr2idStr) {
        long dr2id = Long.parseLong(dr2idStr);
        if (missing.contains(dr2id)) {
            return null;
        }
        long trans = changed.get(dr2id);
        
        if (trans != 0) {
            return trans+"";            
            
        } else {
            return dr2idStr;
        }
    }
    
}
