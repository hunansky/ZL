package com.wdtx.littlebell.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import com.wdtx.littlebell.R;
import com.wdtx.littlebell.activity.MainActivity;

/**
 * Created by wending on 2017/6/29.
 */


public class NotificationFactory {

    private static void sendNotification(
            Context context, String message, Uri uri, int id){
        Intent notificationIntent = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0,notificationIntent, 0);
        Notification notification =
                new Notification.Builder(context)
                        .setContentTitle(message)
                        .setContentText("点击查看小宝贝的状态...")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .setSound(uri)
                        .setVibrate(new long[]{0,1000,1000,1000})
                        .build();
        notification.ledARGB = Color.GREEN;
        notification.ledOnMS = 1000;
        notification.ledOffMS = 1000;
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
        manager.notify(id, notification);
    }

    public static void tempLowNotify(Context context){
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/"+ R.raw.temp_low);
        sendNotification(context,"宝宝的体温过低",uri,0);
    }

    public static void tempHighNotify(Context context){
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/"+R.raw.temp_high);
        sendNotification(context,"宝宝的体温过高",uri,0);
    }

    public static void sleepStateError(Context context){
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/"+R.raw.error_sleep_state);
        sendNotification(context,"宝宝正在趴着睡",uri,0);
    }

    public static void awakeNotify(Context context){
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getPackageName() + "/"+R.raw.awake);
        sendNotification(context,"宝宝睡醒了",uri,0);
    }
}