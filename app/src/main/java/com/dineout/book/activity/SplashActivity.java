package com.dineout.book.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.LocationApiManager;
import com.dineout.book.util.LocationUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class SplashActivity extends MasterDOLauncherActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationUtil.LocationUtilityHelper, LocationApiManager.OnLocationApiResponseListener {

    LocationUtil locationUtil = null;
    private int REQUEST_CODE_GET_APP_CONFIG = 106;
    LocationApiManager locationApiManager;
    private GoogleApiClient googleApiClient;
    private Timer locationTimer;
    private boolean isLocationFetched=false;
    private String responseFU="0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (checkPlayServices()) {
            initializeGoogleAPIClient();

            //track event for qGraph, apsalar
            trackEventForQgraphApsalar(getString(R.string.action_d_splash),new HashMap<String, Object>(),true,true,true);

            locationTimer = new Timer();
            if(TextUtils.isEmpty(DOPreferences.getELatitude(getApplicationContext()))) {


                locationTimer.schedule(new TimeoutLocation(), 7000);
            }
            else {
                locationTimer.schedule(new TimeoutLocation(), 4000);
            }
            // Take API Hit: Get App Config
            initGetAppConfig();
        } else {
            makeAlertDialog();
        }
        

    }


    private void initGetAppConfig(){



            getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_APP_CONFIG, AppConstant.URL_GET_APP_CONFIG,
                    null, this, null, true);
        }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        if (request.getIdentifier() == REQUEST_CODE_GET_APP_CONFIG) {
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("output_params") != null) {
                    JSONObject dataJsonObject = responseObject.
                            optJSONObject("output_params").optJSONObject("data");

                    if (dataJsonObject != null) {
                        // Save Config
                        DOPreferences.saveAppConfig(getApplicationContext(), dataJsonObject);

                        // save the accessibility flag
                        JSONObject systemConfigObject;
                        if ((systemConfigObject = dataJsonObject.optJSONObject("system_config")) !=
                                null) {
//                            AccessibilityPreference.storeAccessibilityFeatureEnableFalg(getApplicationContext(),
//                                   false);

                            JSONObject girfJSonObj=systemConfigObject.optJSONObject("girf");
                            if(girfJSonObj!=null) {
                                String isGirfEnabled = girfJSonObj.optString("is_enabled");
                                DOPreferences.setGIRFEnabled(getApplicationContext(), isGirfEnabled.equalsIgnoreCase("1") ? true : false);
                                String girf_url = girfJSonObj.optString("url");
                                DOPreferences.setGIRFURL(getApplicationContext(), girf_url);
                            }

                                String isCountlyEnabled=systemConfigObject.optString("countly_enabled");
                                DOPreferences.setIsCountlyEnabled(getApplicationContext(),isCountlyEnabled.equalsIgnoreCase("1") ? true : false );

                                // send screen tracking
                                trackScreenName(getString(R.string.countly_d_splash));

                                // track event for splash screen once countly flag is set
                                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getApplicationContext());
                                trackEventForCountlyAndGA(getString(R.string.countly_d_splash), getString(R.string.action_d_splash), getString(R.string.action_d_splash), hMap);

                            
                        }

                        }
                    }


                    //startMainActivity(isLocationFetched);

                }
            }

            if (response!=null && response.cacheEntry != null) {

                // final Response<JSONObject> response1 = response;


                if (response.cacheEntry.responseHeaders != null) {

                    String isForceUpdate = response.cacheEntry.responseHeaders.get("fu");

                    if (!TextUtils.isEmpty(isForceUpdate)) {
                        if (isForceUpdate.equals("1") || isForceUpdate.equals("2")) {

                            setResponseFU(isForceUpdate);
                        }
                    }



                }


            }
        }



    private void setResponseFU(String responseFU){
        this.responseFU=responseFU;
    }

    private String getResponseFU(){
        return responseFU;
    }

    private void makeAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Play Services Unavailable");
        builder.setMessage("Kindly update Google Play Services to use Dineout Application");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(false);

        builder.create().show();
    }

    protected void onStart() {
        super.onStart();

        connectGoogleAPIClient();

        locationApiManager = LocationApiManager.getInstance(SplashActivity.this, getNetworkManager());
        locationApiManager.setOnLocationApiResponseListener(this);
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).startCountly(this);
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }


    public void initializeGoogleAPIClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void connectGoogleAPIClient() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_splash_screen;
    }

    @Override
    public void handleLocationRequestSettingsResult(boolean success) {
        if (success) {
            locationUtil.getLocationFromGPS(this);
        } else {
            isLocationFetched=false;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getUserLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationFetched(Location location) {
        if (location != null) {
            DOPreferences.saveLatAndLong(
                    getApplicationContext(),
                    location);

            DOPreferences.setAutoMode(getApplicationContext(), true);

            if (locationApiManager != null) {
                locationApiManager.getLocationDetailsFromApi();
            }
        } else {
            DOPreferences.setAutoMode(getApplicationContext(), false);
            isLocationFetched=false;
        }
    }

    @Override
    public void onLocationFailed() {
        isLocationFetched=false;
    }

    @Override
    public void onLocationSettingsResolutionRequired(Status status) {

        if (TextUtils.isEmpty(DOPreferences.getELatitude(getApplicationContext()))) {
            try {
                status.startResolutionForResult(
                        SplashActivity.this,
                        LocationUtil.REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            // Splash Screen Subsequent launches Location Pop should not come
           isLocationFetched=true;
        }
    }

    @Override
    public void onLocationSettingsSuccess() {
        locationUtil.requestLocationUpdates();
    }

    private void startMainActivity(boolean isLocationFetchSuccessful) {
        Intent intent = new Intent(this, DineoutMainActivity.class);
        intent.putExtra("locationFetched", isLocationFetchSuccessful);
        intent.putExtra("forceUpgrade",getResponseFU());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (locationTimer != null) {
            locationTimer.cancel();
        }
    }


    public void getUserLocation() {
        fetchLocationFromGPS();
    }

    public void fetchLocationFromGPS() {
        locationUtil = new LocationUtil(this, this);
        locationUtil.setGoogleApiClient(googleApiClient);
        locationUtil.getLocationFromGPS(SplashActivity.this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (locationApiManager != null) {
            locationApiManager.setOnLocationApiResponseListener(null);
        }

        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).stopCountly();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        googleApiClient = null;

        if (locationTimer != null) {
            locationTimer.cancel();
            locationTimer = null;
        }
    }

    @Override
    public void onLocationApiSuccess(JSONObject locationJsonObject) {
        if (locationJsonObject == null) {
            isLocationFetched=false;
            return;
        }

        DOPreferences.saveLocationDetails(getApplicationContext(), locationJsonObject, true);
        isLocationFetched=true;
    }

    @Override
    public void onLocationApiFailure() {
        isLocationFetched=false;
    }

    @Override
    public void onLocationApiErrorMessageShow(String error_msg) {

        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Some Error occurred");
            builder.setMessage(error_msg);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startMainActivity(false);
                }
            });

            builder.setCancelable(false);
            builder.create().show();
        }
    }

    public class TimeoutLocation extends TimerTask {

        @Override
        public void run() {
            if (locationApiManager != null) {
                locationApiManager.setOnLocationApiResponseListener(null);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startMainActivity(isLocationFetched);
                }
            });

        }
    }
}
