package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;


public class ResetPasswordFragment extends LoginFlowBaseFragment implements View.OnClickListener {

    private EditText newPassword;
    private EditText confirmPassword;
    private EditText resetCode;
    protected final int RESET_PASSWORD_API = 0x625;

    public static ResetPasswordFragment newInstance(Bundle bundle) {

        ResetPasswordFragment fragment = new ResetPasswordFragment();
        if (bundle != null)
            fragment.setArguments(bundle);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trackScreenName(getString(R.string.countly_reset_password));

        setToolbarTitle("Reset Password");
        initializeViews();
        AppUtil.hideKeyboard(getActivity());

//        AppUtil.showKeyBoard(getActivity());
    }

    private void initializeViews() {
        if(getView()!=null) {
            newPassword = (EditText) getView().findViewById(R.id.new_password_edtxt);
            confirmPassword = (EditText) getView().findViewById(R.id.confirm_password_edtxt);
            resetCode = (EditText) getView().findViewById(R.id.reset_code_edtxt);
            (getView().findViewById(R.id.send_code_btn)).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_code_btn:
                // tracking
//                trackEventGA(getString(R.string.ga_new_login_category_name),
//                        getString(R.string.ga_new_login_reset_password_screen_reset_action),
//                        getString(R.string.ga_new_login_reset_password_screen_label));

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA(getString(R.string.countly_reset_password),"ResetPasswordClick","ResetPasswordClick",hMap);

                processReset();
                break;
        }
    }

    private void processReset() {

        showLoader();
        String otpId="";
        if(getArguments()!=null){
            otpId=getArguments().getString("otp_id");
        }


        getNetworkManager().stringRequestPost(RESET_PASSWORD_API, AppConstant.URL_RESET_PASSWORD,
                ApiParams.getRestPasswordParams(newPassword.getText().toString(), confirmPassword.getText().toString(), otpId, resetCode.getText().toString()), this, this, false);
    }

    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        try {
            if (responseObject != null) {
                JSONObject responseObj = new JSONObject(responseObject);
                if (responseObj.optBoolean("status")) {
                    JSONObject outParam=responseObj.optJSONObject("output_params");
                    if(outParam!=null){
                        JSONObject data= outParam.optJSONObject("data");
                        if(data!=null){
                            String msg=data.optString("msg");
                            if(!AppUtil.isStringEmpty(msg) && getContext()!=null){
                                UiUtil.showToastMessage(getContext(),msg);
                            }
                            popBackStack(getFragmentManager());
                        }
                    }


                } else {
                    String errorMsg=responseObj.optString("error_msg");
                    if(AppUtil.isStringEmpty(errorMsg)){
                        errorMsg=getResources().getString(R.string.login_error_message);
                    }
                    hideKeyboardAndShowToast(getActivity(),  errorMsg);
                }
            }

        } catch (Exception e) {
            hideKeyboardAndShowToast(getActivity(),  getResources().getString(R.string.login_error_message));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppUtil.hideKeyboard(getActivity());
    }


    @Override
    public void handleNavigation() {

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_reset_password),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
