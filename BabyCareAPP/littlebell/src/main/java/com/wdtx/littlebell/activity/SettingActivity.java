package com.wdtx.littlebell.activity;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.wdtx.littlebell.R;
import com.wdtx.littlebell.other.Constant;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingActivity extends AppCompatActivity
implements CompoundButton.OnCheckedChangeListener{

    public static void startActivity(Context context){
        Intent intent = new Intent(context,SettingActivity.class);
        context.startActivity(intent);
    }

    Switch switch1,switch2,switch3,switch4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("系统设置");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        switch1 = (Switch) findViewById(R.id.id_temp_1);
        switch2 = (Switch) findViewById(R.id.id_temp_2);
        switch3 = (Switch) findViewById(R.id.id_temp_3);
        switch4 = (Switch) findViewById(R.id.id_temp_4);


        SharedPreferences preferences = getSharedPreferences(Constant
                .PREFERENCES_SETTING,MODE_PRIVATE);
        switch1.setChecked(preferences.getBoolean(Constant.KEY_BOOLEAN_TEMP_LOW,false));
        switch2.setChecked(preferences.getBoolean(Constant.KEY_BOOLEAN_TEMP_HIGH,
                false));
        switch3.setChecked(preferences.getBoolean(Constant.KEY_BOOLEAN_AWAKE,false));
        switch4.setChecked(preferences.getBoolean(Constant
                .KEY_BOOLEAN_WRONG_POSTURE,false));

        switch1.setOnCheckedChangeListener(this);
        switch2.setOnCheckedChangeListener(this);
        switch3.setOnCheckedChangeListener(this);
        switch4.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i("wending", "onCheckedChanged: " + isChecked);
        SharedPreferences preferences = getSharedPreferences(Constant
                .PREFERENCES_SETTING,MODE_PRIVATE);
        switch (buttonView.getId()){
            case R.id.id_temp_1:
                preferences.edit().putBoolean(Constant.KEY_BOOLEAN_TEMP_LOW,
                        isChecked).apply();
                break;
            case R.id.id_temp_2:
                preferences.edit().putBoolean(Constant.KEY_BOOLEAN_TEMP_HIGH,
                        isChecked).apply();
                break;
            case R.id.id_temp_3:
                preferences.edit().putBoolean(Constant.KEY_BOOLEAN_AWAKE,
                        isChecked).apply();
                break;
            case R.id.id_temp_4:
                preferences.edit().putBoolean(Constant.KEY_BOOLEAN_WRONG_POSTURE,
                        isChecked).apply();
                break;
        }
    }
}
