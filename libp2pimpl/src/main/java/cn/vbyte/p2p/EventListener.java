package cn.vbyte.p2p;

/**
 * Created by passion on 15-11-5.
 */
public interface EventListener {
    /**
     * 仿照android的事件处理职责链模式
     *
     * @param code 标识事件类型的事件码
     * @param message 事件说明
     * @return true， 后续的事件不再处理，包括listener的
     *         false，后续的事件继续处理
     */
    void onEvent(int code, String message);

    /**
     * 仿照android的错误处理职责链模式
     *
     * @param code 代表一类错误的错误码
     * @param message 错误说明
     * @return true， 后续的错误不再处理，包括listener的
     *         false，后续的错误继续处理
     */
    void onError(int code, String message);
}
