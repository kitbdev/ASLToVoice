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

import java.util.Scanner;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public static boolean running = true;

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

        public void Run(Object data) {
            //System.out.printf("> %s\n", name);
            execute(data);
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

        String trainingDataLoc = null;

        // create all menu items
        List<MenuItem> mainmis = new ArrayList<>();
        List<MenuItem> tdmis = new ArrayList<>();
        // main menu items
        mainmis.add(new MenuItem("Record training data", 'r'));
        mainmis.get(mainmis.size() - 1).needsController = true;
        mainmis.get(mainmis.size() - 1).command = (Object data) -> {
            isRecordingTrainingData = true;
            try {
                leapSensor.StartDataFile(true, saveLoc);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        };
        mainmis.add(new RecordTestData("Record new test data", 'n'));//TODO needs !trainingData.isEmpty() and hasModel?
        mainmis.add(new LoadTrainingData("Load training data", 'd'));
        mainmis.add(new SaveModel("Save model", 's'));//TODO needs hasModel -- in method now?
        mainmis.add(new LoadModel("Load model", 'l'));
        //mainmis.add(new MenuItem("Change classifier type", 'c'));
        mainmis.add(new BuildModel("Build model with training data", 'b'));
        mainmis.add(new MenuItem("LoopTestData", 'o'));
        mainmis.get(mainmis.size() - 1).command = (Object data) -> {
            RecordTestData m = new RecordTestData("Record new test data", '_');
            scanner.useDelimiter("\n");
            while (true) {
                System.out.print("\n>");
                int ch = 0;
                String sIn = scanner.next().toLowerCase();
                char s = sIn.charAt(0);
                if (s == 'e') {
                    running = false;
                    break;
                }
                m.Run(data);
            }
        };
        mainmis.add(new MenuItem("Exit", 'e'));
        mainmis.get(mainmis.size() - 1).command = (Object data) -> {
            running = false;
        };

        // training data menu items
        tdmis.add(new RecordSigns("Start Recording with new label", 'i', false));
        //tdmis.add(new RecordSigns("Start Recording with same label", 'r', false));
        tdmis.add(new RecordSigns("Start Recording Sequence of classes", 'q', true));
        tdmis.add(new RecordMode("Set to record n frames", 'f'));
        tdmis.add(new MenuItem("Save Current Recording", 's')); // include has data flag?
        tdmis.get(tdmis.size() - 1).command = (Object data) -> {
            System.out.println("Saving last recorded data");
            leapSensor.SaveRecording();
        };
        tdmis.add(new MenuItem("Clear Current Recording", 'c'));// TODO: needs leapSensor.HasData();
        tdmis.get(tdmis.size() - 1).command = (Object data) -> {
            System.out.println("Clearing last recorded data");
            leapSensor.ClearRecording();
        };
        tdmis.add(new MenuItem("Stop Recording training data", 't'));
        tdmis.get(tdmis.size() - 1).command = new Command() {
            @Override
            public void execute(Object data) {
                System.out.println("Finishing recording of training data");
                leapSensor.FinishDataFile();
                isRecordingTrainingData = false;
                //trainingDataLoc = leapSensor.savePath; // do we need this?
                // TODO: delete file if empty (and better names)
                //TODO auto stop if not connected?
            }
        };

        while (running) {
            isConnected = controller.isConnected();
            System.out.print("\n");
            System.out.println("Classifier: " + classifier.toString());
            if (isConnected) {
                System.out.println("CONNECTED");
            } else {
                //System.out.println("Connect to a LEAP motion sensor for more options");
                System.out.println("DISCONNECTED");
            }
            if (isRecordingTrainingData) {
                if (framesToRecord > 0) {
                    System.out.println("Recording for: " + framesToRecord + " frames");
                } else {
                    System.out.println("Recording until keypress.");
                }
                if (leapSensor.HasData()) {
                    System.out.println("Unsaved data recorded, please clear or save.");
                }
            }
            // prompts
            System.out.print("Press a key and enter to make a selection:\n");

            // print training data prompts
            if (isRecordingTrainingData) {
                for (int i = 0; i < tdmis.size(); i++) {
                    if (tdmis.get(i).IsAvailable(isConnected)) {
                        tdmis.get(i).PrintPrompt();
                    }
                }
            }
            // print main menu prompts
            for (int i = 0; i < mainmis.size(); i++) {
                if (mainmis.get(i).IsAvailable(isConnected)) {
                    mainmis.get(i).PrintPrompt();
                }
            }

            // get input
            String sIn = "_";
            //if (scanner.hasNext()) {
            sIn = scanner.next();
            //}
            char s = sIn.toLowerCase().charAt(0);

            // Check input
            boolean found = false;
            // check main menu 
            for (int i = 0; i < mainmis.size(); i++) {
                MenuItem mm = mainmis.get(i);
                if (mm.IsAvailable(isConnected) && mm.Check(s)) {
                    mm.Run(null);
                    found = true;
                    break;
                }
            }
            // check training data 
            if (isRecordingTrainingData && !found) {
                for (int i = 0; i < tdmis.size(); i++) {
                    MenuItem tm = tdmis.get(i);
                    if (tm.IsAvailable(isConnected) && tm.Check(s)) {
                        tm.Run(null);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                System.out.printf("[%s] not found", s);
            }

            // wait for next command
            //TODO: look into clearing console
            if (running && found) {
                System.out.print("\n");
                for (int i = 0; i < 2; i++) {
                    System.out.print(".");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.print("\n------------------------------------------------------------------\n");
        }
    }

    public static class RecordTestData extends MenuItem {
        public RecordTestData(String promptname, char keycode) {
            super(promptname, keycode);
            needsController = true;
        }

        @Override
        public void execute(Object data) {
            if (hasModel) {
                RecordIn("_", framesToRecord, 1);
                //System.out.println("\007");
                System.out.println("\nAnalysing recorded data...");
                DenseInstance di = new DenseInstance(trainingData.numAttributes());
                //classifier.getCapabilities().
                //get new values into array
                // LoadValues() ?
                for (int i = 0; i < di.numAttributes(); i++) {
                    di.setValue(i, leapSensor.LoadDataAt(i));
                }

                di.setDataset(trainingData);
                try {
                    double n = classifier.classifyInstance(di);
                    System.out.print("The sign you signed is: \n");
                    Thread.sleep(1000);
                    System.out.print(">" + (int)n + "<\n");
                    Thread.sleep(1000);
                    //TODO: get accuracy
//                    double[] ns = classifier.distributionForInstance(di);
//                    System.out.print("Accuracy:\n");
//                    for(int i=0;i<10;i++) {// only works with the numbers
//                        if (i==(int)n) {
//                            System.out.print(">");
//                        } else {
//                            System.out.print("");
//                        }
//                        System.out.print((int)i + ": ");
//                        System.out.printf("%.3f", ns[i]);
//                        System.out.print("%\n");
//                    }
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
        //boolean inSeconds;
        public RecordMode(String promptname, char keycode) {
            super(promptname, keycode);
            //needsController = true;
        }

        @Override
        public void execute(Object data) {
            // prompt user for number of frames to record for
            System.out.print("\nEnter the number of frames to record for (0 waits for keypress): ");
            int n;
            n = scanner.nextInt();
            if (n <= 0) {
                framesToRecord = 0;
                System.out.println("Will record until a key is pressed.");
            } else {
                framesToRecord = n;
                System.out.println("Will record for " + framesToRecord + " frames.");
            }
        }
    }

    public static class SaveModel extends MenuItem {

        public SaveModel(String promptname, char keycode) {
            super(promptname, keycode);
        }

        @Override
        public void execute(Object data) {
            if (!hasModel) {
                System.out.print("There is no model to save!");
                return;
            }
            LocalDateTime date = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("kkmmss_ddMMyy");//hourminutesecond_daymonthyear
            String text = date.format(formatter);
            String modelSavePath = saveLoc + "m_" + text + ".model";
            System.out.printf("Saving model to %s...", modelSavePath);
            try {
                weka.core.SerializationHelper.write(modelSavePath, classifier);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            System.out.println("Model Saved!");
        }
    }

    public static class LoadModel extends MenuItem {

        public LoadModel(String promptname, char keycode) {
            super(promptname, keycode);
        }

        @Override
        public void execute(Object data) {
            System.out.print("Enter Model Location: " + saveLoc);
            String fileLoc = saveLoc;
            fileLoc += scanner.next();
            System.out.printf("Loading model from %s...", fileLoc);
            try {
                classifier = (Classifier) weka.core.SerializationHelper.read(fileLoc);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            hasModel = true;
            System.out.println("\nModel Loaded!");
        }
    }

    public static class LoadTrainingData extends MenuItem {

        public LoadTrainingData(String promptname, char keycode) {
            super(promptname, keycode);
        }

        @Override
        public void execute(Object data) {
            System.out.print("Enter Training Data Location: " + saveLoc);
            String fileLoc = saveLoc;
            fileLoc += scanner.next();
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
            // TODO: check file to make sure there is enough data?
            if (trainingData == null) {
                System.out.println("No training data! Please load data with [d]");
                return;
            }
            try {
                classifier.buildClassifier(trainingData);
                Evaluation eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);
                String summ = eval.toSummaryString();
                System.out.println(summ);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
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
                // TODO: remove last few frames if record mode is keypress?
            }
            System.out.println("All signs recorded");
        }
    }

    public static void RecordIn(String sign, int nframes, int delay) {
        if (leapSensor.HasData()) {
            leapSensor.SaveRecording();
        }
        if (sign != "_") {
            System.out.println("\nSign " + sign);
        }
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
        if (sign != "_") {
            if (nframes == 0) {
                System.out.println("Press Enter to stop recording");
            } else {
                System.out.println("Recording for " + nframes + " frames");//, or " + ((float) nframes / POLLRATE) + " seconds.");
            }
        }
        leapSensor.ClearRecording();
        leapSensor.StartRecording(sign); // replace with some sign
        // TODO: recording mode (n frames, n sec, until press, until detected stop)
        long timeSinceStart = System.currentTimeMillis();
        //long dt = 0, frameStart = 0;
        if (sign != "_") {
            System.out.println("Recording " + sign);
        } else { 
            System.out.println("Recording ");
        }
        int framesLeft = nframes + 1;
        boolean exit = false;
        while (!exit) {
            long frameStart = System.currentTimeMillis();
            if (nframes <= 0) {
                if (nframes == 0) {
                    if (System.in.available() > 0) {
                        exit = true;
                        break;
                    }
                } else {
                    // TODO: record mode for no hands
                    if (System.in.available() > 0) {
                        exit = true;
                        break;
                    }
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
                while (!leapSensor.HandAvailable(controller.frame())) {
                    System.out.print(".");
                    Thread.sleep(100);
                }
                System.out.print("\n");
            } else {
                if (nframes >= 100 && framesLeft % 10 == 0) {
                    System.out.println("frame " + (nframes - framesLeft));
                }
                //System.out.print(framesLeft+", \n");
            }
            long dt = System.currentTimeMillis() - frameStart; // time that this update took
            long timeLeftThisFrame = POLLRATE - dt;
            if (timeLeftThisFrame < 0) {
                timeLeftThisFrame = 0;
            }
            Thread.sleep(timeLeftThisFrame);// sleep for updates/sec-dt
        }
        leapSensor.StopRecording();
        
        System.out.println("Done recording");
        
    }

    public static boolean Update() {
        Frame frame = controller.frame();
        return leapSensor.ProcessFrame(frame);
    }
}
