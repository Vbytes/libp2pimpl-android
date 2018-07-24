package cn.vbyte.p2p;

import android.net.Uri;
import android.util.Log;

import com.vbyte.p2p.IController;
import com.vbyte.p2p.OnLoadedListener;
import static cn.vbyte.p2p.VbyteP2PModule.contrlMap;

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


    private static OnLoadedListener currentListener;

    /**
     * 获取直播控制器
     * @return 直播控制器的唯一接口
     */

    private long _pointer;

    public LiveController() {
        _pointer = _construct();
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
        synchronized (this) {
            currentListener = listener;
            this._load(_pointer, channel, resolution, startTime);
            contrlMap.put(this.getID(), this);
        }
    }


    public void load(String channel, byte[] data, OnLoadedListener listener)
            throws Exception {

        synchronized(this) {
            currentListener = listener;
            this._preLoad(_pointer, channel, data);
            contrlMap.put(this.getID(), this);
        }
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

        synchronized(this) {

            currentListener = listener;
            this._load(_pointer, channel, resolution, startTime, netState);
            contrlMap.put(this.getID(), this);
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
        synchronized(this) {
            int id = this.getID();
            super.unload();
            this._unload(_pointer);
            if (contrlMap.containsKey(id)) {
                contrlMap.remove(id);
            }
        }
    }

    /**
     * 获取当前播放时间，仅对flv格式有效
     * @return 当前播放的时间点
     */
    public int getCurrentPlayTime() {
        return _getCurrentPlayTime(_pointer);
    }
    /**
     * 获取特征ID
     * @return 特征ID
     */
    public int getID() {
        return _getID(_pointer);
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
    protected void onLocalEvent(int code, String msg) {
        switch (code) {
            case Event.STARTED:
                synchronized(this) {
                    if (currentListener != null) {
                        Uri uri = Uri.parse(msg);
                        currentListener.onLoaded(uri);
                        currentListener = null;
                    }
                }
                break;
        }
    }

    private native long _construct();

    private native void _load(long pointer, String channel, String resolution, double startTime);


    private native void _preLoad(long pointer, String channel, byte[] data);

    private native void _load(long pointer, String channel, String resolution, double startTime, int netState);


    private native void _unload(long pointer);

    private native int _getCurrentPlayTime(long pointer);
    private native int _getID(long pointer);

    private native long _getStatistic(long pointer, int type);
    private native void _resetStatistic(long pointer, int type);
}