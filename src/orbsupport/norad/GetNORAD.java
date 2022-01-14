
package orbsupport.norad;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Get the latest NORAD TLE file.
 * Copied from the Space-track Java example
 * https://www.space-track.org/documentation#howto
 * and modified for use here.  
 * This program creates a file norad.tle in the current directory
 * with the latest set of NORAD elements.
 * 
 * @author Tom
 */
public class GetNORAD {
    
    static String ask(String prompt) {
        // Somewhat concerning that we create
        // a separate reader for each question, but
        // seems to work -- might be system dependent.
        System.out.print(prompt+": ");
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));
 
        // Reading data using readLine
        try {
            String inp = reader.readLine();
            return inp;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        // Get the username and password from the environment
        // or in response to prompts.  
        String userName = System.getenv("NORAD_NAME");
        if (userName == null) {
            userName = ask("Username");
        }
        String password = System.getenv("NORAD_PWD");
        if (password == null) {
            password = ask("Password");
        }
        
        if (userName == null || password == null) {
            System.err.println("Cannot find connection information");
            System.exit(-1);
        }
        
        String baseURL  = "https://www.space-track.org";
        
        String authPath = "/ajaxauth/login";
        String query    = "/basicspacedata/query/class/gp/EPOCH/%3Enow-30/orderby/NORAD_CAT_ID,EPOCH/format/3le";


        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);

        URL url = new URL(baseURL+authPath);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        String input = "identity="+userName+"&password="+password;
        
        // First connection sends name and password and gets cookie
        // that allows successful download.

        OutputStream os = conn.getOutputStream();
        // Post data
        os.write(input.getBytes());
        os.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        System.out.println("Authentication from Server: normally no real content \n");
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }

        // Now send actual query.
        url = new URL(baseURL + query);

        br = new BufferedReader(new InputStreamReader((url.openStream())));

        BufferedWriter bw = new BufferedWriter(new FileWriter("norad.tle"));
        while ((output = br.readLine()) != null) {
            bw.write(output+"\n");
        }

        // Disconnect from server.
        url = new URL(baseURL + "/ajaxauth/logout");
        br = new BufferedReader(new InputStreamReader((url.openStream())));
        conn.disconnect();
    }
}
