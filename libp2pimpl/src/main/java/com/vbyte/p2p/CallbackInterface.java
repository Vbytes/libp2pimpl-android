package com.vbyte.p2p;

/**
 * XP2P-SDK 消息的事件回调接口
 */

public interface CallbackInterface {
    /**
     * 接受来着P2P-SDK的事件
     * @param code 事件码
     * @param msg 事件对应的数据，取决于具体的事件
     */
    void handleMessage(int code, String msg);
}
