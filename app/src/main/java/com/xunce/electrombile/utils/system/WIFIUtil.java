package com.xunce.electrombile.utils.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Xingw on 2015/12/16.
 */
public class WIFIUtil {
    /**
     * 判断是不是在WIFI环境下
     * @param mContext
     * @return WIFI环境返回true 否则返回false
     */
    public static boolean isWIFI(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }
}
