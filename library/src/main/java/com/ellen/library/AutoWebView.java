package com.ellen.library;

import android.content.Context;
import android.webkit.WebView;

/**
 * 如果你的项目中新生成的是自定义的WebView,那么就需要:
 * WebViewCachePool.setNewWebView来进行自定义设置
 */
public interface AutoWebView {
    /**
     * 获取当一个WebView对象
     * @return
     */
    WebView getNewWebView(Context context);

    /**
     * WebViewCachePool里最大的WebViw数量
     * 默认值为：10
     * 此方法是修改最大数量的唯一途径
     * @return
     */
    int maxSize();
}
