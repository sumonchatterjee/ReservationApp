package com.dineout.book.controller;

import android.content.Context;
import android.text.TextUtils;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;


public class LocationApiManager implements Response.Listener<JSONObject>, Response.ErrorListener {


    public static final int REQUEST_CODE_GET_LOCATION_DETAILS=888;
    private Context context;
    private DineoutNetworkManager networkManager;
    private static LocationApiManager locationApiManager;
    private OnLocationApiResponseListener onLocationApiResponseListener;
    private LocationApiManager(Context context, DineoutNetworkManager networkManager){
        this.context=context;
        this.networkManager=networkManager;

    }

    public static LocationApiManager getInstance(Context context, DineoutNetworkManager networkManager){
        if(locationApiManager ==null){
            locationApiManager =new LocationApiManager(context,networkManager);
        }
        return locationApiManager;
    }

    public void setOnLocationApiResponseListener(OnLocationApiResponseListener onLocationApiResponseListener){
        this.onLocationApiResponseListener=onLocationApiResponseListener;
    }

    public OnLocationApiResponseListener getOnLocationApiResponseListener(){
        return  onLocationApiResponseListener;
    }

    public void getLocationDetailsFromApi() {
        networkManager.jsonRequestGet(REQUEST_CODE_GET_LOCATION_DETAILS, AppConstant.URL_LOCATION_SEARCH,
                ApiParams.getLocationParams(
                        null, DOPreferences.getCurrentLatitude(context.getApplicationContext()),
                        DOPreferences.getCurrentLongitude(context.getApplicationContext())),
                this, this, true);
    }


    public void parseLocationDetailResponse(JSONObject responseObject) {

        if (responseObject != null && responseObject.optBoolean("status")) {
            // Get Output Params
            JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

            if (outputParamsJsonObject != null) {
                // Get Locations Object
                JSONObject locationsJsonObject = outputParamsJsonObject.optJSONObject("locations");

                if (locationsJsonObject != null) {
                    // Get Location Keys
                    JSONArray locationKeysJsonArray = outputParamsJsonObject.optJSONArray("location_keys");

                    if (locationKeysJsonArray != null && locationKeysJsonArray.length() > 0) {
                        String firstItem = locationKeysJsonArray.optString(0);

                        if (!TextUtils.isEmpty(firstItem)) {
                            processCityData(locationsJsonObject.optJSONArray(firstItem).optJSONObject(0));
                        }
                    }
                }
            }
        } else {
            if (!TextUtils.isEmpty(responseObject.optString("error_msg"))) {

//                // Show Alert Dialog
//                AlertDialog.Builder builder = UiUtil.getAlertDialog(context, "Some Error occurred", responseObject.optString("error_msg"),
//                        null, null, false, false, false);
//
//                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //startMainActivity(false);
//                    }
//                });
//
//                // Show Alert Dialog
//                builder.show();

                if(getOnLocationApiResponseListener()!=null) {

                    getOnLocationApiResponseListener().onLocationApiErrorMessageShow(responseObject.optString("error_msg"));
                }

            }
        }
    }

    public void processCityData(JSONObject locationJsonObject) {

        if(getOnLocationApiResponseListener()!=null) {

            getOnLocationApiResponseListener().onLocationApiSuccess(locationJsonObject);
        }


    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {

        if (request.getIdentifier() == REQUEST_CODE_GET_LOCATION_DETAILS) {
            parseLocationDetailResponse(responseObject);

        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if (request.getIdentifier() == REQUEST_CODE_GET_LOCATION_DETAILS) {
            if(getOnLocationApiResponseListener()!=null) {

                getOnLocationApiResponseListener().onLocationApiFailure();
            }


        }
    }

    public interface OnLocationApiResponseListener{
        void onLocationApiSuccess(JSONObject locationJsonObject);

        void onLocationApiFailure();

        void onLocationApiErrorMessageShow(String error_msg);

    }
}
