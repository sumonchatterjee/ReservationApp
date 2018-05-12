package com.analytics.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Created by sumon.chatterjee on 4/20/16.
 */
public class NetworkUtil {

    public static String getNetworkType(Context context) {
        ConnectivityManager mConnectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        String networkType = null;
        if (info != null && info.isConnected()) {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                networkType = DONetwork.WI_FI;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                int networkSubType = info.getSubtype();
                switch (networkSubType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        networkType = DONetwork.TWO_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        networkType = DONetwork.THREE_G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        networkType = DONetwork.FOUR_G;
                        break;
                    default:
                        networkType = "other";
                }
            }
        }

        return networkType;
    }

    public static String appendSizeQuery(Context context, String url) {

        if (TextUtils.isEmpty(url)) {
            return url;
        }

        Uri.Builder builder = Uri.parse(url).buildUpon();

        if (NetworkUtil.DONetwork.TWO_G.equalsIgnoreCase(NetworkUtil.getNetworkType(context))) {
            builder.appendQueryParameter("q", "70");
        } else {
            builder.appendQueryParameter("q", "80");
        }

        return builder.build().toString();
    }


    public interface DONetwork {
        String TWO_G = "2g";
        String THREE_G = "3g";
        String FOUR_G = "4g";
        String WI_FI = "wifi";
    }
}
