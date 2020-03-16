package com.perriello.rf.spectrumanalyzer.datamgrs;
import com.perriello.rf.rfspectrummessaging.SpectrumDataPacket;
import com.perriello.rf.spectrumanalyzer.config.PropertiesHandler;
import com.perriello.rf.spectrumanalyzer.gui.RFView;
import javafx.scene.text.Text;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles connect to server socket
 * and start up of threads
 * Created by ajperriello on 2/10/2018.
 */
public class Monitor {
    private static final Logger logger = LogManager.getLogger(Monitor.class);
    final ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue = new ConcurrentLinkedQueue<SpectrumDataPacket>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private RFMonitorData rfMonitorData;
    private MessageDataSender messageDataSender;
    private DataPlotter dataPlotter;
    private ExecutorService dataPlotterExecutor;
    private ExecutorService rfMonitorDataExecutor;
    private Thread rfMonitor;
    private Thread plotter;
    private RFView rfView;
    private PropertiesHandler propertiesHandler;

    /**
     *
     * @param rfView
     * @param propertiesHandler
     * @param messageDataSender
     */
    public Monitor(RFView rfView,
                   PropertiesHandler propertiesHandler,
                   MessageDataSender messageDataSender) {
        this.rfView = rfView;
        this.propertiesHandler = propertiesHandler;
        this.messageDataSender = messageDataSender;
    }

    /**
     * connect to the RFMonitor when use triggers menu connection
     * @param ipAddress
     * @param port
     * @return
     */
    public boolean connectToServer(String ipAddress, int port){
        logger.info("connecting to RFMonitor at " + ipAddress + " port " + String.valueOf(port));
        rfView.displayConnecting();
        try {
            socket = new Socket(ipAddress, port);
            logger.info("InetAddress : " + socket.getInetAddress());
            logger.info("port : "+ socket.getPort());
            logger.info("RemoteSocketAddress : " + socket.getRemoteSocketAddress());
            logger.info("LocalPort : " + socket.getLocalPort());
            logger.info("remote socket address " + socket.getRemoteSocketAddress());
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }catch(Exception e){
            logger.info("caught exception connecting to server");
            logger.info(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * launch the worker threads to handle data collection and routing to GUI
     */
    public boolean launchThreads(){
        try {
            //prime the sender to stream commands to the RFMonitor server
            messageDataSender.setDataQueue(dataQueue);
            messageDataSender.setDataOutputStream(dataOutputStream);
            //thread to collect data from RFMonitor server
            rfMonitorData = new RFMonitorData(dataInputStream, rfView, dataQueue);
            rfMonitor = new Thread(rfMonitorData);
            rfMonitor.setDaemon(true);
            rfMonitor.setName("rfMonitor data receiver thread");
            rfMonitorDataExecutor = Executors.newSingleThreadExecutor();
            rfMonitorDataExecutor.execute(rfMonitor);
            //thread to plot the data on main application thread's line graph
            dataPlotter = new DataPlotter(rfView, propertiesHandler, dataQueue);
            plotter = new Thread(dataPlotter);
            plotter.setDaemon(true);
            plotter.setName("plotter thread for data display");
            dataPlotterExecutor = Executors.newSingleThreadExecutor();
            dataPlotterExecutor.execute(plotter);

        }
        catch(IOException ioException){
            logger.error("launchThreads caught  IOException launching worker threads");
            logger.error(ioException.getMessage());
            ioException.printStackTrace();
            rfView.displayThreadsFailedToLaunch();
            return false;
        }
        catch(Exception exception) {
            logger.error("launchThreads caugh exception launching worker threads");
            logger.error(exception.getMessage());
            exception.printStackTrace();
            rfView.displayThreadsFailedToLaunch();
            return false;
        }

        return true;

    }

    public void shutdown() {
        try {
            if(socket != null) {
                socket.close();
            }
            if(dataInputStream != null) {
                dataInputStream.close();
            }
            if(dataOutputStream != null) {
                dataOutputStream.close();
            }
        }
        catch (IOException ioException) {
            logger.error(ioException.getMessage());
        }

        dataQueue.clear();
        rfMonitorData.shutdown();
        dataPlotter.shutdown();
        rfMonitorDataExecutor.shutdown();
        dataPlotterExecutor.shutdown();


    }
}

