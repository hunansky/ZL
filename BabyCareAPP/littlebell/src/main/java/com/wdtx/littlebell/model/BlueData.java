package com.wdtx.littlebell.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by wending on 2017/6/26.
 */

public class BlueData implements DisplayableData,Serializable{

    private final byte[] data;
    private final Date time;

    public BlueData(byte[] data){
        this.data = data;
        time = new Date();
    }

    public byte[] getData() {
        return data;
    }

    public boolean hasData(){
        return data != null;
    }

    public double getTemperature(){
        if (hasData()){
            return readShort(0) / 100d;
        }
        return 0;
    }

    public double getHeartRate(){
        if (hasData()){
            return readShort(2);
        }
        return 0;
    }

    public Posture getPosture(){
        if (hasData()){
            if (readShort(4) < -10000){
                return Posture.RIGHT_LYING;
            } else if (readShort(6) < -10000){
                return Posture.STAND;
            } else if (readShort(6) > 10000){
                return Posture.STAND;
            } else if (readShort(8)<-10000){
                return Posture.LYING;
            } else if (readShort(8) > 10000){
                return Posture.FACE_DOWN;
            } else {
                return Posture.LEFT_LYING;
            }
        }
        return null;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String getDisTemperature() {
        if (hasData()){
            return getTemperature() + " ℃";
        }
        return "无数据";
    }

    @Override
    public String getDisHeartRate() {
        if (hasData()){
            return getHeartRate() + " bpm";
        }
        return "无数据";
    }

    @Override
    public String getDisPosture() {
        if (hasData()){
            return getPosture().display();
        }
        return "无数据";
    }

    private short readShort(int offset) {
        return (short) ((data[offset] & 0x0ff) << 8 | (data[offset + 1] & 0x0ff));
    }
}
