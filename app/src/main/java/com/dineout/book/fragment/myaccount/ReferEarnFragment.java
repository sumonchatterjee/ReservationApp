package com.dineout.book.fragment.myaccount;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.book.dialogs.GenericShareDialog;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class ReferEarnFragment extends YouPageWrapperFragment
        implements View.OnClickListener, View.OnLongClickListener {

    private TextView mDORefer, mDOPlusRefer;
    private TextView mDOReferTitle, mDoPlusReferTitle, mReferCode,
            mDoReferAmount, mDOHeaderTitle, mDoPlusReferAmount, mKnowMoreDO, mKnowMoreDoPlus;

    public static ReferEarnFragment newInstance() {
        ReferEarnFragment fragment = new ReferEarnFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //track screen
        trackScreenName(getString(R.string.countly_refer_n_earn));
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_refer_n_earn),getString(R.string.d_refer_and_viewed),getString(R.string.d_refer_and_viewed),hMap);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refer_earn, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Title
        setToolbarTitle(getString(R.string.title_refer_earn));

        // Find View by Ids
        findViewByIDS();

        authenticateUser();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        callReferApi();


    }

    private void findViewByIDS() {
        mDOHeaderTitle = (TextView) getView().findViewById(R.id.refer_head_title);
        mReferCode = (TextView) getView().findViewById(R.id.referral_code);
        mDOReferTitle = (TextView) getView().findViewById(R.id.refer_do_title);
        mDoReferAmount = (TextView) getView().findViewById(R.id.refer_do_amount);
        mDoPlusReferTitle = (TextView) getView().findViewById(R.id.refer_do_plus_title);
        mDoPlusReferAmount = (TextView) getView().findViewById(R.id.refer_do_plus_amount);
        mDORefer = (TextView) getView().findViewById(R.id.refer_do_button);
        mDORefer.setOnClickListener(this);
        mDOPlusRefer = (TextView) getView().findViewById(R.id.refer_do_plus_btn);
        mDOPlusRefer.setOnClickListener(this);
        mKnowMoreDO = (TextView) getView().findViewById(R.id.refer_know_do);
        mKnowMoreDoPlus = (TextView) getView().findViewById(R.id.refer_know_do_plus);

        mReferCode.setOnLongClickListener(this);
    }

    private void callReferApi() {

        showLoader();

        getNetworkManager().jsonRequestGet(101, AppConstant.URL_REFER_EARN,
                ApiParams.getRewardSchemesDataParams(DOPreferences.getDinerId(getContext())),
                this, this, true);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getView() == null || getActivity() == null)
            return;

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {
                JSONObject outputParams = responseObject.optJSONObject("output_params");
                if (outputParams != null) {
                    JSONObject data = outputParams.optJSONObject("data");
                    if (getView() != null) {
                        initializeView(data.optJSONArray("section"), data.optJSONObject("section_data"));
                    }
                }
            } else if (!responseObject.optBoolean("status")) {
//                handleApiResponse(responseObject);

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
    }

    private void initializeView(JSONArray array, JSONObject data) {
        // Show View
        getView().findViewById(R.id.linear_layout_refer_container).setVisibility(View.VISIBLE);

        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {
                JSONObject section = array.optJSONObject(i);
                int type = section.optInt("section_type");

                switch (type) {
                    case 1:
                        initializeHeader(data.optJSONObject(section.optString("section_key")));
                        break;

                    case 2:
                        initializeDOCell(data.optJSONObject(section.optString("section_key")));
                        break;

                    case 3:
                        initializeDOPlusCell(data.optJSONObject(section.optString("section_key")));
                        break;
                }
            }
        }
    }

    private void initializeHeader(JSONObject data) {
        if (data != null) {
            mReferCode.setText(data.optString("ref_code"));
            mDOHeaderTitle.setText(data.optString("title"));
        }
    }

    private void initializeDOCell(JSONObject data) {
        if (data != null) {
            mDOReferTitle.setText(data.optString("title"));
            mDoReferAmount.setText(data.optString("earn_txt", "").replaceAll("RS_SYMBOL",
                    getResources().getString(R.string.rupee_symbol)));
            mDORefer.setText(data.optString("btn_txt"));
            mDORefer.setTag(data.optJSONObject("share_message").toString());
            mKnowMoreDO.setText(data.optString("know_more_txt"));

            if (TextUtils.isEmpty(data.optString("know_more_link"))) {
                mKnowMoreDO.setVisibility(View.INVISIBLE);
            } else {
                mKnowMoreDO.setOnClickListener(new KnowMoreListener(data.optString("know_more_link_type"),
                        data.optString("know_more_link")));
            }
        }
    }

    private void initializeDOPlusCell(JSONObject data) {
        if (data != null) {
            mDoPlusReferTitle.setText(data.optString("title"));
            mDoPlusReferAmount.setText(data.optString("earn_txt").replaceAll("RS_SYMBOL",
                    getResources().getString(R.string.rupee_symbol)));
            mDOPlusRefer.setText(data.optString("btn_txt"));
            mDOPlusRefer.setTag(data.optJSONObject("share_message").toString());
            mKnowMoreDoPlus.setText(data.optString("know_more_txt"));

            if (TextUtils.isEmpty(data.optString("know_more_link"))) {
                mKnowMoreDoPlus.setVisibility(View.INVISIBLE);
            } else {
                mKnowMoreDoPlus.setOnClickListener(new KnowMoreListener(data.optString("know_more_link_type"),
                        data.optString("know_more_link")));
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.refer_do_button && view.getTag() != null) {


            // track for countly and ga
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_refer_n_earn),
                     getString(R.string.d_refer_now_clicked),getString(R.string.d_refer_now_clicked),hMap);



        } else {

            // Track Event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_refer_n_earn),
                    getString(R.string.d_refer_dop),getString(R.string.d_refer_dop),hMap);

            //track for qgraph, mixpanel and branch
            trackEventQGraphApsalar(getString(R.string.d_refer_dop),new HashMap<String, Object>(),true,false,false);

        }

        GenericShareDialog shareDialog = GenericShareDialog.getInstance((String) view.getTag());
        shareDialog.show(getActivity().getSupportFragmentManager(), ReferEarnFragment.class.getSimpleName());
    }

    @Override
    public boolean onLongClick(View v) {
        copyCode(mReferCode.getText().toString());
        return true;
    }

    private void copyCode(String code) {
        UiUtil.showToastMessage(getContext(), "Referral code copied");

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(code);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().
                    getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", code);
            clipboard.setPrimaryClip(clip);
        }
    }




    private class KnowMoreListener implements View.OnClickListener {
        private String mType;
        private String mContent;

        public KnowMoreListener(String type, String content) {
            this.mType = type;
            this.mContent = content;
        }

        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.refer_know_do) {

                // Track Event
//                trackEventGA(getString(R.string.refer_n_earn),
//                        getString(R.string.ga_know_more_app), null);



            } else {

                // Track Event
//                trackEventGA(getString(R.string.refer_n_earn),
//                        getString(R.string.ga_know_more), null);

            }

            if (mType.equalsIgnoreCase("popup")) {

            } else if (mType.equalsIgnoreCase("web_view")) {

                WebViewFragment webViewFragment = new WebViewFragment();

                Bundle bundle = new Bundle();
                bundle.putString("title", ((TextView) view).getText().toString());
                bundle.putString("url", mContent);
                webViewFragment.setArguments(bundle);

                addToBackStack(getActivity().getSupportFragmentManager(), webViewFragment);
            }
        }
    }


    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_refer_n_earn),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }

}
