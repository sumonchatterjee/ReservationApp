package com.dineout.book.view.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.net.ConnectivityManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.VolleyError;
import com.dineout.baseview.AppFacebookListener;
import com.dineout.baseview.PermissionUtils;
import com.dineout.book.R;
import com.dineout.book.controller.IntentManager;
import com.dineout.book.model.listeners.LoginDialogListener;
import com.dineout.book.model.listeners.OnLocationPermissionGrantedListener;
import com.dineout.book.util.UiUtil;
import com.dineout.book.view.fragment.HomePageMasterFragment;
import com.dineout.book.view.fragment.IntroScreensPagerFragment;
import com.dineout.book.view.fragment.LocationSelectionFragment;
import com.dineout.book.view.fragment.MasterDOFragment;
import com.dineout.book.view.fragment.SplashFragment;
import com.dineout.book.view.fragment.payments.util.FragmentUtils;
import com.dineout.book.view.widgets.dialogs.DOLoginDialog;
import com.dineout.book.view.widgets.dialogs.ReviewDialogView;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class DineoutMainActivity extends MasterDOLauncherActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, Response.Listener<JSONObject>, ErrorListener,
        OnLocationPermissionGrantedListener {

    public static final int REQUEST_CHECK_SETTINGS = 2000;
    private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onNetworkChanged(intent);
        }
    };
    private int REQUEST_CODE_BOOKING_REVIEW = 101;
    private int REQUEST_CODE_GET_LOCATION_DETAILS = 102;
    //    private int REQUEST_CODE_BOOKING_DETAIL = 103;
    private int REQUEST_CODE_GET_APP_CONFIG = 105;
    private GoogleApiClient googleApiClient, googleApiClientAppIndex;
    private Uri APP_URI;
    private DOLoginDialog dialog;
    private ReviewDialogView reviewDialogView;

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_fragment_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DOPreferences.setLauncherFreshLaunch(getApplicationContext(), true);

        // Register for Location Permission Callback
        registerForLocationPermission();

        // Take API Hit: Get App Config
        initiateGetAppConfigAPI();

        MasterDOFragment.showFragment(getFragManager(), new SplashFragment());

        if (!DOPreferences.checkFirstLaunch(getApplicationContext())) {
            HomePageMasterFragment fragment = new HomePageMasterFragment();
            MasterDOFragment.addToBackStack(getFragManager(), fragment, false);
        }
        else {
            IntroScreensPagerFragment introScreensPagerFragment=new IntroScreensPagerFragment();
            MasterDOFragment.addToBackStack(getFragManager(),introScreensPagerFragment,false);
        }

        //getBranchIODetails();

        initializeGoogleAPIClient();

        // Instantiate Review Dialog
        reviewDialogView = (ReviewDialogView) findViewById(R.id.reviewDialog);

        //Instantiate Google API Client
        googleApiClientAppIndex = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // Start Google App Link
        setGoogleAppLink();

        //for new user
        if (!TextUtils.isEmpty(DOPreferences.getDinerEmail(getApplicationContext())) &&
                DOPreferences.isDinerNewUser(getApplicationContext())) {
//            showRedeemDialog(null);
            DOPreferences.setDinerNewUser(getApplicationContext(), "0");
        }

        if (!DOPreferences.checkFirstLaunch(this)) {
            //Check Review Dialog
            checkReviewDialog();
        }
    }

    private void registerForLocationPermission() {
        registerListenerForLocationPermission(this);
    }

    private void unregisterForLocationPermission() {
        unregisterListenerForLocationPermission();
    }

    @Override
    public void onLocationPermissionGrant(boolean granted) {
        if (granted) {
            // Fetch User's current location
            if (isSplashFragment()) {
                getLastKnownLocation();

            } else {
                getLastKnownLocationForAutoDetection();
            }
        } else {
            // Handle Location Settings Failed Response
//            DOPreferences.setGPS(getApplicationContext(), false);
            DOPreferences.setAutoMode(getApplicationContext(), false);

           // if (isSplashFragment()) {
                Fragment fragment = getFragManager().findFragmentByTag("dialog");

                if (fragment != null && fragment instanceof SplashFragment) {
                    ((SplashFragment) fragment).handleNavigation(false);


            } else {
                //Fragment fragment = getFragManager().findFragmentByTag("dialog");

                if (fragment != null && fragment instanceof LocationSelectionFragment) {
                    ((LocationSelectionFragment) fragment).onPostLocationFetched(false);
                }
            }
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null) {
            Fragment fragment = getFragManager().findFragmentById(R.id.fragment_base_container);

            if (fragment != null && fragment instanceof HomePageMasterFragment) {
                ((HomePageMasterFragment) fragment).handleDeepLinks(intent);
            }
        }
    }

    public void connectGoogleAPIClient() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        } else {
            if (isSplashFragment()) {
                getLastKnownLocation();
            } else {

                getLastKnownLocationForAutoDetection();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (DOPreferences.isLauncherFreshLaunch(getApplicationContext())) {

            // Connect Google API Client
            connectGoogleAPIClient();
        }
    }


    private void checkReviewDialog() {
        if (TextUtils.isEmpty(DOPreferences.getDinerEmail(getApplicationContext()))) {
            return;
        }

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_BOOKING_REVIEW, AppConstant.GET_BOOKING_FOR_REVIEW,
                ApiParams.getBookingForReviewParams(DOPreferences.getDinerId(getApplicationContext())),
                this, null, false);
    }

    private void showLoginDialog() {

        if (dialog != null && dialog.isVisible()) {
            return;
        }

        dialog = DOLoginDialog.newInstance(this, getResources().getString(R.string.auth_failed_login_msg),
                R.drawable.img_cta_book_table, null, false, new LoginDialogListener() {
                    @Override
                    public void onSSOSuccess() {
                        checkReviewDialog();
                        dialog.dismiss();
                    }

                    @Override
                    public void onSSOFailure(JSONObject response) {

                    }

                    @Override
                    public void onFBUser() {

                    }

                    @Override
                    public void onGoogleUser() {

                    }
                }, new AppFacebookListener() {
                    @Override
                    public void handleFacebookLoginSuccess() {
                        checkReviewDialog();
                        dialog.dismiss();
                    }
                }
        );

        dialog.show(getSupportFragmentManager(), "session_expired");
    }

    private void setGoogleAppLink() {
        //Google API Client
        googleApiClientAppIndex.connect();

        APP_URI = Uri.parse("android-app://com.dineout.book/dineout/home");

        Action viewAction = Action.newAction(Action.TYPE_VIEW, getString(R.string.app_name), AppConstant.WEB_URL, APP_URI);

        AppIndex.AppIndexApi.start(googleApiClientAppIndex, viewAction);
    }

    private void stopGoogleAppLink() {
        // Call end() and disconnect the client
        try {
            Action viewAction = Action.newAction(Action.TYPE_VIEW, getString(R.string.app_name), AppConstant.WEB_URL, APP_URI);

            AppIndex.AppIndexApi.end(googleApiClientAppIndex, viewAction);

            googleApiClientAppIndex.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSplashFragment() {
        MasterDOFragment fragment = (MasterDOFragment)getFragManager().findFragmentByTag("dialog");

        if (fragment != null && fragment instanceof SplashFragment) {
            return true;
        }

        return false;
    }

    void onNetworkChanged(Intent intent) {
        ConnectivityManager networkManager = (ConnectivityManager) getSystemService(
                CONNECTIVITY_SERVICE);
        NetworkInfo networkInfoFromBroadcast = ConnectivityManagerCompat
                .getNetworkInfoFromBroadcast(networkManager, intent);
        onNetworkChanged(networkInfoFromBroadcast.isConnected());
    }

    public void onNetworkChanged(boolean connected) {

        Fragment currentFragment = FragmentUtils.getTopVisibleFragment(
                getSupportFragmentManager(), R.id.fragment_base_container);

        if (currentFragment != null
                && currentFragment instanceof MasterDOFragment) {
            ((MasterDOFragment) currentFragment)
                    .onNetworkConnectivityChanged(connected);
        } else {
            Fragment fragment = FragmentUtils
                    .getTopFragment(getSupportFragmentManager());
            Fragment topFragment = FragmentUtils
                    .getTopFragmentFromList(getSupportFragmentManager());
            if (topFragment != null && topFragment != fragment
                    && topFragment instanceof MasterDOFragment) {
                ((MasterDOFragment) topFragment)
                        .onNetworkConnectivityChanged(connected);
            }
        }
    }

    private void getBranchIODetails() {
        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    // params are the deep linked params associated with the link that the user clicked before showing up
                    Log.i("BranchConfigTest", "deep link data: " + referringParams.toString());
                    String url = referringParams.optString("$deeplink_path");
                    if (!TextUtils.isEmpty(url)) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("dineout://" + url));
                        onNewIntent(intent);
                    }
                }
            }
        }, getIntent().getData(), this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Fragment fragment = getFragManager().findFragmentByTag("dialog");

        if (fragment != null && fragment instanceof SplashFragment) {
            getLastKnownLocation();
            // Show Selected Location

        } else {
            getLastKnownLocationForAutoDetection();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectGoogleAPIClient();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void getLastKnownLocation() {
        if (PermissionUtils.checkLocationPermission(getApplicationContext())) {
            if (googleApiClient != null) {
                // Get Last Known Location
                Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if (lastKnownLocation == null) {
                    // Fetch User's current location
                    if (TextUtils.isEmpty(DOPreferences.getCurrentLatitude(getApplicationContext()))) {
                        fetchUserCurrentLocation();

                    } else {
//                        DOPreferences.setGPS(getApplicationContext(), true);
                        // DOPreferences.setAutoMode(getApplicationContext(), true);

                        Fragment fragment = getFragManager().findFragmentByTag("dialog");

                        if (fragment != null && fragment instanceof SplashFragment) {
                            ((SplashFragment) fragment).handleNavigation(true);
                        }
                    }
                } else {
                    // Set Location
//                    DOPreferences.setGPS(getApplicationContext(), true);

                    DOPreferences.saveLatAndLong(
                            getApplicationContext(),
                            String.valueOf(lastKnownLocation.getLatitude()),
                            String.valueOf(lastKnownLocation.getLongitude()));


                    DOPreferences.setAutoMode(getApplicationContext(), true);

                    getLocationDetailsFromApi();

                    Fragment fragment = getFragManager().findFragmentByTag("dialog");

                    if (fragment != null && fragment instanceof SplashFragment) {
                        ((SplashFragment) fragment).handleNavigation(true);
                    }
                }
            }
        } else {
            PermissionUtils.grantLocationPermission(this);
        }
    }

    public void getLastKnownLocationForAutoDetection() {
        if (PermissionUtils.checkLocationPermission(getApplicationContext())) {
            if (googleApiClient != null) {
                // Get Last Known Location
                Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if (lastKnownLocation == null) {
                    // Fetch User's current location
                    fetchUserCurrentLocation();

                } else {
                    DOPreferences.setAutoMode(getApplicationContext(), true);

                    // Set Location
                    DOPreferences.saveLatAndLong(
                            getApplicationContext(),
                            String.valueOf(lastKnownLocation.getLatitude()),
                            String.valueOf(lastKnownLocation.getLongitude()));

                    getLocationDetailsFromApi();

                    Fragment fragment = getFragManager().findFragmentByTag("dialog");

                    if (fragment != null && fragment instanceof LocationSelectionFragment) {
                        ((LocationSelectionFragment) fragment).onPostLocationFetched(true);
                    }
                }
            }
        } else {
            PermissionUtils.grantLocationPermission(this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        DOPreferences.setLauncherFreshLaunch(getApplicationContext(), false);

        //Stop Google App Linking

        stopGoogleAppLink();


        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // UnRegister for Location Permission Callback
        unregisterForLocationPermission();

        googleApiClient = null;
        googleApiClientAppIndex = null;
    }

    @Override
    public void handleLocationRequestSettingsResult(boolean isSuccess) {
        if (isSuccess) {
            // Fetch User's current location
            if (isSplashFragment()) {
                getLastKnownLocation();

            } else {
                getLastKnownLocationForAutoDetection();
            }
        } else {
            // Handle Location Settings Failed Response
//            DOPreferences.setGPS(getApplicationContext(), false);
            DOPreferences.setAutoMode(getApplicationContext(), false);


            Fragment fragment = getFragManager().findFragmentByTag("dialog");
            if(fragment!=null && fragment instanceof SplashFragment){

                ((SplashFragment) fragment).handleNavigation(false);



            } else {
               // Fragment locationFragment = getFragManager().findFragmentById(R.id.fragment_base_container);

                if (fragment != null && fragment instanceof LocationSelectionFragment) {
                    ((LocationSelectionFragment) fragment).onPostLocationFetched(false);
                }
            }
        }
    }

    // Fetch User's current location
    public void fetchUserCurrentLocation() {
        // Create Location Request
        LocationRequest locRequest = null;
        locRequest = createLocationRequest();

        // Create Location Settings Instance
        LocationSettingsRequest.Builder locationSettingsRequestBuilder =
                new LocationSettingsRequest.Builder();
        locationSettingsRequestBuilder.addLocationRequest(locRequest);

        // Hide Never Button
        locationSettingsRequestBuilder.setAlwaysShow(true);

        // Check Settings for Location Request
        PendingResult<LocationSettingsResult> pendingResult =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsRequestBuilder.build());

        // Set Location Settings Callback
        pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                // Get Status
                Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        requestLocationUpdates(createLocationRequest());
                        // Request Location Updates
                        //getLastKnownLocation();

                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {


                            status.startResolutionForResult(
                                    DineoutMainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }

                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        // Handle Location Settings Error
//                        DOPreferences.setGPS(getApplicationContext(), false);

                        DOPreferences.setAutoMode(getApplicationContext(), false);

                        //if (isSplashFragment()) {
                            Fragment fragment = getFragManager().findFragmentByTag("dialog");

                            if (fragment != null && fragment instanceof SplashFragment) {
                                ((SplashFragment) fragment).handleNavigation(false);

                        } else {
                            //Fragment fragment = getFragManager().findFragmentById(R.id.fragment_base_container);

                            if (fragment != null && fragment instanceof LocationSelectionFragment) {
                                ((LocationSelectionFragment) fragment).onPostLocationFetched(false);
                            }
                        }
                        //TODO Vibhas
                        // locationInteracter.onLocationSettingsError();

                        break;
                }
            }
        });
    }

    // Request Location Updates
    private void requestLocationUpdates(LocationRequest locationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            // Set Location
//            DOPreferences.setGPS(getApplicationContext(), true);

            DOPreferences.saveLatAndLong(
                    getApplicationContext(),
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()));

            DOPreferences.setAutoMode(getApplicationContext(), true);

            getLocationDetailsFromApi();

            //if (isSplashFragment()) {
                Fragment fragment = getFragManager().findFragmentByTag("dialog");

                if (fragment != null && fragment instanceof SplashFragment) {
                    ((SplashFragment) fragment).handleNavigation(true);
                //}
            } else {
                //Fragment fragment = getFragManager().findFragmentById(R.id.fragment_base_container);

                if (fragment != null && fragment instanceof LocationSelectionFragment) {
                    ((LocationSelectionFragment) fragment).onPostLocationFetched(true);
                }
            }
        } else {
//            DOPreferences.setGPS(getApplicationContext(), false);
            DOPreferences.setAutoMode(getApplicationContext(), false);

           // if (isSplashFragment()) {

                Fragment fragment = getFragManager().findFragmentByTag("dialog");

                if (fragment != null && fragment instanceof SplashFragment) {
                    ((SplashFragment) fragment).handleNavigation(false);
               // }
            } else {
               // Fragment fragment = getFragManager().findFragmentById(R.id.fragment_base_container);

                if (fragment != null && fragment instanceof LocationSelectionFragment) {
                    ((LocationSelectionFragment) fragment)
                            .onPostLocationFetched(false);
                }
            }
        }

        // Stop Location Updates
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    //Create Location Request
    private LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 secs
        locationRequest.setFastestInterval(5000); // 5 secs
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setNumUpdates(1);

        return locationRequest;
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
                // Show Alert Dialog
                AlertDialog.Builder builder = UiUtil.getAlertDialog(this, R.string.text_not_serving_title, R.string.text_not_serving_message,
                        null, null, false, false, false);

                builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Show default lat/lng
                    }
                });

                // Show Alert Dialog
                builder.show();
            }
        }
    }

    public void processCityData(JSONObject locationJsonObject) {
        // Check if has result
        if (locationJsonObject == null)
            return;

        DOPreferences.saveLocationDetails(getApplicationContext(), locationJsonObject, true);

        MasterDOFragment fragment = (MasterDOFragment) getSupportFragmentManager().findFragmentById
                (R.id.fragment_base_container);

        if (fragment instanceof HomePageMasterFragment) {
            ((HomePageMasterFragment) fragment).refreshPagerAdapters();
        }
    }

    public void initiateGetAppConfigAPI() {
        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_APP_CONFIG, AppConstant.URL_GET_APP_CONFIG,
                null, this, null, true);
    }

    public void getLocationDetailsFromApi() {
        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_LOCATION_DETAILS, AppConstant.URL_LOCATION_SEARCH,
                ApiParams.getLocationParams(
                        null, DOPreferences.getCurrentLatitude(getApplicationContext()),
                        DOPreferences.getCurrentLongitude(getApplicationContext())),
                this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (request.getIdentifier() == REQUEST_CODE_GET_LOCATION_DETAILS) {
            parseLocationDetailResponse(responseObject);

        } else if (request.getIdentifier() == REQUEST_CODE_BOOKING_REVIEW) {

            // Get Res Auth JSONObject
            if (responseObject != null) {
                JSONObject resAuthJsonObject = responseObject.optJSONObject("res_auth");

                if (responseObject.optBoolean("status")) {

                    // Get Output Params Object
                    JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                    if (outputParamsJsonObject != null) {

                        // Get Data Object
                        JSONArray dataJsonArray = outputParamsJsonObject.optJSONArray("data");

                        if (dataJsonArray != null && dataJsonArray.length() > 0) {
                            // Get First Restaurant for Review
                            JSONObject firstRestaurantJsonObject = dataJsonArray.optJSONObject(0);

                            if (firstRestaurantJsonObject != null) {
                                // Check if the Restaurant is Reviewed already for the respective booking
                                if (DOPreferences.getReviewedBookingId(getApplicationContext()).equals(firstRestaurantJsonObject.optString("b_id"))) {
                                    return;
                                }

                                // Set Booking Id as reviewed
                                DOPreferences.setReviewedBookingId(getApplicationContext(),
                                        firstRestaurantJsonObject.optString("b_id"));

                                // Initiate Review Booking Screen
                                reviewDialogView.setData(firstRestaurantJsonObject.optString("b_id"),
                                        firstRestaurantJsonObject.optString("screen_name"), getApplicationContext());
                                reviewDialogView.setVisibility(View.VISIBLE);
                                reviewDialogView.setActivityInstance(this);
                            }
                        }
                    } else if (resAuthJsonObject != null && !resAuthJsonObject.optBoolean("status", false)) {
                        showLoginDialog();
                    }
                }
            }
        } else if (request.getIdentifier() == REQUEST_CODE_GET_APP_CONFIG) {
            if (responseObject != null && responseObject.optBoolean("status")) {
                if (responseObject.optJSONObject("output_params") != null) {
                    JSONObject dataJsonObject = responseObject.
                            optJSONObject("output_params").optJSONObject("data");

                    if (dataJsonObject != null) {
                        // Save Config
                        DOPreferences.saveAppConfig(getApplicationContext(), dataJsonObject);
                    }
                }
            }

            if (response.cacheEntry != null) {

                final Response<JSONObject> response1 = response;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showUpgradeDialog(response1.cacheEntry.responseHeaders);
                    }
                }, 2200);
            }
        }
    }

    private void showUpgradeDialog(Map<String, String> responseHeaders) {
        if (responseHeaders != null) {

            String isForceUpdate = responseHeaders.get("fu");

            if (!TextUtils.isEmpty(isForceUpdate)) {
                switch (isForceUpdate) {

                    case "0": {
                        // do nothing
                        break;
                    }
                    case "1": {
                        //Force update with Skip button
                        Intent i = IntentManager.navigateToForceUpdate(this, true);
                        startActivity(i);
                        break;
                    }
                    case "2": {
                        //Hard force update
                        Intent i = IntentManager.navigateToForceUpdate(this, false);
                        startActivity(i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }
}