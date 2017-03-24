package asltovoice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
Start point of application
creates other classes
contains Command line interface
saves and loads csv files
record loop
*/
public class ASLtoVoiceMain {

    public static Scanner scanner = new Scanner(System.in);
    public static LeapSensor leapSensor = new LeapSensor();
    public static GestureInterpreter gestureInterpreter = new GestureInterpreter();
    
    public static SignData curSign = new SignData();
    
    public static boolean devMode = true; // enables saving to a file for recording training data
    public static boolean running = true;
    public static long POLLRATE = 50;//ms
    public static String saveLoc = "../savedata/";

    public static void main(String[] args) {
        while (running) {
            CLI();
        }
    }
    static void CLI() {
        System.out.println("Enter a command:");
        String[] com = scanner.nextLine().toLowerCase().trim().split(" ");
//        System.out.println(">"+com.length+",");
        if ("exit".equals(com[0])) {
            running = false;
        }
        if ("record".equals(com[0])) {
            float recDelay = 0;
            if (com.length<2) {
                System.out.println("need a class name to record");
                return;
            }
            if (com.length>2) {
                try {
                    recDelay = Float.parseFloat(com[1]);
                } catch (Exception e) {}
            }
            RecordIn(com[1], recDelay);
        }
        if ("save".equals(com[0])) {
            String fname = "";
            if (com.length > 1) {
                fname = com[1];
            }
            Save(fname);
        }
    }
    static void RecordIn(String sign, float recordIn) {
        if (!leapSensor.ControllerConnected()){
            System.out.println("need controller!");
            return;
        }
        if (recordIn > 0) {
            for (int i = 0; i < recordIn; i++) {
                System.out.println((recordIn - i) + "...");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            try {
                Record(sign);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    static void Record(String sign) throws InterruptedException, IOException {
        System.out.println("Recording " + sign);
        leapSensor.StartRecording(sign);
        while (true) {
            long frameStart = System.currentTimeMillis();
            
            if (System.in.available()>0) {
                break;
            }
            
            boolean gotFrame = leapSensor.RecordFrame();
            if (gotFrame) {
                if (gestureInterpreter.IsSignOver(leapSensor.curFrame)) {
                    gestureInterpreter.ClassifyGesture(curSign);
                    //break; // TODO continuous sign detection
                } else {
                    curSign.AddFrame(leapSensor.curFrame);
                }
                //System.out.print(framesLeft+", \n");
                System.out.print("\n");
            } else {
                System.out.println("Hand not detected! No data recorded.");
                while (!leapSensor.HandAvailable()) {
                    System.out.print(".");
                    Thread.sleep(100);
                }
                // TODO: if we didnt get frame this should stop ?
            }
            
            long timeTaken = System.currentTimeMillis() - frameStart;
            long timeLeftThisFrame = POLLRATE - timeTaken;
            if (timeLeftThisFrame < 0) {
                timeLeftThisFrame = 0;
            }
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        System.out.println("Done recording");
    }
    static void Save(String fname) {
        StringBuilder sb = new StringBuilder();
//        FrameData[] frames = hmm.GetFrames();

        //create file
        String filename = "";
        filename += saveLoc;
        if ("".equals(fname)) {
            filename += "td_";
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
            filename += date.format(formatter);
            // TODO: add types of classes to path?
        } else {
            filename += fname;
        }
        filename += ".csv";
        
        System.out.println("New file is: ");
        System.out.println(filename);
        PrintWriter openFile;
        try {
            openFile = new PrintWriter(new File(filename));
        } catch (FileNotFoundException e) {
            System.out.println("creating file failed."+e.getMessage());
            return;
        }
        // add data to the file
        // add header line
        sb.append(curSign.frames.get(0).GetHeaderLine());
        sb.append('\n');
        
        sb.append(curSign.GetAllData());// string one
//        int numFrames = frames.length;
//        for (int i = 0; i < numFrames; i++) {
//            sb.append(frames[i].GetData());
//            sb.append('\n');
//        }
        openFile.write(sb.toString());
        openFile.close();
//        hmm.Clear();
        System.out.println("Saved!");
    }
    static void Load(String fn) {
        try {
            Path filename = Paths.get(saveLoc, fn);
            List<String> lines = Files.readAllLines(filename, Charset.defaultCharset());
            FrameData[] frames;//...
        } catch (IOException ex) {
            System.out.println(ex);
            return;
        }
    }
}
