package com.wdtx.littlebell.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.service.BleService;

import java.util.ArrayList;
import java.util.List;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_CONN_STATE;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_ADDRESS;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_NAME;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_DEVICE;

public class FindDeviceActivity extends AppCompatActivity
        implements View.OnClickListener,BluetoothAdapter.LeScanCallback{

    public static void startActivity(Context context){
        Intent intent = new Intent(context,FindDeviceActivity.class);
        context.startActivity(intent);
    }

    private LinearLayout[] devices = new LinearLayout[4];
    private TextView[] deviceNames = new TextView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_device);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("绑定设备");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        devices[0] = (LinearLayout) findViewById(R.id.id_ll_device_1);
        devices[1] = (LinearLayout) findViewById(R.id.id_ll_device_2);
        devices[2] = (LinearLayout) findViewById(R.id.id_ll_device_3);
        devices[3] = (LinearLayout) findViewById(R.id.id_ll_device_4);

        deviceNames[0] = (TextView) findViewById(R.id.id_tv_device_1);
        deviceNames[1] = (TextView) findViewById(R.id.id_tv_device_2);
        deviceNames[2] = (TextView) findViewById(R.id.id_tv_device_3);
        deviceNames[3] = (TextView) findViewById(R.id.id_tv_device_4);

        for (LinearLayout d : devices) {
            d.setOnClickListener(this);
        }

        //android sdk23权限申请
        int checkAnswer = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (checkAnswer != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS);

        ((RippleBackground) findViewById(R.id.content)).startRippleAnimation();

        SharedPreferences preferences = getSharedPreferences
                (PREFERENCES_BIND_DEVICE,MODE_PRIVATE);
        String address = preferences.getString(KEY_STRING_ADDRESS, null);
        if (address != null){
            BluetoothDevice remoteDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            onLeScan(remoteDevice,0,null);
        }
    }

    private List<BluetoothDevice> foundDevices = new ArrayList<>();

    @Override
    public void onClick(View v) {
        int position = 0;
        switch (v.getId()){
            case R.id.id_ll_device_1:
                position = 0;
                break;
            case R.id.id_ll_device_2:
                position = 1;
                 break;
            case R.id.id_ll_device_3:
                position = 2;
                break;
            case R.id.id_ll_device_4:
                position = 3;
                break;
        }
        BluetoothDevice device = foundDevices.get(position);
        SharedPreferences preferences = getSharedPreferences
                (PREFERENCES_BIND_DEVICE,MODE_PRIVATE);
        preferences.edit()
                .putString(KEY_STRING_NAME,device.getName())
                .putString(KEY_STRING_ADDRESS,device.getAddress())
                .putInt(KEY_INTEGER_CONN_STATE, BluetoothProfile.STATE_DISCONNECTED)
                .apply();

        Intent intent = new Intent(this, BleService.class);
        intent.setAction(BleService.ACTION_CONNECT_BIND_DEVICE);
        startService(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter.getDefaultAdapter().startLeScan(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothAdapter.getDefaultAdapter().stopLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(!foundDevices.contains(device)){
            foundDevices.add(device);
            updateUI();
        }
    }

    private void updateUI(){
        for (int i = 0; i < foundDevices.size(); i++) {
            devices[i].setVisibility(View.VISIBLE);
            deviceNames[i].setText(foundDevices.get(i).getName());
        }
    }
}
