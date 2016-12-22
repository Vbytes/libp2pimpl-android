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
    private static String originUrl;

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
        public static final int RESOLUTION_INVALID = 10011001;
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

    private long _pointer;

    private LiveController() {
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
        if (loadQueue.size() > 2) {
            throw new Exception("You must forget unload last channel!");
        }
        if (strIsUrl(channel)){     //针对于原始的url，截取channel并conf，失败后直接播放该Url
            originUrl = channel;
            channel = cutChannelFromUrl(channel);
        }
        LoadEvent loadEvent = new LoadEvent(VIDEO_LIVE, channel, resolution, startTime, listener);
        loadQueue.add(loadEvent);
        Log.i(TAG, "loadQueue size is " + loadQueue.size());
        if (loadQueue.size() == 1) {
            this._load(_pointer, channel, resolution, startTime);
        }
    }

    public boolean strIsUrl(String str){
        if ((str.indexOf("http") != -1) || (str.indexOf("rtmp") != -1)){
            return true;
        }
        return false;
    }

    public String cutChannelFromUrl(String urlStr){
        int parameterGap = urlStr.indexOf("?"); //对于传参的Url，channel在第一个“？”之前
        if (parameterGap != -1){    //?存在，则只需找到？之前的“/”，截取即得到
            urlStr = urlStr.substring(0,parameterGap);
            int last = urlStr.lastIndexOf("/");
            if (last != -1){
                urlStr = urlStr.substring(last+1);
            }
        }else{    //？不存在，找到最后一个“/”
            int last = urlStr.lastIndexOf("/");
            if (last != -1){
                urlStr = urlStr.substring(last+1);
            }
        }
        //后缀存在的话并删除“.”后缀
        int pos = urlStr.lastIndexOf(".");
        if (pos != -1){
            urlStr = urlStr.substring(0,pos);
        }
        return urlStr;
    }


    @Override
    protected void loadDirectly(String channel, String resolution, double startTime) {
        this._load(_pointer, channel, resolution, startTime);
    }

    /**
     * 卸载当前直播流频道
     */
    @Override
    public void unload() {
        this._unload(_pointer);
    }

    /**
     * 获取当前播放时间，仅对flv格式有效
     * @return 当前播放的时间点
     */
    public int getCurrentPlayTime() {
        return _getCurrentPlayTime(_pointer);
    }

    @Override
    protected void onEvent(int code, String msg) {
        switch (code) {
            case Event.STARTED:
                LoadEvent loadEvent = loadQueue.get(0);
                Uri uri = Uri.parse(msg);
                loadEvent.listener.onLoaded(uri);
                break;
        }
    }

    @Override
    protected void onError(int code, String msg){
        switch (code){
            case Error.NO_SUCH_CHANNEL:
                Log.i(TAG, "p2p conf failed");
                LoadEvent loadFailedEvent = loadQueue.get(0);
                Uri uriOrigin = Uri.parse(originUrl);
                loadFailedEvent.listener.onLoaded(uriOrigin);
                break;
        }
    }

    private native long _construct();

    private native void _load(long pointer, String channel, String resolution, double startTime);

    private native void _unload(long pointer);

    private native int _getCurrentPlayTime(long pointer);
}
