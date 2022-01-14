package gaia.utils;

import java.io.File;

/**
 *
 * @author Tom
 */
public class Utils {
    /** Convert a windows file string to Unix */
    public static String unixFile(String input) {
        if (input.charAt(1) == ':') {
            input = "/mnt/"+input.charAt(0)+"/"+input.substring(2);
        }
        input = input.replaceAll("\\\\", "/");
        return input;
    } 
    
    /** Convert a unix file string to Windows. */
    public static String winFile(String input) {
        if (input.startsWith("/mnt/")) {
            input = input.charAt(6)+":"+input.substring(7);
        }
        input = input.replaceAll("/", "\\");
        return input;
    }
    
    public static String findFile(String input) {
        if (new File(input).exists()) {
            return input;
        } else if (input.indexOf(":") > 0 || input.indexOf("\\") >= 0) {
            if (new File(unixFile(input)).exists()) {
                return unixFile(input);
            }
        } else if (input.indexOf("/") >= 0) {
            if (new File(winFile(input)).exists()) {
                return winFile(input);
            }
        }
        return null;
    }
}
