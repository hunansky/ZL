package com.wdtx.littlebell.model;

import java.util.Date;

/**
 * Created by wending on 2017/7/1.
 * ...
 */
public class PostureDistribute {

    private int id;
    private int userId;
    private Posture posture;
    private long startTime;
    private long endTime;

    public PostureDistribute() {
    }

    public PostureDistribute(int userId, Posture posture, long startTime, long endTime) {
        this.userId = userId;
        this.posture = posture;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public Posture getPosture() {
        return posture;
    }

    public void setPosture(Posture posture) {
        this.posture = posture;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "PostureDistribute{" +
                "id=" + id +
                ", userId=" + userId +
                ", posture=" + posture +
                ", startTime=" + new Date(startTime) +
                ", endTime=" + new Date(endTime) +
                '}';
    }
}
