package com.vbyte.p2p;

import android.text.TextUtils;

import com.vbyte.UrlGenerator;

/**
 * 点播控制器实例
 */
public final class VodController extends BaseController {

    public interface Event {
        int PAUSE = 10010100;
        /**
         * 点播专有,恢复
         */
        int RESUME = 10010101;
    }

    private static VodController instance;

    /**
     * 获取Vod点播控制器
     *
     * @return Vod点播控制器的唯一实例
     */
    public static VodController getInstance() {
        if (instance == null) {
            instance = new VodController();
        }
        return instance;
    }

    private UrlGenerator UrlGenerator;

    public VodController() {
        _pointer = _construct();
        videoType = LoadEvent.VideoType.VIDEO_VOD;

        // urlGenerator给出一个默认值，能够直接传url就能播放
        UrlGenerator = new UrlGenerator() {
            @Override
            public SecurityUrl createSecurityUrl(String originUrl) {
                return new SecurityUrl(originUrl);
            }
        };
    }

    @Override
    protected void onEvent(int code, String msg) {
        super.onEvent(code, msg);
    }

    @Override
    void doNativeLoad(long pointer, String channel, String resolution, double startTime, int netState) {
        if (TextUtils.isEmpty(channel)) {
            throw new IllegalArgumentException("channel is empty");
        }
        SecurityUrl securityUrl = UrlGenerator.createSecurityUrl(channel);
        if (securityUrl == null) {
            throw new IllegalStateException("createSecurityUrl return null");
        }
        _load(pointer, securityUrl.toString(), netState);
    }

    @Override
    void doNativeUnload(long pointer) {
        _unload(pointer);
    }

    @Override
    void doNativeDestruct(long pointer) {
        _destruct(pointer);
    }

    /**
     * 获取点播视频的总时长
     *
     * @return return
     */
    public int getDuration() {
        return this._getDuration(_pointer);
    }

    /**
     * 设置防盗链url生成器，这在播放时url因时间超时失效时发挥作用，重新获取新的防盗链url去播放
     *
     * @param urlGenerator 传入的url生成器实例
     */
    public void setUrlGenerator(UrlGenerator urlGenerator) {
        if (urlGenerator == null) {
            throw new IllegalArgumentException("UrlGenerator is null");
        }
        this.UrlGenerator = urlGenerator;
    }

    /**
     * 获取防盗链url生成器
     *
     * @return 防盗链url生成器
     */
    public UrlGenerator getUrlGenerator() {
        return UrlGenerator;
    }

//    /**
//     * 随机播放当前点播视频的某一时间点，注意调用之后，要让播放器重新加载uri
//     *
//     * @param startTime 随机点播时的起始时间点
//     */
//    public void seek(double startTime) {
//        this._seek(_pointer, startTime);
//    }

    /**
     * 播放暂停
     */
    public void pause() {
        this._pause(_pointer);
    }

    /**
     * 暂停后恢复播放
     */
    public void resume() {
        this._resume(_pointer);
    }

//    public String playStreamInfo() {
//        return this._getPlayStreamInfo(_pointer);
//    }

    private native long _construct();

    private native void _destruct(long pointer);

    private native void _load(long pointer, String jsonUrl, int netState);

    private native void _unload(long pointer);

    private native int _getDuration(long pointer);

    private native void _seek(long pointer, double startTime);

    private native void _pause(long pointer);

    private native void _resume(long pointer);

    private native String _getPlayStreamInfo(long pointer);

    private String retrieveUrl(String oriUrl) {
        SecurityUrl securityUrl = UrlGenerator.createSecurityUrl(oriUrl);
        return securityUrl.toString();
    }
}
