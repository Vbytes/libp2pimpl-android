package cn.vbyte.p2p;

import android.net.Uri;

/**
 * Created by passion on 16-1-14.
 */
public abstract class BaseController implements IController {

    @Override
    public void setMediaFormat(String format) {}

    /**
     * 为方便子类实现
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @return
     */
    @Override
    public Uri load(String channel, String resolution) {
        return load(channel, resolution, 0);
    }

    /**
     * 对直播而言，seek函数并无意义
     * @param startTime 随机点播时的起始时间点
     */
    @Override
    public void seek(double startTime) {
        return;
    }

    @Override
    public void pause() {
        return;
    }

    @Override
    public void resume() { return; }
}
