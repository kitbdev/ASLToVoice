
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
    String savePath = "";
    PrintWriter openFile;
    boolean isFileOpen = false;
    public void ProcessFrame(Frame frame) {
        if (recording) {
            if (lastFrameID != frame.id()) {
                //HandList hands = frame.hands();
                //what to do with multiple hands?
                Hand hand = frame.hands().frontmost();
                if (hand.isValid()) {
                    //LocalDateTime timeNow = LocalDateTime.now();
                    
                    //hand.direction();
                    // TODO: track by id?
                    //PointableList pointables = frame.pointables();
                    //ToolList tools = frame.tools();
                    FingerList fingers = hand.fingers();
                    // TODO: add numbers to records
                    // currently arm, hand, and finger pos rot and vel
//                    Arm arm = hand.arm();
//                    Vector armPos = arm.elbowPosition();
//                    records.add(armPos.getX());
//                    records.add(armPos.getY());
//                    records.add(armPos.getZ());
//                    Vector armDir = arm.direction();
//                    records.add(armDir.getX());
//                    records.add(armDir.getY());
//                    records.add(armDir.getZ());
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
//                    Vector handVel = hand.palmVelocity();
//                    records.add(handVel.getX());
//                    records.add(handVel.getY());
//                    records.add(handVel.getZ());
                    // fingers
                    for (int i=0; i<5; i++) {
                        Finger f = hand.finger(i);
                        // finger pos relative to hand
                        Vector fPos = f.tipPosition().minus(handPos);
                        records.add(fPos.getX());
                        records.add(fPos.getY());
                        records.add(fPos.getZ());
                        Vector fDir = f.direction();
                        records.add(fDir.getX());
                        records.add(fDir.getY());
                        records.add(fDir.getZ());
//                        Vector fVel = f.tipVelocity();// TODO: do we even want velocity?
//                        records.add(fVel.getX());
//                        records.add(fVel.getY());
//                        records.add(fVel.getZ());
                    }
                    numFrames++;
                }
            } 
        }
        lastFrameID = frame.id();
    }
    // start recording with the leap
    public void StartRecording(String signLabel){
        recording = true;
        lastFrameID = 0;
        ClearRecording();
        if (isFileOpen && signLabel=="") {
            // use the last sign 
        } else {
            sign = signLabel;
        }
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
        sign = "";
    }
    public boolean HasData() {
        return !records.isEmpty();
    }
    public void StartDataFile(boolean isTrainingData) throws FileNotFoundException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
        String text = date.format(formatter);
        String fname = "..\\savedata\\";
        if (isTrainingData) {
            fname += "td_";
        }
        fname += text+".csv";
        savePath = fname;
        System.out.println("New file is "+fname+"");
        openFile = new PrintWriter(new File(fname));
        isFileOpen = true;
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        //TODO: include frame number ?
        //TODO: see weka api for what to include
        sb.append("time,");
       // AddPosRotVel(sb, "arm");
        AddPosRotVel(sb, "hand");
        for (int i=1; i<=5; i++) {
            AddPosRotVel(sb, "finger"+i);
        }
        if (isTrainingData) {
            sb.append("sign");
        }
        sb.append('\n');
        openFile.write(sb.toString());
    } 
    public void FinishDataFile() {
        openFile.close();
        isFileOpen = false;
        //System.out.println("Finished file!");
    }
    public void SaveRecording() {
        // TODO: test if file is open
        if (!isFileOpen) {
            System.out.println("File not open!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        // append the data
        if (numFrames == 0) {
            System.out.println("no data to save!");
        }
        LocalDateTime date = LocalDateTime.now();
        int dataPerFrame = 9;
        for (int i=0; i<numFrames; i++) {
            //TODO: id, frame num, time
            sb.append(i);
            sb.append(',');
            sb.append(date.getMinute());// get min:sec:nano
            sb.append(date.getSecond());
            sb.append(date.getNano());
            sb.append(',');
            for (int j=0; j<dataPerFrame; j++) {
                sb.append(records.get(i*dataPerFrame+j).toString());
                sb.append(',');
            }
            if (sign != ""){
                sb.append(sign);
            }
            sb.append('\n');
        }
        openFile.write(sb.toString());
        System.out.println("Saved!"+sign);
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
//        sb.append(name);
//        sb.append("_vel_x,");
//        sb.append(name);
//        sb.append("_vel_y,");
//        sb.append(name);
//        sb.append("_vel_z,");
    }
}
