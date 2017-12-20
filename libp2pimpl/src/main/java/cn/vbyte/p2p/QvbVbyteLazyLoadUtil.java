package cn.vbyte.p2p;

/**
 * Created by hugodeng on 2017/12/20.
 */

public class QvbVbyteLazyLoadUtil {
    /**
     * 获取当前运行的ABI名称
     * @return 当前运行的ABI名称
     */
    private static native String _qvbvbyte_targetArchABI();

    public static final String getTargetArchABI() {
        return _qvbvbyte_targetArchABI();
    }
}