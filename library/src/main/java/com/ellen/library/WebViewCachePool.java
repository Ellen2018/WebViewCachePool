package com.ellen.library;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

public class WebViewCachePool {

    /**
     * 正在被使用的WebVie List集合
     */
    private List<WebView> usedWebViewList;
    /**
     * 预准备的WebView List集合(当回收集合里没有WebView才会被使用)
     */
    private List<WebView> readyWebViewList;
    /**
     * 被回收的WebView List 集合(可被复用)
     */
    private List<WebView> recyclerWebViewList;
    /**
     * 最大的WebView个数(以上集合内的WebView个数不会超过这个数，为内存作考虑)
     * 注意：为负数时,会无限采用复用,不存在销毁，谨慎使用
     */
    private int maxWebViewCount = 10;

    private static WebViewCachePool webViewCachePool;

    private WeakReference<Context> contextWeakReference;

    private WebViewCachePool(){}

    public static WebViewCachePool getInstance(Context context){
        if(webViewCachePool == null){
            synchronized (WebViewCachePool.class){
                if(webViewCachePool == null) {
                    webViewCachePool = new WebViewCachePool();
                    //绑定应用上下文
                    webViewCachePool.contextWeakReference = new WeakReference<>(context.getApplicationContext());
                }
            }
        }
        return webViewCachePool;
    }

    public WebView getWebView(){
        WebView canUseWebView = getCanUseWebView();
        if(usedWebViewList == null){
            //说明是第一次获取
            if(usedWebViewList == null){
                usedWebViewList = new Vector<>();
            }
        }
        usedWebViewList.add(canUseWebView);
        canUseWebView.loadUrl("");
        return canUseWebView;
    }

    public void destory(WebView webView,String jsName){
        usedWebViewList.remove(webView);
        if(recyclerWebViewList == null){
            recyclerWebViewList = new Vector<>();
        }
        if(getAllWebViewSize() >= maxWebViewCount && maxWebViewCount >= 0){
            //不能进行复用了，达到最大数目
            //将WebView进行完全回收
            destoryWebView(webView);
        }else {
            //只是将WebView进行初始化状态
            initWebViewAndUse(webView,jsName);
            recyclerWebViewList.add(webView);
        }
    }

    private int getAllWebViewSize(){
        int size = 0;
        if(usedWebViewList != null){
            size = size + usedWebViewList.size();
        }
        if(readyWebViewList != null){
            size = size + readyWebViewList.size();
        }
        if(recyclerWebViewList != null){
            size = size + recyclerWebViewList.size();
        }
        return size;
    }

    /**
     * 完全销毁WebView
     * @param webView
     */
    private void destoryWebView(WebView webView){
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.stopLoading();
        ViewGroup parent = (ViewGroup) webView.getParent();
        if (parent != null) {
            parent.removeView(webView);
        }
        webView.getSettings().setJavaScriptEnabled(false);
        webView.clearHistory();
        webView.removeAllViews();
        webView.destroy();
        webView.clearCache(true);
    }

    /**
     * 只是将WebView设置为可重用状态
     * @param webView
     * @param jsName
     */
    private void initWebViewAndUse(WebView webView,String jsName){
        webView.loadUrl("");
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        if(jsName != null)
        webView.removeJavascriptInterface(jsName);
        ViewGroup parent = (ViewGroup) webView.getParent();
        if (parent != null) {
            parent.removeView(webView);
        }
        //清理缓存(要是不清理，占用的内存只会越来越大，最终导致OOM)
        webView.clearHistory();
        webView.clearCache(true);
    }

    private WebView getCanUseWebView(){
        WebView canUseWebView = null;
        //先使用复用集合的
        if(recyclerWebViewList == null || recyclerWebViewList.size() == 0){
            //说明复用集合里没有能够复用的WebView,那就只能进行制造
            if(readyWebViewList == null || readyWebViewList.size() == 0){
                canUseWebView = initWebSetting(createWebView());
                if(readyWebViewList == null){
                    readyWebViewList = new Vector<>();
                }
                readyWebViewList.add(initWebSetting(createWebView()));
            }else {
                canUseWebView = readyWebViewList.get(0);
                readyWebViewList.remove(0);
                readyWebViewList.add(initWebSetting(createWebView()));
            }
        }else {
            //能够进行复用
            canUseWebView = recyclerWebViewList.get(0);
            recyclerWebViewList.remove(0);
        }
        return canUseWebView;
    }

    private WebView createWebView(){
        //如果使用者需要new一个自定义的WebView,要么直接改掉这里的代码，要么使用Callback形式再封装一下
        WebView webView = new WebView(contextWeakReference.get());
        return webView;
    }

    private WebView initWebSetting(WebView webView) {
        //这句不加会导致WebView显示不正常
        ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);
        WebSettings webSettings = webView.getSettings();
        //页面白屏问题
        webView.setBackgroundColor(ContextCompat.getColor(contextWeakReference.get(), android.R.color.transparent));
        webView.setBackgroundColor(Color.parseColor("#ffffff"));

        //这里是加的
        webSettings.setJavaScriptEnabled(true);
        // init webview settings
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setTextZoom(100);

        // 设置在页面装载完成之后再去加载图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        return  webView;
    }
}
