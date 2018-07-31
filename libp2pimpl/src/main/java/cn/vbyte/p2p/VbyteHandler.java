package cn.vbyte.p2p;

import android.os.Handler;
import android.os.Message;

/**
 * Created by passion on 16-8-23.
 */
public class VbyteHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        int code = msg.what;
        int id = msg.arg1;
        String description = (String) msg.obj;
        BaseController contrl = VbyteP2PModule.getInstance().contrlMap.get(id);
        if (contrl == null) {
            return;
        }
        contrl.onLocalEvent(code, description);
    }
}
