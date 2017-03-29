package cn.vbyte.p2p;

import android.net.Uri;
import android.os.AsyncTask;

import com.vbyte.p2p.IController;
import com.vbyte.p2p.OnLoadedListener;
import com.vbyte.p2p.SecurityUrl;
import com.vbyte.p2p.UrlGenerator;

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
        public static final int STARTED = 10020001;
        /**
         * 停止
         */
        public static final int STOP = 10020002;
        /**
         * 告诉应用，点播频道已经被停止
         */
        public static final int STOPPED = 10020003;
        /**
         * 点播专有，暂停
         */
        public static final int PAUSE = 10020004;
        /**
         * 点播专有,恢复
         */
        public static final int RESUME = 10020005;
        /**
         * 通知应用层要重新获取防盗链url
         */
        public static final int RETRIEVE_URL = 10020006;
        /**
         *应用层已经重新生成url
         */
        public static final int RETRIEVED_URL = 10020007;
        /**
         * 告诉应用已经探测到最后一片数据，即将结束
         */
        public static final int FINISHED = 10020008;
        public static final int HEADER_READY = 10020009;           // 媒体数据的header解析完毕

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
    private UrlGenerator urlGenerator;

    private VodController() {
        _pointer = _construct();

        // urlGenerator给出一个默认值，能够直接传url就能播放
        urlGenerator = new UrlGenerator() {
            @Override
            public SecurityUrl createSecurityUrl(String originUrl) {
                return new SecurityUrl(originUrl);
            }
        };
    }

    /**
     * 从随机的某时间点加载播放点播视频
     * @param channel 资源链接，主要为点播调用
     * @param resolution 资源的清晰度，现在统一为"UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    @Override
    public void load(String channel, String resolution, double startTime, OnLoadedListener listener)
            throws  Exception {
        if (!loadQueue.isEmpty()) {
            loadQueue.clear();
            throw new Exception("You must forget to unload last channel!");
        }
        LoadEvent loadEvent = new LoadEvent(VIDEO_VOD, channel, resolution, startTime, listener);
        loadQueue.add(loadEvent);
        if (curLoadEvent == null) {
            curLoadEvent = loadQueue.get(0);
            loadQueue.remove(0);
            this._load(_pointer, channel, resolution, startTime);
        }
    }

    protected void onEvent(int code, String msg) {
        switch (code) {
            case Event.STARTED:
                if (curLoadEvent != null) {
                    Uri uri = Uri.parse(msg);
                    curLoadEvent.listener.onLoaded(uri);
                }
                break;
            case Event.RETRIEVE_URL:
                if (urlGenerator != null) {
                    final String sourceId = msg;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SecurityUrl securityUrl = urlGenerator.createSecurityUrl(sourceId);
                            if (securityUrl != null) {
                                _setNewUrl(_pointer, securityUrl.toString());
                            }
                        }
                    }).start();
                }
                break;
        }
    }

    @Override
    protected void loadDirectly(String url, String resolution, double startTime) {
        this._load(_pointer, url, resolution, startTime);
    }

    /**
     * 获取点播视频的总时长
     */
    public int getDuration() {
        return this._getDuration(_pointer);
    }

    /**
     * 设置防盗链url生成器，这在播放时url因时间超时失效时发挥作用，重新获取新的防盗链url去播放
     * @param urlGenerator 传入的url生成器实例
     */
    public void setUrlGenerator(UrlGenerator urlGenerator) {
        this.urlGenerator = urlGenerator;
    }

    /**
     * 获取防盗链url生成器
     * @return 防盗链url生成器
     */
    public UrlGenerator getUrlGenerator() {
        return urlGenerator;
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
        super.unload();
        this._unload(_pointer);
    }

    private native long _construct();

    private native String _load(long pointer, String url, String resolution, double startTime);

    private native int _getDuration(long pointer);
    
    private native void _seek(long pointer, double startTime);

    private native void _pause(long pointer);

    private native void _resume(long pointer);

    private native void _unload(long pointer);

    private native void _setNewUrl(long pointer, String newUrl);
}
