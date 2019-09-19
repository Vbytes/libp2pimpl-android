package com.vbyte.p2p;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vbyte.LoggerCallback;
import com.vbyte.update.DynamicLibManager;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * XP2P-SDK全局配置
 */
public final class VbyteP2PModule {
    static final String TAG = "[TencentXP2P][Vbyte]";
    private static final String DYNAMIC_LIB_NAME = "libp2pmodule";

    public interface Event {
        /**
         * 公共的，告诉应用已经初始化完毕
         */
        int INITED = 10000000;

    }

    // 持久保存SDK版本号
    private static String SDK_VERSION;
    private static VbyteP2PModule instance;
    private static String archCpuAbi = "";
    static boolean initedSDK = false;

    /**
     * 新启动一个p2p模块，注意四个参数绝对不能为null,在程序启动时调用
     *
     * @param context      上下文
     * @param appId        应用唯一标识
     * @param appKey       应用密钥
     * @param appSecretKey 应用加密混淆字段，可选
     * @return P2PModule的唯一实例
     * @throws Exception 当参数为null或者p2p模块加载不成功时抛出异常
     */
    public static VbyteP2PModule create(Context context, String appId, String appKey, String appSecretKey)
            throws Exception {
        if (instance == null) {
            instance = new VbyteP2PModule(context, appId, appKey, appSecretKey);
        }
        return instance;
    }

    public static VbyteP2PModule getInstance() {
        return instance;
    }

    /**
     * 获取native应用的版本号
     *
     * @return SDK的版本号
     */
    public static String getVersion() {
        if (SDK_VERSION == null) {
            SDK_VERSION = VbyteP2PModule._version();
        }
        return SDK_VERSION;
    }

    //判断是不是这五种唯一支持的arch
    private static boolean isArchValid(String arch) {
        //所有合理的arch
        String[] allValidArch = {"armeabi", "armeabi-v7a", "arm64-v8a", "x86", "x86_64"};
        return Arrays.asList(allValidArch).contains(arch);
    }

    /**
     * 获取native应用的abi arch
     *
     * @return armeabi|armeabi-v7a|arm64-v8a|x86|x86_64
     * 返回5种架构中的一个获取null
     */
    public static String getArchABI() {
        if (archCpuAbi.isEmpty()) {
            archCpuAbi = VbyteP2PModule._targetArchABI();
        }
        return isArchValid(archCpuAbi) ? archCpuAbi : "";
    }

    /**
     * 启用调试模式，该模式会输出一些调试信息，对测试版本尤其有效
     */
    public static void enableDebug() {
        VbyteP2PModule._enableDebug();
    }

    /**
     * 关闭调试模式。默认调试功能是关闭的，发布应用时也应该关闭调试模式。
     */
    public static void disableDebug() {
        VbyteP2PModule._disableDebug();
    }

    /**
     * 设置一个回调接口，用于接收XP2P-SDK输出的日志
     *
     * @param logger
     */
    public static void setLoggerCallback(LoggerCallback logger) {
        LoggerCallback.setLoggerCallback(logger);
        VbyteP2PModule._setLoggerCallback();
    }

    /**
     * 获取P2P模块的版本号
     *
     * @return P2P模块的版本号
     */
    private static native String _version();

    /**
     * 获取当前运行的ABI名称
     *
     * @return 当前运行的ABI名称
     */
    private static native String _targetArchABI();

    /**
     * 打开调试模式，默认是关闭调试模式的
     */
    private static native void _enableDebug();

    /**
     * 关闭调试模式，应用上线时应关闭调试模式
     */
    private static native void _disableDebug();

    /**
     * 设置自定义logger打印回调函数
     */
    private static native void _setLoggerCallback();

    private DynamicLibManager dynamicLibManager;
    // native代码对应的对象实例，标准做法
    private long _pointer;
    private WeakReference<Context> _context;

    private VbyteP2PModule(Context context, String appId, String appKey, String appSecretKey)
            throws Exception {
        if (context == null || appId == null || appKey == null || appSecretKey == null) {
            throw new NullPointerException("context or appId or appKey or appSecretKey can't be null when init p2p live stream!");
        }

        prepareVbyteHandler();
        loadLibrary(context);
        construct(context, appId, appKey, appSecretKey);
    }

    private void prepareVbyteHandler() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            Looper.prepare();
        }
    }

    private void construct(Context context, String appId, String appKey, String appSecretKey) {
        _context = new WeakReference<>(context);
        String cacheDir = context.getCacheDir().getAbsolutePath();
        String diskDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        _pointer = this._construct(appId, appKey, appSecretKey, context, cacheDir, diskDir);
        if (_pointer == 0) {
            throw new RuntimeException("Can not init P2P");
        }
    }

    private void loadLibrary(Context context) {
        // System.loadLibrary("event");
        /**
         *
         * 能从jni里面获取到arch, 就进行下面的升级、加载，否则加载lib/ 下的libp2pmodule
         * android.os.Build.CPU_ABI、android.os.Build.SUPPORT_ABIS不靠谱，很多机型获取不到，不能用这个。因此，不用这个获取。
         * archCpuAbi再次验证一下
         */

        String soFilePath = null;
        dynamicLibManager = new DynamicLibManager(context);

        try {
            //这里加一个check libp2pmodule文件的md5值，因为应用目录/files目录下 很可能被别的应用扫描到给破坏了就load错误了
            soFilePath = dynamicLibManager.locate(DYNAMIC_LIB_NAME);
        } catch (Exception e) {
            // 因获取不到程序版本号而导致的自动升级失败，默认使用安装时自带的
        } catch (UnsatisfiedLinkError e) {

        }
        if (soFilePath == null) {
            System.loadLibrary("p2pmodule");
        } else {
            System.load(soFilePath);
        }

        if (!getArchABI().isEmpty()) {
            //得到了arch, 开始check升级用false即可
            dynamicLibManager.checkUpdateV2(false, "libp2pmodule_" + VbyteP2PModule.getVersion() + "_20170928.so", getArchABI());
        }
    }

    private void onEvent(int code, String msg, long ctrlID) {
        Log.i(TAG, "onEvent, code=" + code + ", msg=" + msg);
        if (code == Event.INITED) {
            initedSDK = true;
        }
    }

    private void onError(int code, String msg, long ctrlID) {
        Log.e(TAG, "onError, code=" + code + ", msg=" + msg);
    }

    /**
     * native应用初始化
     *
     * @return 成功返回native代码里面对应对象的指针，失败返回0
     */
    private native long _construct(String appId, String appKey, String appSecretKey, Context context, String cacheDir, String diskDir);

    /**
     * 设置imei
     *
     * @param imei    应用混淆密钥
     * @param pointer native层对应对象的指针
     */
    private native void _setImei(long pointer, String imei);
}