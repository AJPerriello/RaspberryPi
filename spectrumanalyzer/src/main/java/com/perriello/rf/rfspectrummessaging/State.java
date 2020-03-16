package com.perriello.rf.rfspectrummessaging;
/**
 * Created by ajperriello on 2/10/2018.
 */
public class State {

    public static final float warningThreshold = (float)5.0;
    public static final float errorThreshold = (float)6.0;

    public static final int initialized = 2;
    public static final int calibrated = 1;
    public static final int powerLevelNormal = 0;
    public static final int powerLevelWarning  = -1;
    public static final int powerLevelError = -2;
}

