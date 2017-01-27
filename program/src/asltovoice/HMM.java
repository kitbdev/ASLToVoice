
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
    public void Analyze(FrameData fd) {

    }
    public void Clear() {
        
    }

}
