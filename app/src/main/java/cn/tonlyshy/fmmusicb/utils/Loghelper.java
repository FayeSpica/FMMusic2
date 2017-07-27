package cn.tonlyshy.fmmusicb.utils;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public class Loghelper {
    public static String getTag(Class cls) {
        return cls.getSimpleName();
    }

    public static void v(String tag, String format, Object... messages) {
        android.util.Log.v(tag, String.format(format, messages));
    }

    public static void d(String tag, String format, Object... messages) {
        android.util.Log.d(tag, String.format(format, messages));
    }

    public static void i(String tag, String format, Object... messages) {
        android.util.Log.i(tag, String.format(format, messages));
    }

    public static void w(String tag, String format, Object... messages) {
        android.util.Log.w(tag, String.format(format, messages));
    }

    public static void e(String tag, String format, Object... messages) {
        android.util.Log.e(tag, String.format(format, messages));
    }
}
