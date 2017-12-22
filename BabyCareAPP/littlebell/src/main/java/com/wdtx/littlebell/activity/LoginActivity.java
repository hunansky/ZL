package com.wdtx.littlebell.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.model.User;
import com.wdtx.littlebell.service.BleService;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.HashMap;
import java.util.Map;

import static com.wdtx.littlebell.other.Constant.*;

public class LoginActivity extends AppCompatActivity
        implements VolleyCallBack<User>,View.OnClickListener {

    private EditText etAccount;
    private EditText etPassword;
    private TextView tvResult;
    private Button btnLogin;

    private boolean logging = false;

    public static void startActivity(Context context){
        Intent intent = new Intent(context,LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MyApplication.instance.setUser(null);

        etAccount = (EditText) findViewById(R.id.id_et_account);
        etPassword = (EditText) findViewById(R.id.id_et_password);
        tvResult = (TextView) findViewById(R.id.id_tv_result);
        btnLogin = (Button) findViewById(R.id.id_btn_login);
        btnLogin.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences(
                PREFERENCES_USER, MODE_PRIVATE);
        String account = preferences.getString(KEY_STRING_ACCOUNT, "");
        String password = preferences.getString(KEY_STRING_PASSWORD, "");
        boolean login = preferences.getBoolean(KEY_BOOLEAN_LOGIN,false);
        etAccount.setText(account);
        etPassword.setText(password);

        Intent intent = new Intent(this, BleService.class);
        intent.setAction(BleService.ACTION_DISCONNECT);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        if (!logging) {
            SharedPreferences preferences = getSharedPreferences(
                    PREFERENCES_USER, MODE_PRIVATE);
            preferences.edit()
                    .putString(KEY_STRING_ACCOUNT,etAccount.getText().toString())
                    .putString(KEY_STRING_PASSWORD,etPassword.getText().toString())
                    .apply();

            btnLogin.setText("登录中...");

            Map<String,String> param = new HashMap<>();
            param.put("account",etAccount.getText().toString());
            param.put("password",etPassword.getText().toString());
            VolleyUtil.getInstance().requestPost("/login",param,this);
        }
    }

    @Override
    public void onSuccess(User user) {
        SharedPreferences preferences = getSharedPreferences(
                PREFERENCES_USER, MODE_PRIVATE);
        preferences.edit().putBoolean(KEY_BOOLEAN_LOGIN, true).apply();
        MyApplication application = (MyApplication) getApplication();
        application.setUser(user);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onFail(int code, String msg) {
        logging = false;
        btnLogin.setText("登录");
        tvResult.setText(msg);
    }

    @Override
    public void onError(VolleyError error) {
        onFail(-1,"网络异常");
        Log.i("wending", "onError: " + error.getMessage());
        error.printStackTrace();
    }
}
