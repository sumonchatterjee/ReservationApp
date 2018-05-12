package com.dineout.book.fragment.master;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

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
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.dialogs.LoginSessionExpireDialog;
import com.dineout.book.dialogs.NetworkErrorView;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;


public abstract class MasterDOStringReqFragment extends MasterDOFragment
        implements Listener<String>, ErrorListener,
        UserAuthenticationController.OtpFlowCompleteCallback,
        UserAuthenticationController.LoginFlowCompleteCallbacks,
        LoginSessionExpireDialog.LoginSessionExpiredButtonCallbacks {

    public static final String API_RESPONSE_TYPE = "type";
    public static final String API_RESPONSE_ERROR_MSG = "error_msg";
    public static final String LOGIN_SESSION_EXPIRE_CODE = "901";
    public static final String OTP_REQUIRE_CODE = "902";

    private View networkErrorView;
    private Context context;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() == null || getView() == null)
            return;

        networkErrorView = getView().findViewById(R.id.networkErrorView);
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        context = getActivity().getApplicationContext();
        if (getView() != null && getActivity() != null)
            hideLoader();
        if (error instanceof TimeoutError && context != null && AppUtil.hasNetworkConnection(getActivity())) {

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
    public void onRemoveErrorView() {
        if (networkErrorView != null) {
            networkErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getNetworkManager().cancel();
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

//            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
//            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
//                return;
//            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
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
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        if(getActivity() == null || getView() == null)
            return;

        hideLoader();

        if (!TextUtils.isEmpty(responseObject)) {
            try {
                JSONObject responseJsonObject = new JSONObject(responseObject);

                if (!responseJsonObject.optBoolean("status")) {
                    String errorCode = responseJsonObject.optString("error_code");

                    if (errorCode != null) {
                        switch (errorCode) {
                            case LOGIN_SESSION_EXPIRE_CODE:
                                //show session expired dialog
                                String errorMsg = responseJsonObject.optString("error_msg");
                                if (getActivity() != null) {
                                    UserAuthenticationController.getInstance(getActivity()).showSessionExpireDialog(errorMsg, this);
                                }
                                break;

                            case OTP_REQUIRE_CODE:
                                Bundle phoneBundle = new Bundle();

                                if (getActivity().getApplicationContext() != null) {
                                    phoneBundle.putString(AppConstant.BUNDLE_PHONE_NUMBER, DOPreferences.getDinerPhone(getActivity().getApplicationContext()));
                                }
                                if (getActivity() != null) {
                                    UserAuthenticationController.getInstance(getActivity()).startOTPFlow(phoneBundle, this);
                                }
                                break;

                            case MasterDOJSONReqFragment.OTP_VERIFY_CODE:
                                JSONObject errorResponse;
                                JSONObject otpInfo;
                                if ((errorResponse = responseJsonObject.optJSONObject("error_response")) != null
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
                                        errorMsg = responseJsonObject.optString("error_msg");
                                        if (!TextUtils.isEmpty(errorMsg) && getContext() != null) {
                                            UiUtil.showToastMessage(getContext(), errorMsg);
                                        }
                                    }
                                }
                                break;

                            default:
                                errorMsg = responseJsonObject.optString("error_msg");
                                if (!TextUtils.isEmpty(errorMsg) && getContext() != null) {
                                    UiUtil.showToastMessage(getContext(), errorMsg);
                                }
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), "Some error in registering a member.Please try again.");
            }
        }

    }
}
