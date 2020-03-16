package com.perriello.rf.rfspectrummessaging;

/**
 * Created by ajperriello on 2/10/2018.
 */
public class Commands {

    public static final int display = 0;
    public static final int calibrate = 1;
    public static final int calibrateCompleted = 2;
    public static final int calibrateCancel = 3;

    public static final int setRotationInterval = 100;

    public static final int setHoldSignal = 200;

    public static final int setLoggingLevel = 300;
    public static final int setLoggingInterval = 301;

    public static final int setAlarmLevel = 400;
    public static final int setAlarmBandwidth = 401;

    public static final int initializeCalibration = 800;
    public static final int setCalibration = 801;

    public static final int disconnecting = 900;
    public static final int suspend = 901;
    public static final int resume = 902;

}
