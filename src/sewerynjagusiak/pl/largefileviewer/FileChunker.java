package sewerynjagusiak.pl.largefileviewer;

import sewerynjagusiak.pl.largefileviewer.limiters.ChunkLimiter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Seweryn
 */
public class FileChunker {
    
    private String path;
    private final StringBuffer buffer;
    private final ChunkLimiter limiter;
    private BufferedReader reader;
    private boolean terminated;
    private int lineNumber;
    private int lineCount;
    private int currentLineCount;
    
    
    public FileChunker(String path, ChunkLimiter limiter) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        this.buffer = new StringBuffer();
        this.limiter = limiter;
        this.path = path;
        this.reset();
    }
    
    public void reset() throws IOException {
        this.flush();
        this.limiter.reset();
        this.lineNumber = 0;
        this.lineCount = 0;
        this.currentLineCount = 0;
        this.terminated = false;
        if (null != this.reader) {
            this.reader.close();
        }
        this.reader = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(this.path), 
                "UTF-8"
            )
        );
    }
    
    public String getNextChunk() throws IOException {
        if (this.terminated) {
            return "";
        }
        
        String line = null;
        while (null != (line = this.reader.readLine())) {
            this.lineNumber++;
            this.currentLineCount++;
            this.push(line);
            if (this.limiter.limitReached(line)) {
                break;
            }
        }
        this.lineCount = this.currentLineCount;
        this.currentLineCount = 0;
        if (null == line) {
            this.terminated = true;
        }
        this.limiter.reset();
        return this.flush();
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isTerminated() {
        return terminated;
    }
    
    public void close() throws IOException {
        this.reader.close();
    }
    
    private void push(String str) {
        this.buffer.append(str).append("\n");
    }
    
    private String flush() {
        String reply = this.buffer.toString();
        this.buffer.delete(0, reply.length());
        return reply;
    }
}
