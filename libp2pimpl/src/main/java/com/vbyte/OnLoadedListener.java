package com.vbyte;

import android.net.Uri;

/**
 * 实现这个接口，以监听返回的代理地址，客户端可通过返回的代理地址拉取流媒体数据
 */
public interface OnLoadedListener {
    /**
     * 返回一个本地代理服务器的地址
     * @param uri 代理服务器的地址
     */
    void onLoaded(Uri uri);
}
