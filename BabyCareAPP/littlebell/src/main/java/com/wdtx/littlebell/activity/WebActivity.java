package com.wdtx.littlebell.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.wdtx.littlebell.R;

public class WebActivity extends AppCompatActivity {

    public static void startActivity(Context context,String url){
        Intent intent = new Intent(context,WebActivity.class);
        intent.putExtra("url",url);
        context.startActivity(intent);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("详细信息");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        WebView webView = (WebView) findViewById(R.id.id_wv_web);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.id_pb_progress);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDisplayZoomControls(true);
        settings.setBuiltInZoomControls(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (progressBar.getVisibility() == View.GONE){
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    progressBar.setProgress(newProgress);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient());

        String url = getIntent().getStringExtra("url");
        webView.loadUrl(url);
    }
}
