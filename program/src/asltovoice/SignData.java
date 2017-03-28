package asltovoice;

import java.util.ArrayList;


// holds all of the data for a sign
// list of frameData for each frame oof the sign recorded
// includes time taken, number of frames, and other needed info
public class SignData {
    
    public int totalDuration; // in ms
    public FrameData.Vector3 totalHandMovement;
    public ArrayList<FrameData> frames;
    
    public String GetAllData() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<frames.size(); i++) {
            sb.append(frames.get(i).GetData());
        }
        return sb.toString();
    }
    
    public void AddFrame(FrameData frame) {
        // TODO check data or something
        frames.add(frame);
    }
    public void Clear() {
        totalDuration = 0;
        //totalHandMovement = FrameData.Vector3();
        frames.clear();
    }
}
