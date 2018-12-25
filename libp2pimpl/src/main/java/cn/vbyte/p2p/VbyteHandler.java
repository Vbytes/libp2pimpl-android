package cn.vbyte.p2p;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by passion on 16-8-23.
 */
public class VbyteHandler extends Handler implements MultiCallbackInterface {

    @Override
    public void handleMessage(Message msg) {
        int code = msg.what;
        long ctrlID  = msg.getData().getLong("ctrlID");
        String description = (String) msg.obj;
        didHandleMessage(code, description, ctrlID);
    }

    private void didHandleMessage(int code, String msg, long id) {
        BaseController controller = VbyteP2PModule.contrlMap.get(id);
        if (controller != null) {
            int prefixOfCode = code / 1000;
            switch (prefixOfCode) {
                case 10010:
                    controller.onEvent(code, msg);
                    break;
                case 10011:
                    controller.onError(code, msg);
                    break;
                case 10020:
                    controller.onEvent(code, msg);
                    break;
                case 10021:
                    controller.onError(code, msg);
                    break;
            }
        }

    }

    @Override
    public void handleMessage(int code, String msg, long id) {
        didHandleMessage(code, msg, id);
    }
}
