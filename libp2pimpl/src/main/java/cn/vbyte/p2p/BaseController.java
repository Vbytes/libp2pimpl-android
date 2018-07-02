package cn.vbyte.p2p;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.vbyte.p2p.IController;
import com.vbyte.p2p.OnLoadedListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by passion on 16-1-14.
 */
public abstract class BaseController implements IController {

    // load => unload => load 操作的异步转同步
    public static final int VIDEO_LIVE = 0;
    public static final int VIDEO_VOD = 1;

    public static final int NETSTATE_MOBILE = 0;
    public static final int NETSTATE_WIFI = 1;
    //同步总的次数是50次
    public static final int syncRetryCount = 50;
    //同步等待时间为100ms
    public static final long syncWaitTime = 100;


    // 事件监听gut
    private Handler eventHandler = null;
    private Handler errorHandler = null;

    /**
     * 设置EventHandler，注意该handler不能叠加，之前设置的handler将无效
     * @param handler 要设置的EventHandler实例
     */
    public void setEventHandler(Handler handler) {
        this.eventHandler = handler;
    }

    /**
     * 设置ErrorHandler，注意该handler不能叠加，之前设置的handler将无效
     * @param handler 要设置的ErrorHandler实例
     */
    public void setErrorHandler(Handler handler) {
        this.errorHandler = handler;
    }

    public void onEvent(int code, String msg) {

        onLocalEvent(code, msg);
        if (eventHandler != null) {
            Looper.getMainLooper();
            Message message = eventHandler.obtainMessage();
            message.what = code;
            message.obj = msg;
            eventHandler.sendMessage(Message.obtain(message));
        }
    }

    public void onError(int code, String msg) {

        onLocalError(code, msg);
        if (errorHandler != null) {
            Looper.getMainLooper();
            Message message = errorHandler.obtainMessage();
            message.what = code;
            message.obj = msg;
            errorHandler.sendMessage(Message.obtain(message));
        }
    }

    /**
     * 这2个工具函数能让LiveController和VodController能事先对P2P线程反应的事件进行预处理
     * @param code 事件码
     * @param msg 事件说明
     */
    protected void onLocalEvent(int code, String msg) {}
    protected void onLocalError(int code, String msg) {}

    /**
     * 为方便子类实现
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, OnLoadedListener listener) throws Exception {
        load(channel, resolution, 0, listener);
    }

    @Override
    public void load(String channel, int netState, OnLoadedListener listener) throws Exception {
        load(channel, "UHD", 0, netState, listener);
    }


    protected volatile Uri mUri = null;
    /**
     * 同步获取uri接口
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @return 取得的uri
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public Uri loadAsync(String channel, String resolution) throws Exception {
        OnLoadedListener listener = new OnLoadedListener() {
            @Override
            public void onLoaded(Uri uri) {
                mUri = uri;
            }
        };
        this.load(channel, resolution, listener);
        int retryCount = syncRetryCount;

        while (mUri == null && (retryCount--) > 0) {
            Thread.sleep(syncWaitTime);
        }
        if (retryCount == 0) {
            this.unload();
        }
        return mUri;
    }

    /**
     * 同步获取uri接口
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param startTime 起始时间，直播是时移，点播即开始时间偏移
     * @return 取得的uri
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public Uri loadAsync(String channel, String resolution, double startTime) throws Exception {
        OnLoadedListener listener = new OnLoadedListener() {
            @Override
            public void onLoaded(Uri uri) {
                mUri = uri;
            }
        };
        this.load(channel, resolution, startTime, listener);
        int retryCount = syncRetryCount;

        while (mUri == null && (retryCount--) > 0) {
            Thread.sleep(syncWaitTime);
        }
        if (retryCount == 0) {
            this.unload();
        }
        return mUri;
    }

    /**
     * 直接调用native层的load
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param startTime 起始时间，直播是时移，点播即开始时间偏移
     */
    protected void loadDirectly(String channel, String resolution, double startTime) {}

    /**
     * 直接调用native层的load
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param netState 网络状态
     * @param startTime 起始时间，直播是时移，点播即开始时间偏移
     */
    protected void loadDirectly(String channel, String resolution, double startTime, int netState) {}

    /**
     * 对直播而言，seek函数并无意义
     * @param startTime 随机点播时的起始时间点
     */
    @Override
    public void seek(double startTime) {
        return;
    }

    @Override
    public void pause() {
        return;
    }

    @Override
    public void resume() { return; }

    @Override
    public void unload() {
        mUri = null;
    }

    @Override
    public String playStreamInfo() {
        return "";
    }
}
