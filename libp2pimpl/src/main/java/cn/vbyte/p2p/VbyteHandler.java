//package cn.vbyte.p2p;
//
//import android.os.Handler;
//import android.os.Message;
//
///**
// * Created by passion on 16-8-23.
// */
//public class VbyteHandler extends Handler {
//
//    @Override
//    public void handleMessage(Message msg) {
//        int code = msg.what;
//        int prefixOfCode = code / 1000;
//        String description = (String) msg.obj;
//        switch (prefixOfCode) {
//            case 10010:
//                LiveController.getInstance().onLocalEvent(code, description);
//                break;
//            case 10011:
//                LiveController.getInstance().onLocalError(code, description);
//                break;
//            case 10020:
//                VodController.getInstance().onLocalEvent(code, description);
//                break;
//            case 10021:
//                VodController.getInstance().onLocalError(code, description);
//                break;
//        }
//    }
//}
