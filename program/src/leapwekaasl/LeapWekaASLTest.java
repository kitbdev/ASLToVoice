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
        Instances trainingData = null;

        boolean isRecordingTrainingData = false, hasModel = false;

        int framesToRecord = 0;

        while (running) {
            isConnected = controller.isConnected();
            //System.out.println("Current Classifier: "+classifier.);
            System.out.print("\n");
            if (isConnected) {
                System.out.println("CONNECTED");
                if (isRecordingTrainingData) {
                    if (framesToRecord > 0) {
                        System.out.print("Will Record for: " + framesToRecord + " frames, or "
                                + (float) framesToRecord / POLLRATE + " seconds.");
                    } else {
                        System.out.println("Recording until keypress.");
                    }
                    if (leapSensor.HasData()) {
                        System.out.print("Usaved data recorded, please clear or save.");
                    }
                    System.out.print("\n");
                }
            } else {
                System.out.println("Connect to a LEAP motion sensor for more options");
                System.out.println("DISCONNECTED");
            }
            // prompts
            System.out.print("\nPress a key and enter to make a selection:\n");

            if (!isRecordingTrainingData) {
                if (isConnected) {
                    System.out.print("[t] Record new training data, ");
                    if (hasModel) {
                        System.out.print("[n] Record data to classify, ");
                    }
                }
                System.out.print("[m] Load Model, ");
                System.out.print("[d] Load training data, ");
                System.out.print("[c] Change classifier type, ");
               // if (!trainingData.isEmpty()) {
               //     System.out.print("[y] Train classifier on data, ");
               // }
               // if (hasModel) {
                    // TODO: save model
               // }
                System.out.print("\n");
            } else if (isConnected) {
                System.out.print("[n] Start Recording with new label, ");
                System.out.print("[r] Start Recording with same label, ");
                System.out.print("[q] Start Recording Sequence of classes, \n");
                System.out.print("[f] Set to record n frames, ");
                System.out.print("[o] Set to record n seconds, ");
                System.out.print("[i] Set to record until keypress, ");
                hasRecording = leapSensor.HasData();
                if (hasRecording) {
                    System.out.print("\n[s] Save Current Recording, ");
                    System.out.print("[c] Clear Current Recording, ");
                }
                System.out.print("\n[t] Stop Recording training data, ");
                // TODO: delete file if empty (and better names)
            } else {
                isRecordingTrainingData = false;
            }

            System.out.print("or [e] Exit:\n");

            // get input
            String sIn = scanner.next();
            char s = sIn.toLowerCase().charAt(0);

            if (!isRecordingTrainingData) {
                if (isConnected) {
                    if (s == 't') {
                        isRecordingTrainingData = true;
                        leapSensor.StartDataFile(true);
                    } else if (s == 'n' && hasModel) {
                        try {
                            Record("", framesToRecord);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // TODO: weka stuff
                        System.out.println("ERROR NOT IMPLEMENTED");
                        System.out.println("The sign you signed is ");
                    }
                }
                if (s == 'm') { 
                    System.out.println("Where is the file located?");
                    String fileLoc = scanner.next();
                    // TODO: shortcut for ../savedata/models
                    
                    // load model
                    //classifier = 
                    hasModel = true;
                } else if (s == 'd') {
                    System.out.println("Where is the file located? (leave blank to use last location)");
                    String fileLoc = scanner.next();
                    // TODO: shortcut for ../savedata/td
                    if (fileLoc == "") {
                        if (trainingDataLoc == "") {
                            fileLoc = trainingDataLoc;
                        }
                    }
                    // load training data
                    DataSource src = new DataSource(fileLoc);
                    trainingData = src.getDataSet();
                    // set class index because this is not an ARFF
                    if (trainingData.classIndex() == -1) {
                        trainingData.setClassIndex(trainingData.numAttributes() - 1);
                    }
                    // remove id, time, cur and total frames
                    trainingData.deleteAttributeAt(3);
                    trainingData.deleteAttributeAt(2);
                    trainingData.deleteAttributeAt(1);
                    trainingData.deleteAttributeAt(0);
                    // TODO: any other preprocessing?
                }
                if (s == 'c') {
                    System.out.println("Current Classifier is: " + classifier.toString());

                }
                if (s == 'y') {
                    System.out.println("Training data on recorded training data with "+ classifier.toString()+"...");
                    
                    classifier.buildClassifier(trainingData);
                    // classifier.getCapabilities() instead of
                    hasModel = true;
                    //classifier.classifyInstance(instnc)
                    // TODO: cross-validation?
                    // TODO: split by recording?
                    // TODO: check file to make sure there is enough data?
                    // TODO: testing data to find accuracy ?
                    System.out.println("Finished training model!");
                }
            } else {
                if (s == 'r' || s == 'n') {
                    String signName = "";
                    if (s == 'n') {
                        System.out.println("Enter the sign name: ");
                        signName = scanner.next();
                    }
                    RecordIn(signName, framesToRecord, 3);
                    leapSensor.SaveRecording();
                }
                if (s == 'q') {
                    System.out.println("Enter the number of signs you will record: ");
                    int numSigns = scanner.nextInt();
                    String signNames[] = new String[numSigns];
                    System.out.println("Enter the sign names one a time: ");
                    for (int i=0; i<numSigns; i++) {
                        System.out.print("\nSign "+(i+1)+": ");
                        signNames[i] = scanner.next();
                    }
                    for (int i=0; i<numSigns; i++) {
                        
                        RecordIn(signNames[i], framesToRecord, 3);
                        
                        // TODO: give chance to retry
                        if (false) {
                            // redo
                            leapSensor.ClearRecording();
                            i--;
                        } else {
                            leapSensor.SaveRecording();
                        }
                    }
                    System.out.println("All signs recorded");
                }
                if (s == 'i') {
                    framesToRecord = 0;
                    System.out.println("Will record until a key is pressed.");
                }
                if (s == 'f' || s == 'o') {
                    System.out.print("\nEnter the number of ");
                    if (s == 'o') {
                        System.out.print("seconds");
                    } else {
                        System.out.print("frames");
                    }
                    System.out.print(" to record for: ");
                    int n;
                    if (s == 'o') {
                        float m = scanner.nextFloat();
                        n = (int)Math.floor(m*POLLRATE);
                    } else {
                        n = scanner.nextInt();
                    }
                    framesToRecord = n;
                    System.out.println("Will record for " + framesToRecord + " frames.");
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
        // TODO: wait a little?
    }
    public static void RecordIn(String sign, int nframes, int delay) {
        if (leapSensor.HasData()) {
            leapSensor.SaveRecording();
        }
        System.out.println("Sign "+sign);
        if (delay>1){
            for(int i=0; i<delay; i++) {
                System.out.println((delay-i)+"...");
                try {
                    Thread.sleep(1000);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //System.out.println("Record!");
        try {
            Record(sign, nframes);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
    public static void Record(String sign, int nframes) throws IOException, InterruptedException {
        // pass in "" to record non-labelled
        if (nframes == 0) {
            System.out.println("Press Enter to stop recording");
        } else {
            System.out.println("Recording for " + nframes + ", or " + nframes / POLLRATE + " seconds.");
        }
        leapSensor.ClearRecording();
        leapSensor.StartRecording(sign); // replace with some sign
        // TODO: recording mode (n frames, n sec, until press, until detected stop)
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;

        System.out.println("Recording "+sign);
        int framesLeft = nframes + 1;
        boolean exit = false;
        while (!exit) {
            long frameStart = System.currentTimeMillis();
            if (nframes == 0) {
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
            boolean gotFrame = Update();
            if (!gotFrame) {
                framesLeft++;
                System.out.println("Hand not detected! No data recorded.");
            } else {
                //System.out.print(framesLeft+", \n");
            }
            long dt = System.currentTimeMillis() - frameStart; // time that this update took
            long timeLeftThisFrame = POLLRATE - dt;
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        leapSensor.StopRecording();
        System.out.println("Stopped recording");
    }

    public static boolean Update() {
        Frame frame = controller.frame();
        return leapSensor.ProcessFrame(frame);
    }
}
