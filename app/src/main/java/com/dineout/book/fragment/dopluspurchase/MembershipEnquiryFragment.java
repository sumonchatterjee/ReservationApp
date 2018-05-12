package com.dineout.book.fragment.dopluspurchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.ValidationManager;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.home.DOPlusFragment;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MembershipEnquiryFragment extends MasterDOStringReqFragment implements
        OnClickListener {

    public static final String CITY_ARRAY = "city_array";
    public static final String TIME_ARRAY = "time_array";
    private final int REQUEST_CODE_QUERY = 0x103;
    private EditText mEmail;
    private EditText mPhone;
    private EditText mName;
    private Spinner mCityArray;
    private Spinner mTimeToCallArray;
    private String mCityName;
    private String mTimeToCall;

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

    private AdapterView.OnItemSelectedListener mTimeToCallListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position > 0)
                mTimeToCall = (String) mTimeToCallArray.getAdapter().getItem(position);
            else
                mTimeToCall = null;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    public static MembershipEnquiryFragment newInstance(Bundle bundle) {
        MembershipEnquiryFragment fragment = new MembershipEnquiryFragment();
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
        return inflater.inflate(R.layout.fragment_membership_enquiry_form, container, false);
    }

    private void findViewByIDs() {
        mCityArray = (Spinner) getView().findViewById(R.id.city_spinner);
        mTimeToCallArray = (Spinner) getView().findViewById(R.id.time_spinner);
        mEmail = (EditText) getView().findViewById(R.id.member_enquiry_email);
        mPhone = (EditText) getView().findViewById(R.id.member_enquiry_phone);
        mName = (EditText) getView().findViewById(R.id.member_enquiry_name);

        getView().findViewById(R.id.btn_do_member_enquiry).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setToolbarTitle(getString(R.string.title_enquiry));
        trackScreenToGA(getString(R.string.ga_screen_do_plus_membership_enquiry));

        findViewByIDs();

        initializeUI();
    }

    @Override
    public void onRemoveErrorView() {

    }

    private void initializeUI() {
        prefetchUserInfo();
        populateCitySpinner();
        populateTimeSpinner();
        mTimeToCallArray.setOnItemSelectedListener(mTimeToCallListener);
        mCityArray.setOnItemSelectedListener(mCityListener);
        mTimeToCallArray.setSelection(0);
        mCityArray.setSelection(0);
    }

    /**
     * If user is login then fill name,email and
     * phone number of registered user
     */
    private void prefetchUserInfo() {
        String userEmail = DOPreferences.getDinerEmail(getActivity().getApplicationContext());

        if (!TextUtils.isEmpty(userEmail)) {
            //Set Name
            String userFullName = (DOPreferences.getDinerFirstName(getActivity().getApplicationContext()) + " " +
                    DOPreferences.getDinerLastName(getActivity().getApplicationContext()));


            if (!TextUtils.isEmpty(userFullName)) {
                mName.setText(userFullName);
            }

            if (!TextUtils.isEmpty(DOPreferences.getDinerPhone(getActivity().getApplicationContext())
            )) {
                //Set Phone
                mPhone.setText(DOPreferences.getDinerPhone(getActivity().getApplicationContext()));
            }

            if (!TextUtils.isEmpty(userEmail)) {
                //Set Email Id
                mEmail.setText(userEmail);
            }
        }
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
            //set the view for the Drop down list
            dataAdapter.setDropDownViewResource(R.layout.drop_down_item_do_plus);
            mCityArray.setAdapter(dataAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void populateTimeSpinner() {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.drop_down_item_do_plus, getArguments().getStringArray(DOPlusFragment.CALL_TIME_ARRAY));
        //set the view for the Drop down list
        dataAdapter.setDropDownViewResource(R.layout.drop_down_item_do_plus);
        mTimeToCallArray.setAdapter(dataAdapter);
    }

    @Override
    public void onClick(View v) {
        trackEventGA(getString(R.string.ga_screen_do_plus_membership_enquiry),
                getString(R.string.ga_action_submit), null);

        if (validateUserInput())
            makeQuery();
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

            if (TextUtils.isEmpty(mCityName)) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please provide city");
            }
        }


        if (areInputFieldsValid) {

            if (TextUtils.isEmpty(mTimeToCall)) {
                areInputFieldsValid = false;
                UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please select time to call.");
            }
        }

        return areInputFieldsValid;
    }


    private void makeQuery() {

//        showLoadingDialog(false);
        Map<String, String> param = new HashMap<>();
        param.put("name", mName.getText().toString().trim());
        param.put("email", mEmail.getText().toString().trim());
        param.put("phone", mPhone.getText().toString().trim());
        param.put("city", mCityName);
        param.put("best_time", mTimeToCall);

        showLoader();
        getNetworkManager().stringRequestPost(REQUEST_CODE_QUERY, AppConstant.URL_REQUEST_INVITE, param,
                this
                , new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {
                        hideLoader();
                    }
                }, false);

    }


    private void handleAddInviteResponse(JSONObject responseObject) {
        hideLoader();
        if (responseObject.optBoolean("status")) {

            String content = responseObject.optString(AppConstant.LINE1) + "\n";
            JSONArray line2JsonArray = responseObject.optJSONArray(AppConstant.LINE2);

            if (line2JsonArray != null && line2JsonArray.length() > 0) {
                int line2ArraySize = line2JsonArray.length();
                for (int index = 0; index < line2ArraySize; index++) {
                    String string = line2JsonArray.optString(index);
                    if (!TextUtils.isEmpty(string)) {
                        content += "\n" + string;
                    }
                }
            }

            FragmentActivity activity = getActivity();
            activity.getSupportFragmentManager().popBackStackImmediate();
            EnquirySuccessFragment fragment = EnquirySuccessFragment.newInstance(responseObject.optString(AppConstant.WELCOME_MSG), content);
            addToBackStack(activity, fragment);

        } else {
            //Show Error Message
            Toast.makeText(getActivity(), responseObject.optString(AppConstant.ERROR_MESSAGE), Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {

        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_QUERY) {
            if (responseObject != null) {
                try {
                    JSONObject resp = new JSONObject(responseObject);
                    handleAddInviteResponse(resp);
                } catch (Exception e) {

                }
            }
        }
    }


    @Override
    public void handleNavigation() {
        popToHome(getActivity());
    }
}
