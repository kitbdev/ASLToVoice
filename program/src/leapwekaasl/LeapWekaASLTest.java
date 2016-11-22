package leapwekaasl;

import com.leapmotion.leap.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.Evaluation;
import weka.core.DenseInstance;

//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
//import jwsfilechooser;
import java.util.List;

public class LeapWekaASLTest {

    public static Scanner scanner = new Scanner(System.in);
    public static Controller controller = new Controller();
    public static LeapSensor leapSensor = new LeapSensor();

    public static long POLLRATE = 50;//ms
    //1.0/100.0;// 1 poll every .1 seconds in ms //TODO: is this good?
    public static Classifier classifier;
    public static int framesToRecord = 10;
    public static boolean isConnected, hasRecording = false;
    public static boolean isRecordingTrainingData = false, hasModel = false;
    public static List<MenuItem> mis;

    public static void main(String[] args)
            throws InterruptedException, IOException {
        //classifier = new J48();
        classifier = new IBk();
        try {
            Menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting...");
    }
    
    public static class MenuItem {
        public String name = "Do this";
        public char code = 'd';
        public boolean enabled = false;
        
        public MenuItem(String promptname, char keycode) {
            name=promptname;
            code=keycode;
        }
        public void PrintPrompt() {
            System.out.printf("[%s] %s.\n", code, name);
        }
        public boolean Check(char checkCode) {
            if (code == checkCode) {
                System.out.printf("Selected: %s.\n", name);
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static void Menu() throws IOException, Exception {
        boolean running = true;
        
        String trainingDataLoc = null;
        Instances trainingData = null;
        
        // TODO: run lambda function ?
        mis.add(new MenuItem("Record training data", 't'));
        mis.add(new MenuItem("Record data to classify", 'n'));
                System.out.print("[d] Load training data, ");
                System.out.print("[c] Change classifier type, ");
                System.out.print("[y] Train classifier on data, ");
                System.out.print("\n");
                System.out.print("[1] record digits 0-9 \n");
                System.out.print("[n] Start Recording with new label, ");
                System.out.print("[r] Start Recording with same label, ");
                System.out.print("[q] Start Recording Sequence of classes, \n");
                System.out.print("[f] Set to record n frames, ");
                System.out.print("[o] Set to record n seconds, ");
                System.out.print("[i] Set to record until keypress, ");
                System.out.print("\n[s] Save Current Recording, ");
                System.out.print("[c] Clear Current Recording, ");
                System.out.print("\n[t] Stop Recording training data\n ");
                // TODO: make these part of menu and have them update active status each run
                
                
                
                
        while (running) {
            isConnected = controller.isConnected();
            System.out.print("\n");
            System.out.println("Classifier: " + classifier.toString());
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
               // System.out.print("[m] Load Model, ");
                System.out.print("[d] Load training data, ");
                System.out.print("[c] Change classifier type, ");
               // if (!trainingData.isEmpty()) {
                System.out.print("[y] Train classifier on data, ");
               // }
               // if (hasModel) {
                    // TODO: save model
               // }
                System.out.print("\n");
            } else if (isConnected) {
                System.out.print("[1] record digits 0-9 \n");
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
                System.out.print("\n[t] Stop Recording training data\n ");
                // TODO: delete file if empty (and better names)
            } else {
                isRecordingTrainingData = false;
            }
            if (!isRecordingTrainingData) {
                System.out.print("or [e] Exit:\n");
            }
        
            
            // get input
            String sIn = "_";
            if (scanner.hasNext()){
                sIn = scanner.next();
            }
            char s = sIn.toLowerCase().charAt(0);
            if (!isRecordingTrainingData) {
                if (isConnected) {
                    if (s == 't') {
                        isRecordingTrainingData = true;
                        leapSensor.StartDataFile(true);
                    }
                    if (s == 'n') {
                        if (hasModel) {
                            RecordIn("_", framesToRecord, 3);
                            // TODO: weka stuff
                            System.out.println("analysing recorded data...");
                            DenseInstance di = new DenseInstance(trainingData.numAttributes());
                            //get new values into array
                            // LoadValues()
                            for (int i=0;i<di.numAttributes();i++){
                                di.setValue(i, leapSensor.LoadDataAt(i));
                            }
                            
                            di.setDataset(trainingData);
                            double n = classifier.classifyInstance(di);
                            
                            System.out.println("The sign you signed is " + n+".");
                        } else {
                            System.out.println("No model!");
                        }
                        leapSensor.ClearRecording();
                    }
                }
                if (s == 'm') { 
                    System.out.println("use [d] instead");
                    System.out.println("Where is the file located?");
                    String fileLoc = scanner.next();
                    // TODO: shortcut for ../savedata/models
                    
                    // load model
                    //classifier = 
                    //hasModel = true;
                } else if (s == 'd') {
                    System.out.println("Where is the file located? (\"...\" to use last location)");
                    String fileLoc = scanner.next();
                    // TODO: shortcut for ../savedata/td
                    if (fileLoc.trim() == "...") {
                        if (trainingDataLoc != "") {
                            fileLoc = trainingDataLoc;
                        } else {
                          System.out.println("no last location!");
                          fileLoc = "";
                        }
                    }
                    if (fileLoc!="") {
                        // load training data
                        System.out.println("loading file at:"+fileLoc);
                        DataSource src = new DataSource(fileLoc);
                        System.out.println("loaded file ");
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
                        System.out.println("data loaded, press [y] to build the model");
                    }
                }
                if (s == 'c') {
                    System.out.println("Classifier: " + classifier.toString());
                }
                if (s == 'y') {
                    System.out.println("Training data on recorded training data with "+ classifier.toString()+"...");
                    // classifier.getCapabilities() instead of
                    hasModel = true;
                    // TODO: check file to make sure there is enough data?
                    if (trainingData==null){
                        System.out.println("No training data! please load some [d]");
                    } else {
                        Classify(trainingData);
                        System.out.println("Finished training the model");
                    }
                }
            } else {// is recording training data
                if (s == 'r' || s == 'n') {
                    String signName = "";
                    if (s == 'n') {
                        System.out.println("Enter the sign name: ");
                        signName = scanner.next();
                    }
                    RecordIn(signName, framesToRecord, 3);
                    leapSensor.SaveRecording();
                }
                if (s == '1') {
                    for (int i=0; i<10; i++) {                        
                        RecordIn(("num"+i), framesToRecord, 3);
                        leapSensor.SaveRecording();
                    }
                    System.out.println("All number signs recorded");
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
        System.out.println("---------------------");
        Thread.sleep(10);
    }
    public static void Classify(Instances inst) {
        System.out.println("Building classifier and evaluating...");
        try{
            classifier.buildClassifier(inst);
            Evaluation eval = new Evaluation(inst);
            eval.evaluateModel(classifier, inst);
            String s = eval.toSummaryString();
            System.out.println(s);
        } catch (Exception e) {
           e.printStackTrace();
        }
    }
    public static void RecordIn(String sign, int nframes, int delay) {
        if (leapSensor.HasData()) {
            leapSensor.SaveRecording();
        }
        System.out.println("\nSign "+sign);
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
            System.out.println("Recording for " + nframes + ", or " + ((float)nframes / POLLRATE) + " seconds.");
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
                if (nframes>=100 && framesLeft%10 == 0){
                    System.out.println("frame "+(nframes-framesLeft));
                }
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
    