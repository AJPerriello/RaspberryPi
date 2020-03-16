package com.perriello.rf.spectrumanalyzer.gui;

import com.perriello.rf.spectrumanalyzer.config.PropertiesHandler;
import com.perriello.rf.spectrumanalyzer.datamgrs.DataPlotter;
import com.perriello.rf.spectrumanalyzer.datamgrs.MessageDataSender;
import com.perriello.rf.spectrumanalyzer.datamgrs.Monitor;
import com.perriello.rf.spectrumanalyzer.datamgrs.RFMonitorData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import java.util.Optional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * connect to the RFMonitor to begin
 * Created by aperriello on 2/10/2018.
 */
public class ConnectionMenu {
    private static final Logger logger = LogManager.getLogger(ConnectionMenu.class);
    PropertiesHandler propertiesHandler;
    MessageDataSender messageDataSender;
    DialogHandler dialogHandler;
    private Menu connection;
    private MenuItem server;
    private String serverIpAddress;
    int serverPort;
    Monitor monitor;
    RFView rfView;

    public ConnectionMenu(RFView rfView,
                          PropertiesHandler propertiesHandler,
                          DialogHandler dialogHandler,
                          MessageDataSender messageDataSender,
                          Monitor monitor){
        this.rfView = rfView;
        this.propertiesHandler = propertiesHandler;
        this.dialogHandler = dialogHandler;
        this.messageDataSender = messageDataSender;
        this.monitor = monitor;
        connection = new Menu("connection");
        serverIpAddress = propertiesHandler.getRFMonitorIpAddress();
        serverPort = propertiesHandler.getRFMonitorPort();
        server = new MenuItem("connect to RFMonitor server");
        connection.getItems().add(server);
        connection.setDisable(true);
    }

    public Menu getMenu(){
        return connection;
    }

    public void activate(){
        enableMenu();
        enableActions();
    }

    public void disable(){
        connection.setDisable(true);
    }

    public void enableMenu(){
        connection.setDisable(false);
    }

    public void enableActions(){
        server.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String connectToServer = "select 'YES' to connect to server at " + serverIpAddress;
                //Dialog.DialogResponse response = dialogHandler.showConfirmDialog(connectToServer, "TCP socket connect", "connection");
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("connection");
                alert.setContentText("is connected");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    // ... user chose OK
                } else {
                    // ... user chose CANCEL or closed the dialog
                }
                if(result.get() == ButtonType.OK)
                {

                    boolean success = monitor.connectToServer(serverIpAddress, serverPort);
                    if(!success){
                        dialogHandler.showErrorDialog("connection to " + serverIpAddress + " failed");
                        rfView.displayConnectionFailed(serverIpAddress);
                        return;
                    }
                    else{
                        boolean doWeHaveTakeoff = monitor.launchThreads();
                        if(!doWeHaveTakeoff) {
                            rfView.displayThreadsFailedToLaunch();
                            return;
                        }
                    }
                    rfView.activateMenus();
                }
                else {
                  return;
                }
            }
        });
    }

}
