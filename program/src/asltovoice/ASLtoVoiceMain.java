package aslToVoice;

import asltovoice.FrameData;
import asltovoice.HMM;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ASLtoVoiceMain {

    public static Scanner scanner = new Scanner(System.in);
    public static HMM hmm = new HMM();
    public static LeapSensor leapSensor = new LeapSensor();
    
    public static boolean devMode = true; // enables saving to a file for recording training data
    public static boolean running = true;
    public static long POLLRATE = 50;//ms
    public static String saveLoc = "../savedata/";

    int main(String[] args) {
        while (running) {
            CLI();
        }
        return 0;
    }
    void CLI() {
        System.out.println("Enter a command:");
        String[] com = scanner.next().toLowerCase().split(" ");
        if (com[0] == "exit") {
            running = false;
        }
        if (com[0] == "record") {
            float recDelay = 0;
            if (com.length<1) {
                System.out.println("need a class name to record");
            }
            if (com.length>2) {
                try {
                    recDelay = Float.parseFloat(com[1]);
                } catch (Exception e) {}
            }
            RecordIn(com[1], recDelay);
        }
        if (com[0] == "save") {
            if (com.length > 1) {
                
            }
        }
    }
    void RecordIn(String sign, float recordIn) {
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
            Record(sign);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    void Record(String sign) throws InterruptedException {
        System.out.println("Recording " + sign);
        hmm.Clear();
        leapSensor.StartRecording(sign);
        while (true) {
            long frameStart = System.currentTimeMillis();
            
            if (hmm.SignEnded()) {
                break;
            }
            
            boolean gotFrame = leapSensor.RecordFrame();
            if (!gotFrame) {
                System.out.println("Hand not detected! No data recorded.");
                while (!leapSensor.HandAvailable()) {
                    System.out.print(".");
                    Thread.sleep(100);
                }
                // TODO: if we didnt get frame this should stop ?
                System.out.print("\n");
            } else {
                hmm.Analyze(leapSensor.GetCurFrame());
                //System.out.print(framesLeft+", \n");
            }
            
            long timeTaken = System.currentTimeMillis() - frameStart;
            long timeLeftThisFrame = POLLRATE - timeTaken;
            if (timeLeftThisFrame < 0) {
                timeLeftThisFrame = 0;
            }
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        leapSensor.StopRecording();
        System.out.println("Done recording");
    }
    void Save() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        FrameData[] frames = hmm.GetFrames();

        //create file
        String filename = "";
        filename += saveLoc;
        filename += "td_";
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
        filename += date.format(formatter);
        // TODO: add types of classes to path?
        filename += ".csv";
        System.out.println("New file is: ");
        System.out.println(filename);
        PrintWriter openFile = new PrintWriter(new File(filename));
        
        // add data to the file
        // add header line
        frames[0].GetHeaderLine();
        sb.append('\n');
        
        int numFrames = frames.length;
        for (int i = 0; i < numFrames; i++) {
            sb.append(frames[i].GetData());
            sb.append('\n');
        }
        openFile.write(sb.toString());
        openFile.close();
        hmm.Clear();
        System.out.println("Saved!");
    }
    void Load() {

    }
}
