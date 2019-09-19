package com.vbyte.p2p;

import android.text.TextUtils;

import com.vbyte.UrlGenerator;

/**
 * 直播控制器，视频播放器可通过该控制器实现直播流的P2P加速
 */
public final class LiveController extends BaseController {

    private static LiveController instance;

    private UrlGenerator urlGenerator;

    /**
     * 客户需要手动创建直播控制器
     */
    public LiveController() {
        _pointer = _construct();
        videoType = LoadEvent.VideoType.VIDEO_LIVE;

        urlGenerator = new UrlGenerator() {
            @Override
            public SecurityUrl createSecurityUrl(String originUrl) {
                return new SecurityUrl(originUrl);
            }
        };
    }

    /**
     * 获取直播控制器唯一单例，若客户端不需要多播放器建议使用这种方式
     * @return 直播控制器的唯一接口
     */
    public static LiveController getInstance() {
        if (instance == null) {
            instance = new LiveController();
        }
        return instance;
    }


    /**
     * 获取防盗链url生成器
     *
     * @return 防盗链url生成器
     */
    public UrlGenerator getUrlGenerator() {
        return urlGenerator;
    }

    public void setUrlGenerator(UrlGenerator urlGenerator) {
        if (urlGenerator == null) {
            throw new IllegalArgumentException("UrlGenerator is null");
        }
        this.urlGenerator = urlGenerator;
    }

    @Override
    protected void onEvent(int code, String msg) {
        super.onEvent(code, msg);
    }

    @Override
    protected void doNativeLoad(long pointer, String channel, String resolution, double startTime, int netState) {
        if (TextUtils.isEmpty(channel)) {
            throw new IllegalArgumentException("channel is empty");
        }
        this._load(getPointer(), channel, resolution, startTime, netState);
    }

    @Override
    protected void doNativeUnload(long pointer) {
        _unload(pointer);
    }

    @Override
    protected void doNativeDestruct(long pointer) {
        _destruct(pointer);
    }

    private String retrieveUrl(String oriUrl) {
        SecurityUrl securityUrl = urlGenerator.createSecurityUrl(oriUrl);
        return securityUrl.toString();
    }

    private native long _construct();
    private native void _load(long pointer, String channel, String resolution, double startTime, int netState);
    private native void _unload(long pointer);
    private native void _destruct(long pointer);
}