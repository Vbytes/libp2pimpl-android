package cn.vbyte.p2p;

public abstract class LoggerCallback {

    void v(String tag, String msg);
    void d(String tag, String msg);
    void i(String tag, String msg);
    void w(String tag, String msg);
    void e(String tag, String msg);

    private static LoggerCallback logger = null;
    public static setLoggerCallback(LoggerCallback aLoggerCallback) {
        logger = aLoggerCallback;
    }

    private static void verbose(String tag, String msg) {
        if (logger != null) {
            logger.v(tag, msg);
        }
    }

    private static void info(String tag, String msg) {
        if (logger != null) {
            logger.i(tag, msg);
        }
    }

    private static void debug(String tag, String msg) {
        if (logger != null) {
            logger.d(tag, msg);
        }
    }

    private static void warn(String tag, String msg) {
        if (logger != null) {
            logger.w(tag, msg);
        }

    }

    private static void error(String tag, String msg) {
        if (logger != null) {
            logger.e(tag, msg);
        }
    }
}