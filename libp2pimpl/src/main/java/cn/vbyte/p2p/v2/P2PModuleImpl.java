package cn.vbyte.p2p.v2;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.vbyte.p2p.OnLoadedListener;
import com.vbyte.p2p.P2PHandler;
import com.vbyte.p2p.P2PModule;

import cn.vbyte.p2p.VbyteP2PModule;
import cn.vbyte.p2p.LiveController;

/**
 * Created by passion on 16-5-13.
 */
public class P2PModuleImpl implements P2PModule {
    private static P2PModule instance;


    public static P2PModule getInstance(String appId, String appKey, String appSecret, Context context) {
        if (instance == null) {
            instance = new P2PModuleImpl(appId, appKey, appSecret, context);
        }
        return instance;
    }

    /**
     * is Nullable
     * @return P2PModule的全局唯一实例，可能为null
     */
    public static P2PModule getInstance() {
        return instance;
    }

    ///////////////////////////////////////////////////////////////
    /*
     * 代理模式，为了兼容老版本的API
     */
    private VbyteP2PModule proxy;
    private Handler handler;
    private String statistic;

    public P2PModuleImpl(String appId, String appKey, String appSecret, Context context) {
        try {
            proxy = VbyteP2PModule.create(context, appId, appKey, appSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPlayPath(String channel) {
        return null;
    }

    @Override
    public void getPlayPath(String channel, OnLoadedListener listener) {
        try {
            LiveController.getInstance().load(channel, "UHD", listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSDKVersion() {
        return VbyteP2PModule.getVersion();
    }

    @Override
    public String getStatistics() {
        return statistic;
    }

    @Override
    public int getCurrentPlayTime() {
        // return LiveController.getInstance();
        return 0;
    }

    @Override
    public void closeModule() {
        LiveController.getInstance().unload();
        proxy = null;
    }

    @Override
    public void setP2PHandler(final P2PHandler handler) {
        this.handler = handler;
        /**
         * aHander就是为了满足俊哥而弄的代理
         */
        Handler aHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Message message = Message.obtain();
                message.obj = msg.obj;
                switch (msg.what) {
                    case VbyteP2PModule.Event.REPORTED:
                        P2PModuleImpl.this.statistic = (String)msg.obj;
                        break;
                    case VbyteP2PModule.Event.STREAM_READY:
                        message.what = P2PHandler.p2p_FirstDataSuccess;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    case VbyteP2PModule.Event.CONF_READY:
                        message.what = P2PHandler.p2p_ChannelInfoSuccess;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    case VbyteP2PModule.Event.DATA_UNBLOCK:
                        message.what = P2PHandler.p2p_WriteDataUnblock;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    case VbyteP2PModule.Error.CONF_INVALID:
                        message.what = P2PHandler.p2p_ChannelInfoFail;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    case VbyteP2PModule.Error.BAD_NETWORK:
                        message.what = P2PHandler.cdn_DownLoadFail;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    case VbyteP2PModule.Error.DATA_BLOCK:
                        message.what = P2PHandler.p2p_WriteDataBlock;
                        P2PModuleImpl.this.handler.sendMessage(message);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        if (proxy != null) {
            proxy.setEventHandler(aHandler);
            proxy.setErrorHandler(aHandler);
        }
    }
}
