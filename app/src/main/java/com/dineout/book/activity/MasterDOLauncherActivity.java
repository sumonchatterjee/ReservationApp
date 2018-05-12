package com.dineout.book.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;

import com.dineout.book.interfaces.AccountPermissionListener;
import com.dineout.book.interfaces.CallPhonePermissionGrantedListener;
import com.dineout.book.interfaces.OnLocationPermissionGrantedListener;
import com.dineout.book.util.AppUtil;
import com.dineout.book.controller.LocationHandler;
import com.dineout.book.util.LocationUtil;
//import com.dineout.livehandler.AccessibilityUtils;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.facebook.CallbackManager;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.branch.referral.Branch;

public abstract class MasterDOLauncherActivity extends AppCompatActivity
        implements Response.Listener<JSONObject> {

    private DineoutNetworkManager networkManager;
    private CallbackManager callbackManager;
    private CallPhonePermissionGrantedListener callPhonePermissionGrantedListener;
    private AccountPermissionListener mAccPermissionListener;
    private OnLocationPermissionGrantedListener mLocationPermissionListener;


    protected abstract int getActivityLayout();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);
        setContentView(getActivityLayout());

        // Capture Device Id
        AppUtil.captureDeviceId();

        // Capture Google Ad Id
        AppUtil.captureGoogleAdId();
    }

    public android.support.v4.app.FragmentManager getFragmentsManager() {
        return getSupportFragmentManager();
    }

    public DineoutNetworkManager getNetworkManager() {
        if (networkManager == null)
            networkManager = DineoutNetworkManager.newInstance(getApplicationContext());

        return networkManager;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case LocationUtil.REQUEST_CHECK_SETTINGS:
                handleLocationRequestSettingsResult(resultCode == Activity.RESULT_OK);
                break;
            case LocationHandler.REQUEST_CHECK_SETTINGS_UPLOAD_BILL:
                handleGpsOnUploadBill(resultCode == Activity.RESULT_OK);
                break;


            case AppConstant.PHONEPE_DEBIT_REQUEST:
                handlePhonePeResponseFromSDK(requestCode, resultCode, data);
                break;

            default:
                break;
        }

        // Set Result back to Login Manager
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void handlePhonePeResponseFromSDK(int requestCode, int resultCode, Intent data) {

    }

    public void handleGpsOnUploadBill(boolean isSuccess) {

    }

    public abstract void handleLocationRequestSettingsResult(boolean success);


    public void registerListenerForAccountPermission(AccountPermissionListener listener) {
        this.mAccPermissionListener = listener;
    }

    public void unregisterListenerForAccountPermission() {
        this.mAccPermissionListener = null;
    }

    public void accountPermissionGranted(boolean granted) {
        if (mAccPermissionListener != null) {
            mAccPermissionListener.onAccountPermissionGrant(granted);
        }
    }

    public void registerListenerForLocationPermission(OnLocationPermissionGrantedListener listener) {
        this.mLocationPermissionListener = listener;
    }

    public void unregisterListenerForLocationPermission() {
        this.mLocationPermissionListener = null;
    }

    public void locationPermissionGranted(boolean granted) {
        if (mLocationPermissionListener != null) {
            mLocationPermissionListener.onLocationPermissionGranted(granted);
        }
    }

    public void registerListenerForCallPhonePermission(CallPhonePermissionGrantedListener listener) {
        this.callPhonePermissionGrantedListener = listener;
    }

    public void unregisterListenerForCallPhonePermission() {
        this.callPhonePermissionGrantedListener = null;
    }

    public void callPhonePermissionGranted(boolean granted) {
        if (callPhonePermissionGrantedListener != null) {
            callPhonePermissionGrantedListener.onCallPhonePermissionGrant(granted);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case AppConstant.REQUEST_PERMISSION_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    accountPermissionGranted(true);

                } else {

                    accountPermissionGranted(false);
                }

                return;
            }

            case AppConstant.REQUEST_PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    locationPermissionGranted(true);

                } else {


                    locationPermissionGranted(false);
                }

                return;
            }

            case AppConstant.REQUEST_PERMISSION_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    callPhonePermissionGranted(true);

                } else {


                    callPhonePermissionGranted(false);
                }

                return;
            }

        }
    }

    //track screen for countly and GA
    public void trackScreenName(String screenName) {
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackScreenCountly(screenName);
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackScreen(screenName);
    }


    // tracking events for countly and GA
    public void trackEventForCountlyAndGA(String category, String action, String label, Map<String, String> hMap) {
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackEventCountly(action, hMap);
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext())
                .trackEventGA(category, action, label);
    }

    //track event for qgraph,apsalar,branch
    public void trackEventForQgraphApsalar(String action, HashMap<String, Object> hMap,
                                           boolean toBePushedInQgraph, boolean toBePushedInApsalar, boolean toBePushedInBranch) {
        if (hMap != null) {
            AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackEventQGraphApsalar(action, hMap,
                    toBePushedInQgraph, toBePushedInApsalar);
        }
        if (toBePushedInBranch) {
            Branch.getInstance(getApplicationContext()).userCompletedAction(action);
        }
    }

}

