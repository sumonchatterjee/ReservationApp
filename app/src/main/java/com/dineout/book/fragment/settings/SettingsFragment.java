package com.dineout.book.fragment.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;

public class SettingsFragment extends MasterDOFragment implements View.OnClickListener {

    private TextView mTNC;
    private TextView mPrivacyPolicy;
    private TextView mFeedback;
    private TextView mRateApp;
    private TextView mAbout;
    //private TextView mAccessibilitySetting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //trackScreenToGA(getString(R.string.ga_screen_settings));
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackScreenName(getString(R.string.countly_settings));
        trackEventForCountlyAndGA(getString(R.string.countly_settings),getString(R.string.d_settings_view),getString(R.string.d_settings_view),hMap);
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle("Settings");

        // Find View by Ids
        findViewByIds();
    }

    private void findViewByIds() {

        mTNC = (TextView) getView().findViewById(R.id.tnc);
        mTNC.setOnClickListener(this);
        mPrivacyPolicy = (TextView) getView().findViewById(R.id.privacy_policy);
        mPrivacyPolicy.setOnClickListener(this);
        mFeedback = (TextView) getView().findViewById(R.id.submit_feedback);
        mFeedback.setOnClickListener(this);
        mRateApp = (TextView) getView().findViewById(R.id.rate_app);
        mRateApp.setOnClickListener(this);
        mAbout = (TextView) getView().findViewById(R.id.about);
        mAbout.setOnClickListener(this);
//        mAccessibilitySetting = (TextView) getView().findViewById(R.id.accessibility_setting);
//        mAccessibilitySetting.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        WebViewFragment webViewFragment = new WebViewFragment();
        Bundle bundle = new Bundle();
        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());

        switch (id) {
            case R.id.tnc:

                trackEventForCountlyAndGA("D_Settings","TermsAndConditionClick","TermsAndConditionClick",hMap);

                bundle.putString("url", AppConstant.DINEOUT_TNC);
                bundle.putString("title", "Terms & Conditions");
                webViewFragment.setArguments(bundle);
                addToBackStack(getActivity(), webViewFragment);
                break;

            case R.id.privacy_policy:

                trackEventForCountlyAndGA("D_Settings","PrivacyPolicyClick","PrivacyPolicyClick",hMap);

                bundle.putString("url", AppConstant.DINEOUT_PRIVACY_POLICY);
                bundle.putString("title", "Privacy Policy");
                webViewFragment.setArguments(bundle);
                addToBackStack(getActivity(), webViewFragment);

                break;
            case R.id.submit_feedback:

                //Track Event
                trackEventForCountlyAndGA("D_Settings","YourFeedbackClick","YourFeedbackClick",hMap);
                AppUtil.sendMail(getActivity());

                break;
            case R.id.rate_app:
                //Track Event
//                trackEventGA(getString(R.string.ga_screen_settings),
//                        getString(R.string.ga_action_rate_our_app), null);

                // tracking
                try {
                    String categoryName = getString(R.string.ga_rnr_category_setting);
                    String actionName = getString(R.string.ga_rnr_action_rate_our_app_click);
                    AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, null);
                } catch (Exception e) {
                    // Exception
                }

                addToBackStack(getActivity(), new AppRatingFragment());
//                MasterDOFragment.showFragment(getActivity().getSupportFragmentManager(), new RatingDialog());

                break;
            case R.id.about:

                //Track Event
                trackEventForCountlyAndGA("D_Settings","AboutDineoutClick","AboutDineoutClick",hMap);
                AppUtil.sendMail(getActivity());

                addToBackStack(getActivity().getSupportFragmentManager(), new AboutFragment());
                break;

//            case R.id.accessibility_setting:
//                addToBackStack(getActivity().getSupportFragmentManager(), new AccessibilitySettingFragment());
//                break;
        }
    }


}
