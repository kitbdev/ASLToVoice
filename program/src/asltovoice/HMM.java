package asltovoice;

import java.util.List;
import java.util.ArrayList;

/**
 *  keeps tack of current Hidden Markov Model state
 *  has methods for adding new frames and the current guess
 *  also manages chain of last guessed signs
 * @author Ian
 */
public class HMM {
    List<FrameData> frames = new ArrayList();
    int lastSign;
    boolean signEnded = false;
    int numStates; // N
    int numObservations; // K
    double probInitial[]; // pi
    double probTrans[][]; // A
    double probEmission[][]; // B
    
    // initialize probability arrays
    public void StartHMM(int numStates, int numObservations) {
        this.numStates = numStates;
        this.numObservations = numObservations;
        
        probInitial = new double[numObservations];
        probTrans = new double[numStates][numStates];
        probEmission = new double[numStates][numObservations];
    }
    
    // baum-welch algorithm to train the hmm
    public void Train(int tdata[], int steps) {
        for (int i=0; i<steps; i++) {
            double[][] fprob = ForwardProcedure(tdata);
            double[][] bprob = BackwardProcedure(tdata);
            
        }
    }
    double[][] ForwardProcedure(int[] o) {
        double probFwd[][] = new double[numStates][o.length];
        for (int i=0; i<numStates; i++) {
            probFwd[i][0] = probInitial[i] * probEmission[i][o[0]];
        }
        for (int j=1; j<numStates; j++) {
            for (int t=1; t<o.length; t++) {
                double pfwdsum = 0;
                for (int k=0; k<numStates; k++) {
                    pfwdsum += probFwd[k][t-1] * probTrans[k][j];
                }
                probFwd[j][t] = probEmission[j][o[t]] * pfwdsum;
            }
        }
        return probFwd;
    }
    double[][] BackwardProcedure(int[] o) {
        double probBack[][] = new double[numStates][o.length];
        for (int i=0; i<numStates; i++) {
            probBack[i][o.length-1] = 1;
        }
        for (int i=0; i<numStates; i++) {
            for (int t=o.length-2; t>=0; t--) {
                double pbacksum = 0;
                for (int k=0; k<numStates; k++) {
                    pbacksum += probBack[k][t+1] * probTrans[i][k] * probEmission[k][o[t+1]];
                }
                probBack[i][t] = pbacksum;
            }
        }
        return probBack;
    }
    
    // viterbi algorithm to determine most likely state
    public void Viterbi(int[] o) {
        double t1[][] = new double[numStates][numObservations];
        double t2[][] = new double[numStates][numObservations];
        
        for (int i=0; i<numObservations; i++) {
            t1[i][0] = probInitial[i] * probEmission[i][o.length];
            t2[i][0] = 0;
        }
        for (int i=1; i<numStates; i++) {
            for (int j=0; j<numObservations; j++) {
                //...
            }
        }
    }
    
    // add frame and check if we have stopped
    public void Analyze(FrameData fd) {
        frames.add(fd);
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
