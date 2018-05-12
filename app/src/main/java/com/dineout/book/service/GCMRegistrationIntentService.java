package com.dineout.book.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.util.AppUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

public class GCMRegistrationIntentService extends IntentService {

    // Constructor
    public GCMRegistrationIntentService() {
        this(GCMRegistrationIntentService.class.getSimpleName());
    }

    // Constructor
    public GCMRegistrationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            // Get Registration Token
            String registrationToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            // Send Registration Token to Server
            sendRegistrationTokenToServer(registrationToken);

        } catch (Exception e) {
            // Save Flag
            DOPreferences.setSentTokenToServer(getApplicationContext(), false);

            // Save Registration Token
            DOPreferences.setGcmRegistrationToken(getApplicationContext(), "");
        }
    }

    // Send Registration Token to Server
    private void sendRegistrationTokenToServer(final String registrationToken) {
        // Check if GCM Registration Token has NOT been sent
        if ((TextUtils.isEmpty(DOPreferences.getGcmRegistrationToken(this)) ||
                DOPreferences.getSentTokenToServer(this) == false ||
                !AppUtil.getVersionName().equals(DOPreferences.getDOVersion(this))) &&
                !TextUtils.isEmpty(registrationToken)) {

            // Get Android Device ID
            AppUtil.captureDeviceId();


           // NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.SERVICE_INITIATED);
            // Call API
            DineoutNetworkManager.newInstance(getApplicationContext())
                    .jsonRequestGetService(101, AppConstant.URL_SEND_REG_ID,
                            ApiParams.getInsertMobileIdParams(registrationToken,
                                    DOPreferences.getDeviceId(MainApplicationClass.getInstance())),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
                                    if (responseObject != null && responseObject.optBoolean("status")) {
                                        // Handle success response for Send Reg Id API
                                        onSendRegIdApiSuccess(registrationToken);
                                    }
                                }
                            },
                            null, false);
        }
    }

    // Handle success response for Send Reg Id API
    private void onSendRegIdApiSuccess(String registrationToken) {
        // Save Flag
        DOPreferences.setSentTokenToServer(getApplicationContext(), true);

        // Save Registration Token
        DOPreferences.setGcmRegistrationToken(getApplicationContext(), registrationToken);

        // Save Current App Version
        DOPreferences.setDOVersion(getApplicationContext(), AppUtil.getVersionName() + "");

        // Update GCM Registration Id on MixPanel
//        AnalyticsHelper.getAnalyticsHelper(getApplicationContext
//                ()).mixPanelPushGCM(registrationToken);

        // Update GCM Registration Token on Hotline
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext())
                .updateGcmRegIdOnHotline(registrationToken);
    }
}
