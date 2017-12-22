package com.wdtx.littlebell.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.util.VolleyCallBack;
import com.wdtx.littlebell.util.VolleyUtil;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FeedbackActivity extends AppCompatActivity implements
        VolleyCallBack<String> {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, FeedbackActivity.class));
    }

    private EditText mPhone, mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("问题反馈");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mPhone = (EditText) findViewById(R.id.id_et_phone);
        mContent = (EditText) findViewById(R.id.id_et_content);
    }

    private SweetAlertDialog mDialog;

    public void submit(View view) {
        if (mPhone.getText().toString().trim().equals("") || mContent.getText()
                .toString().trim().equals("")){
            new SweetAlertDialog(this,
                    SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("内容不正确")
                    .setConfirmText("确认")
                    .setCancelText("取消")
                    .setContentText("联系方式和反馈内容均不能为空。")
                    .show();
            return;
        }

        mDialog = new SweetAlertDialog(this,
                SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Are you sure?")
                .setConfirmText("Yes")
                .setCancelText("No")
                .setContentText("反馈给我们")
                .setConfirmClickListener(new SweetListener());
        mDialog.show();
    }

    @Override
    public void onSuccess(String data) {
        mDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
        mDialog.setTitleText("SUCCESS");
        mDialog.setConfirmText("确定");
        mDialog.setCancelText("取消");
        mDialog.setConfirmClickListener(null);
        mDialog.setContentText("您反馈的问题我们已经收到，感谢您的反馈");
        mDialog.setCancelable(false);
        mPhone.setText("");
        mContent.setText("");
    }

    @Override
    public void onFail(int code, String msg) {

        mDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
        mDialog.setTitleText("ERROR")
                .setCancelText("取消")
                .setConfirmText("重试")
                .setContentText(msg + "，请重试")
                .show();
    }

    @Override
    public void onError(VolleyError error) {
        onFail(-1, "网络异常");
    }

    private class SweetListener implements SweetAlertDialog.OnSweetClickListener {

        @Override
        public void onClick(SweetAlertDialog dialog) {
            dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            dialog.setTitleText("发送中...");
            dialog.setContentText("");
            dialog.setCancelable(false);
            Map<String, String> map = new HashMap<>();
            map.put("phone", mPhone.getText().toString());
            map.put("content", mContent.getText().toString());
            VolleyUtil.getInstance().requestPost("/feedback", map, FeedbackActivity.this);
        }
    }


}
