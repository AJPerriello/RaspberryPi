package com.perriello.rf.spectrumanalyzer.info;

import com.perriello.rf.rfspectrummessaging.Spectrum;
import com.perriello.rf.spectrumanalyzer.gui.RFView;

/**
 * a enumeration of spectrum information sets
 * Created by aperriello on 2/10/2018.
 */
public enum SpectrumInformation {

    SPECTRUM_1 (Spectrum.s1, "IO pin : "),

    SPECTRUM_2 (Spectrum.s2, "IO pin : "),

    SPECTRUM_3 (Spectrum.s3, "IO pin : "),

    SPECTRUM_4 (Spectrum.s4, "IO pin : "),

    SPECTRUM_5 (Spectrum.s5, "IO pin : "),

    SPECTRUM_6 (Spectrum.s6, "IO pin : "),

    SPECTRUM_7 (Spectrum.s7, "IO pin : "),

    SPECTRUM_8 (Spectrum.s8, "IO pin : ");



    private final String name;
    private final String spectrum;
    private String transponder;
    private String lBand;
    private String cBand;
    private String uplink;
    private String feed;
    private String status;
    private String rfSwitchInputPort;

    SpectrumInformation(String name , String spectrum) { 
    	this.name = name;
    	this.spectrum = spectrum;
    }

    public String getName() {

        return this.name;
    }

    public String getTransponder() {

        return this.transponder;
    }

    public String getLBand() {

        return this.lBand;
    }

    public String getCBand() {

        return this.cBand;
    }

    public String getUplink() {

        return this.uplink;
    }

    public String getFeed() {

        return this.feed;
    }

    public void setStatus(String state) {

        this.status = state;
    }

    public String getStatus() {

        return this.status;
    }

    public void setRfSwitchInputPort(String inputPort) {
        this.rfSwitchInputPort = inputPort;
    }

    public String getRfSwitchInputPort() {

        return this.rfSwitchInputPort;
    }

    public String getTechnicalInformation() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getTransponder());
        stringBuilder.append("\n");
        stringBuilder.append("center frequency is lband");
        stringBuilder.append("\n");
        stringBuilder.append("lband = " + getLBand() + " MHz");
        stringBuilder.append("\n");
        stringBuilder.append("cband = " + getCBand() + " MHz");
        stringBuilder.append("\n");
        stringBuilder.append("uplink = " + getUplink() + " MHz");
        String technicalInformation = stringBuilder.toString();
        return technicalInformation;

    }

}

