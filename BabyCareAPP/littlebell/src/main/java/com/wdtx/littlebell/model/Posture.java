package com.wdtx.littlebell.model;

/**
 * Created by wending on 2017/6/26.
 */

public enum Posture {
    LEFT_LYING{
        public String display(){
            return "左侧睡";
        }
    },
    RIGHT_LYING{
        public String display(){
            return "右侧睡";
        }
    },
    AWAKE{
        public String display(){
            return "睡醒";
        }
    },
    FACE_DOWN{
        public String display(){
            return "趴着睡";
        }
    },
    LYING{
        public String display(){
            return "仰面睡";
        }
    },
    STAND{
        public String display(){
            return "站立";
        }
    };

    public String display(){
        return "";
    }
}
