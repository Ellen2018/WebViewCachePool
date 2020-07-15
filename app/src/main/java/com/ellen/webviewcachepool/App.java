package com.ellen.webviewcachepool;

import android.app.Application;
import android.content.Context;
import android.webkit.WebView;

import com.ellen.library.AutoWebView;
import com.ellen.library.WebViewCachePool;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //这里是自定义库内部使用的WebView以及指定库内部的WebView最大个数
        WebViewCachePool.getInstance(this).setAutoWebView(new AutoWebView() {
            @Override
            public WebView getNewWebView(Context context) {
                return new MyWebView(context);
            }

            @Override
            public int maxSize() {
                return 20;
            }
        });
    }
}
