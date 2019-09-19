package com.vbyte;


/**
 * 控制器加载的频道信息, 通过{@link com.vbyte.ChannelInfo.Builder} 创建
 */
public class ChannelInfo {

    /**
     * 当前的网络类型(目前仅区分移动网络及WIFI网络)
     */
    public enum NetState {
        /** 移动网络 **/
        NET_MOBILE(0),

        /** WIFI网络 **/
        NET_WIFI(1);

        private int value;
        NetState(int value) { this.value = value;}
        public int value() { return value; }
    }

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
    private ChannelInfo.NetState netState;
    /**
     * 当成功load时的回调函数
     */
    private OnLoadedListener listener;

    /**
     * {@link ChannelInfo}的构建器, 可只传递必要信息
     */
    public static class Builder {
        private ChannelInfo channelInfo;

        /**
         * 构建一个Builder, 必须提供确切的频道链接，以及{@link OnLoadedListener}
         * @param channelUrl
         * @param listener
         */
        public Builder(String channelUrl, OnLoadedListener listener) {
            channelInfo = new ChannelInfo();
            channelInfo.channel = channelUrl;
            channelInfo.listener = listener;
            channelInfo.resolution = "UHD";
            channelInfo.netState = NetState.NET_WIFI;//默认是WIFI
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

        public Builder netState(ChannelInfo.NetState netState) {
            channelInfo.netState = netState;
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

    public ChannelInfo.NetState getNetState() {
        return netState;
    }

    public OnLoadedListener getListener() {
        return listener;
    }
}
