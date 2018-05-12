package com.analytics.utilities;

import android.content.Context;
import android.text.TextUtils;

import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.R;
import com.freshdesk.hotline.Hotline;
import com.freshdesk.hotline.HotlineConfig;
import com.freshdesk.hotline.HotlineUser;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class  AnalyticsUtil {

     //keep this as false only for production release
    final public static boolean IS_DEVELOPMENT = false;

    // Prepare Hotline User
    public static HotlineUser prepareHotlineUser(Context context) {
        HotlineUser hlUser = null;

        // Check if User Details are available
        if (!TextUtils.isEmpty(DOPreferences.getDinerEmail(context))) {
            // Update User Details
            hlUser = Hotline.getInstance(context).getUser();

            hlUser.setName(DOPreferences.getDinerFirstName(context) + " " +
                    DOPreferences.getDinerLastName(context));
            hlUser.setEmail(DOPreferences.getDinerEmail(context));
            hlUser.setPhone("+91", DOPreferences.getDinerPhone(context));
        }

        return hlUser;
    }

    public static Map<String, String> prepareHotlineUserProperties(Context context) {
        Map<String, String> userMeta = null;

        // Check if User Details are available
        if (!TextUtils.isEmpty(DOPreferences.getDinerEmail(context))) {
            userMeta = new HashMap<>();
            userMeta.put("DinerId", DOPreferences.getDinerId(context));
        }

        return userMeta;
    }

    // Prepare Hotline Config
    public static HotlineConfig prepareHotlineConfig(Context context) {
        HotlineConfig hotlineConfig = new HotlineConfig(
                context.getString(R.string.konotor_app_id),
                context.getString(R.string.konotor_app_key));

        hotlineConfig.setDomain("https://app.konotor.com");
        return hotlineConfig;
    }


    public static String getDateFormat(long dateInMillis){
        Date date = new Date(dateInMillis);
        String dateDetails = DateFormat.getDateInstance().format(date);
        return dateDetails;
    }
}
