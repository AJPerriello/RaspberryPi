package com.perriello.rf.rfspectrummessaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Represents a packet of sweep data
 * This object is passed between server and an output type ( such as a client GUI, log, database, etc )
 * Created by aperriello on 4/13/2015.
 */
public class SpectrumDataPacket implements Serializable {

    private String haystackName;
    private int inputSwitch;
    private double frequencyCenter;
    private double frequencySpan;
    private int lBand;
    private int cBand;
    private int upLink;
    private float baselinePowerLevel;
    private float averagePowerLevel;
    private float minWarningPowerLevel;
    private float maxWarningPowerLevel;
    private float minErrorPowerLevel;
    private float maxErrorPowerLevel;
    private int powerLevelState;
    private boolean isCalibrated;
    private String calibrationDate;
    private int rotationInterval;
    private int logInterval;
    private boolean hasHaystackRotated;
    private int command;
    private int state;
    private String timestamp;
    private long timestampMilliseconds;
    ArrayList<Double> xValues;
    ArrayList<Double> yValues;

    public SpectrumDataPacket(String haystackName,
                              int inputSwitch,
                              double frequencyCenter,
                              double frequencySpan,
                              int lBand,
                              int cBand,
                              int upLink,
                              float baselinePowerLevel,
                              float averagePowerLevel,
                              float minWarningPowerLevel,
                              float maxWarningPowerLevel,
                              float minErrorPowerLevel,
                              float maxErrorPowerLevel,
                              int powerLevelState,
                              boolean isCalibrated,
                              String calibrationDate,
                              int rotationInterval,
                              int logInterval,
                              boolean hasHaystackRotated,
                              int command,
                              int state,
                              String timestamp,
                              long timestampMilliseconds,
                              ArrayList<Double> xAxisValues,
                              ArrayList<Double> yAxisValues) {

        this.haystackName = haystackName;
        this.inputSwitch = inputSwitch;
        this.frequencyCenter = frequencyCenter;
        this.frequencySpan = frequencySpan;
        this.lBand = lBand;
        this.cBand = cBand;
        this.upLink = upLink;
        this.baselinePowerLevel = baselinePowerLevel;
        this.averagePowerLevel = averagePowerLevel;
        this.minWarningPowerLevel = minWarningPowerLevel;
        this.maxWarningPowerLevel = maxWarningPowerLevel;
        this.minErrorPowerLevel = minErrorPowerLevel;
        this.maxErrorPowerLevel = maxErrorPowerLevel;
        this.powerLevelState = powerLevelState;
        this.isCalibrated = isCalibrated;
        this.calibrationDate = calibrationDate;
        this.rotationInterval = rotationInterval;
        this.logInterval = logInterval;
        this.hasHaystackRotated = hasHaystackRotated;
        this.command = command;
        this.state = state;
        this.timestamp = timestamp;
        this.timestampMilliseconds = timestampMilliseconds;
        this.xValues = xAxisValues;
        this.yValues = yAxisValues;
    }

    public void setHaystackName(String haystackName) {

        this.haystackName = haystackName;
    }

    public String getHaystackName() {

        return this.haystackName;
    }

    public void setInputSwitch(int inputSwitch) {

        this.inputSwitch = inputSwitch;
    }

    public int getInputSwitch() {

        return this.inputSwitch;
    }

    public void setFrequencyCenter(double frequencyCenter){

        this.frequencyCenter = frequencyCenter;
    }

    public double getFrequencyCenter() {

        return this.frequencyCenter;
    }

    public void setFrequencySpan(double frequencySpan){

        this.frequencySpan = frequencySpan;
    }

    public double getFrequencySpan() {

        return this.frequencySpan;
    }

    public void setLBand(int lBand) {

        this.lBand = lBand;
    }

    public int getLBand() {

        return this.lBand;
    }

    public void setCBand(int cBand) {

        this.cBand = cBand;
    }

    public int getCBand() {

        return this.cBand;
    }

    public void setUpLink(int upLink) {

        this.upLink = upLink;
    }

    public int getUpLink() {

        return this.upLink;
    }

    public void setBaselinePowerLevel(float baselinePowerLevel) {

        this.baselinePowerLevel = baselinePowerLevel;
    }

    public float getBaselinePowerLevel(){

        return this.baselinePowerLevel;
    }

    public void setAveragePowerLevel(float averagePowerLevel) {

        this.averagePowerLevel = averagePowerLevel;
    }

    public float getAveragePowerLevel(){

        return this.averagePowerLevel;
    }

    public void setMinWarningPowerLevel(float minWarningPowerLevel) {

        this.minWarningPowerLevel = minWarningPowerLevel;
    }

    public float getMinWarningPowerLevel() {

        return this.minWarningPowerLevel;
    }

    public void setMaxWarningPowerLevel(float maxWarningPowerLevel) {

        this.maxWarningPowerLevel = maxWarningPowerLevel;
    }

    public void setMinErrorPowerLevel(float minErrorPowerLevel) {

        this.minErrorPowerLevel = minErrorPowerLevel;
    }

    public float getMinErrorPowerLevel() {

        return this.minErrorPowerLevel;
    }

    public void setMaxErrorPowerLevel(float maxErrorPowerLevel) {

        this.maxErrorPowerLevel = maxErrorPowerLevel;
    }

    public float getMaxErrorPowerLevel() {

        return this.maxErrorPowerLevel;
    }

    public float getMaxWarningPowerLevel() {

        return this.maxWarningPowerLevel;
    }

    public void setPowerLevelState(int powerLevelState) {

        this.powerLevelState = powerLevelState;
    }

    public int getPowerLevelState() {

        return this.powerLevelState;
    }

    public void setIsCalibrated(boolean isCalibrated) {

        this.isCalibrated = isCalibrated;
    }

    public boolean getIsCalibrated() {

        return this.isCalibrated;
    }

    public void setCalibrationDate(String calibrationDate) {

        this.calibrationDate = calibrationDate;
    }

    public String getCalibrationDate() {

        return this.calibrationDate;
    }

    public void setRotationInterval(int rotationInterval) {

        this.rotationInterval = rotationInterval;
    }

    public int getRotationInterval() {

        return this.rotationInterval;
    }

    public void setLogInterval(int logInterval) {

        this.logInterval = logInterval;
    }

    public int getLogInterval() {

        return this.logInterval;
    }

    public void setHasHaystackRotated(boolean hasHaystackRotated) {

        this.hasHaystackRotated = hasHaystackRotated;
    }

    public boolean getHasHaystackRotated() {

        return this.hasHaystackRotated;
    }

    public void setCommand(int command) {

        this.command = command;
    }

    public int getCommand() {

        return this.command;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return this.state;
    }

    public void setTimestamp(String timestamp){

        this.timestamp = timestamp;
    }

    public String getTimestamp() {

        return this.timestamp;
    }

    public void setTimestampMilliseconds(long timestampMilliseconds) {
        this.timestampMilliseconds = timestampMilliseconds;
    }

    public long getTimestampMilliseconds() {
        return this.timestampMilliseconds;
    }

    public void setXValues(ArrayList<Double> xValues){

        this.xValues = xValues;
    }

    public ArrayList<Double> getXValues(){

        return this.xValues;
    }

    public void setYValues(ArrayList<Double> yValues) {

        this.yValues = yValues;
    }

    public ArrayList<Double> getYValues() {

        return yValues;
    }

}

