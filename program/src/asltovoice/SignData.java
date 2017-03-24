package asltovoice;

import java.util.ArrayList;


// holds all of the data for a sign
// list of frameData for each frame oof the sign recorded
// includes time taken, number of frames, and other needed info
public class SignData {
    
    public int totalDuration; // in ms
    public FrameData.Vector3 totalHandMovement;
    public ArrayList<FrameData> frames;
    
    public double[] GetAllData() {
        double[] data = new double[2];
        for (int i=0; i<frames.size(); i++) {
//            data+=frames.get(i).GetDoubleData();
        }
        return data;
    }
    
    public void AddFrame(FrameData frame) {
        // TODO check data or something
        frames.add(frame);
    }
    
}
