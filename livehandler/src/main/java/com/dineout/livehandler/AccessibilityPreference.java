package com.dineout.livehandler;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by prateek.aggarwal on 6/23/16.
 */
public class AccessibilityPreference {
    private static final String prefName = "accessibility_pref";
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public static String cityList = "New Delhi|Mumbai|Hyderabad|Pune|Kolkata";
    public static String CITY_LIST = "city_accessibility";
    public static String getCityList() {
        return cityList;
    }

    public static String getCityListRaw(Context context) {
        String returnValue = "";
        if (context != null) {
            returnValue = getSharedPreferences(context).getString(CITY_LIST, "New Delhi|Mumbai|Hyderabad|Pune|Kolkata");
        }
        return returnValue;
    }

    public static void setCityList(Context context, String value) {
        if (context != null) {
            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putString(CITY_LIST, value);
            editor.apply();

            cityList = value;
        }
    }

    // should use accessibility features
    private static boolean isAccessibilityServiceEnabled;
    public static String ENABLE_SERVICE = "accessibility_enabled";
    public static boolean isAccessibilityFeatureEnabled() {
        return isAccessibilityServiceEnabled;
    }

    public static boolean isAccessibilityFeatureEnabledRaw(Context context) {
        boolean returnValue = false;
        if (context != null) {
            returnValue = getSharedPreferences(context).getBoolean(ENABLE_SERVICE, false);
        }
        return returnValue;
    }

    public static void storeAccessibilityFeatureEnableFalg(Context context,boolean flag) {
        if (context != null) {
            isAccessibilityServiceEnabled = flag;

            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putBoolean(ENABLE_SERVICE, flag);
            editor.apply();
        }
    }

    // check box flag
    private static boolean mShouldShowDialogFlag = true;
    public static String SHOW_DIALOG = "dialog_flag";
    public static boolean shouldShowDialog() {
        return mShouldShowDialogFlag;
    }

    public static boolean shouldShowDialogRaw(Context context) {
        boolean returnValue = false;
        if (context != null) {
            returnValue = getSharedPreferences(context).getBoolean(SHOW_DIALOG, true);
        }
        return returnValue;
    }

    public static void storeShowDialogFlag(Context context,boolean flag) {
        if (context != null) {
            mShouldShowDialogFlag = flag;

            SharedPreferences.Editor editor = getSharedPreferences(context).edit();
            editor.putBoolean(SHOW_DIALOG, flag);
            editor.apply();
        }
    }

}
