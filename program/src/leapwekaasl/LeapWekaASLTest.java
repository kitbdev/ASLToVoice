
package leapwekaasl;

import com.leapmotion.leap.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
//import weka.classifiers.

//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileReader;
//import java.io.FileWriter;

import java.util.Scanner;
import java.io.IOException;
//import jwsfilechooser;

public class LeapWekaASLTest {
    public static Scanner scanner = new Scanner(System.in);
    public static Controller controller = new Controller();
    public static LeapSensor leapSensor = new LeapSensor();
    
    public static void main(String[] args) 
                throws InterruptedException, IOException {
        if(controller.isConnected()) {
            System.out.println("Controller connected!");
        } else {
            System.out.println("No controller connected!");
        }
        try {
            Menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting...");
    }
    public static void Menu() throws IOException, Exception {
        boolean running = true;
        boolean connected, hasRecording = false;
        
        String trainingDataLoc = null;
        Instances trainingData;
        
        boolean isRecordingTrainingData = false;
        
        //boolean canRecordTrainingData;
        
        while (running) {
            connected = controller.isConnected();
            // prompts
            System.out.print("\nPress a key and enter to make a selection:\n");
            if (connected) {
                if (!isRecordingTrainingData) {
                    System.out.print("[t] Record new training data, ");
                    System.out.print("[l] Load training data, ");
                    System.out.print("[c] Change classifier, ");
                    System.out.print("[d] Train on data, ");
                    System.out.print("[n] Record data to classify, ");
                } else {
                    System.out.print("[n] Start Recording with new label, ");
                    System.out.print("[r] Start Recording with same label, ");
                    hasRecording = leapSensor.HasData();
                    if (hasRecording) {
                        System.out.print("[s] Save Recording, ");
                        System.out.print("[c] Clear Recording, ");
                    }
                    System.out.print("[t] Stop Recording training data, ");
                }
            }
            System.out.print("or [E]xit:\n");

            // get input
            String sIn = scanner.next();
            char s = sIn.toLowerCase().charAt(0);
            
            if (!isRecordingTrainingData) {
                if (s == 't') {
                    isRecordingTrainingData = true;
                    leapSensor.StartDataFile(true);
                }
                if (s == 'l') {
                    
                }
                if (s == 'c') {
                    System.out.println("Current Classifier is: ");
                    
                }
                if (s == 'd') {
                    System.out.println("Training data on recorded training data");
                    System.out.println("Using (classifier)");
                    
                    // get training data
                    DataSource src = new DataSource(trainingDataLoc);
                    trainingData = src.getDataSet();
                    // set class index because this is not an ARFF
                    if (trainingData.classIndex() == -1) {
                        trainingData.setClassIndex(trainingData.numAttributes() - 1);
                    }
                    // remove id and time
                    trainingData.deleteAttributeAt(1);
                    trainingData.deleteAttributeAt(0);
                    
                    // TODO: train data with classifier
                    // TODO: check file to make sure there is enough data?
                    // TODO: testing data needed to find accuracy ?
                    
                }
                if (s == 'n') {
                    try {
                        Record("");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // TODO: weka
                    System.out.println("The sign you signed is ");
                    
                }
            } else {
                if (s == 'r' || s == 'n'){
                    String signName = "";
                    if (s == 'n') {
                        System.out.println("Enter the sign name: ");
                        signName = scanner.next();
                    }
                    try {
                        Record(signName);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (hasRecording) {
                    if (s == 's') {
                        System.out.println("Saving last recorded data");
                        leapSensor.SaveRecording();
                    }
                    if (s == 'c') {
                        System.out.println("Clearing last recorded data");
                        leapSensor.ClearRecording();
                    }
                }
                if (s == 't') {
                    System.out.println("Finishing recording of training data");
                    leapSensor.FinishDataFile();
                    isRecordingTrainingData = false;
                    trainingDataLoc = leapSensor.savePath;
                }
            }
            if (s == 'e') {
                running = false;
            }
        }
    }
    public static void Record(String sign) throws IOException, InterruptedException {
        // pass in "" to record non-labelled
        System.out.println("Press Enter to stop recording");
        leapSensor.ClearRecording();
        leapSensor.StartRecording(sign); // replace with some sign
        
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;
        long pollRate = 1/100;// 1 poll every .1 seconds
        // only loop when looking for data
        System.out.println("Recording");
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
        System.out.println("Stopped recording");
    }
    public static void Update() {
        Frame frame = controller.frame();
        leapSensor.ProcessFrame(frame);
    }
}
