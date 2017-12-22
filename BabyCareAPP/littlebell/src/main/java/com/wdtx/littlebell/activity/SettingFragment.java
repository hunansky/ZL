package com.wdtx.littlebell.activity;

import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.other.BitmapCache;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.service.BleService;
import com.wdtx.littlebell.util.VolleyUtil;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_CONN_STATE;
import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_USER_ID;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_BABY_NAME;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_NAME;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_BABY;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_DEVICE;
import static com.wdtx.littlebell.other.Constant.SERVER_HOST;

public class SettingFragment extends Fragment implements View.OnClickListener{

    private NetworkImageView mHeader;
    private TextView mNickname,mAccount,mProblem,mSetting,mCheckUpdate;
    private LinearLayout mDeviceMsg,mUserMsg;
    private TextView mDeviceName,mConnState,mFamily,mMbApply,mLogout;
    private TextView mBabyName,mAddBaby;

    private User mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initView(view);
        initEvent();
        initDisplay();

        return view;
    }

    public static String[] msg  = {"未连接","正在连接中...","已连接","断开连接中..."};

    @Override
    public void onResume() {
        super.onResume();
        if (MyApplication.instance.getUser().getRole() == 3){
            SharedPreferences preferences = getActivity().getSharedPreferences
                    (PREFERENCES_BIND_BABY,Context.MODE_PRIVATE);
            String baby = preferences.getString(KEY_STRING_BABY_NAME, "宝宝：未绑定");
            mBabyName.setText(baby);
        } else if (MyApplication.instance.getUser().getRole() == 2){
            updateConnState();
            getActivity().registerReceiver(mReceiver,new IntentFilter(BleService
                    .ACTION_BIND_DEVICE_STATE_CHANGE));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (MyApplication.instance.getUser().getRole() == 2){
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnState();
        }
    };

    private void updateConnState() {
        SharedPreferences preferences = getActivity().getSharedPreferences
                (PREFERENCES_BIND_DEVICE, Context.MODE_PRIVATE);
        String deviceName = preferences.getString(KEY_STRING_NAME,"未绑定设备");
        mDeviceName.setText(deviceName);
        int state = preferences.getInt(KEY_INTEGER_CONN_STATE, BluetoothProfile
                .STATE_DISCONNECTED);

        String conn = "连接状态：" + msg[state];

        mConnState.setText(conn);
    }

    private void initView(View view){

        LinearLayout babyModel = (LinearLayout) view.findViewById(R.id.id_ll_babyModel);
        LinearLayout genModel = (LinearLayout) view.findViewById(R.id.id_ll_genModel);
        mUserMsg = (LinearLayout) view.findViewById(R.id.id_ll_userMsg);

        mHeader = (NetworkImageView) view.findViewById(R.id.id_iv_header);
        mNickname = (TextView) view.findViewById(R.id.id_tv_nickname);
        mAccount = (TextView) view.findViewById(R.id.id_tv_account);
        mProblem = (TextView) view.findViewById(R.id.id_tv_problem);
        mCheckUpdate = (TextView) view.findViewById(R.id.id_tv_checkUpdate);
        mSetting = (TextView) view.findViewById(R.id.id_tv_setting);
        mLogout = (TextView) view.findViewById(R.id.id_tv_logout);

        mUser = MyApplication.instance.getUser();

        mHeader.setImageUrl(SERVER_HOST+"/upload/header/" + mUser.getHeader(),
                new ImageLoader(VolleyUtil.getRequestQueue(),new BitmapCache()));

        if (mUser.getRole() == 2){
            genModel.setVisibility(View.GONE);

            mDeviceMsg = (LinearLayout) view.findViewById(R.id.id_ll_deviceMsg);
            mDeviceName = (TextView) view.findViewById(R.id.id_tv_deviceName);
            mConnState = (TextView) view.findViewById(R.id.id_tv_connState);
            mFamily = (TextView) view.findViewById(R.id.id_tv_family);
            mMbApply = (TextView) view.findViewById(R.id.id_tv_mbApply);
        } else if (mUser.getRole() == 3){
            babyModel.setVisibility(View.GONE);

            mBabyName = (TextView) view.findViewById(R.id.id_tv_babyName);
            mAddBaby = (TextView) view.findViewById(R.id.id_tv_addBaby);
        } else {
            genModel.setVisibility(View.GONE);
            babyModel.setVisibility(View.GONE);
        }
    }

    private void initEvent() {
        mUserMsg.setOnClickListener(this);
        mProblem.setOnClickListener(this);
        mCheckUpdate.setOnClickListener(this);
        mSetting.setOnClickListener(this);
        mLogout.setOnClickListener(this);
        if (mDeviceMsg != null){
            mDeviceMsg.setOnClickListener(this);
            mFamily.setOnClickListener(this);
            mMbApply.setOnClickListener(this);
        }

        if (mBabyName != null){
            mBabyName.setOnClickListener(this);
            mAddBaby.setOnClickListener(this);
        }
    }

    private void initDisplay() {
        mNickname.setText(mUser.getNickname());
        mAccount.setText(mUser.getAccount());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.id_ll_deviceMsg:
                FindDeviceActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_problem:
                FeedbackActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_checkUpdate:
                UpdateActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_setting:
                SettingActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_family:
                MemberActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_mbApply:
                ConfirmActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_babyName:
                BabySelectActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_addBaby:
                ApplyActivity.startActivity(getActivity());
                break;
            case R.id.id_tv_logout:
                LoginActivity.startActivity(getActivity());
                getActivity().finish();
                break;
        }
    }
}
