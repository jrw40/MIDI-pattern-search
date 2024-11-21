package com.example.ug_project.model;

public class TrackEvent {
    private int deltaTime;
    private String type;
    private String message;
    private int channelNumber;
    private String data1;
    private String data2;

    public TrackEvent(int deltaTime) {
        this.deltaTime = deltaTime;
    }

    public TrackEvent(int deltaTime, String type, String message, int channelNumber, String data1, String data2) {
        this.deltaTime = deltaTime;
        this.type = type;
        this.message = message;
        this.channelNumber = channelNumber;
        this.data1 = data1;
        this.data2 = data2;
    }

    public TrackEvent(int deltaTime, String type, String message, int channelNumber, String data1) {
        this.deltaTime = deltaTime;
        this.type = type;
        this.message = message;
        this.channelNumber = channelNumber;
        this.data1 = data1;
        this.data2 = "NA";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public String getData2() {
        return data2;
    }

    public void setData2(String data2) {
        this.data2 = data2;
    }

    public int getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(int deltaTime) {
        this.deltaTime = deltaTime;
    }
}
