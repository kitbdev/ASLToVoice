
package leapwekaasl;

import com.leapmotion.leap.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
/**
 * This class will process the frame and output to a file
 * @author Ian
 */
public class LeapSensor {
    public long lastFrameID = 0;
    boolean recording = false;
    List<Float> records = new ArrayList<>();
    int numFrames = 0;
    String sign = "";
    public void ProcessFrame(Frame frame) {
        if (recording) {
            if (lastFrameID != frame.id()) {
                //HandList hands = frame.hands();
                //what to do with multiple hands?
                Hand hand = frame.hands().frontmost();
                if (hand.isValid()) {
                    //hand.direction();
                    // TODO: track by id?
                    //PointableList pointables = frame.pointables();
                    //ToolList tools = frame.tools();
                    FingerList fingers = hand.fingers();
                    // TODO: add numbers to records
                    // currently arm, hand, and finger pos rot and vel
                    Arm arm = hand.arm();
                    Vector armPos = arm.elbowPosition();
                    records.add(armPos.getX());
                    records.add(armPos.getY());
                    records.add(armPos.getZ());
                    Vector armDir = arm.direction();
                    records.add(armDir.getX());
                    records.add(armDir.getY());
                    records.add(armDir.getZ());
                    // no arm vel
                    // hand
                    Vector handPos = hand.palmPosition();
                    records.add(handPos.getX());
                    records.add(handPos.getY());
                    records.add(handPos.getZ());
                    Vector handDir = hand.direction();
                    records.add(handDir.getX());
                    records.add(handDir.getY());
                    records.add(handDir.getZ());
                    Vector handVel = hand.palmVelocity();
                    records.add(handVel.getX());
                    records.add(handVel.getY());
                    records.add(handVel.getZ());
                    // fingers
                    for (int i=0; i<5; i++) {
                        Finger f = hand.finger(i);
                        Vector fPos = f.tipPosition();
                        records.add(fPos.getX());
                        records.add(fPos.getY());
                        records.add(fPos.getZ());
                        Vector fDir = f.direction();
                        records.add(fDir.getX());
                        records.add(fDir.getY());
                        records.add(fDir.getZ());
                        Vector fVel = f.tipVelocity();// TODO: do we even want velocity?
                        records.add(fVel.getX());
                        records.add(fVel.getY());
                        records.add(fVel.getZ());
                    }
                    numFrames++;
                }
            } 
        }
        lastFrameID = frame.id();
        System.out.print("");
    }
    // start recording with the leap
    public void StartRecording(String signLabel){
        recording = true;
        lastFrameID = 0;
        ClearRecording();
        sign = signLabel;
    }
    // stop recording
    public void StopRecording(){
        recording = false;
        System.out.println(numFrames+" frames recorded.");
    }
    // clear the data we recorded
    public void ClearRecording(){
        records.clear();
        numFrames = 0;
    }
    public boolean HasData() {
        return !records.isEmpty();
    }
    public void SaveRecording() throws FileNotFoundException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
        String text = date.format(formatter);
        String fname = "..\\savedata\\"+text+".csv";
        System.out.println("Saving to "+fname+" ...");
        PrintWriter pw = new PrintWriter(new File(fname));
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        //TODO: include frame number
        //TODO: see weka api for this
        sb.append("time,");
        AddPosRotVel(sb, "arm");
        AddPosRotVel(sb, "hand");
        for (int i=1; i<=5; i++) {
            AddPosRotVel(sb, "finger"+i);
        }
        sb.append("sign");
        sb.append('\n');
        // append the data
        if (numFrames == 0) {
            System.out.println("no data to save!");
        }
        int dataPerFrame = 9;
        for (int i=0; i<numFrames; i++) {
            //TODO: id, frame num, time
            sb.append(i);
            sb.append(',');
            // 
            for (int j=0; j<dataPerFrame; j++) {
                sb.append(records.get(i*dataPerFrame+j).toString());
                sb.append(',');
            }
            sb.append(sign);
            sb.append('\n');
        }
        pw.write(sb.toString());
        pw.close();
        System.out.println("Saved!");
    }
    public void LoadRecording(){
        
    }
    void AddPosRotVel(StringBuilder sb, String name) {
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
        sb.append(name);
        sb.append("_vel_x,");
        sb.append(name);
        sb.append("_vel_y,");
        sb.append(name);
        sb.append("_vel_z,");
    }
}
