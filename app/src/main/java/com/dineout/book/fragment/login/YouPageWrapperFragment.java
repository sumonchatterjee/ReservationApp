package com.dineout.book.fragment.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.util.AppUtil;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONObject;

import java.util.HashMap;


public abstract class YouPageWrapperFragment extends AuthenticationWrapperJSONReqFragment {

    // initiate authentication flow
    public void initiateAuthenticationFlow(Bundle bundle) {
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(bundle, this);
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSuccessObject) {
        super.loginFlowCompleteSuccess(loginFlowCompleteSuccessObject);

        if (getActivity() != null &&  getActivity().getApplicationContext() != null) {
            if (getView() != null) {
                View loginView = getView().findViewById(R.id.login_screen);
                loginView.setVisibility(View.GONE);
            }
//            onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getActivity().getApplicationContext()));
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

//            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
//            // Show Error Message

        }
    }

    // Start initial login flow from here
    public void authenticateUser() {
        if (getActivity() != null) {
            if (TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()))) {
                if (getView() != null) {
                    View loginView = getView().findViewById(R.id.login_screen);
                    if (loginView != null) {
                        loginView.setVisibility(View.VISIBLE);

                        View button = loginView.findViewById(R.id.login_btn);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, YouPageWrapperFragment.this);

                                // tracking
//                                trackEventGA(getString(R.string.ga_new_login_category_name),
//                                        getString(R.string.ga_new_login_artwork_screen_action),
//                                        getString(R.string.ga_new_login_artwork_screen_label));

                                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                                trackEventForCountlyAndGA(getString(R.string.countly_login_artwork),getString(R.string.d_login_click),getString(R.string.d_login_click),hMap);
                            }
                        });
                    }
                }
            } else {
                if (getView() != null) {
                    View loginView = getView().findViewById(R.id.login_screen);
                    if (loginView != null) {
                        loginView.setVisibility(View.GONE);
                    }
                }
                onNetworkConnectionChanged(AppUtil.hasNetworkConnection(getContext().getApplicationContext()));
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppUtil.hideKeyboard(getActivity());
    }

    @Override
    public void LoginSessionExpiredNegativeClick() {
        super.LoginSessionExpiredNegativeClick();

        // authenticate the user
        authenticateUser();
    }
}
