package com.vbyte.p2p;

import android.net.Uri;

/**
 * Created by passion on 16-1-14.
 */
public interface IController {

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, String resolution, OnLoadedListener listener) throws Exception;

    /**
     * 加载一个频道，此函数没有起始时间参数
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param netState 网络状态
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, int netState, OnLoadedListener listener) throws Exception;

    /**
     * 加载一个频道
     * @param channel 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, String resolution, double startTime, OnLoadedListener listener) throws Exception;

    /**
     * 加载一个频道
     * @param channel 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @param listener 当成功load时的回调函数
     * @param async listener是否在UI线程回调
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, String resolution, double startTime, OnLoadedListener listener, boolean async) throws Exception;

    /**
     * 加载一个频道
     * @param channel 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @param netState 网络状态
     * @param listener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, String resolution, double startTime, int netState, OnLoadedListener listener) throws Exception;

    /**
     * 加载一个频道
     * @param channel 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @param netState 网络状态
     * @param listener 当成功load时的回调函数
     * @param async listener是否在UI线程回调
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     * @deprecated 请使用{@link com.vbyte.p2p.IController#load(ChannelInfo)}替代
     */
    void load(String channel, String resolution, double startTime, int netState, OnLoadedListener listener, boolean async) throws Exception;

    /**
     * 加载一个频道
     * @param channel 对直播是频道ID，对点播是资源链接
     * @param resolution 统一为 "UHD"
     * @return
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     */
    Uri loadAsync(String channel, String resolution) throws Exception;

    /**
     * 同步加载一个频道
     * @param channel 资源链接，主要为点播调用
     * @param resolution 统一为 "UHD"
     * @param startTime 视频的起始位置，以秒为单位
     * @return
     * @throws Exception 当load/unload没有成对调用时，会抛出异常
     */
    Uri loadAsync(String channel, String resolution, double startTime) throws Exception;

    /**
     * 对点播有用
     * @param startTime 随机点播时的起始时间点
     */
    void seek(double startTime);

    void pause();

    void resume();

    /**
     * 卸载一个频道或者卸载一个vod视频，释放其占用的资源
     */
    void unload();

    /**
     * 获取播放统计信息
     * @return
     */
    String playStreamInfo();

    /**
     * 控制器加载的频道信息, 使用builder模式创建，可只传递必要信息
     */
    class ChannelInfo {
        private ChannelInfo() {}

        /**
         * 资源链接，主要为点播调用
         */
        private String channel;
        /**
         * resolution 暂时统一为 "UHD"
         */
        private String resolution;
        /**
         * 视频的起始位置，以秒为单位
         */
        private double startTime;
        /**
         * 网络状态
         */
        private int netState;
        /**
         * 当成功load时的回调函数
         */
        private OnLoadedListener listener;
        /**
         * listener是否在UI线程回调
         */
        private boolean async;

        public static class Builder {
            private ChannelInfo channelInfo;

            public Builder(String channelUrl, OnLoadedListener listener) {
                channelInfo = new ChannelInfo();
                channelInfo.channel = channelUrl;
                channelInfo.listener = listener;
                channelInfo.resolution = "UHD";
            }

            public Builder channel(String channel) {
                channelInfo.channel = channel;
                return this;
            }

            public Builder listener(OnLoadedListener listener) {
                channelInfo.listener = listener;
                return this;
            }

            public Builder resolution(String resolution) {
                channelInfo.resolution = resolution;
                return this;
            }

            public Builder startTime(double startTime) {
                channelInfo.startTime = startTime;
                return this;
            }

            public Builder netState(int netState) {
                channelInfo.netState = netState;
                return this;
            }
            public Builder resolution(boolean async) {
                channelInfo.async = async;
                return this;
            }
            public ChannelInfo build() {
                return channelInfo;
            }
        }

        public String getChannel() {
            return channel;
        }

        public String getResolution() {
            return resolution;
        }

        public double getStartTime() {
            return startTime;
        }

        public int getNetState() {
            return netState;
        }

        public OnLoadedListener getListener() {
            return listener;
        }

        public boolean isAsync() {
            return async;
        }
    }

    /**
     * 加载一个频道
     * @param channel {@see Channel}
     * @throws Exception
     */
    void load(ChannelInfo channel) throws Exception;
}
