/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package gaia.nea;

/**
 *
 * @author Tom
 */
public class TestSize {
    public static void main(String[] args) {
        long buf[] = new long[500000000];
        for (int i=0; i<buf.length; i += 1) {
            buf[i] = i;   
        }
        long l = 0;
        for (int i=0; i<buf.length; i += 1) {
            l += buf[i];
        }
        System.out.println("L is:"+l);
    }
    
}
