package com.vbyte.p2p;

import android.net.Uri;
import android.util.Log;

import com.vbyte.ChannelInfo;
import com.vbyte.OnLoadedListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 控制器基类，直播/点播控制器复用的功能放在这里面，这个类不对用户开放
 */
public abstract class BaseController extends AbstractController {

    public interface Event {
        /**
         * 公共的，告诉应用流已经准备就绪
         */
        int STREAM_READY = 10000004;                // 公共的，流已经准备就绪
        /**
         * 公共的，告诉应用配置已经就绪
         */
        int CONF_READY = 10000005;                  // 公共的，配置已经就绪
        int STUN_CONNECTED = 10000006;              // 公共的，点播，直播都在监听一个，且同一时刻，只能有一个监听此信号
        /**
         * 公共的，告诉应用已经加入P2P
         */
        int JOINED = 10000007;                      // 公共的，属于tracker的
        /**
         * 公共的，告诉应用伙伴已经就绪
         */
        int PARTNER_READY = 10000008;               // 公共的，伙伴已经就绪
        /**
         * 公共的，告诉应用有一个新伙伴加入你
         */
        int NEW_PARTNER = 10000009;                 // 公共的，有一个新的伙伴
        /**
         * 公共的，表明P2P稳定
         */
        int P2P_STABLE = 10000010;                  // 公共的，表明P2P稳定
        /**
         * 公共的，表明一次上报统计事件
         */
        int REPORTED = 10000011;                    // 公共的，监听的上报信号

        /**
         * 公共的，表明卡播恢复
         */
        int DATA_UNBLOCK = 10000012;                    // 公共的，监听的上报信号
        /**
         * 公共的，表明接收到请求
         */
        int RECEIVE_REQUEST = 10000014;

        /**
         * 启动一个流
         */
        int START = 10010000;
        /**
         * 告诉应用流已经启动
         */
        int STARTED = 10010001;
        /**
         * 停止一个流
         */
        int STOP = 10010002;
        /**
         * 告诉应用流已被停止
         */
        int STOPPED = 10010003;

        /**
         * 告诉应用流播放异常，需要回源播放
         */
        int BACK_TO_ORIGIN = 10010005;

        int STATISTICS = 10010006;
        int WANT_IMEI = 10010007;
        /**
         * 返回切片重定向后的地址
         */
        int REDIRECT_ADDR = 10010008;
    }

    public interface Error {
        /**
         * 表明配置服务不可用，此时加载失败
         */
        int CONF_UNAVAILABLE = 10001000;
        /**
         * 表明认证失败，请检查传入的appId，appKey，appSecretKey是否正确，
         * 以及是否与当前应用对应得上。
         */
        int AUTH_FAILED = 10001001;
        /**
         * 表示conf的内容错误，不能被解析
         */
        int CONF_INVALID = 10001002;
        /**
         * 表明下载资源时网络太差，这很有可能是资源服务器的带宽不足引起的
         */
        int SRC_UNSTABLE = 10001003;
        /**
         * 流媒体解析错误，请确认要播放的流是正确的能被解析的流媒体。
         */
        int MEDIA_ERROR = 10001004;
        /**
         * 网络不好
         */
        int BAD_NETWORK = 10001005;
        /**
         * 这个说明穿透不成功，此错误通常因客户端所在网络限制造成的，将导致没有P2P加速。
         */
        int STUN_FAILED = 10001006;
        /**
         * 此错误说明某次获取伙伴失败了，不过不用太担心，它会一段时间后重试
         */
        int JOIN_FAILED = 10001007;
        /**
         * 此错误事件表明一次上报事件失败，不会有其他影响
         */
        int REPORT_FAILED = 10001008;
        /**
         * 此错误表明收到未知类型的包，将忽略，一般很少发生
         */
        int UNKNOWN_PACKET = 10001009;
        /**
         * 此错误表明收到包数据签名不对，将忽略，一般很少发生
         */
        int INVALID_PACKET = 10001010;
        /**
         * 此错误表明数据不够，发生卡播
         */
        int DATA_BLOCK = 10001011;

        /**
         * 此错误是因为该频道ID不存在，或者已被删除
         */
        int NO_SUCH_CHANNEL = 10011001;
        /**
         * 此错误表明传入的分辨率不对
         */
        int RESOLUTION_INVALID = 10011002;

        int FORMAT_INVALID = 10011003;

        int SOURCE_DATA_ERROR = 10011004;
    }

    /**
     * 记录维护连续的load事件
     */
    private List<LoadEvent> loadQueue = Collections.synchronizedList(new LinkedList<LoadEvent>());

    /**
     * 当前load事件, 每个控制器实例当前只能处理一个事件
     */
    private LoadEvent curLoadEvent = null;

    /**
     * 用于标记{@link BaseController#destruct()} 调用时native层未执行完上次load的情况
     */
    private boolean pendingDestruct = false;

    /**
     * 当时控制器的视频类型， 直播或者点播，需要在子类赋值
     */
    protected LoadEvent.VideoType videoType;

    /**
     * 当前控制器在native层实例的指针，需要在子类赋值
     */
    protected long _pointer = 0;

    protected Statistic statistic;

    /**
     * 子类控制器这个函数中执行native层的加载动作
     **/
    abstract void doNativeLoad(long pointer, String channel, String resolution, double startTime, int netState);

    /**
     * 子类控制器在该函数中执行native层的卸载动作
     **/
    abstract void doNativeUnload(long pointer);

    /**
     * 子类控制器在该函数中执行nativeg层的销毁动作
     **/
    abstract void doNativeDestruct(long pointer);

    public BaseController() {
        super();
        statistic= new Statistic();
    }

    /**
     * 返回当前实例在Native层的指针
     *
     * @return
     */
    protected long getPointer() {
        if (_pointer == 0) {
            throw new RuntimeException("Native object has been destroyed, Did you called destruct()?");
        }
        return _pointer;
    }

    /**
     * 加载一个频道，此函数没有起始时间参数
     *
     * @param channel {@see IController.Channel}
     * @throws RuntimeException
     */
    @Override
    public void load(ChannelInfo channel) throws RuntimeException {
        this.load(channel.getChannel(),
                channel.getResolution(),
                channel.getStartTime(),
                channel.getNetState(),
                channel.getListener());
    }

    private void addEvent(String channel, String resolution, double startTime,
                          ChannelInfo.NetState netState, OnLoadedListener onLoadedListener) {
        if (!loadQueue.isEmpty()) {
            loadQueue.clear();
        }
        LoadEvent loadEvent = new LoadEvent(videoType, channel, resolution, startTime, netState, onLoadedListener);
        loadQueue.add(loadEvent);
    }

    private void processEvent(String channel, String resolution, double startTime,
                              ChannelInfo.NetState netState) {
        if (VbyteP2PModule.initedSDK && curLoadEvent == null) {
            curLoadEvent = loadQueue.get(0);
            loadQueue.remove(0);
            Log.i(TAG, this + ":native load");
            doNativeLoad(getPointer(), channel, resolution, startTime, netState.value());
        }
    }

    /**
     * 加载一个频道
     * @param channel          直播流频道ID
     * @param resolution       统一为 "UHD"
     * @param startTime        视频的起始位置，以秒为单位，支持一天之内的视频时移回放
     * @param netState         网络状态
     * @param onLoadedListener 当成功load时的回调函数
     * @throws Exception 当load/unload没有成对调用时，会抛出异常提示
     */
    public void load(String channel, String resolution, double startTime,
                        ChannelInfo.NetState netState, OnLoadedListener onLoadedListener) {
        synchronized (this) {
            addEvent(channel, resolution, startTime, netState, onLoadedListener);
            Log.i(TAG, this + ",load");
            processEvent(channel, resolution, startTime, netState);
        }
    }

    /**
     * 卸载当前频道
     */
    @Override
    public void unload() throws RuntimeException {
        Log.i(TAG, this + ", unload");
        synchronized (this) {
            //当前有事件的时候, 才unload, 屏蔽空unload
            if (curLoadEvent != null) {
                Log.i(TAG, this + ", real unload");
                doNativeUnload(getPointer());
            }
        }
    }

    /**
     * 销毁Native层实例，调用该函数后当前Controller无法继续使用，若要使用请重新创建
     */
    @Override
    public void destruct() throws RuntimeException {
        if (curLoadEvent == null) {
            doNativeDestruct(getPointer());
            _pointer = 0;
        } else {
            //用户已调用unload但未收到stoped信号, 我们标记一下在收到stoped之后再析构
            pendingDestruct = true;
        }
    }

    protected void onEvent(int code, String msg) {
        // Log.i(TAG, "onEvent code:" + code + ",msg:" + msg);
        switch (code) {
            case VbyteP2PModule.Event.INITED:
                recvAsyncEvent();
                break;
            case Event.STARTED:
                gotProxyAddress(msg);
                break;
            case Event.STOPPED:
                recvAsyncEvent();
                break;
            case Event.STATISTICS:
                statistic.update(msg);
                break;
            default:
                break;
        }
        //将消息发送给客户
        notifyEventIfNeeded(eventHandler, code, msg);
        notifyEventIfNeeded(nativeEventHandler, code, msg);
    }

    protected void onError(int code, String msg) {
        //将消息发送给客户
        notifyEventIfNeeded(errorHandler, code, msg);
        notifyEventIfNeeded(nativeErrorHandler, code, msg);
    }


    private void gotProxyAddress(String msg) {
        synchronized (this) {
            if (curLoadEvent != null) {
                Uri uri = Uri.parse(msg);
                if (curLoadEvent.listener != null) {
                    curLoadEvent.listener.onLoaded(uri);
                    curLoadEvent.listener = null;
                    Log.i(TAG, this + ", Got proxy address.");
                }
            }
        }
    }

    /**
     * 在unload之后Event.STOPPED、INITED信号收到以前，用户可能会执行{@link LiveController#load 或者 {@link LiveController#destruct()}
     * 这个时候我们需要在收到该信号以后完成用户执行的操作
     */
    private void recvAsyncEvent() {
        synchronized (this) {
            if (pendingDestruct) {
                Log.i(TAG, this + ":Pedding destruct, do it.");
                if (getPointer() != 0) {
                    doNativeDestruct(getPointer());
                }
                _pointer = 0;
                curLoadEvent = null;
            } else if (!loadQueue.isEmpty()) {
                Log.i(TAG, this + ":Load channel before unload/init, get it out.");
                // unload or init完成以前用户加载了频道
                curLoadEvent = loadQueue.get(0);
                loadQueue.remove(0);
                if (curLoadEvent.videoType == videoType) {
                    doNativeLoad(getPointer(), curLoadEvent.channel, curLoadEvent.resolution, curLoadEvent.startTime, curLoadEvent.netState.value());
                }
            } else {
                curLoadEvent = null;
            }
        }
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void resetStatistic() {
        statistic.reset();
    }
}
