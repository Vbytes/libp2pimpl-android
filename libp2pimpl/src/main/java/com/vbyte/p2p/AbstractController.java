package com.vbyte.p2p;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.vbyte.ChannelInfo;


/**
 * 每个Controller和用户的一个播放器对应，用户可以有多个Controller对应不同的播放器实例.
 * 这个接口定义每个Controller的用户接口.
 * {@link LiveController}
 * {@link VodController}
 */
abstract class AbstractController {
    /**
     * 当前的子类控制器标签
     */
    protected String TAG;

    /**
     * Event hadnelr 在XP2P线程中回调
     */
    CallbackInterface eventHandler = null;

    /**
     * Error hadnelr 在XP2P线程中回调
     */
    CallbackInterface errorHandler = null;

    /**
     * Event hadnelr 在Handler的线程中回调
     */
    Handler nativeEventHandler = null;

    /**
     * Error hadnelr 在Handler的线程中回调
     */
    Handler nativeErrorHandler = null;

    AbstractController() {
        this.TAG = VbyteP2PModule.TAG;
    }

    /**
     *
     * 设置EventHandler，注意该handler不能叠加，之前设置的handler将无效.事件直接在P2P线程回调.
     * @param handler 要设置的EventHandler实例
     */
    public void setEventHandler(CallbackInterface handler) {
        eventHandler = handler;
    }

    /**
     * 设置ErrorHandler，注意该handler不能叠加，之前设置的handler将无效.事件直接在P2P线程回调.
     * @param handler 要设置的ErrorHandler实例
     */
    public void setErrorHandler(CallbackInterface handler) {
        errorHandler = handler;
    }

    /**
     * 设置Event Handler，
     * @param handler Android的Handler, Message.what是事件码，Message.obj是事件数据
     */
    public void setAndroidEventHandler(Handler handler) {
        nativeEventHandler = handler;
    }

    /**
     * 设置Error Handler，
     * @param handler Android的Handler, Message.what是事件码，Message.obj是事件数据
     */
    public void setAndroidErrorHandler(Handler handler) {
        nativeErrorHandler = handler;
    }

    void notifyEventIfNeeded(CallbackInterface handler, int code, String msg) {
        if (handler != null) {
            try {
                handler.handleMessage(code, msg);
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    }

    void notifyEventIfNeeded(Handler handler, int code, String msg) {
        if (handler != null) {
            Message message = handler.obtainMessage();
            message.what = code;
            message.obj = msg;
            handler.sendMessage(Message.obtain(message));
        }
    }


    /**
     * 加载一个频道
     * @param channel {@link com.vbyte.ChannelInfo} 频道信息
     * @throws RuntimeException
     */
    abstract void load(ChannelInfo channel) throws RuntimeException;

    /**
     * 卸载一个频道，释放其占用的资源
     */
    abstract void unload() throws RuntimeException;

    /**
     * 销毁Native层创建的实例，请注意该函数被调用后控制器无法复用
     */
    abstract void destruct() throws RuntimeException;
}
