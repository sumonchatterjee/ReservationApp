package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.controller.OTPApiController;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;


public class EnterPasswordPhoneFragment extends LoginFlowBaseFragment implements View.OnClickListener,OTPApiController.OTPCallbacks {

    private TextView phone;
    private RelativeLayout otpLayout;
    private EditText password;
    protected final int NATIVE_SIGNIN_API = 0x900;

    private String userInput;
    private UserAuthenticationCallback mCallback;


    public static EnterPasswordPhoneFragment newInstance(Bundle bundle, UserAuthenticationCallback callBack) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);


        EnterPasswordPhoneFragment fragment = new EnterPasswordPhoneFragment();
        fragment.mCallback = callBack;

        fragment.setArguments(bundle);


        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //trackScreenToGA(getString(R.string.ga_screen_enter_password));
        trackScreenName(getString(R.string.countly_login_password));

        return inflater.inflate(R.layout.fragment_enter_password, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Log In");
        initializeView();
    }


    private void initializeView() {
        if(getView()!=null) {
            phone = (TextView) getView().findViewById(R.id.phone_edtxt);
            otpLayout = (RelativeLayout) getView().findViewById(R.id.otp_layout);
            password = (EditText) getView().findViewById(R.id.code_edtxt);


            (getView().findViewById(R.id.login_btn)).setOnClickListener(this);
            (getView().findViewById(R.id.login_otp_btn)).setOnClickListener(this);
            (getView().findViewById(R.id.reset_password)).setOnClickListener(this);
            if (getArguments() != null) {

                if (getArguments().getInt(AppConstant.BUNDLE_GUEST_LOGIN_VIA_OTP) == 1) {
                    otpLayout.setVisibility(View.VISIBLE);
                } else {
                    otpLayout.setVisibility(View.GONE);
                }
                setUserInput();
                phone.setText(userInput);
            }
        }
    }

    private void setUserInput() {
        if(getArguments()!=null) {
            if (!AppUtil.isStringEmpty(getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE))) {
                userInput = getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE);
            } else {
                userInput = getArguments().getString(AppConstant.BUNDLE_GUEST_EMAIL);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                // tracking
                sendLoginClickTracking();

                makeLoginRequest();
                break;

            case R.id.login_otp_btn:
                // tracking
                sendOTPClickTracking();

                processOTPflow("EnterOTP","login");
                break;

            case R.id.reset_password:
                // tracking
                sendResetPasswordTracking();

                processOTPflow("ResetPassword","reset_password");
                break;
        }
    }

    private void makeLoginRequest() {
            makeLoginRequest(NATIVE_SIGNIN_API, ApiParams.getLoginPhoneEmailParameter(phone
                    .getText().toString(), password.getText().toString()), AppConstant.URL_LOGIN, new LoginCallbacks() {
                @Override
                public void loginSuccess(JSONObject loginSuccessObject) {
                    setFragmentStackEntryCount(loginSuccessObject);

                    if (mCallback != null) {
                        mCallback.userAuthenticationLoginSuccess(loginSuccessObject);
                    }
                }

                @Override
                public void loginFailure(JSONObject loginFailureObject) {
                    if (mCallback != null) {
                        mCallback.userAuthenticationLoginFailure(loginFailureObject);
                    }
                }
            });
        }


    private void processOTPflow(String source,String type) {
        showLoader();

        Bundle b = new Bundle();
        b.putString(USER_INPUT_TEXT_KEY, phone.getText().toString());
        OTPApiController.sendOTPToDiner(b, getNetworkManager(), this, source,type);
    }


    @Override
    public void sendOTPToDinerSuccess(JSONObject resp, String source) {
        hideLoader();

        JSONObject data = processResponse(resp);
        if (source.equalsIgnoreCase("EnterOTP")) {
            sendToOtpScreen(data);
        } else if (source.equalsIgnoreCase("ResetPassword")){
            sendToResetPasswordScreen(data);
        }
    }


    @Override
    public void sendOTPToDinerFailure(String errorMsg) {
        hideLoader();

        if (getActivity() != null) {
            if (!AppUtil.hasNetworkConnection(getActivity())) {
                errorMsg = getResources().getString(R.string.no_network_connection);

            }
            if(!TextUtils.isEmpty(errorMsg)) {
                hideKeyboardAndShowToast(getActivity(), errorMsg);
            }
        }
    }

    //navigate to enter otp screen
    private void sendToOtpScreen(JSONObject data) {
        if (data != null) {
            Bundle b = getArguments();
            if (b == null) {
                b = new Bundle();
            }
            b.putString("otp_id", data.optString("otp_id"));
            b.putString("msg", data.optString("msg"));
            b.putString("resend_otp_time", data.optString("resend_otp_time"));
            b.putString("user_input", userInput);
            b.putString("type",data.optString("type"));

            EnterOTPFragment fragment = EnterOTPFragment.newInstance(b, mCallback);
            addToBackStack(getFragmentManager(), fragment, R.anim.f_enter, R.anim.f_exit);
        }
    }

    //send to reset password screen
    private void sendToResetPasswordScreen(JSONObject data) {
        if (data != null) {
            Bundle b = new Bundle();
            b.putString("otp_id", data.optString("otp_id"));
            b.putString("msg", data.optString("msg"));
            b.putString("resend_otp_time", data.optString("resend_otp_time"));

            ResetPasswordFragment fragment = ResetPasswordFragment.newInstance(b);
            addToBackStack(getFragmentManager(), fragment, R.anim.f_enter, R.anim.f_exit);
        }
    }

    //process json response
    private JSONObject processResponse(JSONObject resp) {
        JSONObject data = null;
        if (resp != null && resp.optBoolean("status")) {
            JSONObject outputParam = resp.optJSONObject("output_params");
            if (outputParam != null) {
                data = outputParam.optJSONObject("data");
                if (data != null && getContext()!=null) {
                    UiUtil.showToastMessage(getContext(), data.optString("msg"));
                }
            }
        }
        return data;
    }

    private void sendLoginClickTracking() {
        String action = "";
        String label = "";
        if (getArguments() != null) {
            if (AppUtil.isStringEmpty(getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE))) {
                action = getString(R.string.d_logins_click);
                label = "Email";
            } else {
                action = getString(R.string.d_logins_click);
                label = getString(R.string.d_mobile_click);
            }
        }
        if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(label)) {
            // tracking
            //trackEventGA(getString(R.string.ga_new_login_category_name), action, label);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_login_password),action,label,hMap);
        }
    }

    private void sendOTPClickTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_password_screen_mobile_login_via_otp_action),
//                getString(R.string.ga_new_login_password_screen_mobile_label));


        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login_password),"LoginViaOTP","LoginViaOTP",hMap);

    }

    private void sendResetPasswordTracking() {
        String action = "";
        String label = "";
        if (getArguments() != null) {
            if (AppUtil.isStringEmpty(getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE))) {
                action = getString(R.string.ga_new_login_password_screen_email_reset_password_action);
                label = "Email";
            } else {
                action = getString(R.string.ga_new_login_password_screen_email_reset_password_action);
                label = "Mobile";
            }
        }
        if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(label)) {
            // tracking
            //trackEventGA(getString(R.string.ga_new_login_category_name), action, label);
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_login_password),action,label,hMap);
        }
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login_password),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
