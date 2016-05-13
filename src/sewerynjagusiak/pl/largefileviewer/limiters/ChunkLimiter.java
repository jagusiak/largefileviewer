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
public interface ChunkLimiter {
    
    public boolean limitReached(String line);
    public void reset();
}
