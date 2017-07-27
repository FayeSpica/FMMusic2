package cn.tonlyshy.fmmusicb.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by liaowm5 on 2017/7/27.
 */

public class ToastHelper {
    public static void show(Context mContext, String format, Object... args) {
        try {
            Toast.makeText(mContext, String.format(format, args), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
