package com.vbyte.p2p;

import com.vbyte.ChannelInfo;
import com.vbyte.OnLoadedListener;

class LoadEvent{
    enum VideoType {
        /** 直播 **/
        VIDEO_LIVE,

        /** 点播 **/
        VIDEO_VOD
    }

    public VideoType videoType;
    public String channel;
    public String resolution;
    public double startTime;
    public ChannelInfo.NetState netState;
    public OnLoadedListener listener;

    public LoadEvent(VideoType videoType, String channel, String resolution, double startTime, ChannelInfo.NetState netState, OnLoadedListener listener) {
        this.videoType = videoType;
        this.channel = channel;
        this.resolution = resolution;
        this.netState = netState;
        this.startTime = startTime;
        this.listener = listener;
    }
}
