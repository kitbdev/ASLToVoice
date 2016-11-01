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
 *
 * @author Ian
 */
public class LeapSensor {

    public long lastFrameID = 0;
    boolean recording = false;
    List<Float> records = new ArrayList<>();
    long recordStartTimeN = 0;
    List<Long> timeRecords = new ArrayList<>();
    int numFrames = 0;
    String sign = "";
    String savePath = "";
    PrintWriter openFile;
    boolean isFileOpen = false;
    private int rid;

    public void ProcessFrame(Frame frame) {
        if (recording) {
            if (lastFrameID != frame.id()) {
                // TODO: multiple hands
                //HandList hands = frame.hands();
                Hand hand = frame.hands().frontmost();
                if (hand.isValid()) {
                    //PointableList pointables = frame.pointables();
                    //ToolList tools = frame.tools();
                    // add time since start frame
                    long dNano = System.nanoTime() - recordStartTimeN;
                    timeRecords.add(dNano);
                    // currently arm, hand, and finger pos rot and vel
                    Arm arm = hand.arm();
                    if (!arm.isValid()) {
                        // uh oh
                        System.out.println("ERROR: NO ARM DETECTED");
                    }
                    Vector armPos = arm.elbowPosition();
                    records.add(armPos.getX());
                    records.add(armPos.getY());
                    records.add(armPos.getZ());
                    Vector armDir = arm.direction();
                    records.add(armDir.getX());
                    records.add(armDir.getY());
                    records.add(armDir.getZ());
                    // no arm velocity

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
                    FingerList fingers = hand.fingers();
                    for (int i = 0; i < 5; i++) {
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
                        Vector fVel = f.tipVelocity();
                        records.add(fVel.getX());
                        records.add(fVel.getY());
                        records.add(fVel.getZ());
                    }
                    numFrames++;
                }
            }
        }
        lastFrameID = frame.id();
        System.out.println("f"+numFrames);
    }

    // start recording with the leap
    public void StartRecording(String signLabel) {
        if (!isFileOpen) {
            System.out.println("start a file first!");
            return;
        }
        recording = true;
        lastFrameID = 0;
        ClearRecording();
        if (signLabel == "") {
            // use the last sign 
        } else {
            sign = signLabel;
        }
        rid += 1;
        recordStartTimeN = System.nanoTime();
    }

    // stop recording
    public void StopRecording() {
        recording = false;
        System.out.println(numFrames + " frames recorded.");
    }

    // clear the data we recorded
    public void ClearRecording() {
        records.clear();
        timeRecords.clear();
        numFrames = 0;
        sign = "";
        rid -= 1;
    }

    public boolean HasData() {
        return !records.isEmpty();
    }

    public void StartDataFile(boolean isTrainingData) throws FileNotFoundException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
        String text = date.format(formatter);
        String fname = "..\\savedata\\trainingdata\\";
        if (isTrainingData) {
            fname += "td_";
        }
        // TODO: add types of classes to path?
        fname += text + ".csv";
        savePath = fname;
        rid = 0;
        System.out.println("New file is " + fname + "");
        openFile = new PrintWriter(new File(fname));
        isFileOpen = true;
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        //TODO: see weka api for what to include
        sb.append("time,");
        sb.append("cur_frame,");
        // TODO: relative time? time since last frame
        sb.append("total_frames,");
        AddPosRot(sb, "arm");
        AddPosRotVel(sb, "hand");
        for (int i = 1; i <= 5; i++) {
            AddPosRotVel(sb, "finger" + i);
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

    // save all recorded data
    public void SaveRecording() {
        if (!isFileOpen) {
            System.out.println("File not open!");
            return;
        }
        StringBuilder sb = new StringBuilder();
        // append the data
        if (numFrames == 0) {
            System.out.println("no data to save!");
            return;
        }
        int dataPerFrame = records.size() / numFrames;
        for (int i = 0; i < numFrames; i++) {
            sb.append(rid * numFrames + i);// id
            sb.append(',');
            sb.append(timeRecords.get(i));// time since start of recording
            sb.append(',');
            sb.append(i);// current frame
            sb.append(',');
            sb.append(numFrames);// total frames of this sign
            sb.append(',');
            for (int j = 0; j < dataPerFrame; j++) {
                sb.append(records.get(i * dataPerFrame + j).toString());
                sb.append(',');
            }
            if (sign != "") {
                sb.append(sign);
            }
            sb.append('\n');
        }
        openFile.write(sb.toString());
        System.out.println("Saved!" + sign);
        ClearRecording();
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
