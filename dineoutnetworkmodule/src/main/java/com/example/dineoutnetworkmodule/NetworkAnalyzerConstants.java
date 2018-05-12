package com.example.dineoutnetworkmodule;

import android.net.TrafficStats;

/**
 * Created by vibhas.chandra on 21/09/16.
 */
public class NetworkAnalyzerConstants {

    public static final int USER_INITIATED_POST = 1;
    public static final int USER_INITIATED_GET = 2;
    public static final int APP_INITIATED = 3;
    public static final int SERVICE_INITIATED =4;

    public static void trackNetworkCallType(int type){
        try {
            TrafficStats.setThreadStatsTag(type);
            // make network request using HttpClient.execute()
        } finally {
            TrafficStats.clearThreadStatsTag();
        }
    }
}
