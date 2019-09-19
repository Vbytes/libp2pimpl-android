package com.vbyte.p2p;

/**
 * 多实例版本的回调接口
 */
public interface MultiCallbackInterface{
//]public interface MultiCallbackInterface extends CallbackInterface{

    /**
     * 监听来自XP2P Native层的回调
     * @param code 事件编号
     * @param msg 事件参数
     * @param id LiveController的实例ID
     */
    public void handleMessage(int code, String msg, long id);
}
