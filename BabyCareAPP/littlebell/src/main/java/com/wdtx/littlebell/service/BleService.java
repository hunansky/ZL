package com.wdtx.littlebell.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.model.BlueData;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.DataGenerate;
import com.wdtx.littlebell.util.Analyzer;
import com.wdtx.littlebell.util.VolleyUtil;

import static com.wdtx.littlebell.other.Constant.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by wending on 2017/6/27.
 * ...
 */
public class BleService extends Service{


    public static final String ACTION_CONNECT_BIND_DEVICE = "ACTION_CONNECT_BIND_DEVICE";
    public static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";

    //广播Action
    public static final String ACTION_BIND_DEVICE_STATE_CHANGE =
            "com.wdtx.littlebell.service.BleService.ACTION_BIND_DEVICE_STATE_CHANGE";
    public static final String ACTION_RECEIVED_DATA =
            "com.wdtx.littlebell.service.BleService.ACTION_RECEIVED_DATA";
    public static final String EXTRA_BLUE_DATA = "EXTRA_BLUE_DATA";

    //服务特征UUID
    private final static UUID UUID_TARGET_SERVICE =
            UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private final static UUID UUID_TARGET_CHARACTERISTIC =
            UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private final static UUID UUID_CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private MyCallBack mCallBack = new MyCallBack();
    private Timer mTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        setConnState(BluetoothProfile.STATE_DISCONNECTED);
        mTimer = new Timer();
        mTimer.schedule(new Task(),100,1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null){
            return START_STICKY;
        }
        switch (intent.getAction()){
            case ACTION_CONNECT_BIND_DEVICE:
                connect();
                break;
            case ACTION_DISCONNECT:
                disconnect();
                break;
        }
        Log.i("wending", "onStartCommand: " + intent.getAction());
        return START_STICKY;
    }

    private void disconnect() {
        setConnState(BluetoothProfile.STATE_DISCONNECTED);
        if (mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
    }

    private String currentDevice;

    public boolean connect() {
        String address = getAddress();
        int connState = getConnState();

        if (mBluetoothAdapter == null || address == null) {
            return false;
        }

        if (currentDevice != null && address.equals(currentDevice) &&
                mBluetoothGatt != null && connState == BluetoothProfile.STATE_DISCONNECTED) {
            return mBluetoothGatt.connect();
        }

        currentDevice = address;
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mBluetoothGatt = device.connectGatt(BleService.this, false, mCallBack);
        return true;
    }

    public String getAddress() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREFERENCES_BIND_DEVICE, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_STRING_ADDRESS,null);
    }

    public void setConnState(int mConnState) {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREFERENCES_BIND_DEVICE, MODE_PRIVATE);
        sharedPreferences.edit()
                .putInt(KEY_INTEGER_CONN_STATE,mConnState)
                .apply();
        //系统状态改变，广播通知其他组件
        Intent intent = new Intent(ACTION_BIND_DEVICE_STATE_CHANGE);
        sendBroadcast(intent);
    }

    public int getConnState() {
        SharedPreferences sharedPreferences =
                getSharedPreferences(PREFERENCES_BIND_DEVICE, MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_INTEGER_CONN_STATE,BluetoothProfile.STATE_DISCONNECTED);
    }

    private BlueData mData;
    private LinkedList<BlueData> mQueue = new LinkedList<>();
    private Handler mHandler = new Handler();
    private Analyzer mAnalyzer = new Analyzer(this,mHandler,mQueue);

    private class Task extends TimerTask{

        @Override
        public void run() {
            //将数据装入队列中
            pushDataToQueue();
            BlueData current = mQueue.getLast();
            notifyActivityUpdate(current);
            startAnalyzer();
            uploadData(current);
        }
    }

    private synchronized void setData(BlueData data){
        mData = data;
    }

    private synchronized void pushDataToQueue() {
        if (mQueue.size() >= 10){
            mQueue.poll();
        }
        if (mData == null){
            mQueue.offer(new BlueData(null));
        } else{
            mQueue.offer(mData);
            setData(null);
        }
    }

    private void startAnalyzer() {
        mAnalyzer.analyzeHeartRate();
        mAnalyzer.analyzeTemperature();
        mAnalyzer.analyzePosture();
    }

    private void notifyActivityUpdate(final BlueData current) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ACTION_RECEIVED_DATA);
                intent.putExtra(EXTRA_BLUE_DATA, current);
                sendBroadcast(intent);
            }
        });
    }

    private void uploadData(BlueData current) {
        if (current.hasData()){
            User user = ((MyApplication) getApplication()).getUser();
            if (user == null){
                return;
            }
            Map<String,String> map = new HashMap<>();
            map.put("userId",user.getId()+"");
            map.put("secretKey",user.getSecretKey());
            map.put("time",current.getTime().getTime() + "");
            map.put("temperature",current.getTemperature()+"");
            map.put("heartRate",current.getHeartRate()+"");
            map.put("posture",current.getPosture().toString());
            VolleyUtil.getInstance().requestPost("/sensor",map,null);
        }
    }

    private class MyCallBack extends BluetoothGattCallback{
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            setData(new BlueData(characteristic.getValue()));
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            setConnState(newState);

            //连接成功后马上搜索服务
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //获得目标BluetoothGattService
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            BluetoothGattService service = gatt.getService(UUID_TARGET_SERVICE);

            //获得目标BluetoothGattCharacteristic
            if (service == null) {
                return;
            }
            BluetoothGattCharacteristic characteristic = service
                    .getCharacteristic(UUID_TARGET_CHARACTERISTIC);

            //设置BluetoothGattCharacteristic通知
            if (characteristic == null) {
                return;
            }
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID_CLIENT_CHARACTERISTIC_CONFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
}
