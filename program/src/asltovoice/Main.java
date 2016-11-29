package asltovoice;

import com.leapmotion.leap.*;
import java.io.FileNotFoundException;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import jwsfilechooser;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static Scanner scanner = new Scanner(System.in);
    public static Controller controller = new Controller();
    public static LeapSensor leapSensor = new LeapSensor();

    public static long POLLRATE = 50;//ms
    //1.0/100.0;// 1 poll every .1 seconds in ms //TODO: is this good?
    public static Classifier classifier;
    public static Instances trainingData = null;

    public static int framesToRecord = 10;
    public static boolean isConnected, hasRecording = false;
    public static boolean isRecordingTrainingData = false, hasModel = false;

    public static String saveLoc = "../savedata/";

    public static void main(String[] args) throws InterruptedException, IOException {
        //classifier = new J48();
        classifier = new IBk();
        try {
            Menu();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting...");
    }

    public interface Command {
        public abstract void execute(Object data);
    }

    public static class MenuItem implements Command {
        public String name = "Do this";
        public char code = 'k';
        public boolean enabled = false;
        public boolean needsController = false;
        public Command command;

        public MenuItem(String promptname, char keycode) {
            name = promptname;
            code = keycode;
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

        public boolean IsAvailable(boolean connected) {
            return !needsController || connected;
        }

        @Override
        public void execute(Object data) {
            if (command != null) {
                command.execute(data);
            } else {
                System.out.printf("ERROR: %s is not implemented!", name);
            }
        }// TODO: wrap this in another function to call that auto checks? 
    }

    public static void Menu() throws IOException, Exception {
        boolean running = true;

        String trainingDataLoc = null;
        //        Interface a;
        //        a = Main.RecordTestData;

        List<MenuItem> mainmis = new ArrayList<>();
        List<MenuItem> tdmis = new ArrayList<>();
        // TODO: run lambda function ?
        mainmis.add(new MenuItem("Record training data", 't'));
        mainmis.get(mainmis.size()-1).command = (Object data) -> {
            isRecordingTrainingData = true;
            try {
                leapSensor.StartDataFile(true, saveLoc);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        };
        mainmis.add(new RecordTestData("Record data to classify", 'n'));//TODO needs !trainingData.isEmpty() and hasModel?
        mainmis.add(new LoadTrainingData("Load training data", 'd'));
        mainmis.add(new SaveModel("Save model", 's'));//TODO needs hasModel
        mainmis.add(new LoadModel("Load model", 'm'));
        //mainmis.add(new MenuItem("Change classifier type", 'c'));
        mainmis.add(new BuildModel("Train classifier on data", 'y'));
        mainmis.add(new MenuItem("Exit", 'e'));
        mainmis.get(mainmis.size()-1).command = new Command() {
            @Override
            public void execute(Object data) {
                //running = false;
                //TODO fix stopping
            }
        };

        //tdmis.add(new MenuItem("Record digits 0-9", '1'));
        tdmis.add(new RecordSigns("Start Recording with new label", 'n', false));
        //tdmis.add(new RecordSigns("Start Recording with same label", 'r', false));
        tdmis.add(new RecordSigns("Start Recording Sequence of classes", 'q', true));
        tdmis.add(new MenuItem("Set to record n frames", 'f'));
        tdmis.add(new MenuItem("Set to record n seconds", 'o'));
        tdmis.add(new MenuItem("Set to record until keypress", 'i'));
        tdmis.add(new MenuItem("Save Current Recording", 's')); // include has data flag?
        mainmis.get(mainmis.size()-1).command = new Command() {
            @Override
            public void execute(Object data) {
                System.out.println("Saving last recorded data");
                leapSensor.SaveRecording();
                }
        };
        tdmis.add(new MenuItem("Clear Current Recording", 'c'));// TODO: needs leapSensor.HasData();
        mainmis.get(mainmis.size()-1).command = new Command() {
            @Override
            public void execute(Object data) {
                System.out.println("Clearing last recorded data");
                leapSensor.ClearRecording();
            }
        };
        tdmis.add(new MenuItem("Stop Recording training data", 't'));
        mainmis.get(mainmis.size()-1).command = new Command() {
            @Override
            public void execute(Object data) {
                leapSensor = (LeapSensor) data;//TODO pass by reference?
                System.out.println("Finishing recording of training data");
                leapSensor.FinishDataFile();
                isRecordingTrainingData = false;
                trainingDataLoc = leapSensor.savePath;
                // TODO: delete file if empty (and better names)
                //TODO auto stop if not connected?
            }
        };
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
                //System.out.println("Connect to a LEAP motion sensor for more options");
                System.out.println("DISCONNECTED");
            }
            // prompts
            System.out.print("\nPress a key and enter to make a selection:\n");

            // print main menu prompts
            for (int i = 0; i < mainmis.size(); i++) {
                if (mainmis.get(i).IsAvailable(isConnected)) {
                    mainmis.get(i).PrintPrompt();
                }
            }
            // print training data prompts
            if (isRecordingTrainingData) {
                for (int i = 0; i < tdmis.size(); i++) {
                    if (tdmis.get(i).IsAvailable(isConnected)) {
                        tdmis.get(i).PrintPrompt();
                    }
                }
            }

            // get input
            String sIn = "_";
            if (scanner.hasNext()) {
                sIn = scanner.next();
            }
            char s = sIn.toLowerCase().charAt(0);

            // Check input
            // check main menu 
            for (int i = 0; i < mainmis.size(); i++) {
                if (mainmis.get(i).IsAvailable(isConnected)) {
                    mainmis.get(i).execute(null);
                }
            }
            // check training data 
            if (isRecordingTrainingData) {
                for (int i = 0; i < tdmis.size(); i++) {
                    if (tdmis.get(i).IsAvailable(isConnected)) {
                        tdmis.get(i).execute(null);
                    }
                }
            }
            // TODO: implement these
            //     if (s == 'c') {
            //         System.out.println("Classifier: " + classifier.toString());
            //     }
            //     if (s == '1') {
            //         // Record Numbers
            //         for (int i = 0; i < 10; i++) {
            //             RecordIn(("num" + i), framesToRecord, 3);
            //             leapSensor.SaveRecording();
            //         }
            //         System.out.println("All number signs recorded");
            //     }
            //     if (s == 'i') {
            //         // Record Mode Key Press
            //         framesToRecord = 0;
            //         System.out.println("Will record until a key is pressed.");
            //     }
            //     if (s == 'f' || s == 'o') {
            //         // Record Mode Frames
            //         System.out.print("\nEnter the number of ");
            //         if (s == 'o') {
            //             System.out.print("seconds");
            //         } else {
            //             System.out.print("frames");
            //         }
            //         System.out.print(" to record for: ");
            //         int n;
            //         if (s == 'o') {
            //             float m = scanner.nextFloat();
            //             n = (int) Math.floor((float) m * POLLRATE);// is this right?
            //         } else {
            //             n = scanner.nextInt();
            //         }
            //         framesToRecord = n;
            //         System.out.println("Will record for " + framesToRecord + " frames.");
            //     }
        }
        //TODO: look into clearing console
        System.out.println("---------------------");
        Thread.sleep(10);
    }

    public static class RecordTestData extends MenuItem {
        public RecordTestData(String promptname, char keycode) {
            super(promptname, keycode);
            needsController = true;
        }

        @Override
        public void execute(Object data) {
            if (hasModel) {
                RecordIn("_", framesToRecord, 3);
                System.out.println("analysing recorded data...");
                DenseInstance di = new DenseInstance(trainingData.numAttributes());
                //get new values into array
                // LoadValues() ?
                for (int i = 0; i < di.numAttributes(); i++) {
                    di.setValue(i, leapSensor.LoadDataAt(i));
                }

                di.setDataset(trainingData);
                try {
                    double n = classifier.classifyInstance(di);
                    System.out.println("The sign you signed is " + n + ".");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                System.out.println("No model!");
            }
            leapSensor.ClearRecording();
        }
    }
    public static class RecordMode extends MenuItem {

        public RecordMode(String promptname, char keycode, boolean isForKeyPress) {
            super(promptname, keycode);
            //needsController = true;

        }

        @Override
        public void execute(Object data) {
            // prompt user for number of frames 
            // TODO
        }
    }
    public static class SaveModel extends MenuItem {

        public SaveModel(String promptname, char keycode) {
            super(promptname, keycode);
        }
        @Override
        public void execute(Object data) {
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
            String text = date.format(formatter);
            try {
                weka.core.SerializationHelper.write(saveLoc + "m_" + text + ".model", classifier);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static class LoadModel extends MenuItem {

        public LoadModel(String promptname, char keycode) {
            super(promptname, keycode);
        }
        @Override
        public void execute(Object data) {
            System.out.println("Enter Model Location: " + saveLoc);
            String fileLoc = saveLoc;
            fileLoc += scanner.next();
            try {
                classifier = (Classifier) weka.core.SerializationHelper.read(fileLoc);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static class LoadTrainingData extends MenuItem {

        public LoadTrainingData(String promptname, char keycode) {
            super(promptname, keycode);
        }
        @Override
        public void execute(Object data) {
            System.out.println("Enter Training Data Location: ../savedata/trainingdata/");
            String fileLoc = "../savedata/trainingdata/";
            fileLoc += scanner.next();
            // TODO: shortcut for ../savedata/td
            // (\"...\" to use last location)");
            //        if (fileLoc.trim() == "...") {
            //            if (trainingDataLoc != "") {
            //                fileLoc = trainingDataLoc;
            //            } else {
            //              System.out.println("no last location!");
            //              fileLoc = "";
            //            }
            //        }
            System.out.println("loading file at:" + fileLoc);
            try {
                DataSource src = new DataSource(fileLoc);
                System.out.println("loaded file ");
                trainingData = src.getDataSet();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            // set class index because this is not an ARFF
            if (trainingData.classIndex() == -1) {
                trainingData.setClassIndex(trainingData.numAttributes() - 1);
            }
            // TODO: one instance includes multiple frames
            // remove id, time, cur and total frames
            trainingData.deleteAttributeAt(3);
            trainingData.deleteAttributeAt(2);
            trainingData.deleteAttributeAt(1);
            trainingData.deleteAttributeAt(0);
            // TODO: any other preprocessing?
            //System.out.println("data loaded, press [y] to build the model");
            System.out.println("Training Data loaded, please build model");
            //BuildModel.execute(null);
        }
    }

    public static class BuildModel extends MenuItem {

        public BuildModel(String promptname, char keycode) {
            super(promptname, keycode);
        }
        @Override
        public void execute(Object data) {
            // Create Model
            System.out.println("Building Model with " + classifier.toString() + "...");
            //System.out.println("Training data on recorded training data with "+ classifier.toString()+"...");
            // TODO: check file to make sure there is enough data?
            if (trainingData == null) {
                System.out.println("No training data! Please load data with [d]");
                return;
            }
            //System.out.println("Building model and evaluating...");
            try {
                classifier.buildClassifier(trainingData);
                Evaluation eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);
                String summ = eval.toSummaryString();
                System.out.println(summ);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Finished training the model");
            // classifier.getCapabilities() instead of
            hasModel = true;
        }
    }

    public static class RecordSigns extends MenuItem {
        boolean multiple = false;

        public RecordSigns(String promptname, char keycode, boolean multipleSigns) {
            super(promptname, keycode);
            multiple = multipleSigns;
            needsController = true;
        }

        public void execute(Object data) {
            //boolean multiple = (boolean) data;
            int numSigns = 1;
            if (multiple) {
                System.out.println("Enter the number of signs you will record: ");
                numSigns = scanner.nextInt();
            }

            String signNames[] = new String[numSigns];
            if (multiple) {
                System.out.println("Enter the sign names one a time: ");
                for (int i = 0; i < numSigns; i++) {
                    System.out.print("\nSign " + (i + 1) + ": ");
                    signNames[i] = scanner.next();
                }
            } else {
                System.out.println("Enter the sign name: ");
                signNames[0] = scanner.next();
            }
            for (int i = 0; i < numSigns; i++) {
                RecordIn(signNames[i], framesToRecord, 3);
                leapSensor.SaveRecording();
                // TODO: give chance to retry
                //            if (false) {
                //                // redo
                //                leapSensor.ClearRecording();
                //                i--;
                //            } else {
                //            }
            }
            System.out.println("All signs recorded");
        }
    }

    public static void RecordIn(String sign, int nframes, int delay) {
        if (leapSensor.HasData()) {
            leapSensor.SaveRecording();
        }
        System.out.println("\nSign " + sign);
        if (delay > 1) {
            for (int i = 0; i < delay; i++) {
                System.out.println((delay - i) + "...");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
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
            System.out.println("Recording for " + nframes + ", or " + ((float) nframes / POLLRATE) + " seconds.");
        }
        leapSensor.ClearRecording();
        leapSensor.StartRecording(sign); // replace with some sign
        // TODO: recording mode (n frames, n sec, until press, until detected stop)
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;

        System.out.println("Recording " + sign);
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
                if (nframes >= 100 && framesLeft % 10 == 0) {
                    System.out.println("frame " + (nframes - framesLeft));
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
