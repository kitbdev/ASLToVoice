
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
                    
                    numFrames++;
                }
            } 
        }
        lastFrameID = frame.id();
        System.out.print("");
    }
    // start recording with the leap
    public void StartRecording(){
        recording = true;
        lastFrameID = 0;
        ClearRecording();
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
        // TODO: add data to file, over some # of frames(?)
        
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
