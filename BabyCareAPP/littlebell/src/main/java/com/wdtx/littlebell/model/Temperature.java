package com.wdtx.littlebell.model;

import java.util.Date;

/**
 * Created by wending on 2017/7/1.
 * ...
 */
public class Temperature {

    private int id;
    private int userId;
    private long time;
    private double temperature;

    public Temperature() {
    }

    public Temperature(int userId, long time, double temperature) {
        this.userId = userId;
        this.time = time;
        this.temperature = temperature;
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

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "id=" + id +
                ", userId=" + userId +
                ", time=" + new Date(time) +
                ", temperature=" + temperature +
                '}';
    }
}
