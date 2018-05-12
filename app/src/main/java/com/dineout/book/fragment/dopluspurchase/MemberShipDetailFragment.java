package com.dineout.book.fragment.dopluspurchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.controller.ValidationManager;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.home.DOPlusFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperStringReqFragment;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.promovoucher.PromoCodeFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MemberShipDetailFragment extends AuthenticationWrapperStringReqFragment implements View.OnClickListener {

    private final int REQUEST_CODE_MEMBER = 0x104;
    private EditText mName;
    private EditText mPhone;
    private EditText mEmail;
    private EditText mAddress1, mAddress2;
    private EditText mPin;
    private Spinner mCityArray;
    private String mCityName;
    private AdapterView.OnItemSelectedListener mCityListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position > 0)
                mCityName = (String) mCityArray.getAdapter().getItem(position);
            else
                mCityName = null;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public static MemberShipDetailFragment newInstance(Bundle bundle) {
        MemberShipDetailFragment fragment = new MemberShipDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dineout_plus_membership_form, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getActivity().getResources().getString(R.string.title_membership_detail));
        trackScreenToGA(getString(R.string.ga_screen_do_plus_membership_details));
        findViewByIDs();
        populateCitySpinner();
        mCityArray.setSelection(0);
        mCityArray.setOnItemSelectedListener(mCityListener);
    }


    private void findViewByIDs() {

        mName = (EditText) getView().findViewById(R.id.member_detail_name);
        mPhone = (EditText) getView().findViewById(R.id.member_detail_phone);
        mEmail = (EditText) getView().findViewById(R.id.member_detail_email);
        mAddress1 = (EditText) getView().findViewById(R.id.member_detail_address_one);
        mAddress2 = (EditText) getView().findViewById(R.id.member_detail_address_two);
        mCityArray = (Spinner) getView().findViewById(R.id.city_spinner);
        mPin = (EditText) getView().findViewById(R.id.member_detail_pincode);
        prefetchUserInfo();
        getView().findViewById(R.id.btn_do_member_signup).setOnClickListener(this);
    }


    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        registerMember();
    }

    private void registerMember() {
        AppUtil.hideKeyboard(getActivity());
        showLoader();
        Map<String, String> param = prepareMembershipParameters();
        getNetworkManager().stringRequestPost(REQUEST_CODE_MEMBER, AppConstant.URL_BILL_PAYMENT,
                param, this, null, false);

    }

    private Map<String, String> prepareMembershipParameters() {

        Map<String, String> param = new HashMap<>();

        param.put(AppConstant.DINER_ID, DOPreferences.getDinerId(getActivity().getApplicationContext()));
        param.put(AppConstant.PARAM_DO_MEMBER_REG_ADDRESS1, mAddress1.getText().toString().trim());
        param.put(AppConstant.PARAM_DO_MEMBER_REG_ADDRESS2, (TextUtils.isEmpty(mAddress2.getText().toString().trim()) ? "" : mAddress2.getText().toString().trim()));
        param.put(AppConstant.PARAM_DO_MEMBER_REG_CITY, mCityName);
        param.put(AppConstant.PARAM_DO_MEMBER_REG_PIN, mPin.getText().toString().trim());
        param.put("diner_phone", mPhone.getText().toString().trim());
        param.put(AppConstant.PARAM_DO_MEMBER_REG_PRODUCT_ID, "1");
        param.put("diner_name", mName.getText().toString().trim());
        param.put("obj_type", "doplus");
        param.put("obj_id", "1");
        param.put("action", "get_bill_amount");
        param.put(AppConstant.PARAM_TEMP, "1");

        return param;
    }

    private void populateCitySpinner() {
        try {
            JSONArray array = new JSONArray(getArguments().getString(DOPlusFragment.CITY_ARRAY_KEY));
            String[] city = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                city[i] = array.optJSONObject(i).optString("value");
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.drop_down_item_do_plus, city);
            dataAdapter.setDropDownViewResource(R.layout.drop_down_item_do_plus);
            mCityArray.setAdapter(dataAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateUserInput() {

        boolean areInputFieldsValid = true;

        if (areInputFieldsValid) {
            String nameErrorMessage = ValidationManager.validateName(getContext(), mName.getText().toString());

            if (!AppUtil.isStringEmpty(nameErrorMessage) && mName.isEnabled()) {
                areInputFieldsValid = false;

                UiUtil.showToastMessage(getActivity().getApplicationContext(), nameErrorMessage);
            }
        }

        if (areInputFieldsValid) {
            String phoneNumberErrorMessage = ValidationManager.validatePhoneNumber(getContext(), mPhone.getText().toString());

            if (!AppUtil.isStringEmpty(phoneNumberErrorMessage) && mPhone.isEnabled()) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), phoneNumberErrorMessage);
            }
        }

        if (areInputFieldsValid) {
            String emailErrorMessage = ValidationManager.validateEmail(getContext(), mEmail.getText().toString());

            if (!AppUtil.isStringEmpty(emailErrorMessage) && mEmail.isEnabled()) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), emailErrorMessage);
            }
        }

        if (areInputFieldsValid) {

            if (TextUtils.isEmpty(mAddress1.getText().toString().trim())) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please provide address line 1");
            }
        }

        if (areInputFieldsValid) {

            if (TextUtils.isEmpty(mCityName)) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please provide city");
            }
        }

        if (areInputFieldsValid) {

            if (TextUtils.isEmpty(mPin.getText().toString().trim())) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please provide pin code");
            }
        }

        return areInputFieldsValid;
    }

    /**
     * If user is login then fill name, email and
     * phone number of registered user
     */
    private void prefetchUserInfo() {
        String userEmail = DOPreferences.getDinerEmail(getActivity().getApplicationContext());

        if (!TextUtils.isEmpty(userEmail)) {
            String userFullName = (DOPreferences.getDinerFirstName(getActivity().getApplicationContext()) + " " +
                    DOPreferences.getDinerLastName(getActivity().getApplicationContext()));

            if (!TextUtils.isEmpty(userFullName)) {
                mName.setText(userFullName);
            }

            if (!TextUtils.isEmpty(DOPreferences.getDinerPhone(getActivity().getApplicationContext()))) {
                mPhone.setText(DOPreferences.getDinerPhone(getActivity().getApplicationContext()));
                mPhone.setEnabled(false);
            }

            if (!TextUtils.isEmpty(userEmail)) {
                mEmail.setText(userEmail);
                mEmail.setEnabled(false);
            }
        }
    }

    // Ask User to Login
    private void askUserToLogin() {
        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }



    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null) {
            return;
        }

        if (request.getIdentifier() == REQUEST_CODE_MEMBER) {
            if (responseObject != null) {
                JSONObject responseJsonObject = null;
                try {
                    responseJsonObject = new JSONObject(responseObject);
                } catch (Exception e) {
                    UiUtil.showToastMessage(getContext(), "Some error in registering a member.Please try again.");
                }

                if (responseJsonObject != null) {
                    if (responseJsonObject.optBoolean("status")) {
                        JSONObject outputParam = responseJsonObject.optJSONObject("output_params");

                        if (outputParam != null) {
                            JSONObject data = outputParam.optJSONObject("data");

                            Bundle bundle = new Bundle();
                            bundle.putString(PromoCodeFragment.AMOUNT_DATA, data.optString("bill_amount", "0"));
                            bundle.putString(PromoCodeFragment.WALLET_AMOUNT_DATA, data.optString("do_wallet_amt"));

                            PromoCodeFragment fragment = PromoCodeFragment.getInstance(bundle);

                            addToBackStack(getActivity(), fragment);
                        }
                    } else if (responseJsonObject.optJSONObject("res_auth") != null &&
                            !responseJsonObject.optJSONObject("res_auth").optBoolean("status")) {

                        // Ask User to Login
                        askUserToLogin();

                    } else {
                        // Show Message
                        UiUtil.showToastMessage(getActivity().getApplicationContext(),
                                getString(R.string.text_unable_fetch_details));
                    }
                } else {
                    // Show Message
                    UiUtil.showToastMessage(getActivity().getApplicationContext(),
                            getString(R.string.text_general_error_message));
                }
            } else {
                // Show Message
                UiUtil.showToastMessage(getActivity().getApplicationContext(),
                        getString(R.string.text_general_error_message));
            }
        }
    }

    @Override
    public void onClick(View v) {
        AppUtil.hideKeyboard(getActivity());
        if (validateUserInput()) {

            // Track Event
            trackEventGA(getString(R.string.ga_screen_do_plus_membership_details),
                    getString(R.string.ga_action_proceed_to_payments), null);

            registerMember();
        }
    }

    @Override
    public void handleNavigation() {
        popToHome(getActivity());
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject jsonObject) {
        if (getActivity() != null) {
            registerMember();
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        if (getActivity() != null && loginFlowCompleteFailureObject != null) {

            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                // Popup Back
                popBackStack(getActivity().getSupportFragmentManager());

                return;
            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
            // Show Error Message
            if (!AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), cause);
            }
        }
    }
}
