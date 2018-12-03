package cn.vbyte.p2p;

import android.os.Handler;
import android.os.Message;

/**
 * Created by passion on 16-8-23.
 */
public class VbyteHandler implements CallbackInterface {

    @Override
    public void handleMessage(int code, String msg) {
        int prefixOfCode = code / 1000;
        switch (prefixOfCode) {
            case 10010:
                LiveController.getInstance().onEvent(code, msg);
                break;
            case 10011:
                LiveController.getInstance().onError(code, msg);
                break;
            case 10020:
                VodController.getInstance().onEvent(code, msg);
                break;
            case 10021:
                VodController.getInstance().onError(code, msg);
                break;
        }
    }
}
