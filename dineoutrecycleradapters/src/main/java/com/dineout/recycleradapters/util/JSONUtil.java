package com.dineout.recycleradapters.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sumon.chatterjee on 4/8/16.
 */
public class JSONUtil {

    public static boolean booleanValue(JSONObject jsonObject, String key) {
        return booleanValue(jsonObject, key, false);
    }

    public static boolean booleanValue(JSONObject jsonObject, String key, boolean defaultValue) {
        return defaultValue || (jsonObject != null &&
                !jsonObject.isNull(key) &&
                jsonObject.optInt(key) == 1);
    }


    public static boolean apiStatus(JSONObject responseObject) {
        return responseObject != null && responseObject.optBoolean("status");
    }


    public static JSONObject getFilters(JSONObject responseObject) {
        if (responseObject.optJSONObject("data") != null &&
                responseObject.optJSONObject("data").optJSONObject("filter") != null) {
            return responseObject.optJSONObject("data").optJSONObject("filter");
        }
        return null;
    }

    public static JSONArray getDealStreamData(JSONObject responseObject) {
        if (responseObject.optJSONObject("data") != null &&
                responseObject.optJSONObject("data").optJSONArray("stream") != null) {
            return responseObject.optJSONObject("data").optJSONArray("stream");
        }
        return null;
    }

    public static JSONArray getDealSortByData(JSONObject responseObject) {
        if (responseObject.optJSONObject("data") != null &&
                responseObject.optJSONObject("data").optJSONArray("sortby") != null) {
            return responseObject.optJSONObject("data").optJSONArray("sortby");
        }
        return null;
    }
}
