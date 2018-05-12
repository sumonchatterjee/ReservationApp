package com.dineout.book.util;

import android.content.Context;
import android.os.Bundle;

import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfirmationDataUtils {
    static Context context;

    public static ConfirmationDataUtils newInstance(Context mContext) {
        ConfirmationDataUtils dealConfirmationFragment = new ConfirmationDataUtils();
        context = mContext;
        return dealConfirmationFragment;
    }

    public JSONArray getEventJSONArray(Bundle bundle, JSONObject eventsObj, String extraInfoJsonString) {
        JSONArray eventsAdapterArray = new JSONArray();

        if (bundle != null) {
            try {

                String userFullName = (DOPreferences.getDinerFirstName(context) + " " + DOPreferences.getDinerLastName(context));
                if (eventsObj != null) {
                    eventsObj.put("type", "ticket");
                    eventsObj.put("restName", bundle.getString(AppConstant.REST_NAME));
                }


                eventsAdapterArray.put(0, eventsObj);
                eventsAdapterArray.put(1, getHeaderJSONObject("GUEST DETAILS", ""));
                eventsAdapterArray.put(2, getGuestJSONObject(userFullName, DOPreferences.getDinerPhone(context), "", DOPreferences.getDinerEmail(context)));

                // Check for Extra Info
                if (!AppUtil.isStringEmpty(extraInfoJsonString)) {
                    eventsAdapterArray.put(3, getExtraInfo(extraInfoJsonString));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return eventsAdapterArray;
    }

    private JSONObject getExtraInfo(String extraInfoJsonString) {
        JSONObject extraInfoSectionJsonObject = new JSONObject();

        try {
            extraInfoSectionJsonObject.put("type", "extraInfo");
            extraInfoSectionJsonObject.put("extraInfo", new JSONArray(extraInfoJsonString));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return extraInfoSectionJsonObject;
    }

    private JSONObject getGuestJSONObject(String name, String phone, String req, String email) {
        JSONObject guestObject = new JSONObject();

        try {
            guestObject.put("name", name);
            guestObject.put("phone", phone);
            guestObject.put("req", req);
            guestObject.put("email", email);
            guestObject.put("type", "info");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return guestObject;
    }

    private JSONObject getHeaderJSONObject(String title, String isEdit) {
        JSONObject headerObject = new JSONObject();

        try {
            headerObject.put("title", title);
            headerObject.put("isEdit", isEdit);
            headerObject.put("type", "header");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return headerObject;
    }

    // will be used for deals
    public JSONArray getDealsJSONArray(Bundle bundle, JSONArray dealsArray, String extraInfoJsonString) {
        JSONArray dealsAdapterArray = new JSONArray();

        if (bundle != null) {
            try {

                dealsAdapterArray.put(0, getDealDetailJSONObject(bundle.getLong(AppConstant.DATE), dealsArray));
                dealsAdapterArray.put(1, getHeaderJSONObject("GUEST DETAILS", ""));
                dealsAdapterArray.put(2, getGuestJSONObject(bundle.getString(AppConstant.DINER_NAME),
                        bundle.getString(AppConstant.DINER_PHONE), bundle.getString(AppConstant.SPCL_REQUEST),
                        bundle.getString(AppConstant.DINER_EMAIL)));

                // Check for Extra Info
                if (!AppUtil.isStringEmpty(extraInfoJsonString)) {
                    dealsAdapterArray.put(3, getExtraInfo(extraInfoJsonString));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return dealsAdapterArray;
    }

    private JSONObject getDealDetailJSONObject(long dateTime, JSONArray dealsArray) {
        JSONObject dealObject = new JSONObject();
        try {
            dealObject.put("type", "ticket");
            dealObject.put("dateTime", dateTime);
            dealObject.put("dealsArr", dealsArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dealObject;
    }
}
