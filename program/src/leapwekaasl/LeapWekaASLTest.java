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
        if (controller.isConnected()) {
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
        Instances trainingData = null;

        boolean isRecordingTrainingData = false, hasModel = false;

        int framesToRecord = 0;

        while (running) {
            isConnected = controller.isConnected();
            //System.out.println("Current Classifier: "+classifier.);
            if (isConnected) {
                System.out.print("\n");
                if (framesToRecord >= 0) {
                    System.out.print("Will Record for: " + framesToRecord + " frames, or "
                            + (float) framesToRecord / POLLRATE + " seconds.");
                } else {
                    System.out.print("Recording until keypress.");
                }
                System.out.print("\n");
            } else {
                System.out.println("Connect to a LEAP motion sensor for more options");
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
                if (!trainingData.isEmpty()) {
                    System.out.print("[y] Train classifier on data, ");
                }
                if (hasModel) {
                    // TODO: save model
                }
                System.out.print("\n");
            } else if (isConnected) {
                System.out.print("[n] Start Recording with new label, ");
                System.out.print("[r] Start Recording with same label, ");
                System.out.print("[f] Record for n frames, ");
                System.out.print("[n] Record for n seconds, ");
                System.out.print("[i] Record untill keypress, ");
                System.out.print("[q] Start Recording Sequence of classes, ");
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
                    try {
                        Record(signName, framesToRecord);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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
                        System.out.println("Sign "+signNames[i]+" in ");
                        System.out.println("3...");
                        Thread.sleep(1000);
                        System.out.println("2...");
                        Thread.sleep(1000);
                        System.out.println("1...");
                        Thread.sleep(1000);
                        System.out.println("Record!");
                        try {
                            Record(signNames[i], framesToRecord);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // TODO: give chance to retry
                        leapSensor.SaveRecording();
                    }
                    System.out.println("All signs recorded");
                }
                if (s == 'i') {
                    framesToRecord = 0;
                    System.out.println("Will record until a key is pressed.");
                }
                if (s == 'f' || s == 'n') {
                    System.out.print("\nEnter the number of ");
                    if (s == 'n') {
                        System.out.print("seconds");
                    } else {
                        System.out.print("frames");
                    }
                    System.out.print(" to record for: ");
                    int n = scanner.nextInt();
                    if (s == 'n') {
                        n *= POLLRATE;
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

        System.out.println("Recording");
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
