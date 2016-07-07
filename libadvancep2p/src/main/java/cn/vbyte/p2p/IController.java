package cn.vbyte.p2p;

import android.net.Uri;

/**
 * Created by passion on 16-1-14.
 */
public interface IController {

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @return 一个播放器能识别的直接播放的uri
     */
    public Uri load(String channel, String resolution);

    /**
     * 加载一个频道
     * @param url 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @return 一个播放器能识别的直接播放的uri
     */
    public Uri load(String url, String resolution, double startTime);

    /**
     * 对点播有用
     * @param startTime 随机点播时的起始时间点
     */
    public void seek(double startTime);

    public void pause();

    public void resume();

    /**
     * 卸载一个频道或者卸载一个vod视频，释放其占用的资源
     */
    public void unload();
}
