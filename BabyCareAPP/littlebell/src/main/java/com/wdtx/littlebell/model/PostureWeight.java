package com.wdtx.littlebell.model;

/**
 * Created by wending on 2017/7/1.
 * ...
 */
public class PostureWeight {
    private int userId;
    private Posture posture;
    private int percentage;
    private int minute;

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
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

    @Override
    public String toString() {
        return "PostureWeight{" +
                "userId=" + userId +
                ", posture=" + posture +
                ", percentage=" + percentage +
                ", minute=" + minute +
                '}';
    }
}
