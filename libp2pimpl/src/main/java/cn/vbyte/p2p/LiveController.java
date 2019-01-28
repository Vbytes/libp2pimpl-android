package cn.vbyte.p2p;

import android.net.Uri;
import android.util.Log;

import com.vbyte.p2p.IController;
import com.vbyte.p2p.OnLoadedListener;

/**
 * Created by passion on 16-1-14.
 */
public final class LiveController extends BaseController implements IController {
    private static final String TAG = "[TencentXP2P]";

    public enum StatisticType {

        LIVE_CDN_VALUE, LIVE_P2P_VALUE, LIVE_BOTH_VALUE

    }

    public enum VPNetState {

        VPNetStateMobile, VPNetStateWIFI

    }

    public static class Event {
        /**
         * 启动一个直播流
         */
        public static final int START = 10010000;
        /**
         * 告诉应用直播流已经启动
         */
        public static final int STARTED = 10010001;
        /**
         * 停止一个直播流
         */
        public static final int STOP = 10010002;
        /**
         * 告诉应用直播流已被停止
         */
        public static final int STOPPED = 10010003;

        /**
         * 告诉应用直播流播放异常，需要回源播放
         */
        public static final int BACK_TO_ORIGIN = 10010005;

        public static final int STATISTICS = 10010006;
        public static final int WANT_IMEI = 10010007;
    }

    public static class Error {
        /**
         * 此错误因传入的频道ID为空所引起
         */
        public static final int CHANNEL_EMPTY = 10011000;
        /**
         * 此错误是因为该频道ID不存在，或者已被删除
         */
        public static final int NO_SUCH_CHANNEL = 10011001;
        /**
         * 此错误表明传入的分辨率不对
         */
        public static final int RESOLUTION_INVALID = 10011002;

        public static final int LIVE_FORMAT_INVALID = 10011003;

        public static final int LIVE_SOURCE_DATA_ERROR = 10011004;
    }

    private static LiveController instance;

    private static int timeoutTimes;

    /**
     * 获取直播控制器
     * @return 直播控制器的唯一接口
     */
    public static LiveController getInstance() {
        if (instance == null) {
            instance = new LiveController();
        }
        return instance;
    }

    private long _pointer;

    private LiveController() {
        _pointer = _construct();
        timeoutTimes = 0;
    }

    /**
     * 加载一个直播流。针对时移，该接口只为flv使用
     * @param channel 直播流频道ID
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位，支持一天之内的视频时移回放
     * @param onLoadedListener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime, OnLoadedListener onLoadedListener)
            throws Exception {
        this.load(channel, resolution, startTime, NETSTATE_WIFI, onLoadedListener, null);
    }

    /**
     * 加载一个直播流。针对时移，该接口只为flv使用
     * @param channel 直播流频道ID
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位，支持一天之内的视频时移回放
     * @param netState 网络状态
     * @param onLoadedListener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime,
                     int netState, OnLoadedListener onLoadedListener)
            throws Exception {
        this.load(channel, resolution, startTime, netState, onLoadedListener, null);
    }

    /**
     * 加载一个直播流。针对时移，该接口只为flv使用
     * @param channel 直播流频道ID
     * @param netState 网络状态
     * @param onLoadedListener 当成功load时的回调函数
     * @param onTimeoutListener 当load超时回调的函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, int netState,
                     OnLoadedListener onLoadedListener, OnTimeoutListener onTimeoutListener)
            throws Exception {
        this.load(channel, "UHD", 0, netState, onLoadedListener, onTimeoutListener);
    }

    /**
     * 加载一个直播流。针对时移，该接口只为flv使用
     * @param channel 直播流频道ID
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位，支持一天之内的视频时移回放
     * @param netState 网络状态
     * @param onLoadedListener 当成功load时的回调函数
     * @param onTimeoutListener 当load超时回调的函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime,
                     int netState, OnLoadedListener onLoadedListener, OnTimeoutListener onTimeoutListener)
            throws Exception {
        synchronized(LiveController.class) {
            if (!loadQueue.isEmpty()) {
                for (LoadEvent loadEvent : loadQueue) {
                    VbyteP2PModule.vbyteHandler.removeCallbacks(loadEvent);
                }
                loadQueue.clear();
            }

            LoadEvent loadEvent = new LoadEvent(VIDEO_LIVE, channel, resolution, startTime, netState, onLoadedListener, onTimeoutListener);
            int delay = 2500 - (timeoutTimes > 7 ? 2000 : timeoutTimes * 250);
            VbyteP2PModule.vbyteHandler.postDelayed(loadEvent, delay);
            loadQueue.add(loadEvent);
            if (initedSDK && curLoadEvent == null) {
                curLoadEvent = loadQueue.get(0);
                loadQueue.remove(0);
                this._load(_pointer, channel, resolution, startTime, netState);
            }
        }
    }

    @Override
    protected void loadDirectly(String channel, String resolution, double startTime) {
        this._load(_pointer, channel, resolution, startTime);
    }

    @Override
    protected void loadDirectly(String channel, String resolution, double startTime, int netState) {
        this._load(_pointer, channel, resolution, startTime, netState);
    }

    /**
     * 卸载当前直播流频道
     */
    @Override
    public void unload() {
        //当前有事件的时候, 才unload, 屏蔽空unload
        if(curLoadEvent != null) {
            Log.i(TAG, "unload");
            VbyteP2PModule.vbyteHandler.removeCallbacks(curLoadEvent);
            super.unload();
            this._unload(_pointer);
        }
    }

    /**
     * 获取当前播放时间，仅对flv格式有效
     * @return 当前播放的时间点
     */
    public int getCurrentPlayTime() {
        return _getCurrentPlayTime(_pointer);
    }

    /*
     *获取统计数据接口
     */
    public long getStatistic(int type) {
        return this._getStatistic(_pointer, type);
    }

    /*

     */
    public void resetStatistic(int type) {
        this._resetStatistic(_pointer, type);
    }

    @Override
    protected void onEvent(int code, String msg) {
        switch (code) {
            case Event.STARTED:
                synchronized(LiveController.class) {
                    if (curLoadEvent != null) {
                        timeoutTimes = 0;
                        VbyteP2PModule.vbyteHandler.removeCallbacks(curLoadEvent);
                        Uri uri = Uri.parse(msg);
                        if (curLoadEvent.listener != null) {
                            curLoadEvent.listener.onLoaded(uri);
                            curLoadEvent.listener = null;
                        }
                    }
                }
                break;
            case Event.WANT_IMEI:
                VbyteP2PModule.getInstance().setImei();
                break;
        }
    }

    private native long _construct();

    private native void _load(long pointer, String channel, String resolution, double startTime);

    private native void _load(long pointer, String channel, String resolution, double startTime, int netState);

    private native void _unload(long pointer);

    private native int _getCurrentPlayTime(long pointer);

    private native long _getStatistic(long pointer, int type);
    private native void _resetStatistic(long pointer, int type);
}