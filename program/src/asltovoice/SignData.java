package asltovoice;

import java.util.ArrayList;


// holds all of the data for a sign
// list of frameData for each frame oof the sign recorded
// includes time taken, number of frames, and other needed info
public class SignData {
    
    public int totalDuration; // in ms
    public FrameData.Vector3 totalHandMovement;
    public ArrayList<FrameData> frames = new ArrayList<FrameData>();
    public String sign;
    
    public String GetAllData() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<frames.size(); i++) {
            sb.append(frames.get(i).GetData());
            sb.append(sign);
            sb.append("\n");
        }
        sb.append(sign);
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
        sign = "";
    }
    public String GetHeaderLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        sb.append("time,");
        sb.append("curFrame,");
        sb.append("totalFrame,");
        AddPosRot(sb, "arm");
        AddPosRotVel(sb, "hand");
        AddPosRotVel(sb, "finger1");
        AddPosRotVel(sb, "finger2");
        AddPosRotVel(sb, "finger3");
        AddPosRotVel(sb, "finger4");
        AddPosRotVel(sb, "finger5");
        sb.append("sign");
        return "";
    }
    void AddPosRot(StringBuilder sb, String name) {
        sb.append(name);
        sb.append("_pos_x,");
        sb.append(name);
        sb.append("_pos_y,");
        sb.append(name);
        sb.append("_pos_z,");
        sb.append(name);
        sb.append("_yaw,");
        sb.append(name);
        sb.append("_roll,");
        sb.append(name);
        sb.append("_pitch,");
    }

    void AddPosRotVel(StringBuilder sb, String name) {
        AddPosRot(sb, name);
        sb.append(name);
        sb.append("_vel_x,");
        sb.append(name);
        sb.append("_vel_y,");
        sb.append(name);
        sb.append("_vel_z,");
    }
}
