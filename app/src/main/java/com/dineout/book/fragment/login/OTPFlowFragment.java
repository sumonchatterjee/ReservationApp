package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.controller.OTPApiController;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;


public class OTPFlowFragment extends LoginFlowBaseFragment implements OTPApiController.OTPCallbacks,View.OnClickListener {
    EditText mUserInputEt;
    Button mSendOTPBtn;
    TextView mDetailTv;
    UserAuthenticationCallback mCallback;

    String userPhoneNumber;

    public static final String USER_INPUT_TEXT_KEY = "user_input";

    public static OTPFlowFragment newInstance(Bundle bundle,UserAuthenticationCallback mAuthticationCallback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        OTPFlowFragment fragment = new OTPFlowFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = mAuthticationCallback;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_otp, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserInputEt = (EditText) view.findViewById(R.id.user_input_et);
        mSendOTPBtn = (Button) view.findViewById(R.id.send_code_btn);
        mDetailTv = (TextView) view.findViewById(R.id.detail_txt);

        if (getArguments() != null){
            userPhoneNumber = getArguments().getString(AppConstant.BUNDLE_PHONE_NUMBER);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //trackScreenName(getString(R.string.countly_enter_mobile_number));

        if (getArguments() != null && !getArguments().getBoolean(CHILD_CONSUMED_EVENT)) {
            setToolbarTitle("Enter Mobile Number");
            setUpValuesInXML();
            inflatePreviousScreenValueInToEditBox();
            setUpSendButtonClickListener();

//            AppUtil.showKeyBoard(getActivity());
        }
    }

    void setUpValuesInXML() {
        if (getResources() != null) {
            if (mUserInputEt != null) {
                mUserInputEt.setInputType(InputType.TYPE_CLASS_PHONE);

                if(!TextUtils.isEmpty(userPhoneNumber)){
                    mUserInputEt.setText(userPhoneNumber);
                    mUserInputEt.setSelection(mUserInputEt.getText().length());

                }else {
                    mUserInputEt.setHint(getResources().getString(R.string.otp_screen_enter_mobile_hint));
                }
            }

            if (mSendOTPBtn != null) {
                mSendOTPBtn.setText(getResources().getString(R.string.send_otp));
            }

            if (mDetailTv != null) {
                mDetailTv.setText(getResources().getString(R.string.update_phone_txt));
            }

        }
    }

    void inflatePreviousScreenValueInToEditBox() {
        if (mUserInputEt != null && getArguments() != null) {
            String userInputText = getArguments().getString(USER_INPUT_TEXT_KEY);

            if (!TextUtils.isEmpty(userInputText)) {
                mUserInputEt.setText(userInputText);
                mUserInputEt.setSelection(mUserInputEt.getText().length());
            }
        }
    }

    void setUpSendButtonClickListener() {
        if (mSendOTPBtn != null) {
            mSendOTPBtn.setOnClickListener(this);
        }
    }

    public void processOTPFlow(Bundle bundle) {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

//        if(hMap!=null){
//            if(!TextUtils.isEmpty(mUserInputEt.getText().toString())) {
//                hMap.put("typeValue", mUserInputEt.getText().toString());
//            }
//
//            if(!TextUtils.isEmpty(mUserInputEt.getText().toString())) {
//                hMap.put("typeValue", mUserInputEt.getText().toString());
//            }
//        }

        //track events
        trackEventForCountlyAndGA(getString(R.string.countly_enter_mobile_number),"MobileNumberType"," ",hMap);
        trackEventForCountlyAndGA(getString(R.string.countly_enter_mobile_number),"SendOTPClick","SendOTPClick",hMap);

        OTPApiController.sendOTPToDiner(bundle, getNetworkManager(), this, "EnterOTP", "verification_phone");
    }

    @Override
    public void sendOTPToDinerSuccess(JSONObject resp, String source) {
        hideLoader();

        JSONObject data = processResponse(resp);
        sendToOtpScreen(data);
    }

    @Override
    public void sendOTPToDinerFailure(String msg) {
        hideLoader();

        if (getActivity() != null) {
            if (!AppUtil.hasNetworkConnection(getActivity())) {
                msg = getResources().getString(R.string.no_network_connection);

            }
            if(!TextUtils.isEmpty(msg)) {
                hideKeyboardAndShowToast(getActivity(), msg);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_code_btn:

                if (mUserInputEt != null && !TextUtils.isEmpty(mUserInputEt.getText())) {
                    showLoader();

                    Bundle b = new Bundle();
                    b.putString(USER_INPUT_TEXT_KEY, mUserInputEt.getText().toString());
                    processOTPFlow(b);
                } else {
                    hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.field_validation_text));
                }

                break;
        }
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

            VerifyMobileNoFragment fragment = VerifyMobileNoFragment.newInstance(b, mCallback);
            addToBackStack(getFragmentManager(), fragment,
                    R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
    }

    //process json response
    private JSONObject processResponse(JSONObject resp) {
        JSONObject data = null;
        if (resp != null && resp.optBoolean("status")) {
            JSONObject outputParam = resp.optJSONObject("output_params");
            if (outputParam != null) {
                data = outputParam.optJSONObject("data");
                if (data != null) {
                    UiUtil.showToastMessage(getContext(), data.optString("msg"));
                }
            }
        }
        return data;
    }


    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_enter_mobile_number),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }

    public void popFragment() {
        super.handleNavigation();
    }
}
