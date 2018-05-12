package com.dineout.recycleradapters.util;

import android.graphics.Color;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sumon.chatterjee on 4/19/16.
 */
public class RestaurantDetailUtil {

    private static final int COLOR_R = 24;
    private static final int COLOR_G = 24;
    private static final int COLOR_B = 24;

    public static JSONObject getOutputParams(JSONObject jsonObject) {
        return (jsonObject != null && !jsonObject.isNull("output_params")) ?
                jsonObject.optJSONObject("output_params") : null;
    }

    public static JSONArray getDataArray(JSONObject jsonObject) {
        JSONObject outputParams = getOutputParams(jsonObject);
        if (outputParams != null) {
            return outputParams.optJSONArray("data");
        }
        return null;
    }

    public static JSONObject getData(JSONObject jsonObject) {
        JSONArray dataArray = getDataArray(jsonObject);
        if (dataArray != null && dataArray.length() > 0) {
            return dataArray.optJSONObject(0);
        }
        return null;
    }

    public static ArrayList<String> getPhoneNumbers(JSONObject data) {
        ArrayList<String> numbers = new ArrayList<>();
        if (data != null && !data.isNull("phone")) {
            JSONArray phoneNumber = data.optJSONArray("phone");
            if (phoneNumber != null && phoneNumber.length() > 0) {
                for (int i = 0; i < phoneNumber.length(); i++) {
                    try {
                        String c = phoneNumber.getString(i);
                        numbers.add(c);
                    } catch (JSONException ex) {

                    }
                }
            }
        }

        return numbers;
    }

    public static String getResturantName(JSONObject data) {
        if (data != null && !data.isNull("screen_name")) {
            return data.optString("screen_name");
        }

        return null;
    }

    public static int colorAlpha(int alpha) {
        return Color.argb(alpha, COLOR_R, COLOR_G, COLOR_B);
    }
}



