
package leapwekaasl;

import com.leapmotion.leap.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import java.util.Scanner;
import java.io.IOException;
//import jwsfilechooser;

public class LeapWekaASLTest {
    public static Scanner scanner = new Scanner(System.in);
    public static Controller controller = new Controller();
    public static LeapSensor leapSensor = new LeapSensor();
    
    public static void main(String[] args) 
                throws InterruptedException, IOException, Exception {
        if(controller.isConnected()) {
            System.out.println("Controller connected!");
        } else {
            System.out.println("No controller connected!");
        }
        //DataSource src = new DataSource("file.csv");
        Menu();
        System.out.println("Exiting...");
    }
    public static void Menu() throws IOException {
        boolean running = true;
        //boolean valid = false;
        while (running) {
            boolean connected = controller.isConnected();
            boolean hasData = leapSensor.HasData();
            //while (!valid) {
                // prompt
                System.out.print("\nPress a key to make a selection:\n");
                if (connected) {
                    System.out.print("[R]ecord, ");
                }
                if (hasData) {
                    System.out.print("[S]ave, ");
                }
                System.out.print("[L]oad, ");
                System.out.print("or [E]xit:\n");
                
                // get input
                String sIn = scanner.next();
                char s = sIn.toLowerCase().charAt(0);
                //valid = true;
                if (s == 'r') {
                    // record unlabbelled data
                    if (connected) {                     
                        System.out.println(s+" is not implemented yet");   
                        try {
                            Record();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("controller not connected!");   
                    }
                } else if (s == 's') {
                    // do something
                    leapSensor.SaveRecording();
                    if (hasData) {
                        //System.out.println(s+" is not implemented yet");
                        //leapSensor.SaveRecording();
                    } else {
                        System.out.println("No Data!");
                    }
                } else if (s == 'l') {
                    // do something
                    System.out.print(s+" is not implemented yet");
                    //System.out.println("Enter the file location");
                    // TODO: what does this actually do?
                    // for weka?
                    
                } else if (s == 'c') {
                    // load data and classify it
                    Instances trainData = new Instances(new BufferedReader(
                            new FileReader(leapSensor.savePath)));
                    trainData.setClassIndex(trainData.numAttributes() - 1);
                        
                    // TODO: revamp structure
                    // create new traindata file
                    //  set training data class (sign name)
                    //  record training data with that name
                    //   re-record or save to the file
                    //  end file or record more data
                    // load trainingdata file
                    //  select classifier?
                    //  train trainingdata with classifier
                    // record sign to identify
                    //  test imm. (no save needed? try to load directly into weka)
                            
                    //Instances labeled = new Instances();
                } else if (s == 'e') {
                    running = false;
                    break;
                } else {
                    //valid = false;
                    System.out.println(s+" is not a valid selection.");
                }
            //}
        }
    }
    public static void Record() throws IOException, InterruptedException {
        // have loop and stuff
        System.out.println("Press Enter to stop recording");
        leapSensor.ClearRecording();
        leapSensor.StartRecording("Sign"); // replace with some sign
        
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;
        long pollRate = 1/100;// 1 poll every .1 seconds
        // TODO: only loop when looking for data
        boolean exit = false;
        while(!exit) {
            long frameStart = System.currentTimeMillis();
            if (System.in.available() > 0) {
                exit = true;
                leapSensor.StopRecording();
                break;
            }
            Update();
            long dt = System.currentTimeMillis() - frameStart; // time that this update took
            long timeLeftThisFrame = pollRate - dt;
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
    }
    public static void Update() {
        Frame frame = controller.frame();
        leapSensor.ProcessFrame(frame);
    }
}
