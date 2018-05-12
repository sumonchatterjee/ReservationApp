package com.dineout.book.fragment.myaccount;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;

public class DineoutPlusCardFragment extends YouPageWrapperFragment {

    private final int REQUEST_CODE_GET_DINER_PROFILE = 101;

    private CoordinatorLayout snackbarDoPlusCardPosition;
    private TextView mDOPlusName, mDOPlusCard, mDOPlusValidity;
    private View mDOPlusContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dineout_plus_card, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackScreenName(getString(R.string.countly_do_plus));
        trackEventForCountlyAndGA(getString(R.string.countly_do_plus),getString(R.string.d_doplus_view),getString(R.string.d_doplus_view),hMap);

        setToolbarTitle(getString(R.string.title_dineout_plus_card));

        // Initialize View
        initializeView();

        // Take API Hit
        onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
    }

    // Initialize View
    private void initializeView() {

        View mDOPlusCardContainer = getView().findViewById(R.id.dineout_plus_card_container);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            mDOPlusCardContainer.setBackground(
                    new BitmapDrawable(getResources(),
                            AppUtil.getRoundedCorner(
                                    BitmapFactory.decodeResource(getResources(),
                                            R.drawable.img_dineout_plus_card_bg), 10)));
        } else {
            mDOPlusCardContainer.setBackgroundResource(R.drawable.img_dineout_plus_card_bg);
        }

        mDOPlusContainer = getView().findViewById(R.id.dineout_plus_container);
        mDOPlusContainer.setVisibility(View.GONE);

        mDOPlusName = (TextView) getView().findViewById(R.id.dineout_plus_member_name);
        mDOPlusCard = (TextView) getView().findViewById(R.id.dineout_plus_member_card_number);
        mDOPlusValidity = (TextView) getView().findViewById(R.id.dineout_plus_member_validity);

        snackbarDoPlusCardPosition = (CoordinatorLayout) getView().findViewById(R.id.snackbarDoPlusCardPosition);
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);

        if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            // Ask User to Login
            askUserToLogin();

        } else {
            initializeData();
        }
    }

    private void askUserToLogin() {
        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    // Initialize Data
    void initializeData() {
        // Show Loading Dialog
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(REQUEST_CODE_GET_DINER_PROFILE, AppConstant.URL_GET_DINER_PROFILE,
                null, this, this, false);
    }



    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_GET_DINER_PROFILE) {
            if (responseObject != null) {
                if (responseObject.optBoolean("status")) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");

                    if (outputParams != null) {
                        JSONObject dinerData = outputParams.optJSONObject("data");

                        // Save Diner Details
                        if (dinerData != null) {
                            DOPreferences.saveDinerCredentials(getActivity(), dinerData);
                        }

                        // Set Visibility of Card
                        mDOPlusContainer.setVisibility(View.VISIBLE);

                        // Set Diner Name
                        mDOPlusName.setText(DOPreferences.getDinerFirstName(getActivity()) + " " +
                                DOPreferences.getDinerLastName(getActivity()));

                        // Set Card Number
                        JSONObject dinerCarbObj = dinerData.optJSONObject("doplus_card");
                        if (dinerCarbObj != null) {
                            String cardNum = dinerCarbObj.optString("card_number");
                            if (!TextUtils.isEmpty(cardNum))
                                mDOPlusCard.setText(cardNum);
                        }

                        if (dinerData.optJSONObject("doplus_card") != null) {
                            // Set Card Validity
                            mDOPlusValidity.setText(AppUtil.getCardExpiry(
                                    dinerData.optJSONObject("doplus_card").optString("expiry_date")));
                        }
                    }
                } else if (responseObject.optJSONObject("res_auth") != null &&
                        !responseObject.optJSONObject("res_auth").optBoolean("status")) {

                    // Ask User to Login
                    askUserToLogin();

                } else {
                    // Show Message
                    UiUtil.showSnackbar(snackbarDoPlusCardPosition,
                            getString(R.string.text_unable_fetch_details), 0);
                }
            } else {
                // Show Error Message
                UiUtil.showSnackbar(snackbarDoPlusCardPosition,
                        getString(R.string.text_general_error_message), 0);
            }
        }
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
        if (getActivity() != null) {
            initializeData();
        }
    }
}
