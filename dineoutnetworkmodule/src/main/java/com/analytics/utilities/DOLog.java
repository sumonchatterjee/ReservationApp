package com.analytics.utilities;

import android.text.TextUtils;
import android.util.Log;

import com.example.dineoutnetworkmodule.BuildConfig;

public class DOLog {

    private static final boolean ENABLE_LOG = true;//BuildConfig.DEBUG;

    private static String TAG = "Dineout";

    private static int LEVEL = (BuildConfig.DEBUG ? Log.VERBOSE : Log.WARN);

    private static void l(final int level, String message) {

        if (!ENABLE_LOG || level < LEVEL || TextUtils.isEmpty(message))
            return;


        switch (level) {
            case Log.VERBOSE:
                Log.v(TAG, message);
                break;
            case Log.DEBUG:
                Log.d(TAG, message);
                break;
            case Log.INFO:
                Log.i(TAG, message);
                break;
            case Log.WARN:
                Log.w(TAG, message);
                break;
            case Log.ERROR:
                Log.e(TAG, message);
                break;
            case Log.ASSERT:
                Log.wtf(TAG, message);
                break;
            default:
                break;
        }
    }

    public static void d(final String msg) {
        l(Log.DEBUG, msg);
    }

    public static void v(final String msg) {
        l(Log.VERBOSE, msg);
    }

    public static void i(final String msg) {
        l(Log.INFO, msg);
    }

    public static void w(final String msg) {
        l(Log.WARN, msg);
    }

    public static void e(final String msg) {
        l(Log.ERROR, msg);
    }

    public static void wtf(final String msg) {
        l(Log.ASSERT, msg);
    }

    public static void d(final String msg, final Throwable throwable) {
        l(Log.DEBUG, msg + '\n' + Log.getStackTraceString(throwable));
    }

    public static void v(final String msg, final Throwable throwable) {
        l(Log.VERBOSE, msg + '\n' + Log.getStackTraceString(throwable));
    }

    public static void i(final String msg, final Throwable throwable) {
        l(Log.INFO, msg + '\n' + Log.getStackTraceString(throwable));
    }

    public static void w(final String msg, final Throwable throwable) {
        l(Log.WARN, msg + '\n' + Log.getStackTraceString(throwable));
    }

    public static void e(final String msg, final Throwable throwable) {
        l(Log.ERROR, msg + '\n' + Log.getStackTraceString(throwable));
    }

    public static void wtf(final String msg, final Throwable throwable) {
        l(Log.ASSERT, msg + '\n' + Log.getStackTraceString(throwable));
    }
}
