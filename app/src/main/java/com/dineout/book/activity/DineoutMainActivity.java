package com.dineout.book.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.service.GCMRegistrationIntentService;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.interfaces.AccountPermissionListener;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.util.FragmentUtils;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.home.HomePageMasterFragment;
import com.dineout.book.fragment.intro.IntroScreensPagerFragment;
import com.dineout.book.fragment.home.LocationSelectionFragment;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.maps.MapFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.payments.PaymentConstant;
import com.dineout.book.fragment.payments.fragment.SelectPaymentFragment;
import com.dineout.book.dialogs.ReviewDialogView;
//import com.dineout.livehandler.AccessibilityPreference;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
//import com.google.android.gms.appindexing.Action;
//import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.phonepe.android.sdk.utils.BundleConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class DineoutMainActivity extends MasterDOLauncherActivity implements
                                                                  AccountPermissionListener, GoogleApiClient.ConnectionCallbacks,
                                                                  GoogleApiClient.OnConnectionFailedListener {


    private boolean shouldFinish = false;
    private Handler mHandler;
    private Runnable cancelFinish = new Runnable() {

        @Override
        public void run() {
            shouldFinish = false;
        }
    };
    private int REQUEST_CODE_BOOKING_REVIEW = 101;
    private int REQUEST_CODE_GET_APP_CONFIG = 105;
    private GoogleApiClient googleApiClient; //googleApiClientAppIndex;
    private Uri APP_URI;
//    private ReviewDialogView reviewDialogView;
    private Intent branchIntent = null;

    private OnUploadBillLocationGPSOn onUploadBillLocationGPSOn;

    @Override
    protected int getActivityLayout() {
        return R.layout.activity_fragment_main;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();


        registerListenerForAccountPermission(this);



        boolean isLocationFetched = getIntent().getBooleanExtra("locationFetched", false);

        if (DOPreferences.checkFirstLaunch(getApplicationContext())) {


            IntroScreensPagerFragment introScreensPagerFragment = new IntroScreensPagerFragment();
            MasterDOFragment
                    .addToBackStack(getFragmentsManager(), introScreensPagerFragment, false);
            introScreensPagerFragment.setLocationFetched(isLocationFetched);

        } else {

            HomePageMasterFragment homePageMasterFragment = new HomePageMasterFragment();
            MasterDOFragment
                    .addToBackStack(getFragmentsManager(), homePageMasterFragment, false);


            if (!isLocationFetched && TextUtils.isEmpty(DOPreferences.getELatitude(getApplicationContext()))) {


                LocationSelectionFragment locationSelectionFragment = new LocationSelectionFragment();
                MasterDOFragment
                        .addToBackStack(getFragmentsManager(), locationSelectionFragment, false);
            }
        }




        initializeGoogleAPIClient();



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

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            // Initialize GCM Service
            initGcmServices();
        }
        // checkIfLocationPermissionGranted();
        if (Build.VERSION.SDK_INT >= 23) {
            checkIfAccountPermissionGranted();
        } else {
            captureEmailsOfUser();
        }



        //show Force upgrade if applicable

        final String isForceUpdate=getIntent().getStringExtra("forceUpgrade");
        new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       showUpgradeDialog(isForceUpdate);
                   }
               }, 1000);


    }


    public void checkIfAccountPermissionGranted() {
        if (!PermissionUtils.hasAccountPermission(this)) {
            PermissionUtils.grantAccountPermission(this);
            return;
        }

    }


    public void initializeGoogleAPIClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(this)
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

    public GoogleApiClient getGoogleApiClientForLocation() {
        return googleApiClient;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);


    }

    @Override
    protected void onStart() {
        super.onStart();

        connectGoogleAPIClient();
        initiallizeBranchIO();
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).startAnalyticsTracker(this);


    }

    @Override
    protected void onResume() {
        super.onResume();


        if (getIntent() != null) {
            MasterDOFragment fragment = (MasterDOFragment) getFragmentsManager().findFragmentById(R.id.fragment_base_container);

            if (fragment != null) {
                fragment.handleDeepLinks(getIntent());
            }

        }

        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackAdTechEventAppMaximize();
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).completeAdTechSession();
    }


    @Override
    protected void onPause() {
        super.onPause();

        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackAdTechEventAppMinimize();
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).completeAdTechSession();
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

        Bundle bundle = new Bundle();
        UserAuthenticationController.getInstance(this).startLoginFlow(bundle, new UserAuthenticationController.LoginFlowCompleteCallbacks() {
            @Override
            public void loginFlowCompleteSuccess(JSONObject object) {
                checkReviewDialog();
            }

            @Override
            public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
                if (loginFlowCompleteFailureObject != null) {

                    String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
                    if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                        return;
                    }

                    String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
                    // Show Error Message
                    if (!com.dineout.book.util.AppUtil.isStringEmpty(cause)) {
                        UiUtil.showToastMessage(DineoutMainActivity.this, cause);
                    }
                }
            }
        });


    }




    public boolean isLocationFragment() {
        Fragment fragment = getFragmentsManager().findFragmentById(R.id.fragment_base_container);
        if (fragment != null && fragment instanceof LocationSelectionFragment) {
            return true;
        }

        return false;
    }



    public void onNetworkChanged(boolean connected) {

        Fragment currentFragment = FragmentUtils.getTopVisibleFragment(
                getSupportFragmentManager(), R.id.fragment_base_container);

        if (currentFragment != null
                && currentFragment instanceof MasterDOFragment) {
            ((MasterDOFragment) currentFragment)
                    .onNetworkConnectionChanged(connected, true);
        } else {
            Fragment fragment = FragmentUtils
                    .getTopFragment(getSupportFragmentManager());
            Fragment topFragment = FragmentUtils
                    .getTopFragmentFromList(getSupportFragmentManager());
            if (topFragment != null && topFragment != fragment
                    && topFragment instanceof MasterDOFragment) {
                ((MasterDOFragment) topFragment)
                        .onNetworkConnectionChanged(connected, true);
            }
        }
    }

    private void initiallizeBranchIO() {
        Branch branch = Branch.getInstance();
        if (getIntent() != null) {
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
                            setBranchDeeplinks(intent);
                        }
                    }
                }
            }, getIntent().getData(), this);
        }
    }

    public Intent getBranchDeeplinks() {
        return branchIntent;
    }

    public void setBranchDeeplinks(Intent branchIntent) {
        this.branchIntent = branchIntent;
    }


    @Override
    protected void onStop() {
        super.onStop();

       // stopGoogleAppLink();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }

        if (getOnUploadBillLocationGPSOn() != null) {
            getOnUploadBillLocationGPSOn().onActivityStopCalled();
        }
        AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).stopAnalyticsTracker(this);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // UnRegister for Location Permission Callback
        unregisterListenerForAccountPermission();


        DOPreferences.setHasDoPlusDeeplink(getApplicationContext(), false);

        googleApiClient = null;
        //googleApiClientAppIndex = null;
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        if (!onPopBackStack()) {

            if (shouldFinish) {
                AnalyticsHelper.getAnalyticsHelper(getApplicationContext()).trackEventGA("Dineout", "app_exit", null);
                mHandler.removeCallbacks(cancelFinish);

                finish();
            } else {

                shouldFinish = true;
                mHandler.postDelayed(cancelFinish, 2000);
                Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    protected boolean onPopBackStack() {

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.executePendingTransactions()) {
            return true;
        }

        MasterDOFragment fragment = (MasterDOFragment) fragmentManager
                .findFragmentById(R.id.fragment_base_container);

        boolean isEventHandledByFragment = fragment != null
                && fragment.onPopBackStack();

        if (fragmentManager.getBackStackEntryCount() > 1
                && !isEventHandledByFragment) {

            fragmentManager.popBackStack();
            fragmentManager.beginTransaction().remove(fragment).commit();

            return true;
        }

        return isEventHandledByFragment;
    }





    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (request.getIdentifier() == REQUEST_CODE_BOOKING_REVIEW) {

            // Get Res Auth JSONObject
            if (responseObject != null) {
                JSONObject resAuthJsonObject = responseObject.optJSONObject("res_auth");

                if (responseObject.optBoolean("status")) {

                    // Get Output Params Object
                    JSONObject outputParamsJsonObject =
                            responseObject.optJSONObject("output_params");

                    if (outputParamsJsonObject != null) {

                        // Get Data Object
                        JSONArray dataJsonArray = outputParamsJsonObject.optJSONArray("data");

                        if (dataJsonArray != null && dataJsonArray.length() > 0) {
                            // Get First Restaurant for Review
                            JSONObject firstRestaurantJsonObject = dataJsonArray.optJSONObject(0);

                            if (firstRestaurantJsonObject != null) {
                                // Check if the Restaurant is Reviewed already for the respective booking
                                if (DOPreferences.getReviewedBookingId(getApplicationContext())
                                        .equals(firstRestaurantJsonObject.optString("b_id"))) {
                                    return;
                                }

                                // Set Booking Id as reviewed
                                DOPreferences.setReviewedBookingId(getApplicationContext(),
                                        firstRestaurantJsonObject.optString("b_id"));

                                // Initiate Review Booking Screen
//                                reviewDialogView.setData(firstRestaurantJsonObject.optString("b_id"),
//                                        firstRestaurantJsonObject.optString("screen_name"), getApplicationContext());
//                                reviewDialogView.setVisibility(View.VISIBLE);
//                                reviewDialogView.setActivityInstance(this);
                            }
                        }
                    } else if (resAuthJsonObject != null &&
                            !resAuthJsonObject.optBoolean("status", false)) {
                        showLoginDialog();
                    }
                }
            }


        }
//        } else if (request.getIdentifier() == REQUEST_CODE_GET_APP_CONFIG) {
//            if (responseObject != null && responseObject.optBoolean("status")) {
//                if (responseObject.optJSONObject("output_params") != null) {
//                    JSONObject dataJsonObject = responseObject.
//                            optJSONObject("output_params").optJSONObject("data");
//
//                    if (dataJsonObject != null) {
//                        // Save Config
//                        DOPreferences.saveAppConfig(getApplicationContext(), dataJsonObject);
//
//                        // save the accessibility flag
//                        JSONObject systemConfigObject;
//                        if ((systemConfigObject = dataJsonObject.optJSONObject("system_config")) != null) {
////                            AccessibilityPreference.storeAccessibilityFeatureEnableFalg(getApplicationContext(),
////                                   false);
//                            JSONObject girfJSonObj=systemConfigObject.optJSONObject("girf");
//                            if(girfJSonObj!=null){
//                                String isGirfEnabled=girfJSonObj.optString("is_enabled");
//                                DOPreferences.setGIRFEnabled(getApplicationContext(),isGirfEnabled.equalsIgnoreCase("1") ? true : false );
//                                String girf_url=girfJSonObj.optString("url");
//                                DOPreferences.setGIRFURL(getApplicationContext(),girf_url);
//
//                            }
//
//                        }
//                    }
//                }
//            }
//
//
//
//
//        }
    }

    @Override
    public void handlePhonePeResponseFromSDK(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstant.PHONEPE_DEBIT_REQUEST) {
            Bundle bundle = data.getExtras();
            String txnId = bundle.getString(BundleConstants.KEY_TRANSACTION_ID);
            if (resultCode == Activity.RESULT_OK) {
                MasterDOFragment fragment = (MasterDOFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_base_container);
                if(fragment!=null && fragment instanceof SelectPaymentFragment){
                    Bundle b = new Bundle();
                    b.putString(PaymentConstant.DISPLAY_ID, txnId);
                    ((SelectPaymentFragment) fragment).showStatusScreen(b);
                }

           /* Inform your server about the completion of transaction */
            } else if (resultCode == Activity.RESULT_CANCELED) {
          // don't to anything - current fragment will be SelectPaymentFragment
            }
        }
    }

    @Override
    public void handleGpsOnUploadBill(boolean isSuccess) {
        if (getOnUploadBillLocationGPSOn() != null) {
            getOnUploadBillLocationGPSOn().onGpsTurnedOn(isSuccess);
        }
    }

    @Override
    public void handleLocationRequestSettingsResult(boolean success) {

        if (isLocationFragment()) {
            MasterDOFragment fragment = (MasterDOFragment) getFragmentsManager()
                    .findFragmentById(R.id.fragment_base_container);

            if (success) {
                ((LocationSelectionFragment) fragment).getLocationFromGPS();
            } else {
                ((LocationSelectionFragment) fragment).hideLoaderAndShowError();
            }
        } else {
            MasterDOFragment fragment = (MasterDOFragment) getFragmentsManager()
                    .findFragmentById(R.id.fragment_base_container);

            if (fragment != null) {
                if (fragment instanceof MapFragment) {
                    if (success) {
                        ((MapFragment) fragment).getLocationFromGPS();
                    } else {
                        ((MapFragment) fragment).onLocationFailed();
                    }
                }
            }
        }
    }

    protected void showUpgradeDialog(String responseFU) {

        if (!TextUtils.isEmpty(responseFU)) {

            //String isForceUpdate = responseHeaders.get("fu");

//            if (!TextUtils.isEmpty(responseFU)) {
                switch (responseFU) {

                    case "0": {
                        // do nothing

                        break;
                    }
                    case "1": {
                        //Force update with Skip button
                        Intent i = navigateToForceUpdate(DineoutMainActivity.this, true);
                        startActivity(i);
                        break;
                    }
                    case "2": {
                        //Hard force update
                        Intent i = navigateToForceUpdate(DineoutMainActivity.this, false);
                        startActivity(i);
                        break;
                    }
                }
            }

    }

    private Intent navigateToForceUpdate(Activity activityContext, boolean isSkip) {
        Intent intent = new Intent(activityContext, ForceUpdateActivity.class);

        if (!isSkip) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }

        intent.putExtra("fu_can_skip", isSkip);

        return intent;
    }


    // Initialize GCM Service
    public void initGcmServices() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(getApplicationContext(), GCMRegistrationIntentService.class);
        startService(intent);
    }



    @Override
    public void onAccountPermissionGrant(boolean granted) {
        if (granted) {
            //capture email id of the user
            captureEmailsOfUser();
        } else {
            // Could not capture user email id
        }
    }

    private void captureEmailsOfUser() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        //HashMap<String, String> emailKeyValuePairs=new HashMap<>();
        int emailCount = 0;
        StringBuilder emailAddressConcatenated = new StringBuilder();
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String primaryGoogleEmail = account.name;
                emailCount++;
                Log.e("primary email", primaryGoogleEmail);

                if (emailCount > 1) {
                    emailAddressConcatenated.append("|");
                }
                emailAddressConcatenated.append(primaryGoogleEmail);
            }
        }
        saveEmailsToServer(emailAddressConcatenated.toString());
    }

    private void saveEmailsToServer(String emailsConcatenated) {
        HashMap<String, String> emailMap = new HashMap<>();
        emailMap.put("emails", emailsConcatenated);
        getNetworkManager().stringRequestPost(101, AppConstant.URL_EMAIL_SUBMISSION, emailMap, new Response.Listener<String>() {

            @Override
            public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                Log.e("response", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(Request request, VolleyError error) {
                Log.e("error_response", error.toString());
            }
        }, false);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public OnUploadBillLocationGPSOn getOnUploadBillLocationGPSOn() {
        return onUploadBillLocationGPSOn;
    }

    public void setOnUploadBillLocationGPSOn(OnUploadBillLocationGPSOn onUploadBillLocationGPSOn) {
        this.onUploadBillLocationGPSOn = onUploadBillLocationGPSOn;
    }

    public interface OnUploadBillLocationGPSOn {
        void onGpsTurnedOn(boolean isSuccess);

        void onActivityStopCalled();
    }


}