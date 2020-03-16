package com.perriello.rf.spectrumanalyzer.gui;


import com.perriello.rf.rfspectrummessaging.Spectrum;
import com.perriello.rf.rfspectrummessaging.State;
import com.perriello.rf.spectrumanalyzer.config.PropertiesHandler;
import com.perriello.rf.spectrumanalyzer.datamgrs.*;
import com.perriello.rf.rfspectrummessaging.SpectrumDataPacket;
import com.perriello.rf.spectrumanalyzer.info.SpectrumInformation;

import javafx.application.Application;
import javafx.event.*;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Dialog;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RFView is an JavaFX application GUI
 * a line graph the shows a real time view of frequency and power level measurements for each input stream configured
 * the plot data measurements displayed on the graph are collected over a socket connection
 * and represent the spectrum data of an actively connection ( can be enhanced for another device )
 * The status is displayed at the bottom of the scene view and shows state information
 * Created by ajperriello on 2/10/2018.
 */
public class RFView extends Application {
    //private static final Logger logger = LogManager.getLogger(RFView.class);
    private final static Logger logger = Logger.getLogger(RFView.class);
    public final NumberAxis xAxis = new NumberAxis();
    public final NumberAxis yAxis = new NumberAxis();
    public final LineChart lineChart = new LineChart(xAxis, yAxis);
    private PropertiesHandler propertiesHandler;
    private MessageDataSender messageDataSender;
    private Monitor monitor;
    public static volatile boolean terminate = false;
    private Text statusBold = new Text();
    private Text statusExtension = new Text();
    boolean validProperties = true;
    private final Object lock = new Object();
    private Stage stage;
    private DialogHandler dialogHandler;
    private ConnectionMenu connectionMenu;
    private AtomicBoolean isHoldInitializing = new AtomicBoolean(false);
    private AtomicBoolean isHolding = new AtomicBoolean(false);
    private AtomicBoolean isCalibrationInitializing = new AtomicBoolean(false);
    private AtomicBoolean isRotationIntervalInitializing = new AtomicBoolean(false);
    private AtomicBoolean isStartUpStatus = new AtomicBoolean(true);
    private AtomicBoolean isRotationIntervalSet = new AtomicBoolean(false);
    private boolean initializeChart = true;
    private String currentHoldRequestName = "";
    private String currentCalibrationRequestName = "";
    private int currentRotationRequestInterval= 0;
    private ArrayList<String> remainingSpectrumsForStatus;
    private SpectrumInformation[] spectrumInformation = SpectrumInformation.values();
    private String serverIpAddress;
    private final double adjustXAxis;
    private final double adjustYAxisUp;
    private final double adjustYAxisDown;
    private final double xTickUnit;
    private final double yTickUnit;
    private final boolean admin;


    /**
     * called by the fx framework to set the layout, scen and stage and show it
     * @param stage
     * @throws Exception
     */
    @Override
    public void start(final Stage stage) throws Exception {
        logger.info("start");
        this.stage = stage;
        
        dialogHandler.setStage(stage);
        if(!validProperties) {
            dialogHandler.showErrorDialog("config.properties is not valid, check log for errors");
            return;
        }
        

        BorderPane borderPane = new BorderPane();

        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.prefWidthProperty().bind(stage.widthProperty());
        //connectionMenu = new ConnectionMenu(this, propertiesHandler, dialogHandler, messageDataSender, monitor);
        //rotationMenu = new RotationMenu(this, propertiesHandler, dialogHandler, messageDataSender);
        //holdMenu = new HoldMenu(propertiesHandler, dialogHandler, messageDataSender);
        //calibrationMenu = new CalibrationMenu(propertiesHandler, dialogHandler, messageDataSender);
        //infoMenu = new InfoMenu(propertiesHandler, dialogHandler, messageDataSender, stage);
        //helpMenu = new HelpMenu(propertiesHandler, dialogHandler, messageDataSender);

        if(admin) {
            menuBar.getMenus().addAll(connectionMenu.getMenu());
        }
        else {
            menuBar.getMenus().addAll(connectionMenu.getMenu());
        }
        connectionMenu.activate();

        borderPane.setTop(menuBar);

        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        xAxis.setLabel("frequency (Hz)");

        yAxis.setAutoRanging(false);
        yAxis.setForceZeroInRange(false);
        yAxis.setLabel("power level (dBm)");


        lineChart.setTitle("Spectrum Analyzer");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        borderPane.setCenter(lineChart);

        displayConnectToServer();

        HBox hbox = new HBox();

        hbox.getChildren().addAll(statusBold, statusExtension);

        borderPane.setBottom(hbox);

        Scene scene = new Scene(borderPane, 1400, 600);
        stage.setScene(scene);
        stage.setTitle("Satellite and Transponder Radio Frequency Viewer - client application to display signal measurement levels");
        stage.show();
    }

    /**
     * update the status tab in the info menu to show calibration state for the haystack
     * @param packet
     * @param spectrumInformation
     * @param isCalibrated
     */
    private void setCalibrationStatus(SpectrumDataPacket packet, SpectrumInformation spectrumInformation, boolean isCalibrated){

        if(isCalibrated) {
            spectrumInformation.setStatus("calibration state = is calibrated\n" +
                                          "calibrated to power level (dBm) = " + packet.getBaselinePowerLevel() + "\n" +
                                          "calibration date =  " + packet.getCalibrationDate() + "\n");
        }
        else {
            spectrumInformation.setStatus("calibration state = is not calibrated\n" +
                                          "currently defaulted to baseline power level (dBm) = " + packet.getBaselinePowerLevel() + "\n" +
                                          "( the defaulted level is read from the configiration which uses historical data )" );
        }
    }

    /**
     * evaluate status of each packet from the DataPlotter thread and determine what display
     * conditional enable and disable of gui state
     * could be starting up,
     * initializing hold,
     * holding,
     * or doing routine display
     * @param packet
     */
    public void status(SpectrumDataPacket packet) {
        String haystackName = packet.getHaystackName();
        StringBuilder display = new StringBuilder();
        boolean routineDisplay = true;

        //if the program is just getting started, then set some status information received from the incoming packets
        if(isStartUpStatus.get() == true){
            if(isRotationIntervalSet.get() == false){
                isRotationIntervalSet.set(true);
            }

            if(!remainingSpectrumsForStatus.isEmpty()){
                if(remainingSpectrumsForStatus.contains(packet.getHaystackName())){
                    for(SpectrumInformation info : spectrumInformation){
                        if(info.getName().equals(packet.getHaystackName())){
                            if(packet.getIsCalibrated()){
                                setCalibrationStatus(packet, info, true);
                            }else {
                                setCalibrationStatus(packet, info, false);
                            }
                        }
                    }
                    int index = remainingSpectrumsForStatus.indexOf(packet.getHaystackName());
                    remainingSpectrumsForStatus.remove(index);
                    logger.info("removed " + packet.getHaystackName() + " from startup status vector");
                }
            }else{
                isStartUpStatus.set(false);
            }

        }

        if(isRotationIntervalInitializing.get() == true){
            routineDisplay = false;
            isHolding.compareAndSet(true, false);
            isHoldInitializing.compareAndSet(true, false);
            isCalibrationInitializing.compareAndSet(true, false);
            if(currentRotationRequestInterval == packet.getRotationInterval()){
                isRotationIntervalInitializing.set(false);
                routineDisplay = true;
            }
            else{
                haystackName = "";
                display.append("initializing rotation interval " + currentRotationRequestInterval);
            }
        }
        if(isHoldInitializing.get() == true){
            logger.info("hold is initialized - hold request name is " + currentHoldRequestName + " and current haystack name is " + packet.getHaystackName());
            routineDisplay = false;
            isCalibrationInitializing.compareAndSet(true, false);
            isHolding.compareAndSet(true, false);
            if(currentHoldRequestName.equals(haystackName)){
                isHoldInitializing.set(false);
                isHolding.set(true);
                //display.append("holding signal on ");
                //calibrationMenu.enableMenuItem(currentHoldRequestSpectrumOrder);
                //activeDisplay = true;
            }
            else{
                haystackName = "processing";
                display.append(" initializing hold request");
            }
        }
        if(isHolding.get() == true) {
            display.append(" holding signal");
            routineDisplay = true;
        }
        if(isCalibrationInitializing.get() == true){
            routineDisplay = false;
            isHolding.set(false);//it's actually still holding signal on linechart, but text and state change to calibrate
            if(currentCalibrationRequestName.equals(packet.getHaystackName())){
                if(packet.getIsCalibrated()){
                    display.append(" calibration successful - calibration baseline = " + packet.getBaselinePowerLevel() + " choose another menu action to resume rotation or hold another signal to calibrate");

                    for(SpectrumInformation info : spectrumInformation){
                        if(info.getName().equals(packet.getHaystackName())){
                            setCalibrationStatus(packet, info, true);
                        }
                    }
                    //this haystack has been given a status of calibrated so remove it from the status list
                    if(!remainingSpectrumsForStatus.isEmpty()) {
                        if (remainingSpectrumsForStatus.contains(packet.getHaystackName())) {
                            int index = remainingSpectrumsForStatus.indexOf(packet.getHaystackName());
                            remainingSpectrumsForStatus.remove(index);
                        }
                    }
                    //routineDisplay = true;

                }
                else{
                    display.append(" initializing calibration request");
                }
            }
        }

        if(initializeChart){
            adjustAxisPositionsForSpectrum(packet);
            initializeChart = false;
        }
        else {
            if (packet.getHasHaystackRotated()) {
                adjustAxisPositionsForSpectrum(packet);
            }
        }
        if(routineDisplay) {

            StringBuilder defaultOrCalibrated = new StringBuilder();
            if(!packet.getIsCalibrated()) {
                defaultOrCalibrated.append("    default baseline (dBm) ");
            }
            else {
                defaultOrCalibrated.append("    calibrated baseline (dBm) ");
            }
            StringBuilder powerLevelState = new StringBuilder();
            switch(packet.getState()) {
                case(State.powerLevelNormal) : powerLevelState.append("     level is normal (dBm) "); break;
                case(State.powerLevelWarning) : powerLevelState.append("     level is warning (dBm) "); break;
                case(State.powerLevelError) : powerLevelState.append("     level is error (dBm) "); break;
                default : break;
            }
            display.append(" : " + " center is lband " + packet.getLBand() + "(MHz)  cband " + packet.getCBand() +
                    "(MHz)  uplink " + packet.getUpLink() + "(MHz)      " +
                    packet.getTimestamp() + powerLevelState.toString() +
                    defaultOrCalibrated.toString() + packet.getBaselinePowerLevel() +
                    "      sweep average (dBm) " + packet.getAveragePowerLevel());
        }

        String displayText = display.toString();
        setBoldText(haystackName);
        setExtendedText(displayText);
    }

    /**
     * each new spectrum needs an adjustment on the x axis for new center values
     * @param packet
     */
    public void adjustAxisPositionsForSpectrum(SpectrumDataPacket packet){

        //rfView.setStatusText(plotMessage.getHaystackName() + "     " + plotMessage.getTimestamp());
        double currentfCent = packet.getFrequencyCenter();
        double fspan = packet.getFrequencySpan();
        xAxis.setLowerBound((currentfCent - fspan / 2) - adjustXAxis);
        xAxis.setUpperBound((currentfCent + fspan / 2) + adjustXAxis);
        //xAxis.setTickUnit(5000000.0);
        xAxis.setTickUnit(xTickUnit);

        yAxis.setLowerBound(packet.getAveragePowerLevel() - adjustYAxisDown);
        yAxis.setUpperBound(packet.getAveragePowerLevel() + adjustYAxisUp);
        yAxis.setTickUnit(yTickUnit);
    }

    /**
     * hold initializing
     * @param name
     */
    public void setHoldInitializing(String name) {
            isHoldInitializing.set(true);
            currentHoldRequestName = name;
    }

    /**
     * calibration initializing
     * @param name
     */
    public void setCalibrationInitializing(String name) {
        if(isCalibrationInitializing.compareAndSet(false, true)) {
            currentCalibrationRequestName = name;
        }
    }


    /**
     * initializing rotation
     * @param interval
     */
    public void setRotationInitializing(int interval) {
        if(isRotationIntervalInitializing.compareAndSet(false, true)) {
            currentRotationRequestInterval = interval;
        }
    }

    /**
     * disable all calibration choices if rotation is resumed
     */
    public void rotationHasResumed() {
         //disable here
    }

    /**
     * once connected activate everything
     * only if admin privileges (admin=yes) in properties file
     */
    public void activateMenus(){
        connectionMenu.disable();
    }

    /**
     * reset some values if disconnected
     */
    public void resetAfterDisconnect() {
        connectionMenu.activate();

    }

    /**
     * welcome display at startup - prompt to connect to server
     */
    public void displayConnectToServer() {
        setBoldText("Welcome to the Spectrum Analyzer" );
        setExtendedText("   you are currently disconnected - select from menu item 'connection' to connect");
    }

    /**
     * waiting to connect
     */
    public void displayConnecting() {
        setBoldText("TCP Socket ");
        setExtendedText("  connecting to " + serverIpAddress);
    }

    /**
     * if the connect to RFMonitor fails - such as it could not be running
     * @param ipAddress
     */
    public void displayConnectionFailed(String ipAddress){
        setBoldText("TCP Error ");
        setExtendedText("the attempt to connect to server at " + ipAddress + " failed - check if the RFMonitor server is running");
    }

    /**
     * if the worker threads fail to launch
     */
    public void displayThreadsFailedToLaunch() {
        setBoldText("Thread Error ");
        setExtendedText(" - worker threads failed to launch - restarted the application to retry");
    }

    /**
     * if the server goes down
     */
    public void displayMonitorDataError(){
        setBoldText("Server Disconnected");
        setExtendedText("    check if server is still running on " + serverIpAddress + " - close this gui - restart the server");
        terminate = true;
        monitor.shutdown();
        resetAfterDisconnect();
    }

    /**
     * bold leading font
     * for haystack name
     * and some actions
     * @param text
     */
    public void setBoldText(String text) {
        statusBold.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        statusBold.setText(text);
    }

    /**
     * descriptive text in regular type font
     * @param text
     */
    public void setExtendedText(String text) {
        statusExtension.setText(text);
    }


    /**
     * construct the main object to drive the GUI
     * @throws Exception
     */
    public RFView() throws Exception
    {
        logger.info("RFView constructor");
        try
        {
            logger.info("loading properties");
            propertiesHandler = new PropertiesHandler();
            propertiesHandler.listProperties();
            serverIpAddress = propertiesHandler.getRFMonitorIpAddress();
            adjustXAxis = propertiesHandler.getIncreaseXAxis();
            adjustYAxisUp = propertiesHandler.getIncreaseYAxisUp();
            adjustYAxisDown = propertiesHandler.getIncreaseYAxisDown();
            xTickUnit = propertiesHandler.getXTickUnit();
            yTickUnit = propertiesHandler.getYTickUnit();
            admin = propertiesHandler.adminPrivileges();
            messageDataSender = new MessageDataSender(this, dialogHandler);
            monitor = new Monitor(this, propertiesHandler, messageDataSender);
            dialogHandler = new DialogHandler();
            remainingSpectrumsForStatus = new ArrayList<String>(Arrays.asList(Spectrum.spectrums));

        }
        catch(Exception e)
        {
            throw e;
        }

    }


    /**
     * last method called before fx app shuts down
     * @throws Exception
     */
    public void stop() throws Exception
    {
        if(admin) {
            if(isHolding.get() == true) {
                logger.info("setting server back to rotation from hold before exiting client");
                /*
                messageDataSender.sendRotationInterval(rotationInterval);
                setBoldText("Preparing to shut down");
                setExtendedText("resetting server back to resume rotation before exiting");
                */
            }
        }
    }


    /**
     * entry point
     * @param args
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}


