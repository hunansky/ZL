package com.wdtx.littlebell.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.model.BlueData;
import com.wdtx.littlebell.model.Posture;
import com.wdtx.littlebell.model.Temperature;
import com.wdtx.littlebell.model.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by wending on 2017/6/27.
 */

public class Analyzer {
    private Context mContext;
    private Handler mHandler;
    private LinkedList<BlueData> mQueue;

    public Analyzer(Context context, Handler handler, LinkedList<BlueData> queue) {
        this.mContext = context;
        this.mHandler = handler;
        this.mQueue = queue;
    }

    private int temperatureState = 0;

    public void analyzeTemperature(){
        BlueData data = mQueue.getLast();
        if (!data.hasData()){
            return;
        }
        double temperature = data.getTemperature();
        if (temperature > 38.5 && temperatureState != 1){
            temperatureState = 1;
            notify(2);
        } else if(temperature < 32  && temperatureState != -1){
            temperatureState = -1;
            notify(1);
        } else if (temperature >= 32 && temperature <= 38.5){
            temperatureState = 0;
        }
    }

    public void analyzeHeartRate(){

    }

    private Posture posture;
    public void analyzePosture(){
        BlueData data = mQueue.getLast();
        if (!data.hasData()){
            posture = null;
            return;
        }

        Posture current = mQueue.getLast().getPosture();
        if (current == posture){
            return;
        } else {
            posture = current;
            if (posture == Posture.FACE_DOWN){
                notify(3);
                return;
            }
        }

        Posture pre = null;
        int cnt = 0;
        for (BlueData blueData : mQueue) {
            if (pre == null || blueData.getPosture() != pre ){
                pre = blueData.getPosture();
                cnt++;
            }
        }
        if (cnt > 5){
            notify(4);
            mQueue.clear();
        }
    }


    private void notify(final int notifyType){
        User user = MyApplication.instance.getUser();
        Map<String,String> map = new HashMap<>();
        map.put("userId",user.getId()+"");
        map.put("secretKey",user.getSecretKey());
        map.put("notifyType",notifyType+"");
        VolleyUtil.getInstance().requestPost("/notify",map,null);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(JPushInterface.ACTION_MESSAGE_RECEIVED);
                intent.putExtra(JPushInterface.EXTRA_MESSAGE,"self,"+notifyType);
                mContext.sendBroadcast(intent);
            }
        });
    }
}
