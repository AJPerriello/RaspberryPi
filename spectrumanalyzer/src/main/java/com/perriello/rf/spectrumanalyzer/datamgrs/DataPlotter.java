package com.perriello.rf.spectrumanalyzer.datamgrs;


import com.perriello.rf.rfspectrummessaging.Commands;
import com.perriello.rf.rfspectrummessaging.*;
import com.perriello.rf.spectrumanalyzer.config.PropertiesHandler;
import com.perriello.rf.spectrumanalyzer.gui.RFView;
import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
//import javafx.scene.control.Dialogs;
import javafx.scene.paint.Color;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Plots the bandwidth (frequency) and power level (dBm) values
 * The thread removes queued packets, determines state, and submits it to the line graph for display on the main GUI thread
 * The objects are shared by server and client using RFSpectrumMessaging
 * Created by ajperriello on 2/10/2018.
 */
public class DataPlotter implements Runnable {
    private static final Logger logger = LogManager.getLogger(DataPlotter.class);
    final ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue;
    PropertiesHandler propertiesHandler;
    ObservableList<XYChart.Data<Number, Number>> list;
    XYChart.Series series;
    NumberAxis xAxis;
    NumberAxis yAxis;
    final RFView rfView;
    int powerLevelState = State.powerLevelNormal;
    private volatile boolean isRunLaterReady = true;

    /**
     * there are a few ways to populate a javafx line chart
     * this way worked the best
     * create a list and then create the series with it - only once
     * the loop in the running thread will populate and clear the list
     * a full sweep of plot points is in the packet object along with state and other plotted data ( frequency, dBm, etc )
     * @param rfView
     * @param propertiesHandler
     * @param dataQueue
     */
    public DataPlotter(RFView rfView,
                       PropertiesHandler propertiesHandler,
                       ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue){
        this.rfView = rfView;
        this.propertiesHandler = propertiesHandler;
        this.dataQueue = dataQueue;
        list = FXCollections.observableArrayList();
        series = new XYChart.Series(list);
        rfView.lineChart.getData().addAll(series);
    }

    public void shutdown() {

        list.clear();
    }


    /**
     * the packet data contains an array of plot points so the entire sweep can be streamed in a single packet per sweep
     * this way the plot points for the whole sweep can fit into one packet, form the line on the graph, then clear it for the next sweep
     * if the line graph is not cleared at the right time that will cause trouble too
     */
    @Override
    public void run() {

        while (!RFView.terminate) {
            if (isRunLaterReady) {
                logger.debug("runlaterready is true");
                if (!dataQueue.isEmpty()) {
                    logger.debug("the dataQueue is not empty");
                    isRunLaterReady = false;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("entered runlater");
                            Number x;
                            Number y;
                            XYChart.Data<Number, Number> xyPlot;
                            SpectrumDataPacket spectrumDataPacket;
                            list.clear();
                            spectrumDataPacket = dataQueue.remove();
                            //rfView.status(spectrumDataPacket);
                            ArrayList<Double> xValues = spectrumDataPacket.getXValues();
                            ArrayList<Double> yValues = spectrumDataPacket.getYValues();
                            if (xValues.size() != yValues.size()) {
                                logger.error("the size of the x and y vectors is different");
                                return;
                            }
                            for (int i = 0; i < xValues.size(); i++) {
                                //x = xValues.elementAt(i);
                                //y = yValues.elementAt(i);
                                x = xValues.get(i);
                                y = yValues.get(i);
                                xyPlot = new XYChart.Data(x, y);
                                list.add(xyPlot);
                            }
                            powerLevelState = spectrumDataPacket.getState();
                            switch (powerLevelState) {
                                case State.powerLevelNormal:
                                    setLineGraphGreen();
                                    break;
                                case State.powerLevelWarning:
                                    setLineGraphYellow();
                                    break;
                                case State.powerLevelError:
                                    setLineGraphRed();
                                    break;
                                default:
                                    break;
                            }
                            isRunLaterReady = true;
                        }//end run
                    });//end platform runlater
                    logger.debug("runlater ended");
                }
            }//end while
        }
    }

    /**
     * green line means within acceptable levels
     */
    private void setLineGraphGreen()
    {
        Node line = series.getNode().lookup(".chart-series-line");
        Color color = Color.LIMEGREEN;
        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
    }

    /**
     * yellow means the levels are past the configured threshold for warning
     */
    private void setLineGraphYellow()
    {
        Node line = series.getNode().lookup(".chart-series-line");
        Color color = Color.YELLOW;
        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
    }

    /**
     * read means the levels are past the configured threshold for error
     */
    private void setLineGraphRed()
    {
        Node line = series.getNode().lookup(".chart-series-line");
        Color color = Color.CRIMSON;
        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
    }

}