package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.interfaces.UserAuthenticationCallback;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.controller.ValidationManager;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;



public class CreateAccountFragment extends LoginFlowBaseFragment implements View.OnClickListener {

    private EditText nameEdt;
    private EditText phoneEdt;
    private EditText emailEdt;
    private EditText passwordEdt;
    private EditText confirmEdt;
    private int ID_SIGN_UP = 101;
    private UserAuthenticationCallback mCallback;

    public static CreateAccountFragment newInstance(Bundle bundle,UserAuthenticationCallback mAuthticationCallback) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        int count = bundle.getInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY);
        bundle.putInt(UserAuthenticationController.FRAG_STACK_COUNT_KEY, count + 1);

        CreateAccountFragment fragment = new CreateAccountFragment();
        fragment.setArguments(bundle);
        fragment.mCallback = mAuthticationCallback;

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //trackScreenToGA(getString(R.string.ga_screen_sign_up));
        trackScreenName(getString(R.string.countly_sign_up));
        return inflater.inflate(R.layout.fragment_create_account, container, false);

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Sign Up");

//        AppUtil.showKeyBoard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        initializeViews();
    }


    private void initializeViews(){
        if(getView()!=null){
            nameEdt=(EditText)getView().findViewById(R.id.name_edtxt);
            phoneEdt = (EditText)getView().findViewById(R.id.phone_edtxt);
            emailEdt = (EditText)getView().findViewById(R.id.email_edtxt);
            passwordEdt =(EditText)getView().findViewById(R.id.password_edtxt);
            confirmEdt=(EditText)getView().findViewById(R.id.confirmpassword_edtxt);
            (getView().findViewById(R.id.send_code_btn)).setOnClickListener(this);
        }

        if (getArguments() != null) {
            if (!AppUtil.isStringEmpty(getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE))) {
                phoneEdt.setText(getArguments().getString(AppConstant.BUNDLE_GUEST_PHONE));
            } else {
                emailEdt.setText(getArguments().getString(AppConstant.BUNDLE_GUEST_EMAIL));
            }
        }

         if(confirmEdt!=null && passwordEdt!=null){
             confirmEdt.setText("");
             passwordEdt.setText("");
         }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_code_btn:
                 handleSignUp();
                break;
        }

    }


    private void handleSignUp() {

        //track events for name, email , phone
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
//        if(hMap!=null){
//            if(!TextUtils.isEmpty(nameEdt.getText().toString())){
//                hMap.put("typeValue",nameEdt.getText().toString());
//            }
//            if(!TextUtils.isEmpty(phoneEdt.getText().toString())) {
//                hMap.put("typeValue", phoneEdt.getText().toString());
//            }
//
//            if(!TextUtils.isEmpty(emailEdt.getText().toString())) {
//                hMap.put("typeValue", emailEdt.getText().toString());
//            }
//
//        }
        trackEventForCountlyAndGA(getString(R.string.countly_sign_up),"NameType","",hMap);
        trackEventForCountlyAndGA(getString(R.string.countly_sign_up),"MobileNumberType","",hMap);
        trackEventForCountlyAndGA(getString(R.string.countly_sign_up),"EmailIDType","",hMap);
        trackEventForCountlyAndGA(getString(R.string.countly_sign_up),getString(R.string.d_signup_click),getString(R.string.d_signup_click),hMap);

        signUp();

    }



    //Validate Input Fields
    private boolean validateInputFields() {
        boolean areInputFieldsValid = true;

        //Validate Phone Number
        if (areInputFieldsValid) {
            String phoneNumberErrorMessage = ValidationManager.validatePhoneNumber(getActivity(), phoneEdt.getText().toString());

            if (!AppUtil.isStringEmpty(phoneNumberErrorMessage)) {
                //Set Flag
                areInputFieldsValid = false;

                //Show Error Message
                UiUtil.showToastMessage(getContext(), phoneNumberErrorMessage);
            }
        }

        //Validate Name
        if (areInputFieldsValid) {
            String nameErrorMessage = ValidationManager.validateName(getActivity(), nameEdt.getText().toString());

            if (!AppUtil.isStringEmpty(nameErrorMessage)) {
                //Set Flag
                areInputFieldsValid = false;

                //Show Error Message
                UiUtil.showToastMessage(getContext(), nameErrorMessage);

            }
        }

        if (areInputFieldsValid) {
            String emailErrorMessage = ValidationManager.validateEmail(getActivity(), emailEdt.getText().toString());

            if (!AppUtil.isStringEmpty(emailErrorMessage)) {
                //Set Flag
                areInputFieldsValid = false;

                UiUtil.showToastMessage(getContext(), emailErrorMessage);
            }
        }
        if (areInputFieldsValid) {
            String passwordErrorMessage = ValidationManager.validatePassword(getActivity(), passwordEdt.getText().toString());

            if (!AppUtil.isStringEmpty(passwordErrorMessage)) {
                areInputFieldsValid = false;

                UiUtil.showToastMessage(getContext(), passwordErrorMessage);
            }
        }

        return areInputFieldsValid;
    }



      //sign up
     private void signUp(){
         showLoader();

         HashMap<String, String> params = ApiParams.getRegisterParams(
                 phoneEdt.getText().toString(),
                 nameEdt.getText().toString(),
                 emailEdt.getText().toString(),
                 passwordEdt.getText().toString(),
                 confirmEdt.getText().toString());

         getNetworkManager().stringRequestPost(ID_SIGN_UP, AppConstant.URL_REGISTER
                 , params, this, this, false);
     }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
//        super.onResponse(request, responseObject, response);

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == ID_SIGN_UP) {
            try {
                if (responseObject != null) {
                    JSONObject resp = new JSONObject(responseObject);
                    if (resp.optBoolean("status")) {
                        String successMsg;
                        JSONObject outputParams = resp.optJSONObject("output_params");
                        if (outputParams != null) {
                            JSONObject data = outputParams.optJSONObject("data");

                            String type = data.optString("type");
                            successMsg = data.optString("msg");
                            if(AppUtil.isStringEmpty(successMsg)){
                                successMsg = outputParams.optString("success_msg");
                            }

                            if(!AppUtil.isStringEmpty(successMsg) && getContext()!=null){
                                UiUtil.showToastMessage(getContext(),successMsg);
                            }

                            if (!AppUtil.isStringEmpty(type)) {

                                if (type.equalsIgnoreCase("login")) {
                                    processLogin(data);

                                } else {
                                    //process to otp screen to fill the details
                                    JSONObject details = processResponse(resp);
                                    sendToOtpScreen(details);

                                }
                            }
                        }
                    } else {
                        String errorMsg = resp.optString("error_msg");
                        if (TextUtils.isEmpty(errorMsg) && getResources() != null) {
                            errorMsg = getResources().getString(R.string.login_error_message);
                        }

                        if (getActivity() != null) {
                            UiUtil.showToastMessage(getActivity(), errorMsg);
                        }
                    }
                } else {
                    if (getActivity() != null && getResources() != null) {
                        UiUtil.showToastMessage(getActivity(), getResources().getString(R.string.login_error_message));
                    }
                }

            } catch (Exception ex) {
                UiUtil.showToastMessage(getActivity(), getResources().getString(R.string.login_error_message));
            }

        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        HashMap<String, Object> props = new HashMap<>();
        props.put("EmailId", emailEdt.getText().toString());
        props.put("Successful", false);

       // trackEventGA(getString(R.string.push_label_signed_in), props);
    }

    private void processLogin(JSONObject data) {

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(AppConstant.BUNDLE_GUEST_PHONE, data.optString("phone"));
        bundle.putString(AppConstant.BUNDLE_GUEST_EMAIL, data.optString("email"));
        bundle.putInt(AppConstant.BUNDLE_GUEST_LOGIN_VIA_OTP, data.optInt("login_via_otp"));

        EnterPasswordPhoneFragment frag = EnterPasswordPhoneFragment.newInstance(bundle, mCallback);
        frag.setArguments(bundle);

        addToBackStack(getFragmentManager(), frag,
                R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);


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

    //navigate to enter otp screen
    private void sendToOtpScreen(JSONObject data) {
        if (data != null) {
            Bundle b = getArguments();
            if (b == null) {
                b = new Bundle();
            }
            String input="";
            if(!AppUtil.isStringEmpty(data.optString("phone"))){
                input=data.optString("phone");
            }else{
                input=data.optString("email");
            }
            b.putString("otp_id", data.optString("otp_id"));
            b.putString("msg", data.optString("msg"));
            b.putString("resend_otp_time", data.optString("resend_otp_time"));
            b.putString("user_input", input);
            b.putString("type",data.optString("type"));

            VerifyMobileNoFragment fragment = VerifyMobileNoFragment.newInstance(b, mCallback);
            addToBackStack(getFragmentManager(), fragment,
                    R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        }
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_sign_up),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
