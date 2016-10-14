
package leapwekaasltest;

import com.leapmotion.leap.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class will process the frame and output to a file
 * @author Ian
 */
public class LeapSensor {
    public static long lastFrameID = 0;
    
    public static void ProcessFrame(Frame frame) {
        if (lastFrameID != frame.id()) {
            HandList hands = frame.hands();
            PointableList pointables = frame.pointables();
            FingerList fingers = frame.fingers();
            ToolList tools = frame.tools();
        }
        lastFrameID = frame.id();
        System.out.print("");
    }
    
    public static void SaveData() 
            throws FileNotFoundException {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
        String text = date.format(formatter);
        String fname = "..\\savedata\\"+text+".csv";
        System.out.println(fname);
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
        System.out.println("done!");
    }
    public static void AddPosRotVel(StringBuilder sb, String name) {
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
