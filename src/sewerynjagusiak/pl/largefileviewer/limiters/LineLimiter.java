/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sewerynjagusiak.pl.largefileviewer.limiters;

/**
 *
 * @author Seweryn
 */
public class LineLimiter implements ChunkLimiter{

    private int lines;
    private int limit;

    public LineLimiter(int limit) {
        this.lines = 0;
        this.limit = limit;
    }
    
    @Override
    public boolean limitReached(String line) {
        return ++this.lines >= this.limit;
    }

    @Override
    public void reset() {
        this.lines = 0;
    }
    
}
