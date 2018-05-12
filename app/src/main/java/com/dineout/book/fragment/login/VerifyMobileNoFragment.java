package com.dineout.book.fragment.login;

import android.os.Bundle;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.controller.OTPApiController;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;


public class VerifyMobileNoFragment extends EnterOTPFragment {
    public static VerifyMobileNoFragment newInstance(Bundle bundle, UserAuthenticationCallback authenticationCallback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        VerifyMobileNoFragment fragment = new VerifyMobileNoFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = authenticationCallback;

        return fragment;
    }

   @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        trackScreenName(getString(R.string.countly_verify_mobile));
        if (getArguments() != null) {
            getArguments().putBoolean(CHILD_CONSUMED_EVENT, true);
        }
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            getArguments().putBoolean(CHILD_CONSUMED_EVENT, false);
        }


        if (getArguments() != null){
            type=getArguments().getString("type");
        }
        setToolbarTitle("Verify Mobile Number");
        initializeViews();
        setUpValuesInXML();
        inflateValuesAndSetUpCountDown();
       
    }

    public void resendOTPtodiner(String source) {
        showResendOtpLoader();

        Bundle b = new Bundle();
        b.putString(OTPFlowFragment.USER_INPUT_TEXT_KEY, userEdtxt
                .getText().toString());
        OTPApiController.sendOTPToDiner(b, getNetworkManager(), this, source,type);
    }

    void setUpValuesInXML() {
        if (getResources() != null) {
            if (otpEdt != null) {
                otpEdt.setHint(getResources().getString(R.string.verification_code));
            }

            if (sendCodeBtn != null) {
                sendCodeBtn.setText(getResources().getString(R.string.button_verify));
            }

            if (detailTxt != null) {
                detailTxt.setText(getResources().getString(R.string.verify_mobile_resend_txt));
            }
        }
    }

    public void verifyActionTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_verify_mobile_screen_verify_mobile_action),
//                getString(R.string.ga_new_login_verify_mobile_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_mobile),"VerifyClick","VerifyClick",hMap);
    }

    public void resendOTPToDinerTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_verify_mobile_screen_resend_otp_action),
//                getString(R.string.ga_new_login_verify_mobile_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_mobile),"ResendCodeClick","ResendCodeClick",hMap);
    }

    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_mobile),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);

        popFragment();
    }
}
