package com.sibaken.multiloginbrowser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class Window1Activity extends AppCompatActivity {

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window1);

        Button btn_test_back = (Button) findViewById(R.id.test_back);
        btn_test_back.setOnClickListener(btn_testBackListener);

        myWebView = (WebView) findViewById(R.id.webView);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.loadUrl("https://www.google.com/");
    }

    View.OnClickListener btn_testBackListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (myWebView.canGoBack()) {
                myWebView.goBack();
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        } else {
            if (myWebView.canGoBack()) {
                myWebView.goBack();
            } else {
                return super.onKeyDown(keyCode, event);
            }
            return false;
        }
    }
}
