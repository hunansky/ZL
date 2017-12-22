package com.wdtx.littlebell.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.DisplayableData;
import com.wdtx.littlebell.model.Posture;
import com.wdtx.littlebell.model.ServerData;
import com.wdtx.littlebell.model.Temperature;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.service.BleService;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainFragment extends Fragment
        implements VolleyCallBack<ServerData>,View.OnClickListener {

    private Timer mTimer = new Timer();
    private Task mTask;

    private TextView mTvHeartRate, mTvTemperature, mTvPosture;
    private ImageView mIvShow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mTvTemperature = (TextView) view.findViewById(R.id.id_tv_temperature);
        mTvHeartRate = (TextView) view.findViewById(R.id.id_tv_heart);
        mTvPosture = (TextView) view.findViewById(R.id.id_tv_gesture);
        mIvShow = (ImageView) view.findViewById(R.id.id_iv_img);

        mTvPosture.setOnClickListener(this);
        mTvTemperature.setOnClickListener(this);
        mTvHeartRate.setOnClickListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


        MyApplication application = (MyApplication) getActivity().getApplication();
        User user = application.getUser();

        //宝宝账号
        if (user.getRole() == 2) {
            startBleService();
            registerBroadcast();
        }
        //家长账号
        else {
            mTask = new Task();
            mTimer.schedule(mTask, 0, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (MyApplication.instance.getUser().getRole() == 2){
            getActivity().unregisterReceiver(mReceiver);
        } else {
            mTask.cancel();
        }
    }

    @Override
    public void onSuccess(ServerData data) {
        onReceivedData(data);
    }

    @Override
    public void onFail(int code, String msg) {
        mTvHeartRate.setText("无数据");
        mTvPosture.setText("无数据");
        mTvTemperature.setText("无数据");
    }

    @Override
    public void onError(VolleyError error) {
        mTvHeartRate.setText("网络异常");
        mTvPosture.setText("网络异常");
        mTvTemperature.setText("网络异常");
    }

    private void startBleService() {
        Intent intent = new Intent(getActivity(), BleService.class);
        intent.setAction(BleService.ACTION_CONNECT_BIND_DEVICE);
        getActivity().startService(intent);
        Log.i("wending", "startBleService: " + 110);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter(BleService.ACTION_RECEIVED_DATA);
        getActivity().registerReceiver(mReceiver, filter);
    }

    private void onReceivedData(DisplayableData data) {
        mTvHeartRate.setText(data.getDisHeartRate());
        mTvTemperature.setText(data.getDisTemperature());
        mTvPosture.setText(data.getDisPosture());
        switch (data.getDisPosture()){
            case "左侧睡":
                mIvShow.setImageResource(R.drawable.img_4);
                break;
            case "右侧睡":
                mIvShow.setImageResource(R.drawable.img_2);
                break;
            case "趴着睡":
                mIvShow.setImageResource(R.drawable.img_3);
                break;
            default:
                mIvShow.setImageResource(R.drawable.img_1);
                break;

        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case BleService.ACTION_RECEIVED_DATA:
                    DisplayableData data = (DisplayableData) intent
                            .getSerializableExtra(BleService.EXTRA_BLUE_DATA);
                    onReceivedData(data);
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_tv_heart:
                HeartRateActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_temperature:
                TemperatureActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_gesture:
                PostureActivity.startActivity(getActivity());
                break;
        }
    }

    private class Task extends TimerTask {
        @Override
        public void run() {
            SharedPreferences preferences = getActivity().getSharedPreferences(
                    Constant.PREFERENCES_BIND_BABY,Context.MODE_PRIVATE);

            int babyId = preferences.getInt(Constant.KEY_INTEGER_BABY_ID,-1);
            User user = MyApplication.instance.getUser();
            Map<String,String> map = new HashMap<>();
            map.put("userId",user.getId()+"");
            map.put("watchId",babyId+"");
            map.put("secretKey",user.getSecretKey());
            VolleyUtil.getInstance().requestPost("/latest", map, MainFragment.this);
        }
    }
}
