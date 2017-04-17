/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asl;

import asltovoice.ASLtoVoiceMain;
import static asltovoice.ASLtoVoiceMain.curSign;
import asltovoice.GestureInterpreter;
import com.leapmotion.leap.Controller;
import de.ruzman.leap.LeapApp;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Inna
 */
public class MainController implements Initializable {

    @FXML
    private HBox label_mode_container;
    @FXML
    private Label mode_text;
    @FXML
    private SplitPane bottomPane;
    @FXML
    private AnchorPane viz_panel;
    @FXML
    private TitledPane loadPane;
    @FXML
    private ChoiceBox<String> classifierMenu;
    @FXML
    private Button loadFile_bt;
    @FXML
    private Button test_bt;
    @FXML
    private TitledPane recordPane;
    @FXML
    private VBox signContainer;
    @FXML
    private TextField signName;
    @FXML
    private Button record_bt;
    @FXML
    private Button save_bt;
    @FXML
    private Label feedback;
    private boolean isFileLoaded = false;
    ASLtoVoiceMain aslInterpreter;
    @FXML
    private VBox classifierPanel;
    @FXML
    private Button redo_bt;
    @FXML
    private HBox saveContainer;
    private final List<String> classifiers = Arrays.asList("IBk", "MultilayerPerceptron", "J48", "SMO", "NaiveBayes");
    @FXML
    private AnchorPane panel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        aslInterpreter = new ASLtoVoiceMain();
        classifierMenu.autosize();
        classifierMenu.getItems().addAll(classifiers);
        classifierMenu.setValue(classifiers.get(0));

        test_bt.setVisible(false);
        saveContainer.setVisible(false);

    }

    @FXML
    private void loadFile(ActionEvent event) {
        for (String s : classifiers) {
            if (s == null ? classifierMenu.getValue() == null : s.equals(classifierMenu.getValue())) {
                aslInterpreter.gestureInterpreter.SetClassificationType(classifiers.indexOf(s));
                break;
            }
        }

        FileChooser browser = new FileChooser();
        browser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
        browser.setInitialDirectory(new File("C:\\Users\\Inna\\Documents\\NetBeansProjects\\AAA"));
        File file = browser.showOpenDialog(null);

        if (file != null) {
            ASLtoVoiceMain.Load(file.getName());
            isFileLoaded = true;
            classifierPanel.setVisible(false);
            test_bt.setVisible(true);
        }

    }

    @FXML
    private void testFile(ActionEvent event) throws InterruptedException, IOException {
        capture(false);

//        LeapApp.getController().setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
//        if (isFileLoaded)
//        {
//            Timeline timeline = new Timeline(
//                    new KeyFrame(Duration.seconds(1), ae -> feedback.setText("3...")),
//                    new KeyFrame(Duration.seconds(1), ae -> feedback.setText("2...")),
//                    new KeyFrame(Duration.seconds(1), ae -> feedback.setText("1...")));
//            timeline.play();
//            feedback.setText("Capturing...");
//            try {
//                String feed  = ASLtoVoiceMain.RecordTest();
//                feedback.setText(feed);
//                aslInterpreter.Say(feed);
//                
//            } catch (InterruptedException | IOException ex) {
//                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        classifierPanel.setVisible(true);
//        System.out.println("LeapApp policy: "+LeapApp.getController().isPolicySet(Controller.PolicyFlag.POLICY_IMAGES));
    }

    private void resetAll() {
        signName.setText("");
        aslInterpreter.curSign.Clear();
        signContainer.setVisible(true);
        feedback.setText("");
        test_bt.setVisible(false);
        saveContainer.setVisible(false);
        classifierPanel.setVisible(true);
    }

    @FXML
    private void modeChange(MouseEvent event) {
        TitledPane pane = (TitledPane) event.getSource();
        if (pane.isExpanded() && pane == recordPane) {
            mode_text.setText("Training Mode");
            resetAll();
        } else if (pane.isExpanded() && pane == loadPane) {
            mode_text.setText("Testing Mode");
            resetAll();
        } else if (!pane.isExpanded()) {
            mode_text.setText("ASL to Voice");
        }

    }

    @FXML
    private void recordSign(ActionEvent event) throws InterruptedException, IOException {
        capture(true);
    }

    public void capture(boolean choice) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), ae -> feedback.setText("Capturing...")),
                new KeyFrame(Duration.seconds(1), ae -> choose(choice)));
        timeline.setCycleCount(1);
        timeline.play();

    }

    private void choose(boolean choice) {
        if (choice) {
            recordTRAIN();
        } else {
            recordTEST();
        }
    }

    private void recordTRAIN() {
        if (signName != null) {
            try {
                long curr_time = System.currentTimeMillis();
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            if (ASLtoVoiceMain.RecordTrain(signName.getText())) {
                                feedback.setText("Captured");
                                signContainer.setVisible(false);
                                saveContainer.setVisible(true);

                                System.out.println("Platform response: " + Platform.isFxApplicationThread());
                                Platform.isFxApplicationThread();
                                timer.cancel();

                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                    }
                }, 0, 100);
            } catch (Exception e) {
                System.out.println(e);

            }

//        if (signName !=null)
//        {
//            long curr_time = System.currentTimeMillis();
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask(){ 
//                @Override
//                public void run(){
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                if (ASLtoVoiceMain.RecordTrain(signName.getText()))
//                                {                           
//                                    signContainer.setVisible(false);
//                                    saveContainer.setVisible(true); 
//                                    feedback.setText("Captured");
//                                    timer.cancel();
//                                }
//                            }
//                            catch (InterruptedException | IOException ex ) {
//                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//             
//                        }
//                    });
//                }
//        },0, 100);
//        }        
        }
    }

    private void recordTEST() {

        if (isFileLoaded) {
            String feed = "";
            long curr_time = System.currentTimeMillis();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (ASLtoVoiceMain.RecordTest()) {
                            feedback.setText(ASLtoVoiceMain.getSignName());
                            aslInterpreter.Say(feed);
                            //feedback.setText("Captured");
                            timer.cancel();
                            feedback.setText("Captured");
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                }
            }, 0, 100);
        }
//        if (isFileLoaded)
//        {
//            try {
//                String feed  = ASLtoVoiceMain.RecordTest();
//                feedback.setText(feed);
//                aslInterpreter.Say(feed);
//            }
//            catch (InterruptedException | IOException ex ) {
//                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }                            
//            long curr_time = System.currentTimeMillis();
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask(){ 
//                @Override
//                public void run(){
//                    Platform.runLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                if (ASLtoVoiceMain.RecordTrain(signName.getText()))
//                                {                           
//                                    String feed  = ASLtoVoiceMain.RecordTest();
//                                    feedback.setText(feed);
//                                    aslInterpreter.Say(feed);
//                                    timer.cancel();
//                                }
//                            }
//                            catch (InterruptedException | IOException ex ) {
//                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//             
//                        }
//                    });
//                }
//        },0, 100);
//        }
//        classifierPanel.setVisible(true);
//        test_bt.setVisible(false);
    }

    @FXML
    private void onSaveFile(ActionEvent event) {
        FileChooser browser = new FileChooser();
        browser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
        browser.setInitialDirectory(new File("C:\\Users\\Inna\\Documents\\NetBeansProjects\\AAA"));
        //System.out.print(browser.getInitialDirectory());
        browser.setTitle("Save CSV");

        File file = browser.showSaveDialog(null);

        try {
            FileWriter openFile = new FileWriter(file.getAbsolutePath(), true); //the true will append the new data
            // add data to the file
            StringBuilder sb = new StringBuilder();
            // add header line
            sb.append(curSign.GetNormalizedHeaderLine());
            sb.append('\n');
            // add data
            sb.append(curSign.GetNormalizedDataString());
            sb.append("\n");

            openFile.write(sb.toString());
            openFile.close();
            // System.out.println("Save finished");
        } catch (IOException e) {
            System.out.println("Creating file failed." + e.getMessage());
        }

        try {
            FileWriter openFile = new FileWriter(file.getAbsolutePath(), true); //the true will append the new data
            // add data to the file
            StringBuilder sb = new StringBuilder();
            // add header line
            sb.append(curSign.GetNormalizedHeaderLine());
            sb.append('\n');
            // add data
            sb.append(curSign.GetNormalizedDataString());
            sb.append("\n");

            openFile.write(sb.toString());
            openFile.close();
            System.out.println("Save finished");

        } catch (Exception e) {
            System.out.println("Creating file failed." + e.getMessage());

        }
        resetAll();

    }

    @FXML
    private void onRedo(ActionEvent event) {
        aslInterpreter.curSign.Clear();
        capture(true);

    }

}
