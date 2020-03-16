package com.perriello.rf.spectrumanalyzer.config;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.Vector;

/**
 * Created by ajperriello on 2/10/2018.
 */

public class PropertiesHandler {
    private static final Logger logger = LogManager.getLogger(PropertiesHandler.class);
    Vector<String> propertyErrors = new Vector<String>();
    private final Properties properties;
    private final String rfMonitorIpAddressProperty = "rfMonitor_IpAddress";
    private final String rfMonitorPortProperty = "rfMonitor_Port";
    private final String increaseXAxisProperty = "increase_x_axis";
    private final String increaseYAxisUpProperty = "increase_y_axis_up";
    private final String increaseYAxisDownProperty = "increase_y_axis_down";
    private final String xTickUnitProperty = "x_tick_unit";
    private final String yTickUnitProperty = "y_tick_unit";
    private final String adminProperty = "admin";

    private final String rfMonitorIpAddressValue;
    private final String rfMonitorPortValue;
    private final String increaseXAxisValue;
    private final String increaseYAxisUpValue;
    private final String increaseYAxisDownValue;
    private final String xTickUnitValue;
    private final String yTickUnitValue;
    private final String adminValue;


    public PropertiesHandler() throws Exception {
        properties = new Properties();
        String propertiesFile = "config.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);

        if (inputStream != null) {
            properties.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propertiesFile + "' not found in the classpath");
        }

        rfMonitorIpAddressValue = properties.getProperty(rfMonitorIpAddressProperty);
        rfMonitorPortValue = properties.getProperty(rfMonitorPortProperty);
        increaseXAxisValue = properties.getProperty(increaseXAxisProperty);
        increaseYAxisUpValue = properties.getProperty(increaseYAxisUpProperty);
        increaseYAxisDownValue = properties.getProperty(increaseYAxisDownProperty);
        xTickUnitValue = properties.getProperty(xTickUnitProperty);
        yTickUnitValue = properties.getProperty(yTickUnitProperty);
        adminValue = properties.getProperty(adminProperty);

        validateProperties();
        validateValues();
        if(propertyErrors.isEmpty())
            logger.info("properties were loaded successfully");
        else
        {
            printErrors();
            throw new Exception("properties errors encountered");
        }
    }

    private void validateProperties(){

        if(rfMonitorIpAddressValue == null)
            propertyErrors.add("property that should be named " + rfMonitorIpAddressProperty + " is null - check spelling");
        if(rfMonitorPortValue == null)
            propertyErrors.add("property that should be named " + rfMonitorPortProperty + " is null - check spelling");
        if(increaseXAxisValue == null)
            propertyErrors.add("property that should be named " + increaseXAxisProperty + " is null - check spelling");
        if(increaseYAxisUpValue == null)
            propertyErrors.add("property that should be named " + increaseYAxisUpProperty + " is null - check spelling");
        if(increaseYAxisDownValue == null)
            propertyErrors.add("property that should be named " + increaseYAxisDownProperty + " is null - check spelling");
        if(xTickUnitValue == null)
            propertyErrors.add("property that should be named " + xTickUnitProperty + " is null - check spelling");
        if(yTickUnitValue == null)
            propertyErrors.add("property that should be named " + yTickUnitProperty + " is null - check spelling");
        if(adminValue == null)
            propertyErrors.add("property that should be name " + adminProperty + " is null - check spelling");
    }


    public void listProperties() {
        logger.info(rfMonitorIpAddressProperty + " = " + rfMonitorIpAddressValue);
        logger.info(rfMonitorPortProperty + " = " + rfMonitorPortValue);
        logger.info(increaseXAxisProperty + " = " + increaseXAxisValue);
        logger.info(increaseYAxisUpProperty + " = " + increaseYAxisUpValue);
        logger.info(increaseYAxisDownProperty + " = " + increaseYAxisDownValue);
        logger.info(xTickUnitProperty + " = " + xTickUnitValue);
        logger.info(yTickUnitProperty + " = " + yTickUnitValue);
        logger.info(adminProperty + " = " + adminValue);

    }

    private void printErrors() {
        logger.info("properties were loaded with the following errors, please correct and relaunch");
        for (int i = 0; i < propertyErrors.size(); i++) {
            logger.info(propertyErrors.elementAt(i));
        }
    }
    private boolean validateYesNo(String s){
        return(!s.equals("no") || !s.equals("yes"));
    }

    private void validateValues(){

        String prefix = "invalid entries found in config.properties : ";
        if(!validateYesNo(adminValue))
            propertyErrors.add(prefix + adminProperty + " = " + adminValue);
    }

    public String getRFMonitorIpAddress(){
        return rfMonitorIpAddressValue;
    }

    public int getRFMonitorPort() {
        int value = Integer.parseInt(rfMonitorPortValue);
        return value;
    }

    public double getIncreaseXAxis() {
        double propertyEntry = Double.parseDouble(increaseXAxisValue);
        double value = propertyEntry * 1000000; //convert to MHz
        return value;
    }

    public double getIncreaseYAxisUp() {
        double propertyEntry = Double.parseDouble(increaseYAxisUpValue);
        //no conversion
        return propertyEntry;
    }

    public double getIncreaseYAxisDown() {
        double propertyEntry = Double.parseDouble(increaseYAxisDownValue);
        //no conversion
        return propertyEntry;
    }

    public double getXTickUnit() {
        double propertyEntry = Double.parseDouble(xTickUnitValue);
        double value = propertyEntry * 1000000; //convert to MHz
        return value;
    }

    public double getYTickUnit() {
        double propertyEntry = Double.parseDouble(yTickUnitValue);
        //no conversion
        return propertyEntry;
    }
    public boolean adminPrivileges() {
            if(adminValue.equals("no"))
                return false;
            else return true;
    }

}


