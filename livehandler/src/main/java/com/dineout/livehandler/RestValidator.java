package com.dineout.livehandler;

import android.content.Context;
import android.text.TextUtils;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sawai on 20/08/16.
 */
public class RestValidator implements Response.Listener<JSONObject>,Response.ErrorListener {
    private Context mContext;
    private String mRestaurantName;
    private String mAddress;
    private DineoutNetworkManager mNetworkManager;
    private Request mRequest;
    private static int REQUEST_CODE_AUTO_SUGGEST =101;
    private static String searchTextRestaurant;

    public RestValidator(Context context) {
        this.mContext = context;
        mNetworkManager = DineoutNetworkManager.newInstance(context);
    }

    public static String getLocalRestId(AccessibilityController controller) {
        return searchTextRestaurant;
    }

    public void hitSearchApi(String restaurantName, String address){
        this.mRestaurantName = restaurantName;
        this.mAddress = address;
        searchTextRestaurant = restaurantName + " " + address;
        if(mRequest == null ) {

            mRequest=mNetworkManager.jsonRequestGet(REQUEST_CODE_AUTO_SUGGEST,
                    AppConstant.URL_SOLR_SEARCH_AUTO_SUGGEST,
                    ApiParams.getSolrSearchAutoSuggestParams(
                            DOPreferences.getCityName(mContext),
                            searchTextRestaurant,
                            false
                    ), this, null, true);
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        try {

            boolean status=responseObject.optBoolean("status");
            if(status) {
                JSONObject data = responseObject.optJSONObject("output_params").optJSONObject("data");
                if(data!=null) {
                    JSONArray restArray=data.optJSONArray("Restaurant");
                    if(restArray!=null && restArray.length() > 0) {
                        JSONObject restObject=restArray.optJSONObject(0);

                        if(restObject!=null){
                            String rId=restObject.optString("r_id");
                        if (!TextUtils.isEmpty(rId)) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.DIALOG_EVENT);
                            jsonObject.addProperty(AccessibilityUtils.RESTAURANT_ID, rId);
                            AccessibilityController.mBus.post(jsonObject);
                         }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // exception
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }


}
