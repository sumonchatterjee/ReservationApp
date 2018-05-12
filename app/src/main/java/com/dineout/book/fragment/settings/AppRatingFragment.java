package com.dineout.book.fragment.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.book.R;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.fragment.login.YouPageWrapperFragment;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONObject;

import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RateNReviewCallbacks;

/**
 * Created by sawai.parihar on 02/04/17.
 */

public class AppRatingFragment extends YouPageWrapperFragment implements RateNReviewCallbacks {
    private final int GET_APP_RATING_INFO = 107;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_rating, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Title
        setToolbarTitle(getString(R.string.title_app_rating));

        authenticateUser();
    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        super.onNetworkConnectionChanged(connected);
        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(GET_APP_RATING_INFO, AppConstant.URL_GET_REVIEW_TAG,
                ApiParams.getAppRatingParams(ApiParams.APP_RATING), this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (request.getIdentifier() == GET_APP_RATING_INFO) {
            // Check Response
            if (responseObject != null && responseObject.optBoolean("status")) {
                // Get Output Params Object
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                if (outputParamsJsonObject != null) {
                    // Get Data Object
                    JSONObject data = outputParamsJsonObject.optJSONObject("data");

                    JSONObject object = new JSONObject();
                    try {
                        RateNReviewUtil.appendObject(object, data.optJSONObject("review_data"));
                        RateNReviewUtil.appendObject(object, data.optJSONObject("review_box"));
                        object.put(RateNReviewUtil.TARGET_SCREEN_KEY, RateNReviewUtil.IN_APP_RATING);
                    } catch (Exception e) {
                        // Exception
                    }

                    RateNReviewDialog RNRDialog = new RateNReviewDialog();
                    RNRDialog.setRateNReviewCallback(this);
                    Bundle bl = new Bundle();
                    bl.putString(INFO_STRING, object.toString());
                    bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_setting));
                    RNRDialog.setArguments(bl);

                    showFragment(getActivity().getSupportFragmentManager(), RNRDialog);
                }
            }
        }
    }

    @Override
    public void onReviewSubmission() {
        popBackStack(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onRNRError(JSONObject errorObject) {
        if (errorObject != null) {
            String errorCode = errorObject.optString("error_code", "");
            switch (errorCode) {
                case LOGIN_SESSION_EXPIRE_CODE:
                    popBackStack(getActivity().getSupportFragmentManager());
                    break;
            }
        }
    }

    @Override
    public void onDialogDismiss() {
        popBackStack(getActivity().getSupportFragmentManager());
    }
}
