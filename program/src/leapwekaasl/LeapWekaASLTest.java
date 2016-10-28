
package leapwekaasl;

import com.leapmotion.leap.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Classifier;

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
    
    public static long POLLRATE = 100;//1.0/100.0;// 1 poll every .1 seconds in ms
        
    public static Classifier classifier;
    
    public static void main(String[] args) 
                throws InterruptedException, IOException {
        if(controller.isConnected()) {
            System.out.println("Controller connected!");
        } else {
            System.out.println("No controller connected!");
        }
        classifier = new J48();
        try {
            Menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting...");
    }
    public static void Menu() throws IOException, Exception {
        boolean running = true;
        boolean isConnected, hasRecording = false;
        
        String trainingDataLoc = null;
        Instances trainingData;
        
        boolean isRecordingTrainingData = false;
        
        int framesToRecord = 0;
        
        while (running) {
            isConnected = controller.isConnected();
            System.out.print("\n");//Classifier: ? ");
            if (framesToRecord >= 0 ) {
                System.out.print("Will Record for: "+framesToRecord + " frames, or "
                         + (float)framesToRecord/POLLRATE+" seconds.");
            } else {
                System.out.print("Recording until keypress.");
            }
            System.out.print("\n");
            // prompts
            System.out.print("\nPress a key and enter to make a selection:\n");
            
                if (!isRecordingTrainingData) {
                    if (isConnected) System.out.print("[t] Record new training data, ");
                    System.out.print("[l] Load training data, ");
                    System.out.print("[c] Change classifier, ");
                    System.out.print("[d] Train on data, ");
                    if (isConnected) System.out.print("[n] Record data to classify, ");
                } else if (isConnected) {
                    System.out.print("[n] Start Recording with new label, ");
                    System.out.print("[r] Start Recording with same label, ");
                    System.out.print("[f] Record for n frames, ");
                    System.out.print("[n] Record for n seconds, ");
                    System.out.print("[i] Record untill keypress, ");
                    //System.out.print("[f] Start Recording for n fr, ");
                    hasRecording = leapSensor.HasData();
                    if (hasRecording) {
                        System.out.print("[s] Save Recording, ");
                        System.out.print("[c] Clear Recording, ");
                    }
                    System.out.print("[t] Stop Recording training data, ");
                } else {
                   isRecordingTrainingData = false; 
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
                    System.out.println("Current Classifier is: " + classifier.toString());
                    
                }
                if (s == 'd') {
                    System.out.println("Training data on recorded training data");
                    System.out.println("Using (classifier)");
                    
                    // TODO: move this to load training data with 
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
                        Record("",framesToRecord);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // TODO: weka stuff
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
                        Record(signName,framesToRecord);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (s == 'i') {
                    framesToRecord = 0;
                    System.out.println("Will record until a key is pressed.");
                }
                if (s == 'f' || s == 'n') {
                    System.out.print("\nEnter the number of ");
                    if (s == 'n') {
                        System.out.print("seconds");
                    }else{
                        System.out.print("frames");
                    }
                    System.out.print(" to record for: ");
                    int n = scanner.nextInt();
                    if (s == 'n') {
                        n*=POLLRATE;
                    }
                    framesToRecord = n;
                    System.out.println("Will record for "+framesToRecord+" frames.");
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
    public static void Record(String sign, int nframes) throws IOException, InterruptedException {
        // pass in "" to record non-labelled
        if (nframes==0) {
            System.out.println("Press Enter to stop recording");
        } else {
            System.out.println("Recording for "+nframes+", or "+nframes/POLLRATE+" seconds.");
        }
        leapSensor.ClearRecording();
        leapSensor.StartRecording(sign); // replace with some sign
        // TODO: recording mode (n frames, n sec, until press, until detected stop)
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;
        
        // loop while recording
        System.out.println("Recording");
        int framesLeft = nframes+1;
        boolean exit = false;
        while(!exit) {
            long frameStart = System.currentTimeMillis();
            if (nframes==0) {
                if (System.in.available() > 0) {
                    exit = true;
                    break;
                }
            } else {
                framesLeft--;
                if (framesLeft <= 0) {
                    exit = true;
                    break;
                }
            }
            Update();
            long dt = System.currentTimeMillis() - frameStart; // time that this update took
            long timeLeftThisFrame = POLLRATE - dt;
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        leapSensor.StopRecording();
        System.out.println("Stopped recording");
    }
    public static void Update() {
        Frame frame = controller.frame();
        leapSensor.ProcessFrame(frame);
    }
}
