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
    public int normalizedNumFrames = 16;
    
    public String GetNormalizedDataString() {
        // make normalized data from all datasets
        ArrayList<double[]> allData = new ArrayList<double[]>();
        for (int i=0; i<frames.size(); i++) {
            allData.add(frames.get(i).GetDoubleData());
        }
        int dataPerFrame = allData.get(0).length - 4;// remove id, time, curframe, and totalframes
        double framesPerFrame = (double)frames.size() / normalizedNumFrames;
        int framesPerFramei = (int)(framesPerFrame);
        ArrayList<double[]> normalizedData = new ArrayList<double[]>();
        for (int i=0; i<normalizedNumFrames; i++) {
            double[] avg = new double[dataPerFrame];
            for (int j=0; j<dataPerFrame; j++) {
                avg[j] = 0;
                for (int k=i; k<i+(int)framesPerFramei; k++) {
                    avg[j] += allData.get(k)[j+4]; 
//                    System.out.print(allData.get(k)[j+4]+",");
                }
                avg[j] /= framesPerFrame;
            }
            normalizedData.add(avg);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(allData.get(0)[0]+", ");// id
        double timeTaken = allData.get(0)[1] - allData.get(allData.size()-1)[1];
        sb.append(timeTaken+", "); // time
        for (int i=0; i<normalizedNumFrames; i++) {
            AddDataArray(sb, normalizedData.get(i));
        }
        sb.append(sign);
        return sb.toString();
    }
    void AddDataArray(StringBuilder sb, double[] data) {
        for (int i=0;i<data.length;i++) {
            sb.append(data[i]);
            sb.append(",");
        }
    }
    public double[] GetNormalizedData() {
        ArrayList<double[]> allData = new ArrayList<double[]>();
        for (int i=0; i<frames.size(); i++) {
            allData.add(frames.get(i).GetDoubleData());
        }
        int dataPerFrame = allData.get(0).length - 4;// remove id, time, curframe, and totalframes
        int framesPerFrame = normalizedNumFrames / frames.size();
        ArrayList<double[]> normalizedData = new ArrayList<double[]>();
        for (int i=0; i<normalizedNumFrames; i++) {
            double[] avg = new double[dataPerFrame];
            for (int j=4; j<3+dataPerFrame; j++) {
                avg[j-4] = 0;
                for (int k=i; k<i+framesPerFrame; k++) {
                    avg[j-4] += allData.get(k)[j]; 
                }
                avg[j-4] /= framesPerFrame;
            }
            normalizedData.add(avg);
        }
        double[] data = new double[normalizedNumFrames*dataPerFrame+1];
        double timeTaken = allData.get(0)[1] - allData.get(allData.size()-1)[1];
        data[0] = timeTaken;
        for (int i=0; i<normalizedNumFrames; i++) {
            System.arraycopy(normalizedData.get(0), 0, data, i*dataPerFrame+1, normalizedData.size());
        }
        return data;
    }
    
    public String GetNormalizedHeaderLine() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        sb.append("time,");// amount of time this sign took
        for (int i=0; i<normalizedNumFrames; i++) {
            AddPosRot(sb, "arm_"+i);
            AddPosRotVel(sb, "hand_"+i);
            AddPosRotVel(sb, "finger1_"+i);
            AddPosRotVel(sb, "finger2_"+i);
            AddPosRotVel(sb, "finger3_"+i);
            AddPosRotVel(sb, "finger4_"+i);
            AddPosRotVel(sb, "finger5_"+i);
        }
        sb.append("sign");
        return sb.toString();
    }
    // 16 * (3*6+2*1)+3 = 323
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
        frames.add(frame);
    }
    public void RemoveLast(int amount) {
        for (int i=0;i<amount;i++) {
            frames.remove(frames.size());
        }
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
