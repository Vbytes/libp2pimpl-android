package com.vbyte.p2p;

public class McdnManager {

    public enum DeamonStatus{
        /** 任务停止 **/
        STATUS_STOPPED,
        /** 任务开启 **/
        STATUS_STARTED,
        /** 任务运行中 **/
        STATUS_RUNNING,
        /** 任务运行中且在分享数据 **/
        STATUS_SHARING
    }

    private static class Holder {
        public static final McdnManager sINSTANCE = new McdnManager();
    }


    private McdnManager() {
        this._prepare();
    }

    public static McdnManager getInstance() {
        return Holder.sINSTANCE;
    }

    /**
     * 设置userId，以便全网用户统一计费
     * @param userId
     */
    public void setUserId(String userId) {
        _setUserId(userId);
    }

    /**
     * 设置bssid(AP Mac address)
     * @param bssid
     */
    public void setBssid(String bssid) {
        _setBSSID(bssid);
    }

    /**
     * 设置userId并启动盒子加速模式的守护线程
     * @param userId
     */
    public void startDeamonWithUserId(String userId) {
        _setUserId(userId);
        startDeamon();
    }

    /**
     * 启动盒子加速模式的守护线程.
     * 启动后终端会按需向CDN请求流量，并分享出去给需要的用户使用
     */
    public void startDeamon() {
        this._startDeamon();
    }

    /**
     * 停止加速盒子守护线程
     */
    public void stopDeamon() {
        this._stopDeamon();
    }

    /**
     *  @return 返回守护进程状态
     *  STATUS_STOPPED= 0,  // 暂停
     *  STATUS_STARTED= 1,  // 开启
     *  STATUS_RUNNING= 2, // 运行任务中
     *  STATUS_SHARING= 3, // 运行任务中，分享数据
     */
    public DeamonStatus getDeamonStatus() {
        return DeamonStatus.values()[this._getDeamonStatus()];
    }

    private native void _prepare();
    private native void _startDeamon();
    private native void _stopDeamon();
    private native void _setUserId(String userID);
    private native void _setBSSID(String bssid);
    private native int _getDeamonStatus();
}
