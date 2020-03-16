package com.perriello.rf.spectrumanalyzer.datamgrs;

import com.perriello.rf.rfspectrummessaging.Commands;
import com.perriello.rf.rfspectrummessaging.SpectrumDataPacket;
import com.perriello.rf.spectrumanalyzer.config.PropertiesHandler;
import com.perriello.rf.spectrumanalyzer.gui.RFView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Handles incoming objects from the RFMonitor server
 *  an small library was created for common messaging objects between RFMonitor server and clients
 *  This thread reads the incoming stream and queues the object for the DataPlotter
 * Created by ajperriello on 2/10/2018.
 */
public class RFMonitorData implements Runnable{
    private static final Logger logger = LogManager.getLogger(RFMonitorData.class);
    final ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue;
    private final DataInputStream dataInputStream;
    private final ObjectInputStream objectInputStream;
    final RFView rfView;
    Calendar calendar = Calendar.getInstance();
    Date previousTimeStamp = calendar.getTime();
    private static final int threshold_size = 100;
    private static boolean isQueueSaturated = false;
    private int lastQueueSize = 0;


    public RFMonitorData(DataInputStream dataInputStream,
                         RFView rfView,
                         ConcurrentLinkedQueue<SpectrumDataPacket> dataQueue) throws IOException {
            this.dataInputStream = dataInputStream;
            this.objectInputStream = new ObjectInputStream(dataInputStream);
            this.rfView = rfView;
            this.dataQueue = dataQueue;

    }

    public void shutdown() {
        try {
            if(objectInputStream != null) {
                objectInputStream.close();
            }
            if(dataInputStream != null) {
                dataInputStream.close();
            }
        }
        catch (IOException ioException) {
            logger.error(ioException.getMessage());
        }
    }


    /**
     * a very trimmed down thread
     * it use to be way more complicated than it needed to be
     * the creation of a packet object greatly simplified incoming data
     * just read an object from the socket stream and queue it for the DataPlotter thread
     * it was tricky to sync the timing of reading the stream and queueing ojbects while updating the gui
     * at one point I was using all sorts of chart damping speed up and slow down tricks
     * it's very simple and robust now
     *  the synchronization after playing with the code for awhile
     * this timing of removing from the objects from the input stream,
     * adding object to the work queue,
     * and removing from queue ( in DataPlotter thread )
     * then plotting it on the applications thread and clearing the graph each time ( DataPlotter thread )
     * the queue size should stay pretty much around 1,2, or zero - meaning the two threads are in sync
     * nevertheless check to see if the thread does grow to large
     */
    @Override
    public void run() {

        try
        {
            while(!RFView.terminate)
            {
                SpectrumDataPacket queueItem = (SpectrumDataPacket) objectInputStream.readObject();
                /*
                Calendar nowCalendar = Calendar.getInstance();
                Date now = nowCalendar.getTime();
                long diff = now.getTime()- previousTimeStamp.getTime();
                long oneMinute = 60L*1000L;
                if(diff >= oneMinute)
                {
                    previousTimeStamp.setTime(now.getTime());
                    logger.debug("size of queue is " + dataQueue.size());
                }
                */

                if (dataQueue.size() > threshold_size) {
                        logger.info("queue maintenance needed : queue size " + dataQueue.size() + " threshold size " + threshold_size);
                        isQueueSaturated = true;
                        lastQueueSize = dataQueue.size();
                }
                if(isQueueSaturated) {
                    logger.info("sleeping to give DataPlotter thread some cycles to empty queue");
                    try{
                        Thread.sleep(300);
                    }
                    catch (InterruptedException ie) {
                        logger.info("thread sleep caught exception");
                    }
                    logger.info("size of queue is " + dataQueue.size());
                    if(lastQueueSize == dataQueue.size()) {
                        dataQueue.clear();
                        logger.info("queue has been cleared");
                    }
                    if(dataQueue.isEmpty()) {
                        logger.info("dataQueue is now empty");
                        logger.info("will resume collecting spectrum data");
                        isQueueSaturated = false;
                    }
                }
                else {
                    dataQueue.add(queueItem);
                }
            }
        }
        catch (SocketException socketException) {
            rfView.displayMonitorDataError();
        }
        catch (Exception e) {
            logger.info(e.toString());

        }
    }
}




