package com.ellen.webviewcachepool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.ellen.library.WebViewCachePool;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private RelativeLayout rlWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlWebView = findViewById(R.id.rl_webview);
        //获取一个WebView
        webView = WebViewCachePool.getInstance(this).getWebView();
        //动态添加到某个布局中
        rlWebView.addView(webView);
        //可直接加载地址
        webView.loadUrl("https://www.baidu.com");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //回收或者销毁当前WebView
        WebViewCachePool.getInstance(this).destroy(webView,null);
    }
}
