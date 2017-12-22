package com.wdtx.littlebell.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.util.NotificationFactory;

import cn.jpush.android.api.JPushInterface;

import static com.wdtx.littlebell.other.Constant.KEY_BOOLEAN_AWAKE;
import static com.wdtx.littlebell.other.Constant.KEY_BOOLEAN_TEMP_HIGH;
import static com.wdtx.littlebell.other.Constant.KEY_BOOLEAN_TEMP_LOW;
import static com.wdtx.littlebell.other.Constant.KEY_BOOLEAN_WRONG_POSTURE;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_SETTING;

/**
 * Created by wending on 2017/6/29.
 */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(JPushInterface.ACTION_MESSAGE_RECEIVED)) {
            String message = intent.getStringExtra(JPushInterface.EXTRA_MESSAGE);
            handleMessage(message);
        }
    }

    private void handleMessage(String message) {
        String[] split = message.split(",");
        SharedPreferences baby = MyApplication.instance
                .getSharedPreferences(Constant.PREFERENCES_BIND_BABY, Context
                        .MODE_PRIVATE);
        SharedPreferences setting = MyApplication.instance
                .getSharedPreferences(PREFERENCES_SETTING, Context
                        .MODE_PRIVATE);
        int babyId = baby.getInt(Constant.KEY_INTEGER_BABY_ID, -1);

        if (!split[0].equals("self")){
            if (Integer.parseInt(split[0]) != babyId) {
                return;
            }
            if (MyApplication.instance.getUser() == null) {
                return;
            }
        }

        switch (split[1]) {
            case "1":
                if (setting.getBoolean(KEY_BOOLEAN_TEMP_LOW, false)) {
                    NotificationFactory.tempLowNotify(MyApplication.instance);
                }
                break;
            case "2":
                if (setting.getBoolean(KEY_BOOLEAN_TEMP_HIGH, false)) {
                    NotificationFactory.tempHighNotify(MyApplication.instance);
                }
                break;
            case "3":
                if (setting.getBoolean(KEY_BOOLEAN_WRONG_POSTURE, false)) {
                    NotificationFactory.sleepStateError(MyApplication.instance);
                }
                break;
            case "4":
                if (setting.getBoolean(KEY_BOOLEAN_AWAKE, false)) {
                    NotificationFactory.awakeNotify(MyApplication.instance);
                }
                break;
        }
    }
}
