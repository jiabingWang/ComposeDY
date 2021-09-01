package com.sixsixsix.dy.help;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * description:网络监测的工具
 */
public class ConnectedUtils {
    public static boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public static int CONNECT_WIFI = 1;
    public static int CONNECT_4G = 0;
    public static int CONNECT_UNUSED = -1;

    public static int getConnectType(Context mContext) {
        ConnectivityManager systemService = (ConnectivityManager) mContext.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo =
                systemService.getActiveNetworkInfo();
        if (systemService != null && activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return CONNECT_WIFI;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return CONNECT_4G;
            } else {
                return CONNECT_UNUSED;
            }
        }
        return CONNECT_UNUSED;
    }
}
