package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.controller.OTPApiController;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;


public class UpdateEmailFragment extends OTPFlowFragment {
    public static UpdateEmailFragment newInstance(Bundle bundle, UserAuthenticationCallback callback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        UpdateEmailFragment fragment = new UpdateEmailFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = callback;

        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {


        trackScreenName(getString(R.string.countly_update_email));

        if (getArguments() != null) {
            getArguments().putBoolean(CHILD_CONSUMED_EVENT, true);
        }

        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            getArguments().putBoolean(CHILD_CONSUMED_EVENT, false);
        }

        setToolbarTitle("Update Email");
        setUpValuesInXML();
        inflatePreviousScreenValueInToEditBox();
        setUpSendButtonClickListener();

//        AppUtil.showKeyBoard(getActivity());
    }

    void setUpValuesInXML() {
        if (getResources() != null) {
            if (mUserInputEt != null) {
                mUserInputEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                mUserInputEt.setHint(getResources().getString(R.string.otp_screen_enter_email_hint));
            }

            if (mSendOTPBtn != null) {
                mSendOTPBtn.setText(getResources().getString(R.string.verify_send_button_text));
            }

            if (mDetailTv != null) {
                mDetailTv.setText(getResources().getString(R.string.update_email_txt));
            }
        }
    }

    public void processOTPFlow(Bundle bundle) {
        //track events

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
//        if(hMap!=null){
//            if(!TextUtils.isEmpty(mUserInputEt.getText().toString()))
//             hMap.put("typeValue",mUserInputEt.getText().toString());
//        }

        trackEventForCountlyAndGA(getString(R.string.countly_update_email),"EmailIDType","",hMap);
        trackEventForCountlyAndGA(getString(R.string.countly_update_email),"SendVerificationCodeClick","SendVerificationCodeClick",hMap);

        OTPApiController.sendOTPToDiner(bundle, getNetworkManager(), this, "UpdateEmail", "change_email");
    }

    void sendToOtpScreen(JSONObject data){
        if (data != null) {
            Bundle b = getArguments();
            if (b == null) {
                b = new Bundle();
            }
            b.putString("otp_id", data.optString("otp_id"));
            b.putString("msg", data.optString("msg"));
            b.putString("resend_otp_time", data.optString("resend_otp_time"));
            b.putString("type", data.optString("type"));
            if (mUserInputEt != null && !TextUtils.isEmpty(mUserInputEt.getText())) {
                b.putString(USER_INPUT_TEXT_KEY, mUserInputEt.getText().toString());
            }

            VerifyEmailFragment fragment = VerifyEmailFragment.newInstance(b, mCallback);
            addToBackStack(getFragmentManager(), fragment, R.anim.f_enter, R.anim.f_exit);
        }
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_update_email),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);

        popFragment();
    }
}
