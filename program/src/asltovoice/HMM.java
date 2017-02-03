package asltovoice;

import java.util.List;
import java.util.ArrayList;

/**
 *  keeps tack of current Hidden Markov Model state
 *  has methods fro adding new frames and the current guess
 *  also manages chain of last guessed signs
 * @author Ian
 */
public class HMM {
    List<FrameData> frames = new ArrayList();
    int lastSign;
    boolean signEnded = false;
    
    // add frame and check if we have stopped
    public void Analyze(FrameData fd) {
        
        signEnded = true;
    }
    
    public boolean SignEnded() {
        return signEnded;
    }
    
    public void Clear() {
        signEnded = false;
        frames.clear();
    }
    
    public FrameData[] GetFrames() {
        return (FrameData[]) frames.toArray();
    }
}
