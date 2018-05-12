package com.dineout.book.fragment.login;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.PermissionUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.controller.OTPApiController;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;


import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;



import static com.dineout.book.fragment.login.OTPFlowFragment.USER_INPUT_TEXT_KEY;



public class EnterOTPFragment extends LoginFlowBaseFragment implements View.OnClickListener, OTPApiController.OTPCallbacks {

    EditText otpEdt;
    private String OTPValue;
    public TextView userEdtxt;
    private TextView resendTxt;
    Button sendCodeBtn;
    ProgressBar resendCodeProgressBar;
    TextView detailTxt;
    String type;
    UserAuthenticationCallback mCallback;


    BroadcastReceiver listener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                String pattern = "(^|([^0-9]))([0-9][0-9][0-9][0-9][0-9][0-9])($|([^0-9]))";
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                if (bundle != null) {
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            String msgBody = msgs[i].getMessageBody();

                            msgBody = msgBody.toLowerCase();
                            if ((msgBody.contains("otp") || msgBody.contains("one time password")) && msgBody.contains("dineout")) {
                                Pattern p = Pattern.compile(pattern);
                                Matcher m = p.matcher(msgBody);

                                if (m.find()) {
                                    OTPValue = m.group(3);
                                    if (!TextUtils.isEmpty(OTPValue)) {
                                        setOTP(OTPValue);
                                    }
                                }


                            }
//                            OTPValue = msgBody.replaceAll("[^0-9]", "");
//                            if ((msgBody.contains("OTP") || msgBody.contains("one time password")) && (msgBody.contains("Dineout") || msgBody.contains("dineout"))) {
//                                setOTP(OTPValue);
//
//                            }
                        }
                    } catch (Exception e) {
                        Log.d("Exception caught", e.getMessage());
                    }
                }
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstant.REQUEST_PERMISSION_RECEIVE_SMS && listener != null) {
            getContext().registerReceiver(listener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if (PermissionUtils.checkReadSMSPermission(getActivity())) {
            getContext().registerReceiver(listener, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
        } else {
            listener = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (listener != null) {
            getContext().unregisterReceiver(listener);
        }
    }


    public void setOTP(final String otpValue) {
        if (otpEdt != null && !TextUtils.isEmpty(otpValue)) {
            otpEdt.setText(otpValue);
            otpEdt.setSelection(otpValue.length());
        }
    }

    public static EnterOTPFragment newInstance(Bundle bundle, UserAuthenticationCallback authenticationCallback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        EnterOTPFragment fragment = new EnterOTPFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = authenticationCallback;

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && !getArguments().getBoolean(CHILD_CONSUMED_EVENT)) {
            if (!PermissionUtils.checkReadSMSPermission(getContext())) {
                requestPermissions(
                        new String[]{Manifest.permission.RECEIVE_SMS},
                        AppConstant.REQUEST_PERMISSION_RECEIVE_SMS);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_enter_otp, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null && !getArguments().getBoolean(CHILD_CONSUMED_EVENT)) {
            if (getArguments() != null) {
                type = getArguments().getString("type");
            }
            setToolbarTitle("Log in");
            initializeViews();
            setUpValuesInXML();
            inflateValuesAndSetUpCountDown();

//            AppUtil.showKeyBoard(getActivity());
        }
    }

    public void initializeViews(){
        if(getView()!=null){
            otpEdt=(EditText)getView().findViewById(R.id.code_edtxt);
            userEdtxt=(TextView)getView().findViewById(R.id.email_edtxt);
            resendTxt=(TextView)getView().findViewById(R.id.timer_txt);
            sendCodeBtn=(Button)getView().findViewById(R.id.send_code_btn);
            resendCodeProgressBar = (ProgressBar) getView().findViewById(R.id.resend_code_progress_bar);
            detailTxt=(TextView)getView().findViewById(R.id.resend_code);

        }
    }

    void setUpValuesInXML() {
        if (getResources() != null) {
            if (otpEdt != null) {
                otpEdt.setHint(getResources().getString(R.string.otp_screen_hint));
            }

            if (sendCodeBtn != null) {
                sendCodeBtn.setText(getResources().getString(R.string.login_txt));
            }

            if (detailTxt != null) {
                detailTxt.setText(getResources().getString(R.string.verify_mobile_resend_txt));
            }
        }
    }

    void inflateValuesAndSetUpCountDown() {
        if (getArguments() != null) {
            if (userEdtxt != null) {
                String userInputText = getArguments().getString(USER_INPUT_TEXT_KEY);
                if (TextUtils.isEmpty(userInputText)) {
                    userInputText = "";
                }
                userEdtxt.setText(userInputText);
            }

            setResendTextAndStartTimer(getArguments().getString("resend_otp_time"));
        }

        if (sendCodeBtn != null) {
            sendCodeBtn.setOnClickListener(this);
        }
    }


    void setResendTextAndStartTimer(String resendOtp){
        if (resendTxt != null) {
            String resendText = "";
            String time = resendOtp;
            if (!TextUtils.isEmpty(time)) {
                String coloredTime = getColoredSpanned(time +" (s)", "#fa5757");
                String resendValue = getColoredSpanned("Resend OTP in ", "#757575");

                resendText = resendValue +" "+ coloredTime;
            }
            if (TextUtils.isEmpty(resendText)) {
                resendText = "";
            }
            resendTxt.setText(Html.fromHtml(resendText));
            resendTxt.setOnClickListener(this);

            updateTimerText(time);
        }
    }

    private void updateTimerText(final String timerValue){
        if(!AppUtil.isStringEmpty(timerValue)){
            int time=Integer.parseInt(timerValue);
            long milli=time*1000;
            new CountDownTimer(milli, 1000) {

                public void onTick(long millisUntilFinished) {

                    String resend = getColoredSpanned("Resend OTP in ", "#757575");
                    String time = getColoredSpanned(millisUntilFinished / 1000 + " (s)", "#fa5757");
                    resendTxt.setText(Html.fromHtml(resend+" "+time));
                    resendTxt.setClickable(false);
                }

                public void onFinish() {
                    resendTxt.setText("Resend Code");
                    resendTxt.setClickable(true);
                }
            }.start();
        }
    }

    private void verifyOTP(){
        if (otpEdt != null) {
            if (!TextUtils.isEmpty(otpEdt.getText())) {
                makeLoginRequest(NATIVE_SIGNIN_API,
                        ApiParams.getVerifyotpParams(getArguments().getString("otp_id"),
                                otpEdt.getText().toString()), AppConstant.URL_VERIFY_DINER_OTP, new LoginCallbacks() {
                            @Override
                            public void loginSuccess(JSONObject loginSuccessObject) {
                                setFragmentStackEntryCount(loginSuccessObject);

                                if(getArguments()!= null && mCallback != null){
                                    if (UserAuthenticationController.LOGIN_FLOW.equalsIgnoreCase(getArguments().getString(UserAuthenticationController.FROM_SCREEN))) {
                                        mCallback.userAuthenticationLoginSuccess(loginSuccessObject);
                                    } else if (UserAuthenticationController.OTP_FLOW.equalsIgnoreCase(getArguments().getString(UserAuthenticationController.FROM_SCREEN))) {
                                        mCallback.userAuthenticationOTPSuccess(loginSuccessObject);
                                    }
                                }
                            }

                            @Override
                            public void loginFailure(JSONObject loginFailureObject) {
                                if(getArguments()!= null && mCallback != null){
                                    if (UserAuthenticationController.LOGIN_FLOW.equalsIgnoreCase(getArguments().getString(UserAuthenticationController.FROM_SCREEN))) {
                                        mCallback.userAuthenticationLoginFailure(loginFailureObject);
                                    } else if (UserAuthenticationController.OTP_FLOW.equalsIgnoreCase(getArguments().getString(UserAuthenticationController.FROM_SCREEN))) {
                                        mCallback.userAuthenticationOTPFailure(loginFailureObject);
                                    }
                                }
                            }
                        });
            } else {
                hideKeyboardAndShowToast(getActivity(), getResources().getString(R.string.field_validation_text));
            }
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_code_btn:
                verifyActionTracking();

                verifyOTP();
                break;


            case R.id.timer_txt:
                resendOTPToDinerTracking();

                resendOTPtodiner("ResendOTP");
                break;
        }

    }


    public void resendOTPtodiner(String source) {
        showResendOtpLoader();

        Bundle b = new Bundle();
        b.putString(USER_INPUT_TEXT_KEY, userEdtxt.getText().toString());
        OTPApiController.sendOTPToDiner(b, getNetworkManager(), this, source,type);
    }

    @Override
    public void sendOTPToDinerSuccess(JSONObject resp, String source) {
        hideResendOtpLoader();

        JSONObject data = processResponse(resp);
        if(data!=null){
            if(!TextUtils.isEmpty(data.optString("resend_otp_time"))) {
                setResendTextAndStartTimer(data.optString("resend_otp_time"));
            }
        }
    }

    @Override
    public void sendOTPToDinerFailure(String msg) {
        hideResendOtpLoader();

        if (getActivity() != null) {
            if (!AppUtil.hasNetworkConnection(getActivity())) {
                msg = getResources().getString(R.string.no_network_connection);

            }
            if(!TextUtils.isEmpty(msg)) {
                hideKeyboardAndShowToast(getActivity(), msg);
            }
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

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }


    public void showResendOtpLoader() {
        if (resendCodeProgressBar != null) {
            resendCodeProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideResendOtpLoader() {
        if (resendCodeProgressBar != null) {
            resendCodeProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void verifyActionTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_via_otp_screen_log_in_action),
//                getString(R.string.ga_new_login_via_otp_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login_via_otp),"LoginCLick","LoginCLick",hMap);

    }

    public void resendOTPToDinerTracking() {
        // tracking
//        trackEventGA(getString(R.string.ga_new_login_category_name),
//                getString(R.string.ga_new_login_via_otp_screen_resend_otp_action),
//                getString(R.string.ga_new_login_via_otp_screen_label));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login_via_otp),getString(R.string.d_resend_code_click),getString(R.string.d_resend_code_click),hMap);
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_login_via_otp),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }

    public void popFragment() {
        super.handleNavigation();
    }
}
