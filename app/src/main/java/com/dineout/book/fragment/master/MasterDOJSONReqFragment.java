package com.dineout.book.fragment.master;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.NetworkError;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;
import com.dineout.android.volley.TimeoutError;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.dialogs.LoginSessionExpireDialog;
import com.dineout.book.dialogs.NetworkErrorView;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;

public abstract class MasterDOJSONReqFragment extends MasterDOFragment
        implements Listener<JSONObject>, ErrorListener,
        UserAuthenticationController.OtpFlowCompleteCallback,
        UserAuthenticationController.LoginFlowCompleteCallbacks,
        LoginSessionExpireDialog.LoginSessionExpiredButtonCallbacks {

    public static final String API_RESPONSE_TYPE = "type";
    public static final String API_RESPONSE_ERROR_MSG = "error_msg";
    public static final String API_RESPONSE_MSG = "msg";
    public static final String LOGIN_SESSION_EXPIRE_CODE = "901";
    public static final String OTP_REQUIRE_CODE = "902";
    public static final String OTP_VERIFY_CODE = "903";
    private boolean isSaveInstanceCalled=false;

    private Context context;
    private View networkErrorView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null)
            return;

        networkErrorView = getView().findViewById(R.id.networkErrorView);
        isSaveInstanceCalled=false;
    }

    @Override
    public void onStart() {
        super.onStart();
        isSaveInstanceCalled=false;
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        hideLoader();

        if (getView() == null || getActivity() == null)
            return;

        context = getActivity().getApplicationContext();

        if ((error instanceof TimeoutError || error instanceof NetworkError)
                && context != null && AppUtil.hasNetworkConnection(getActivity())) {
            if (networkErrorView != null) {
                networkErrorView.setVisibility(View.VISIBLE);
                if (networkErrorView instanceof NetworkErrorView) {
                    ((NetworkErrorView) networkErrorView)
                            .setType(NetworkErrorView.ConditionalDialog.SERVER_ERROR);
                }
            }
        } else if (!AppUtil.hasNetworkConnection(getActivity())) {
            if (networkErrorView != null) {
                networkErrorView.setVisibility(View.VISIBLE);
                if (networkErrorView instanceof NetworkErrorView) {
                    ((NetworkErrorView) networkErrorView)
                            .setType(NetworkErrorView.ConditionalDialog.INTERNET_CONNECTION);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRemoveErrorView() {
        if (networkErrorView != null) {
            networkErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isSaveInstanceCalled=true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getNetworkManager().cancel();
    }

    @Override
    public void handleNavigation() {
        super.handleNavigation();
    }


    @Override
    public void LoginSessionExpiredPositiveClick() {
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    @Override
    public void LoginSessionExpiredNegativeClick() {

    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            // Set New Diner Flag
            if (DOPreferences.isDinerNewUser(getActivity().getApplicationContext())) {
                // Set Is New User flag
                DOPreferences.setDinerNewUser(getActivity().getApplicationContext(), "0");
            }
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        if (getActivity() != null && loginFlowCompleteFailureObject != null) {

//            String type = loginFlowCompleteFailureObject.optString(API_RESPONSE_TYPE);
//            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
//                return;
//            }

            String cause = loginFlowCompleteFailureObject.optString(API_RESPONSE_ERROR_MSG);
            // Show Error Message
            if (!AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), cause);
            }
        }
    }

    @Override
    public void otpFlowCompleteSuccess(JSONObject result) {

    }

    @Override
    public void otpFlowCompleteFailure(JSONObject error) {
        if (getActivity() != null) {
            UiUtil.showToastMessage(getActivity(), "OTP ERROR generated");
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        if(getActivity() == null || getView() == null)
            return;

        hideLoader();

        handleErrorResponse(responseObject);
    }

    public void handleErrorResponse(JSONObject responseObject) {
        if (responseObject != null && !responseObject.optBoolean("status")) {
            String errorCode = responseObject.optString("error_code");

            if (errorCode != null) {
                switch (errorCode) {
                    case LOGIN_SESSION_EXPIRE_CODE:
                        //show session expired dialog
                        String errorMsg = responseObject.optString("error_msg");
                        if (getActivity() != null && !isSaveInstanceCalled) {
                            UserAuthenticationController.getInstance(getActivity()).showSessionExpireDialog(errorMsg, this);
                        }
                        break;

                    case OTP_REQUIRE_CODE:
                        Bundle phoneBundle = new Bundle();

                        if (getActivity().getApplicationContext() != null) {
                            //phoneBundle.putString(AppConstant.BUNDLE_PHONE_NUMBER, DOPreferences.getDinerPhone(getActivity().getApplicationContext()));
                            phoneBundle.putString(AppConstant.BUNDLE_PHONE_NUMBER, DOPreferences.getTempDinerPhone(getActivity().getApplicationContext()));
                        }
                        if (getActivity() != null) {
                            UserAuthenticationController.getInstance(getActivity()).startOTPFlow(phoneBundle, this);
                        }
                        break;

                    case OTP_VERIFY_CODE:
                        JSONObject errorResponse;
                        JSONObject otpInfo;
                        if ((errorResponse = responseObject.optJSONObject("error_response")) != null
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

                                    if (getActivity() != null) {
                                        UserAuthenticationController.getInstance(getActivity()).startOTPVerificationFlow(bundle, this);
                                    }
                                }
                            } else {
                                errorMsg = responseObject.optString("error_msg");
                                if (!TextUtils.isEmpty(errorMsg) && getContext() != null) {
                                    UiUtil.showToastMessage(getContext(), errorMsg);
                                }
                            }
                        }
                        break;

                    default:
                        errorMsg = responseObject.optString("error_msg");
                        if (!TextUtils.isEmpty(errorMsg) && getContext() != null) {
                            UiUtil.showToastMessage(getContext(), errorMsg);
                        }
                        break;
                }
            }
        }
    }
}
