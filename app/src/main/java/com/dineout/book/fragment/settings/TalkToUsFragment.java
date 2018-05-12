package com.dineout.book.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.freshdesk.hotline.Hotline;

import org.json.JSONObject;

import java.util.HashMap;

public class TalkToUsFragment extends YouPageWrapperFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.talk_to_us_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //trackScreenToGA(getString(R.string.ga_screen_talk_to_us));

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackScreenName(getString(R.string.countly_talk_to_us));
        trackEventForCountlyAndGA(getString(R.string.countly_talk_to_us),getString(R.string.d_talk_to_us_view),getString(R.string.d_talk_to_us_view),hMap);

        setToolbarTitle(R.string.ga_screen_talk_to_us);

        if (AppUtil.isStringEmpty(DOPreferences.getDinerId(getActivity().getApplicationContext()))) {
            authenticateUser();
        } else {
            initiateTalkToUs();
        }
    }

    private void initiateTalkToUs() {
        Hotline.showConversations(getActivity().getApplicationContext());
        popBackStack(getFragmentManager());
    }


    private void proceedLoginFailure() {
        //Show Error Message
        UiUtil.showToastMessage(getContext(), getString(R.string.text_general_error_message));

        popBackStack(getFragmentManager());
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject loginFlowCompleteSucessObject) {

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
            proceedLoginFailure();
        }
    }
}
