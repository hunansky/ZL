package com.wdtx.littlebell.model;

/**
 * Created by wending on 2017/6/29.
 */

public class ServerData implements DisplayableData {

    private int id;
    private int userId;
    private long time;
    private double temperature;
    private double heartRate;
    private Posture posture;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    public Posture getPosture() {
        return posture;
    }

    public void setPosture(Posture posture) {
        this.posture = posture;
    }

    @Override
    public String getDisTemperature() {
        return getTemperature() + " ℃";
    }

    @Override
    public String getDisHeartRate() {
        return getHeartRate() + " bpm";
    }

    @Override
    public String getDisPosture() {
        return getPosture().display();
    }
}
