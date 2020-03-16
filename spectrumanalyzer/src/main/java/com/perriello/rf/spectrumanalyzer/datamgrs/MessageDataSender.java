package com.perriello.rf.spectrumanalyzer.datamgrs;

import com.perriello.rf.rfspectrummessaging.Commands;
import com.perriello.rf.rfspectrummessaging.SpectrumDataPacket;
import com.perriello.rf.spectrumanalyzer.gui.DialogHandler;
import com.perriello.rf.spectrumanalyzer.gui.RFView;
import javafx.scene.text.Text;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.apache.log4j.message.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles sending commands to the RFMonitor, using the custom RFSpectrumMessaging library
 * like most of the code in this app, it has been greatly simplified and a lot of code taken out
 * keep it simple for first install
 * can get more fancy later
 * Created by ajperriello on 2/10/2018.
 */
public class MessageDataSender {
    private static final Logger logger = LogManager.getLogger(MessageDataSender.class);
    private DataOutputStream dataOutputStream;
    private final RFView rfView;
    private final DialogHandler dialogHandler;
    private ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue;

    public MessageDataSender(RFView rfv, DialogHandler dh){
        rfView = rfv;
        dialogHandler = dh;
    }

    public void setDataQueue(ConcurrentLinkedQueue<SpectrumDataPacket> dq ) {
        dataQueue = dq;
    }

    public void setDataOutputStream(DataOutputStream os) {
        dataOutputStream = os;
    }

    public void sendRotationInterval(int value) throws IOException{
        //rfView.setStatusText("processing request for rotation interval of " + value + " seconds ...");
        rfView.setRotationInitializing(value);
        dataOutputStream.writeInt(Commands.setRotationInterval);
        dataOutputStream.writeInt(value);
    }

    public void setCalibrationLevel(String name) throws IOException {
        logger.info("sending calibration command for " + name);
        rfView.setCalibrationInitializing(name);
        int length = name.length();
        dataOutputStream.writeInt(Commands.setCalibration);
        dataOutputStream.writeInt(length);
        dataOutputStream.writeChars(name);

    }

    public void setHoldSignal(String name) throws IOException {
        logger.info("sending hold signal for " + name);
        rfView.setHoldInitializing(name);
        int length = name.length();
        dataOutputStream.writeInt(Commands.setHoldSignal);
        dataOutputStream.writeInt(length);
        dataOutputStream.writeChars(name);
    }
}
