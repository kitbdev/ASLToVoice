package asltovoice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.ArrayList;
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
    public static TTS tts = new TTS();

    public static SignData curSign = new SignData();
    //public static ArrayList<SignData> allSigns = new ArrayList<SignData>();

//    public static boolean devMode = true; // enables saving to a file for recording training data
    public static boolean running = true;
    public static long POLLRATE = 50;//ms
    public static String saveLoc = "../savedata/";
    public static boolean connected;

    public static void main(String[] args) {
        tts.allocate();
//        tts.mute = true;
        //ml.tts = tts;
        while (running) {
            connected = leapSensor.ControllerConnected();
            CLI();
        }
        System.out.println("Exiting...");
        tts.deallocate();
    }

    public static void CLI() {
        System.out.println("...");
        System.out.println("Leap is " + (connected ? "" : "not ") + "connected.");
        System.out.println("exit, record (training data),\n"
                + "clear(remove all signs), test, load, view (recorded sign), or save(recorded data)");
        System.out.println("Enter a command:");
        String[] com = scanner.nextLine().toLowerCase().trim().split(" ");
//        System.out.println(">"+com.length+",");
        String command = com[0].trim();
//        System.out.println("\""+command+"\" entered");
        if ("exit".equals(command) || "e".equals(command)) {
            running = false;
            return;
        }
        if ("record".equals(command) || "r".equals(command)) {
            // record training data
            if (!connected) {
                System.out.println("Connect to the leap motion first!");
                return;
            }
            float recDelay = 0;
            if (com.length < 2) {
                System.out.println("Enter a class name to record!");
                return;
            }
            if (com.length > 2) {
                try {
                    recDelay = Float.parseFloat(com[1]);
                } catch (Exception e) {
                }
            }
            RecordIn(com[1], recDelay);
        }
        if ("clear".equals(command) || "c".equals(command)) {
            curSign.Clear();
            System.out.println("Cleared all signs");
        }
        if ("test".equals(command) || "t".equals(command)) {
            if (!connected) {
                System.out.println("Connect to the leap motion first!");
                return;
            }
            if (!gestureInterpreter.hasData) {
                if (curSign.hasEnoughFrames()) {
                    System.out.println("Load some data first!");
                    return;
                }
                // attempt to use the data we just recorded
                System.out.println("Saving and loading current sign data first...");
                Load(Save(""));
            }
            if (gestureInterpreter.needsRebuilding) {
                System.out.println("Building model first, retry when finished...");
                gestureInterpreter.BuildModel();
                return;
            }
            try {
                // start recording and test that data continuously
                RecordTest();
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (InterruptedException ex) {
                System.out.println(ex);
            }
        }
        if ("load".equals(command) || "l".equals(command)) {
            String fname = "";
            if (com.length > 1) {
                fname = com[1];
            } else {
                System.out.println("Enter a filename to load!");
                return;
            }
            Load(fname);
        }
        if ("view".equals(command) || "v".equals(command)) {
            System.out.println(curSign.GetAllData());
            System.out.println();
        }
        if ("save".equals(command) || "s".equals(command)) {
            String fname = "";
            if (com.length > 1) {
                fname = com[1];
            }
            Save(fname);
        }
        if ("say".equals(command)) {
            if (com.length > 1) {
                Say(com[1]);
            }
        }
        if ("classifier".equals(command)) {
            if (com.length > 1) {
                gestureInterpreter.SetClassificationType(Integer.parseInt(com[1]));
            }
        }
    }

    // starts recordTrain in specified seconds
    public static void RecordIn(String sign, float recordIn) {
        if (!leapSensor.ControllerConnected()) {
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
            RecordTrain(sign);
        } catch ( Exception ex) {
            System.out.println(ex);
        }
    }

    // records data to curSign from leap until the sign end is detected
    public static void RecordTrain(String sign) throws InterruptedException, IOException {
        if (!connected) {
            System.out.println("Connect to a leap sensor first!");
        }
        System.out.println("Recording sign:" + sign);
        curSign.Clear();
        curSign.sign = sign;
        leapSensor.StartRecording();
        System.out.println("Stop moving, remove hand, or press any key to finish");
        while (!leapSensor.HandAvailable()) {
            System.out.println("Hand not detected!");
            Thread.sleep(100);
        }
        while (true) {
            long frameStart = System.currentTimeMillis();
            if (System.in.available() > 0) {
                break;
            }

            boolean gotFrame = leapSensor.RecordFrame();

            if (gotFrame) {
                curSign.AddFrame(leapSensor.curFrame);
                if (gestureInterpreter.IsSignOver(curSign)) {
                    //curSign.RemoveLast(gestureInterpreter.maxNoMovementFrames);
                    break;
                }
                //System.out.print("\n");
            } else {
                System.out.println("Hand not detected!");
                // if hand still not detected after .5 seconds, stop recording
                Thread.sleep(500);
                if (!leapSensor.HandAvailable()) {
                    System.out.println("Hand still not detected; Stopping recording.");
                    break;
                }
            }

            long timeTaken = System.currentTimeMillis() - frameStart;
            long timeLeftThisFrame = POLLRATE - timeTaken;
            if (timeLeftThisFrame < 0) {
                timeLeftThisFrame = 0;
            }
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
//        System.out.println("last sign frame");
//        int lastSign = allSigns.size() -1;
//        int lastSignLastFrame = allSigns.get(lastSign).frames.size()-1;
//        System.out.println(allSigns.get(lastSign).frames.get(lastSignLastFrame).handRot.x);
        System.out.println("Done recording");
    }

    // records and tests for a sign 
    public static String RecordTest() throws InterruptedException, IOException {
        if (!connected) {
            System.out.println("Connect to a leap sensor first!");
            return "";
        }
        System.out.println("Recording signs to test");
        curSign.Clear();
        leapSensor.StartRecording();
        System.out.println("Press any key to exit");
        while (true) {
            long frameStart = System.currentTimeMillis();
            if (System.in.available() > 0) {
                break;
            }
            boolean gotFrame = leapSensor.RecordFrame();
            if (gotFrame) {
                curSign.AddFrame(leapSensor.curFrame);
                if (gestureInterpreter.IsSignOver(curSign)) {
                    break;
                }
            } else {
                System.out.println("Hand not detected!");
                // wait until hand is detected
                while (!leapSensor.HandAvailable()) {
                    Thread.sleep(1000);
                    if (System.in.available() > 0) {
                        break;
                    }
                }
            }

            long timeTaken = System.currentTimeMillis() - frameStart;
            long timeLeftThisFrame = POLLRATE - timeTaken;
            if (timeLeftThisFrame < 0) {
                timeLeftThisFrame = 0;
            }
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        System.out.println("Done recording");
        if (curSign.frames.size() < curSign.normalizedNumFrames) {
            System.out.println("Not enough frames! " + curSign.frames.size() + " / " + curSign.normalizedNumFrames);
            return "ERROR: Not enough frames";
        }
        String guessSign = gestureInterpreter.ClassifyGesture(curSign.GetNormalizedData());
        if (guessSign.contains("ERROR")) {
            // uh oh
        }
        System.out.println("Did you sign: " + guessSign + "?");
        tts.speak(guessSign);
        curSign.Clear();
        return guessSign;
    }

    // save all signs to a file
    public static String Save(String fname) {
        // get the filename
        String filename = "";
        if (fname.contains("`")) {
            filename += saveLoc;
            fname = fname.replace("`","");
        }
        if ("".equals(fname)) {
            // default filename is td_[time]_[date].csv
            filename += "td_";
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
            filename += date.format(formatter);
            filename += "_" + curSign.sign;
        } else {
            filename += fname;
        }
        if (!filename.contains(".csv")) {
            filename += ".csv";
        }
        // create the file
        try {
            boolean hasFile = new File(filename).exists();
            if (hasFile)
                System.out.println("Saving to " + filename);
            else
                System.out.println("Appending to " + filename);
            FileWriter openFile = new FileWriter(filename,true); //the true will append the new data
            
            // add data to the file
            StringBuilder sb = new StringBuilder();
            if (!hasFile) {
                // add header line
                sb.append(curSign.GetNormalizedHeaderLine());
                sb.append('\n');
            }
            // add data
            sb.append(curSign.GetNormalizedDataString());
            sb.append("\n");

            openFile.write(sb.toString());
            openFile.close();
            System.out.println("Save finished");
            return filename;
        } catch (Exception e) {
            System.out.println("Creating file failed." + e.getMessage());
            return "ERROR";
        }
    }
    public static void Load(String fname) {
        String filename = "";
        if (fname.contains("`")) {
            filename += saveLoc;
            fname = fname.replace("`","");
        }
        filename += fname;
        if (!filename.contains(".csv")) {
            filename += ".csv";
        }
        System.out.println("Loading from " + filename);
        gestureInterpreter.LoadData(filename);
    }
    public static void Say(String s) {
        tts.speak(s);
    }
}
