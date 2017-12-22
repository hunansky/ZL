package com.wdtx.littlebell.model;

import java.util.Date;

/**
 * Created by wending on 2017/7/1.
 * ...
 */
public class HeartRate {
    private int id;
    private int userId;
    private long time;
    private double heartRate;

    public HeartRate() {
    }

    public HeartRate(int userId, long time, double heartRate) {
        this.userId = userId;
        this.time = time;
        this.heartRate = heartRate;
    }

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

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    @Override
    public String toString() {
        return "HeartRate{" +
                "id=" + id +
                ", userId=" + userId +
                ", time=" + new Date(time) +
                ", heartRate=" + heartRate +
                '}';
    }
}
