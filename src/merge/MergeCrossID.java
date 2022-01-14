/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package merge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TreeSet;

/**
 *
 * @author Tom
 */
public class MergeCrossID {
    public static void main(String[] args) throws Exception {
        new MergeCrossID().run(args);
    }
    
    TreeSet<DataStream> streams;
    SplittingWriter     out;
    
    public void run(String[] args) throws Exception {
        
        streams = new TreeSet(new DataStreamComparator());
        start(args);
        out = new SplittingWriter("sort", "csv", 5000000);
        run();
        out.closeFile();
       
    }
    
        
    void start(String[] args) throws Exception {
        for (int i=0; i<args.length; i += 1) {
            BufferedReader br = new BufferedReader(new FileReader(args[i]));
            // Skip the first line.
            br.readLine();
            String line = br.readLine();
            streams.add(new DataStream(i, br, line));
            if (i%10 == 0) {
                System.out.println("Opend file "+i);
            }
        }
        System.out.println("Completed initialization");
    }
    
    void run() throws Exception {
        System.out.println("Starting run:"+streams.size());
        long lineCount = 0;
        while (streams.size() > 0) {
            if (lineCount%100000  == 0) {
                System.out.println("Processed input line:"+lineCount+" #Streams:"+streams.size());
            }
            lineCount += 1;
            DataStream top = streams.first();
            out.write(top.currentLine);
            streams.remove(top);
            String line = top.rdr.readLine();
            if (line == null) {
                top.rdr.close();
                System.out.println("Closing stream:"+top.n);
            } else {
                DataStream newS = new DataStream(top.n,top.rdr, line);
                streams.add(newS);
            }
            
        }
        out.close();
    }
}
