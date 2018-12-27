package cn.vbyte.p2p;

import android.net.Uri;
import android.util.Log;

import com.vbyte.p2p.IController;
import com.vbyte.p2p.OnLoadedListener;

/**
 * Created by passion on 16-1-14.
 */
public final class LiveController extends BaseController implements IController {
    private static final String TAG = "cn.vbyte.p2p.live";

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

    private static LiveController instance;
    private long _pointer;
    private boolean pendingDestruct = false;

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



    public LiveController() {
        _pointer = _construct();
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
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime, OnLoadedListener listener)
            throws Exception {
        if (!loadQueue.isEmpty()) {
            loadQueue.clear();
//            throw new Exception("You must forget unload last channel!");
        }

        LoadEvent loadEvent = new LoadEvent(VIDEO_LIVE, channel, resolution, startTime, listener);
        loadQueue.add(loadEvent);
        Log.i(TAG, "loadQueue size is " + loadQueue.size());
        if (initedSDK && curLoadEvent == null) {
            curLoadEvent = loadQueue.get(0);
            loadQueue.remove(0);
            VbyteP2PModule.contrlMap.put(getCtrlID(), this);
            this._load(_pointer, channel, resolution, startTime);
        }
    }

    //返回contrlMap内LiveController的ID
    private long getCtrlID() {
        return _pointer;
    }

    /**
     * 加载一个直播流。针对时移，该接口只为flv使用
     * @param channel 直播流频道ID
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位，支持一天之内的视频时移回放
     * @param netState 网络状态
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime, int netState, OnLoadedListener listener)
            throws Exception {
        if (!loadQueue.isEmpty()) {
            loadQueue.clear();
//            throw new Exception("You must forget unload last channel!");
        }
        LoadEvent loadEvent = new LoadEvent(VIDEO_LIVE, channel, resolution, startTime, netState, listener);
        loadQueue.add(loadEvent);
        Log.i(TAG, "loadQueue@1 size is " + loadQueue.size());
        if (initedSDK && curLoadEvent == null) {
            VbyteP2PModule.contrlMap.put(getCtrlID(), this);
            curLoadEvent = loadQueue.get(0);
            loadQueue.remove(0);
            this._load(_pointer, channel, resolution, startTime, netState);
        }
    }

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param listener 当成功load时的回调函数
     * @param async listener是否要在UI线程回调，这个参数现在无效了，请使用{@link cn.vbyte.p2p.VbyteP2PModule#setCallOnLoadListener(boolean)}
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     */
    public void load(String channel, OnLoadedListener listener, boolean async) throws Exception {
        load(channel, "UHD", 0, listener);
    }

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel {@see IController.Channel}
     * @throws Exception
     */
    public void load(ChannelInfo channel) throws Exception {
        load(channel.getChannel(), channel.getResolution(), channel.getStartTime(), channel.getListener());
    }

    @Override
    protected void loadDirectly(String channel, String resolution, double startTime) {
        this._load(getPointer(), channel, resolution, startTime);
    }

    @Override
    protected void loadDirectly(String channel, String resolution, double startTime, int netState) {
        this._load(getPointer(), channel, resolution, startTime, netState);
    }

    /**
     * 卸载当前直播流频道
     */
    @Override
    public void unload() {
        Log.i(LiveController.TAG, "LiveController:" + this + ", unload");

        //当前有事件的时候, 才unload, 屏蔽空unload
        if(curLoadEvent != null) {
            super.unload();
            this._unload(getPointer());
        }
    }

    /**
     * 销毁Native层实例，调用该函数后当前LiveController无法继续使用，若要使用请重新创建
     */
    @Override
    public void destruct() {
        Log.i(LiveController.TAG, "LiveController:" + this + ", destruct");
        if (curLoadEvent == null) {
            _destruct(getPointer());
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
     * 在unload之后Event.STOPPED信号收到以前，用户可能会执行{@link LiveController#load 或者 {@link LiveController#destruct()}
     * 这个时候我们需要在收到该信号以后完成用户执行的操作
     */
    private void afterEventSTOPED() {
        if (pendingDestruct) {
            _destruct(getPointer());
            _pointer = 0;
            curLoadEvent = null;
            VbyteP2PModule.contrlMap.remove(getCtrlID());
            Log.i(LiveController.TAG, "LiveController afterEventSTOPED _destruct");
        } else if (!loadQueue.isEmpty()){
            Log.i(LiveController.TAG, "LiveController afterEventSTOPED");
            //unload完成以前用户加载了频道
            curLoadEvent = loadQueue.get(0);
            loadQueue.remove(0);
            VbyteP2PModule.contrlMap.put(getCtrlID(), this);
            if (curLoadEvent.videoType == BaseController.VIDEO_LIVE) {
                loadDirectly(curLoadEvent.channel, curLoadEvent.resolution, curLoadEvent.startTime, curLoadEvent.netState);
            }
        }
    }

    @Override
    protected void onEvent(int code, String msg) {
        Log.i(LiveController.TAG, "LiveController onEvent code:" + code + ",msg:" + msg);
        switch (code) {
            case Event.STARTED:
                synchronized(LiveController.class) {
                    if (curLoadEvent != null) {
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
                afterEventSTOPED();
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

    /**
     * 调用这个接口会销毁所有native层的对象，调用之后LiveController不可复用
     */
    private native void _destruct(long pointer);
}