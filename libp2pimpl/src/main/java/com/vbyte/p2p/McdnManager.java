package com.vbyte.p2p;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class McdnManager {
    private static final String TAG = "McdnManager";

    public interface Event {
        /** 盒子守护线程模式已经启动 **/
        int DEAMON_STARTED = 10000015;

        /** 盒子守护线程已经停止 **/
        int DEAMON_STOPPED = 10000016;

        /** 返回NAT类型，当instruction为'UDPBlocked'/'SymmetricNAT'时，应告知客户无法分享
         *  Checking, 检查中
         *  Blocked, UDP Blocked
         *  OpenInternet, 开放外网
         *  FullCone, 全锥形NAT
         *  SymmetricUDPFirewall, UDP 防火墙
         *  RestricNAT, ip限制锥形NAT
         *  RestricPortNAT, 端口限制锥形NAT
         *  SymmetricNAT, 对称型网络
         *  ChangedAddressError, 变更地址异常
         *  Unknown, 未知
         * **/
        int NAT_DETECTED = 10000017;

        /** 定期回调，返回上一次回调的累积分享数据 **/
        int METRIC = 10000018;
    }

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


    private Handler eventHandler = null;
    private Handler errorHandler = null;

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
        Log.d(TAG, "start Deamon");
        this._startDeamon();
    }

    /**
     * 停止加速盒子守护线程
     */
    public void stopDeamon() {
        Log.d(TAG, "stop Deamon");
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

    /**
     * 设置用于接受事件的Handler, 事件ID(通过{@link Message.what} 获取) 定义在{@link McdnManager.Event}
     * @param eventHandler
     */
    public void setEventHandler(Handler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private void onEvent(int code, String msg) {
        Log.d(TAG, "onEvent " + code + " " + msg);
        notifyEvent(eventHandler, code, msg);
    }

    private void onError(int code, String msg) {
        Log.d(TAG, "onError " + code + " " + msg);
        notifyEvent(errorHandler, code, msg);
    }

    private void notifyEvent(Handler handler, int code, String msg) {
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = code;
            message.obj = msg;
            handler.sendMessage(Message.obtain(message));
        }
    }

    private native void _prepare();
    private native void _startDeamon();
    private native void _stopDeamon();
    private native void _setUserId(String userID);
    private native void _setBSSID(String bssid);
    private native int _getDeamonStatus();
}
