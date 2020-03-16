package com.perriello.rf.spectrumanalyzer.gui;

import javafx.scene.control.Dialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * uses a GIT HUB library that nicely wraps up popups in windows like dialog boxes
 * this is javafx 2.2 ( so need to do some extra tricks )
 * Created by aperriello on 2/10/2018.
 */
public class DialogHandler {
    private static final Logger logger = LogManager.getLogger(DialogHandler.class);
    Stage stage;
    Alert a = new Alert(AlertType.NONE); 

    public void setStage(Stage s) {
        stage = s;
    }

    public void showErrorDialog(String errorText) {
        //Dialog.showErrorDialog(stage, errorText, "error encountered", "error");
    	
    }

    public void showInformationDialog(String message) {
    	a.setAlertType(AlertType.ERROR); 
        a.show(); 
    }

    public void showInformationDialog(String message, String title) {
    	a.setAlertType(AlertType.INFORMATION); 
        a.show(); 
    }

}
