/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package asl;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Inna
 */
public class TrainingController implements Initializable {

    @FXML
    private HBox label_mode_container;
    @FXML
    private Label mode_text;
    @FXML
    private HBox toggle_container;
    @FXML
    private Button mode_bt;
    @FXML
    private SplitPane bottomPane;
    @FXML
    private AnchorPane viz_panel;
    @FXML
    private AnchorPane data;
    @FXML
    private VBox signContainer;
    @FXML
    private TextField signName;
    @FXML
    private ChoiceBox<?> classifierMenu;
    @FXML
    private Button submit_sign_bt;
    @FXML
    private Button mode_bt1;
    @FXML
    private ImageView logo;
    @FXML
    private Label feedback;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void modeChange(ActionEvent event) {
    }

    @FXML
    private void clicked(MouseEvent event) {
    }

    @FXML
    private void addSignSubmitted(ActionEvent event) {
    }
    
}
