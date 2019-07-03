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
        /**
         * 返回切片重定向后的地址
         */
        public static final int REDIRECT_ADDR = 10010008;
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

    private long _pointer;
    private boolean pendingDestruct = false;

    private CallbackInterface eventHandler = null;
    private CallbackInterface errorHandler = null;
    private int timeoutTimes;

    public LiveController() {
        _pointer = _construct();
        timeoutTimes = 0;
    }

    private static LiveController instance;
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

    private long getPointer() {
        if (_pointer == 0) {
            throw new RuntimeException("LiveController的Native层实例已经被销毁，请确认是否已经调用了destruct()");
        }
        return _pointer;
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
            throws RuntimeException {
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
            throws RuntimeException {
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
            throws RuntimeException {
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
            throws RuntimeException {
        synchronized(this) {
            if (!loadQueue.isEmpty()) {
                loadQueue.clear();
            }

            LoadEvent loadEvent = new LoadEvent(VIDEO_LIVE, channel, resolution, startTime, netState, onLoadedListener);
            loadQueue.add(loadEvent);
            if (initedSDK && curLoadEvent == null) {
                VbyteP2PModule.contrlMap.put(getCtrlID(), this);
                curLoadEvent = loadQueue.get(0);
                loadQueue.remove(0);
                this._load(_pointer, channel, resolution, startTime, netState);
            }
        }
    }

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel {@see IController.Channel}
     * @throws RuntimeException
     */
    public void load(ChannelInfo channel) throws RuntimeException {
        this.load(channel.getChannel(), channel.getResolution(), channel.getStartTime(), channel.getListener());
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
    public void unload() throws RuntimeException{
        Log.i(LiveController.TAG, "LiveController:" + this + ", unload");
        synchronized (this) {
            //当前有事件的时候, 才unload, 屏蔽空unload
            if(curLoadEvent != null) {
                super.unload();
                this._unload(_pointer);
            }
        }
    }

    /**
     * 销毁Native层实例，调用该函数后当前LiveController无法继续使用，若要使用请重新创建
     */
    @Override
    public void destruct() throws RuntimeException{
        if (curLoadEvent == null) {
            _destruct(_pointer);
            _pointer = 0;
        } else {
            //用户已调用unload但未收到stoped信号, 我们标记一下在收到stoped之后再析构
            pendingDestruct = true;
        }

    }

    /**
     * 获取当前播放时间，仅对flv格式有效
     * @return 当前播放的时间点
     */
    public int getCurrentPlayTime() {
        return _getCurrentPlayTime(getPointer());
    }

    /*
     *获取统计数据接口
     */
    public long getStatistic(int type) {
        return this._getStatistic(getPointer(), type);
    }

    /*

     */
    public void resetStatistic(int type) {
        this._resetStatistic(getPointer(), type);
    }

    /**
     * 在unload之后Event.STOPPED、INITED信号收到以前，用户可能会执行{@link LiveController#load 或者 {@link LiveController#destruct()}
     * 这个时候我们需要在收到该信号以后完成用户执行的操作
     */
    private void recvAsyncEvent() {
        synchronized(this) {
            if (pendingDestruct) {
                if (_pointer != 0) {
                    _destruct(getPointer());
                    VbyteP2PModule.contrlMap.remove(getCtrlID());
                }
                _pointer = 0;
                curLoadEvent = null;
                Log.i(LiveController.TAG, "LiveController recvAsyncEvent _destruct");
            } else if (!loadQueue.isEmpty()){
                Log.i(LiveController.TAG, "LiveController recvAsyncEvent");
                // unload or init完成以前用户加载了频道
                curLoadEvent = loadQueue.get(0);
                loadQueue.remove(0);
                VbyteP2PModule.contrlMap.put(getCtrlID(), this);
                if (curLoadEvent.videoType == BaseController.VIDEO_LIVE) {
                    loadDirectly(curLoadEvent.channel, curLoadEvent.resolution, curLoadEvent.startTime, curLoadEvent.netState);
                }
            } else {
                curLoadEvent = null;
            }
        }
    }

    @Override
    protected void onEvent(int code, String msg) {
        Log.i(LiveController.TAG, "LiveController onEvent code:" + code + ",msg:" + msg);
        switch (code) {
            case VbyteP2PModule.Event.INITED:
                recvAsyncEvent();
                break;
            case Event.STARTED:
                synchronized(this) {
                    if (curLoadEvent != null) {
                        timeoutTimes = 0;
                        Uri uri = Uri.parse(msg);
                        if (curLoadEvent.listener != null) {
                            curLoadEvent.listener.onLoaded(uri);
                            curLoadEvent.listener = null;
                            Log.i(LiveController.TAG, "LiveController:" + this + ", Event.STARTED");
                        }
                    }
                }
                break;
            case Event.STOPPED:
                Log.i(LiveController.TAG, "LiveController:" + this + "Event.STOPPED pendingDestruct:" + pendingDestruct);
                recvAsyncEvent();
                break;
            case Event.WANT_IMEI:
                VbyteP2PModule.getInstance().setImei();
                break;
        }
        if (eventHandler != null) {
            try {
                eventHandler.handleMessage(_pointer, code, msg);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    protected void onError(int code, String msg) {
        super.onError(code, msg);

        if (errorHandler != null) {
            try {
                errorHandler.handleMessage(_pointer, code, msg);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    //返回contrlMap内LiveController的ID
    public long getCtrlID() {
        return _pointer;
    }

    /**
     * 设置EventHandler，注意该handler不能叠加，之前设置的handler将无效
     * 使用该接口LiveController仅可以使用
     * @param handler 要设置的EventHandler实例
     */
    public void setEventHandler(CallbackInterface handler) {
        this.eventHandler = handler;
    }

    /**
     * 设置ErrorHandler，注意该handler不能叠加，之前设置的handler将无效
     * @param handler 要设置的ErrorHandler实例
     */
    public void setErrorHandler(CallbackInterface handler) {
        this.errorHandler = handler;
    }

    private native long _construct();

    private native void _load(long pointer, String channel, String resolution, double startTime);

    private native void _load(long pointer, String channel, String resolution, double startTime, int netState);

    private native void _unload(long pointer);

    private native int _getCurrentPlayTime(long pointer);

    private native long _getStatistic(long pointer, int type);
    private native void _resetStatistic(long pointer, int type);

    /**
     * 调用这个接口会销毁所有native层的对象，调用之后LiveController不可复用
     */
    private native void _destruct(long pointer);
}