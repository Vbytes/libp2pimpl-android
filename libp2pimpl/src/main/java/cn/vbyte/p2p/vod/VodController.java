package cn.vbyte.p2p.vod;

import android.net.Uri;

import cn.vbyte.p2p.BaseController;
import cn.vbyte.p2p.IController;

/**
 * Created by passion on 16-1-14.
 */
public final class VodController extends BaseController implements IController {

    private static final String TAG = "cn.vbyte.p2p.vod";

    public static class Event {
        /**
         * 告诉应用启动一个点播资源
         */
        public static final int START = 10020000;
        /**
         * 告诉应用媒体已分析完毕
         */
        public static final int HEADER_READY = 10020001;           // 媒体数据的header解析完毕
        public static final int SEEK = 10020002;                   // 点播专有
        /**
         * 告诉应用已经探测到最后一片数据，即将结束
         */
        public static final int FINISHED = 10020003;
        /**
         * 停止
         */
        public static final int STOP = 10020004;
        /**
         * 告诉应用，点播频道已经被停止
         */
        public static final int STOPPED = 10020005;
    }

    public static class Error {
        /**
         * 此错误表明传入的URI为空，请检查。
         */
        public static final int URI_EMPTY = 10021000;                     // URI为空
        /**
         * 此错误表明点播资源不能被成功下载，请检查资源是否存在，以及带宽是否足够
         */
        public static final int DOWNLOAD_FAILED = 10021000;               // SOURCE下载失败
        /**
         * 此错误表明媒体的格式不被支持
         */
        public static final int FORMAT_INVALID = 10021000;                // 文件格式不支持
    }

    public static class MEDIAFORMAT {
        public static final int UNKNOWN = 0;
        public static final int FLV = 1;
        public static final int MP4 = 2;
        public static final int TS = 3;
        public static final int M3U8 = 4;
    }
    
    private static VodController instance;

    /**
     * 获取Vod点播控制器
     * @return Vod点播控制器的唯一实例
     */
    public static VodController getInstance() {
        if (instance == null) {
            instance = new VodController();
        }
        return instance;
    }

    private long _pointer;

    private VodController() {
        _pointer = _construct();
    }

    /**
     * 从随机的某时间点加载播放点播视频
     * @param url 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @return
     */
    @Override
    public Uri load(String url, String resolution, double startTime) {
        String path = this._load(_pointer, url, resolution, startTime);
        return Uri.parse(path);
    }

    /*
    获取点播视频的总时长
     */
    public int getDuration() {
        return this._getDuration(_pointer);
    }
    
    /**
     * 随机播放当前点播视频的某一时间点，注意调用之后，要让播放器重新加载uri
     * @param startTime 随机点播时的起始时间点
     */
    @Override
    public void seek(double startTime) {
        this._seek(_pointer, startTime);
    }

    /**
     * 播放暂停
     */
    @Override
    public void pause() {
        this._pause(_pointer);
    }

    /**
     * 暂停后恢复播放
     */
    @Override
    public void resume() {
        this._resume(_pointer);
    }

    /**
     * 卸载此点播资源
     */
    @Override
    public void unload() {
        this._unload(_pointer);
    }

    private native long _construct();

    private native String _load(long pointer, String url, String resolution, double startTime);

    private native int _getDuration(long pointer);
    
    private native void _seek(long _pointer, double startTime);

    private native void _pause(long _pointer);

    private native void _resume(long _pointer);

    private native void _unload(long pointer);
}
