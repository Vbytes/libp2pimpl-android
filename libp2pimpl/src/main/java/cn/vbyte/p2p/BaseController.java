package cn.vbyte.p2p;

import android.net.Uri;

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

    protected List<LoadEvent> loadQueue = Collections.synchronizedList(new LinkedList<LoadEvent>());
    protected LoadEvent curLoadEvent = null;
    protected static boolean initedSDK = false;

    public static class LoadEvent {
        public int videoType;
        public String channel;
        public String resolution;
        public double startTime;
        public int netState;
        public OnLoadedListener listener;
        public boolean callOnUIThread = false;//回调是否在UI线程,在UI线程需要使用Handler(斗鱼不使用Handler)

        LoadEvent(int videoType, String channel, OnLoadedListener listener) {
            this(videoType, channel, "UHD", 0, listener);
        }

        public LoadEvent(int videoType, String channel, String resolution, double startTime, OnLoadedListener listener) {
            this(videoType, channel, resolution, startTime, NETSTATE_WIFI, listener);
        }

        public LoadEvent(int videoType, String channel, String resolution, double startTime, int netState, OnLoadedListener listener) {
            this(videoType, channel, resolution, startTime, netState, listener, false);
        }

        public LoadEvent(int videoType, String channel, String resolution, double startTime, int netState, OnLoadedListener listener, boolean callOnUIThread) {
            this.videoType = videoType;
            this.channel = channel;
            this.resolution = resolution;
            this.netState = netState;
            this.startTime = startTime;
            this.listener = listener;
            this.callOnUIThread = callOnUIThread;
        }
    }

    /**
     * 这2个工具函数能让LiveController和VodController能事先对P2P线程反应的事件进行预处理
     * @param code 事件码
     * @param msg 事件说明
     */
    protected void onEvent(int code, String msg) {}
    protected void onError(int code, String msg) {}

    /**
     * 为方便子类实现
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param listener 当成功load时的回调函数
     * @throws RuntimeException 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, OnLoadedListener listener) throws RuntimeException {
        load(channel, resolution, 0, listener);
    }

    @Override
    public void load(String channel, int netState, OnLoadedListener listener) throws RuntimeException {
        load(channel, "UHD", 0, netState, listener);
    }

    @Override
    public void load(String channel, String resolution, double startTime, int netState, OnLoadedListener onLoadedListener, OnTimeoutListener onTimeoutListener) throws RuntimeException {
        //子类实现
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
    public void unload() throws RuntimeException{
        mUri = null;
    }

    @Override
    public String playStreamInfo() {
        return "";
    }
}
