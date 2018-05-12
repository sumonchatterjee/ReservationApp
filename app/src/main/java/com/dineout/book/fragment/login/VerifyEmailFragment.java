package com.dineout.book.fragment.login;

import android.os.Bundle;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.controller.OTPApiController;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;


public class VerifyEmailFragment extends EnterOTPFragment {

    public static VerifyEmailFragment newInstance(Bundle bundle, UserAuthenticationCallback callback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        VerifyEmailFragment fragment = new VerifyEmailFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = callback;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            getArguments().putBoolean(CHILD_CONSUMED_EVENT, true);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        trackScreenName(getString(R.string.countly_verify_email));

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
        setToolbarTitle("Verify Email ID");
        initializeViews();
        setUpValuesInXML();
        inflateValuesAndSetUpCountDown();

//        AppUtil.showKeyBoard(getActivity());
    }

    void setUpValuesInXML() {
        if (getResources() != null) {
            if (otpEdt != null) {
                otpEdt.setHint(getResources().getString(R.string.enter_code));
            }

            if (sendCodeBtn != null) {
                sendCodeBtn.setText(getResources().getString(R.string.verify_text));
            }

            if (detailTxt != null) {
                detailTxt.setText(getResources().getString(R.string.verify_email_resend_txt));
            }
        }
    }

    public void resendOTPtodiner(String source) {
        showResendOtpLoader();

        Bundle b = new Bundle();
        b.putString("user_input", userEdtxt
                .getText().toString());
        //OTPApiController.sendOTPToDiner(b, getNetworkManager(), this, source,"change_email");
        OTPApiController.sendOTPToDiner(b, getNetworkManager(), this, source,type);
    }

    public void verifyActionTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_verify_email_screen_verify_email_action),
//                getString(R.string.ga_new_login_verify_email_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_email),"VerifyClick","VerifyClick",hMap);

    }

    public void resendOTPToDinerTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_verify_email_screen_resend_otp_action),
//                getString(R.string.ga_new_login_verify_email_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_email),"ResendCodeClick","ResendCodeClick",hMap);


    }

    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_verify_email),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);

        popFragment();
    }
}
