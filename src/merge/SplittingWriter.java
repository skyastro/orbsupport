package merge;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

/**
 *
 * @author Tom
 */
public class SplittingWriter {
    
    int delta;
    String stem;
    String type;
    Writer log;
    
    int lineCount = 0;
    int fileCount = 0;
    int lineLimit = delta;
    
    Writer wr;
    
    SplittingWriter(String stem, String type, int delta) throws Exception {
        this.stem = stem;
        this.type = type;
        this.delta = delta;
        log = new BufferedWriter(new FileWriter(stem+".log"));
        startFile();
    }
    
    void write(String line) throws Exception {
        if (lineCount == 0) {
            log.write(stem+"_"+fileCount+" start:"+line+"\n");
        }
        wr.write(line);
        wr.write("\n");
        lineCount += 1;
        if (lineCount >= delta) {
            log.write(fileCount+" end:"+line+"\n");
            closeFile();
            startFile();
        }
    }
    
    void startFile() throws Exception {
        String indexStr = String.format("%05d", fileCount);
        System.out.println("Creating split file element:"+indexStr);
        fileCount += 1;
        lineCount  = 0;
        String fileName = stem+"-"+indexStr+"."+type;
        wr = new BufferedWriter(new FileWriter(fileName));
    }
    void closeFile() throws Exception  {
        log.flush();
        wr.flush();
        wr.close();        
    }
    void close() throws Exception  {
        log.flush();
        log.close();
    }
}
