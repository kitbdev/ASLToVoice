package asltovoice;

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
    private int rid, totalId=0;

    public boolean ProcessFrame(Frame frame) {
        if (recording) {
            if (lastFrameID != frame.id()) {
                HandList hands = frame.hands();
                if (hands.count() > 0) {
                    // add time since start frame
                    long dNano = System.nanoTime() - recordStartTimeN;
                    timeRecords.add(dNano);
                    // add right hand then left hand to records, 0s if not available
                    Hand hand = hands.get(0);
                    if (hand.isRight()) {
                        // this is the right hand
                        RecordHand(hand);
                        if(hands.count() > 1) {
                            Hand lhand = hands.get(1);
                            RecordHand(lhand);
                        } else {
                            RecordHand(null);
                        }
                    } else {
                        // this is the left hand
                        if(hands.count() > 1) {
                            Hand rhand = hands.get(1);
                            RecordHand(rhand);
                        } else {
                            RecordHand(null);
                        }
                        RecordHand(hand);
                    }
                    numFrames++;
                }
            }
        }
        lastFrameID = frame.id();
        //System.out.println("f"+numFrames);
        return numFrames != 0;
    }
    void RecordHand(Hand hand) {
        if (hand==null) {
            // there are 60 records added
            for (int i=0; i<60; i++) {
                records.add(0f);
            }
            return;
        }
        // currently arm, hand, and finger pos rot and vel
        Arm arm = hand.arm();
        if (!arm.isValid()) {
            for (int i=0; i<6; i++) {
                records.add(0f);
            }
        } else {
            Vector armPos = arm.elbowPosition();
            records.add(armPos.getX());
            records.add(armPos.getY());
            records.add(armPos.getZ());
            Vector armDir = arm.direction();
            records.add(armDir.getX());
            records.add(armDir.getY());
            records.add(armDir.getZ());
            // no arm velocity
        }
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
            Finger f = fingers.get(i);
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
    }
    public boolean HandAvailable(Frame frame) {
        Hand hand = frame.hands().frontmost();
        return hand.isValid();
    }

    // start recording with the leap
    public void StartRecording(String signLabel) {
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
    public void StartDataFile(boolean isTrainingData, String saveLoc) throws FileNotFoundException {
        StartDataFile(isTrainingData, saveLoc, "_");
    }
    public void StartDataFile(boolean isTrainingData, String saveLoc, String filename) throws FileNotFoundException {
        String fname = "";
        if (filename=="_") {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
            String text = date.format(formatter);
            fname = saveLoc;
            if (isTrainingData) {
                fname += "td_";
            }
            // TODO: add types of classes to path?
            fname += text + ".csv";
        }
        else {
            fname = filename;
        }
        savePath = fname;
        rid = 0;
        totalId = 0;
        System.out.println("New file is: ");
        System.out.println(fname);
        openFile = new PrintWriter(new File(fname));
        isFileOpen = true;
        StringBuilder sb = new StringBuilder();
        sb.append("id,");
        //TODO: see weka api for what to include
        sb.append("time,");
        sb.append("cur_frame,");
        // TODO: relative time? time since last frame
        sb.append("total_frames,");
        AddPosRot(sb, "rarm");
        AddPosRotVel(sb, "rhand");
        for (int i = 1; i <= 5; i++) {
            AddPosRotVel(sb, "rfinger" + i);
        }
        AddPosRot(sb, "larm");
        AddPosRotVel(sb, "lhand");
        for (int i = 1; i <= 5; i++) {
            AddPosRotVel(sb, "lfinger" + i);
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
        totalId = 0;
        System.out.println("Finished file "+savePath);
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
            sb.append(totalId++);// id //rid * numFrames + i + numFrames
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
        System.out.println("Saved " + sign);
        ClearRecording();
    }
    public float LoadDataAt(int recordsIndex){
        //TODO: average out data?
        return records.get(recordsIndex);
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
