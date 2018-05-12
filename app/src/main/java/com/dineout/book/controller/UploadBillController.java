package com.dineout.book.controller;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.interfaces.LocationHandlerCallbacks;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.activity.DineoutMainActivity;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.uploadbill.StepsToUploadFragment;
import com.dineout.book.fragment.uploadbill.BillUploadFragment;
import com.dineout.book.dialogs.LoginSessionExpireDialog;
import com.dineout.book.dialogs.ProgressDialog;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment.OTP_VERIFY_CODE;
import static com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment.LOGIN_SESSION_EXPIRE_CODE;
import static com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment.OTP_REQUIRE_CODE;
import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;
import static com.dineout.recycleradapters.util.RateNReviewUtil.appendObject;


public class UploadBillController implements Response.Listener<String>,
                                             Response.ErrorListener, LocationHandlerCallbacks, DineoutMainActivity.OnUploadBillLocationGPSOn,
                                             UserAuthenticationController.LoginFlowCompleteCallbacks,
                                             UserAuthenticationController.OtpFlowCompleteCallback,
                                             LoginSessionExpireDialog.LoginSessionExpiredButtonCallbacks {


    public static final String UPLOAD_BILL_PREVIOUS_SCREEN_NAME_KEY = "upload_bill_previous_screen_name_key";
    private String mPreviousScreenName = "NONE";
    private boolean isActivityStopped = false;
    private FragmentActivity mContext;
    private String mRestId,
            mBookingId = "", mType;
    private DineoutNetworkManager mManager;
    private LocationHandler mLocationHandler;
    private DialogListener mRetryListener = new DialogListener() {
        @Override
        public void onPositiveButtonClick(AlertDialog alertDialog) {

            alertDialog.dismiss();
            validateLogin();
        }

        @Override
        public void onNegativeButtonClick(AlertDialog alertDialog) {

            alertDialog.dismiss();
        }
    };

    public UploadBillController(@NonNull DineoutNetworkManager manager,
                                @NonNull FragmentActivity context, @NonNull String restId) {

        if (TextUtils.isEmpty(restId))
            Log.e("UploadBill", "Rest ID cannot be empty");

        mManager = manager;
        mContext = context;
        mType = "restaurant";
        mRestId = restId;


    }

    public UploadBillController(@NonNull DineoutNetworkManager manager,
                                @NonNull FragmentActivity context, @NonNull String restId, @NonNull String bid) {

        if (TextUtils.isEmpty(restId) || TextUtils.isEmpty(bid))
            Log.e("UploadBill", "Rest ID cannot be empty");

        mManager = manager;
        mType = "booking";
        mRestId = restId;
        mBookingId = bid;
        mContext = context;

    }

    public void validate() {
        //mLocationHandler = new LocationHandler(mContext, this);
        ((DineoutMainActivity) mContext).setOnUploadBillLocationGPSOn(this);
        isActivityStopped = false;
        validateLogin();
    }

    private void validatateGps() {

        if (checkFakeGps()) {

            showLoader(mContext.getSupportFragmentManager());

            if (mLocationHandler == null) {
                mLocationHandler = new LocationHandler(mContext, this);
            }

            mLocationHandler.fetchCurrentLocation();
        }
    }

    private void showLoader(FragmentManager manager) {

        dismissLoader(manager);
        ProgressDialog dialog = new ProgressDialog();
        dialog.show(manager, "Progress");
    }

    private void dismissLoader(FragmentManager manager) {

        if (manager != null && manager.findFragmentByTag("Progress") != null) {

            ((ProgressDialog) manager.findFragmentByTag("Progress")).dismissAllowingStateLoss();
        }
    }

    private void makeApiCall() {
        //showLoader(mContext.getSupportFragmentManager());
        String lat=DOPreferences.getUploadBillLatitude(mContext);
        String lng=DOPreferences.getUploadBillLongitude(mContext);

        HashMap<String, String> param = getRequestParameter();
        param.put(AppConstant.UPLOAD_BILL_PARAM_DEVICE_LATITUDE, lat);
        param.put(AppConstant.UPLOAD_BILL_PARAM_DEVICE_LONGITUDE, lng);

        mManager.stringRequestPostNode(101, AppConstant.URL_VALIDATE_BILL, param, this, this, false,lat,lng);
    }

    private void validateLogin() {
        if (!checkLogin()) {
            login();
            return;
        }
        validatateGps();
    }

    private void login() {
        Bundle bundle = new Bundle();
        // initiate login flow
        UserAuthenticationController.getInstance(mContext).startLoginFlow(bundle, this);
    }
//    private void login() {
//        if (mContext == null)
//            return;
//
//        if (mContext.getSupportFragmentManager().findFragmentByTag("ValidateLogin") != null) {
//            DialogFragment prev = (DialogFragment) mContext.getSupportFragmentManager().findFragmentByTag("ValidateLogin");
//            prev.dismissAllowingStateLoss();
//        }
//
//        ValidateAccountFragment dialog = ValidateAccountFragment.newInstance(new Bundle(), this);
//        dialog.show(mContext.getSupportFragmentManager(), "ValidateLogin");
//    }

    private boolean checkFakeGps() {

        boolean isMockLocation = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            isMockLocation = DOPreferences.isMockProviderLocation(mContext);
        } else {

            isMockLocation = Settings.Secure.getString(mContext.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION).equals("1");
        }
        if (isMockLocation) {


            UiUtil.showCustomDialog(mContext, getDialogBundle("Alert", "You have fake gps enabled. Please go to the " +
                    "developer settings and disable mock location", null, null), null);
            return false;
        } else
            return true;
    }

    private boolean checkLogin() {
        if (mContext != null) {
            return !TextUtils.isEmpty(DOPreferences.getDinerId(mContext.getApplicationContext()));
        }
        return false;
    }

    private HashMap<String, String> getRequestParameter() {

        HashMap<String, String> param = new HashMap<>();
        param.put(AppConstant.PARAM_BILL_UPLOAD_TYPE, mType);
        param.put(AppConstant.PARAM_BILL_UPLOAD_RESTID, mRestId);
        param.put(AppConstant.PARAM_BILL_UPLOAD_BOOKING_ID, mBookingId);
        return param;
    }



    @Override
    public void onErrorResponse(Request request, VolleyError error) {

        if (mContext == null)
            return;
        dismissLoader(mContext.getSupportFragmentManager());
        showRetryDialog();


    }

    private void showRetryDialog() {
        String message =
                "Some error in initializing upload bill .Do you want to retry?";
        String title = "Failed";
        UiUtil.showCustomDialog(mContext, getDialogBundle(title, message, "Yes", "No"), mRetryListener);
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        if (mContext == null) {
            return;
        }

        dismissLoader(mContext.getSupportFragmentManager());

        if (responseObject != null) {
            handleValidateResponse(responseObject);
        } else {
            showRetryDialog();
        }
    }


    private void handleValidateResponse(String response) {

        try {
            JSONObject resp = new JSONObject(response);
            JSONObject respData = resp.optJSONObject("data");


            if (resp.optBoolean("status") && respData != null) {

                DOPreferences.setUploadDisplayMessage(mContext, respData.optString("displayMsg"));
                JSONObject review = validateReviewData(respData.optJSONObject("reviewData"),
                        respData.optString("restaurantName"), respData.optString("imageUrl"));
                respData.put("reviewData", review);

                if (!isActivityStopped) {
                    navigateToFragment(respData);
                }
            } else {
                String errorCode = resp.optString("error_code");

                if (errorCode != null) {
                    switch (errorCode) {
                        case LOGIN_SESSION_EXPIRE_CODE:
                            //show session expired dialog
                            String errorMsg = resp.optString("error_msg");
                            if (mContext != null) {
                                UserAuthenticationController.getInstance(mContext).showSessionExpireDialog(errorMsg, this);
                            }
                            break;

                        case OTP_REQUIRE_CODE:
                            Bundle phoneBundle = new Bundle();

                            if (mContext.getApplicationContext() != null) {
                                phoneBundle.putString(AppConstant.BUNDLE_PHONE_NUMBER, DOPreferences.getDinerPhone(mContext.getApplicationContext()));
                            }
                            if (mContext != null) {
                                UserAuthenticationController.getInstance(mContext).startOTPFlow(phoneBundle, this);
                            }
                            break;

                        case OTP_VERIFY_CODE:
                            JSONObject errorResponse;
                            JSONObject otpInfo;
                            if ((errorResponse = resp.optJSONObject("error_response")) != null
                                    && (otpInfo = errorResponse.optJSONObject("otp_info")) != null) {
                                if (otpInfo.optBoolean("status")) {
                                    JSONObject outputParams;
                                    JSONObject data;
                                    if ((outputParams = otpInfo.optJSONObject("output_params")) != null
                                            && (data = outputParams.optJSONObject("data")) != null) {
                                        Bundle bundle = new Bundle();
                                        String userInput = data.optString("phone");
                                        if (TextUtils.isEmpty(userInput)) {
                                            userInput = data.optString("email");
                                        }
                                        bundle.putString(USER_INPUT_TEXT_KEY, userInput);
                                        bundle.putString("otp_id", data.optString("otp_id"));
                                        bundle.putString("type", data.optString("type"));
                                        bundle.putString("resend_otp_time", data.optString("resend_otp_time"));

                                        if (mContext != null) {
                                            UserAuthenticationController.getInstance(mContext).startOTPVerificationFlow(bundle, this);
                                        }
                                    }
                                } else {
                                    errorMsg = resp.optString("error_msg");
                                    if (!TextUtils.isEmpty(errorMsg) && mContext != null) {
                                        UiUtil.showToastMessage(mContext, errorMsg);
                                    }
                                }
                            }
                            break;

                        default:
                            String message = TextUtils.isEmpty(resp.optString("error")) ?
                                    "Some error in initializing upload bill" : resp.optString("error");

                            if (respData != null && respData.has("msg")) {
                                message = respData.optString("msg");
                            }
                            String title = "Failed";

                            UiUtil.showCustomDialog(mContext, getDialogBundle(title, message, null, null), null);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            // Exception
        }
    }

    private JSONObject validateReviewData(JSONObject data, String name, String image) {

        JSONObject temp = new JSONObject();
        try {
            if (data == null) {
                temp.put("rest_id", mRestId);
                if (!TextUtils.isEmpty(mBookingId) && !mBookingId.equalsIgnoreCase("-1"))
                    temp.put("b_id", mBookingId);
            }

            if (data != null) {
                if (!data.has("rest_id")) {
                    temp.put("rest_id", mRestId);
                }

                if (!data.has("b_id") && !TextUtils.isEmpty(mBookingId)) {
                    temp.put("b_id", mBookingId);
                }

                if (data.has("reviewId"))
                    temp.put("review_id", data.optString("reviewId"));
                temp.put("rating", data.optInt("rating"));
                temp.put("reviewText", data.optString("reviewText"));
            }

            temp.put("rest_name", name);
            temp.put("rest_url", image);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    private void navigateToFragment(JSONObject data) {
        if (DOPreferences.isShowStepUpload(mContext)) {
            MasterDOFragment fragment = StepsToUploadFragment.newInstance(data);
            Bundle b = new Bundle();
            b.putString(UPLOAD_BILL_PREVIOUS_SCREEN_NAME_KEY, mPreviousScreenName);
            fragment.setArguments(b);
            fragment.show(mContext.getSupportFragmentManager(), "stepsToUpload");

            // tracking
            try {
                String categoryName = mContext.getString(R.string.ga_rnr_category_upload_bill);
                String actionName = mContext.getString(R.string.ga_rnr_action_steps_to_upload_bill_view);
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(mContext);
                AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(categoryName, actionName, mPreviousScreenName);
                if(hMap!=null){
                    hMap.put("category",categoryName);
                    hMap.put("action",actionName);
                    hMap.put("label",mPreviousScreenName);
                }
                AnalyticsHelper.getAnalyticsHelper(mContext).trackEventCountly(actionName,hMap);

            } catch (Exception e) {
                // Exception
            }

        } else {
            JSONObject object = new JSONObject();
            appendObject(object, data.optJSONObject("reviewData"));
            appendObject(object, data.optJSONObject("reviewBox"));
            MasterDOFragment fragment = BillUploadFragment.newInstance(data.optString("billID"),
                    object, data.optBoolean("skipReview"));
            MasterDOFragment.addToBackStack(mContext, fragment);
        }
    }

    private Bundle getDialogBundle(String title, String message, String positive, String negative) {

        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, title);
        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION, message);
        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, positive);
        bundle.putString(AppConstant.BUNDLE_DIALOG_NEGATIVE_BUTTON_TEXT, negative);
        return bundle;
    }


    @Override
    public void locationUpdateFailed(String cause) {

        if(mContext!=null) {
            dismissLoader(mContext.getSupportFragmentManager());
            UiUtil.showToastMessage(mContext, cause);
            mContext = null;
        }
    }

    @Override
    public void locationUpdateSuccess() {
        mLocationHandler = null;
        if (checkFakeGps())
            makeApiCall();

    }


    @Override
    public void onGpsTurnedOn(boolean isSuccess) {
        if (isSuccess) {
            validatateGps();
        } else {
            if (mContext != null) {
                dismissLoader(mContext.getSupportFragmentManager());
            }
        }
    }

    @Override
    public void onActivityStopCalled() {
        if (mManager != null) {
            mManager.cancel();

            if (mContext != null) {
                dismissLoader(mContext.getSupportFragmentManager());
            }
        }

        isActivityStopped = true;
    }

    public void setPreviousScreenName(String screenName) {
        mPreviousScreenName = screenName;
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject object) {
        if (mContext != null) {
            validatateGps();
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        if (mContext != null && mContext.getApplicationContext() != null&& loginFlowCompleteFailureObject != null) {

            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                return;
            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
            // Show Error Message
            if (!AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(mContext.getApplicationContext(), cause);
            }
        }
    }

    @Override
    public void otpFlowCompleteSuccess(JSONObject otpFlowCompleteSuccessObject) {
        validatateGps();
    }

    @Override
    public void otpFlowCompleteFailure(JSONObject otpFlowCompleteFailureObject) {
        String errorMsg = "";
        if (otpFlowCompleteFailureObject != null) {
            errorMsg = otpFlowCompleteFailureObject.optString(AuthenticationWrapperStringReqFragment.API_RESPONSE_ERROR_MSG);
        }

        if (TextUtils.isEmpty(errorMsg) && mContext != null && mContext.getResources() != null) {
            errorMsg = mContext.getResources().getString(R.string.default_error_message);
        }

        if (mContext != null) {
            UiUtil.showToastMessage(mContext, errorMsg);
        }
    }

    @Override
    public void LoginSessionExpiredPositiveClick() {
        login();
    }

    @Override
    public void LoginSessionExpiredNegativeClick() {

    }
}
