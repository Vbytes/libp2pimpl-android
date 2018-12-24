package cn.vbyte.p2p;

/**
 * XP2P Native消息的事件回调接口
 */

public interface CallbackInterface {
    /**
     * 使用这个接口LiveController无法使用多实例，若要使用多实例请使用Handler
     * @param code
     * @param msg
     */
    void handleMessage(int code, String msg);
}
