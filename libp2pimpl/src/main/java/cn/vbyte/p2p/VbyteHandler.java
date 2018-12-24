package cn.vbyte.p2p;

import android.os.Handler;
import android.os.Message;

/**
 * Created by passion on 16-8-23.
 */
public class VbyteHandler extends Handler implements MultiCallbackInterface {

    @Override
    public void handleMessage(Message msg) {
        int code = msg.what;
        int id = msg.arg1;
        String description = (String) msg.obj;
        didHandleMessage(code, description, id);
    }

    private void didHandleMessage(int code, String msg, int id) {
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
    public void handleMessage(int code, String msg, int id) {
        didHandleMessage(code, msg, id);
    }
}
