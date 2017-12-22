package com.wdtx.littlebell;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * Created by wending on 2017/6/4.
 */

public class MyApplication extends Application {

    public static MyApplication instance;
    private Handler handler = new Handler();

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        instance = this;
    }

}
