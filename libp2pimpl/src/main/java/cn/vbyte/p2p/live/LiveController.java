package cn.vbyte.p2p.live;

import android.net.Uri;
import android.util.Log;
import cn.vbyte.p2p.BaseController;
import cn.vbyte.p2p.IController;

/**
 * Created by passion on 16-1-14.
 */
public final class LiveController extends BaseController implements IController {
    private static final String TAG = "cn.vbyte.p2p.live";

    public static class Event {
        /**
         * 启动一个直播流
         */
        public static final int START = 10010000;
        /**
         * 停止一个直播流
         */
        public static final int STOP = 10010001;
        /**
         * 告诉应用直播流已被停止
         */
        public static final int STOPPED = 10010002;
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

    /**
     * 销毁直播控制器，必须在应用退出前显式调用
     */
    public static void dismiss() {
        if (instance != null) {
            instance.destruct();
            instance = null;
        }
    }

    private long _pointer;

    private LiveController() {
        _pointer = _construct();
    }

    /**
     * 加载一个直播流。起始时间在这里并没有意义，应该使用父类的2个参数的load函数
     * @param channel 直播流频道ID
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @return uri 播放器可打开的uri，可能是个文件地址，也可能是个http链接
     */
    @Override
    public Uri load(String channel, String resolution, double startTime) {
        String path = this._load(_pointer, channel, resolution);
        Log.v(TAG, path);
        return Uri.parse(path);
    }

    /**
     * 卸载当前直播流频道
     */
    @Override
    public void unload() {
        this._unload(_pointer);
    }

    private void destruct() {
        this._destruct(_pointer);
    }

    private native long _construct();

    private native void _destruct(long pointer);

    private native String _load(long pointer, String channel, String resolution);

    private native void _unload(long pointer);

    private native long _cdnprobe(long pointer);
}
