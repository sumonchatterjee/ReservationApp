package com.dineout.book.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.recycleradapters.RateNReviewAdapter;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.book.dialogs.RateNReviewThankYouDialog.THANK_YOU_MSG_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.IN_APP_RATING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RESTAURANT_NAME;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_NONE_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.RateNReviewCallbacks;
import static com.dineout.recycleradapters.util.RateNReviewUtil.TARGET_SCREEN_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.UploadBillRateNReviewCallback;
import static com.dineout.recycleradapters.util.RateNReviewUtil.getCurrentRatingValue;
import static com.dineout.recycleradapters.util.RateNReviewUtil.getDialogCancelableFlagStatus;
import static com.dineout.recycleradapters.util.RateNReviewUtil.storeAppRatingFlagToPref;
import static com.dineout.recycleradapters.util.RateNReviewUtil.updateViewState;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class RateNReviewDialog extends MasterDOStringReqFragment implements View.OnClickListener,
        RateNReviewAdapter.RateNReviewAdapterCallback, RateNReviewAdapter.TrackingCallback {
    private final static int REQUEST_CODE_APP_RATING = 101;
    private final static int REQUEST_CODE_SUBMIT_REVIEW = 102;

    private RateNReviewCallbacks mCallback;
    private UploadBillRateNReviewCallback mUploadBillCallback;
    private RateNReviewAdapter mRateNReviewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rate_n_review, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(new Float(0.85));
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);

        // set dialog width
        Rect displayRectangle = new Rect();
        Window window = getDialog().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        getDialog().getWindow().setLayout((int) (displayRectangle.width() * .88), WindowManager.LayoutParams.WRAP_CONTENT);


        ImageView crossIv = (ImageView) view.findViewById(R.id.cross_iv);
        crossIv.setOnClickListener(this);

        // set cross iv visibility status
        if (getArguments() != null) {
            boolean isVisible = getDialogCancelableFlagStatus(getArguments().getString(INFO_STRING));
            crossIv.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);

        mRateNReviewAdapter = new RateNReviewAdapter(getActivity());
        mRateNReviewAdapter.setRateNReviewAdapterCallback(this);
        mRateNReviewAdapter.setTrackingCallback(this);

        if (getArguments() != null) {
            JSONArray arr = RateNReviewUtil.prepareAdapterData(getArguments().getString(INFO_STRING));

            // submit button
            View submitView = view.findViewById(R.id.submit_tv);
            submitView.setTag(arr);
            submitView.setOnClickListener(this);

            // set adapter
            mRateNReviewAdapter.setJsonArray(arr);
            mRateNReviewAdapter.initMainData();

            // action according to rating value
            if (getCurrentRatingValue(arr) > 0) {
                mRateNReviewAdapter.actionAtNonZeroRating();
            } else {
                mRateNReviewAdapter.actionAtZeroRating();
            }

            rv.setAdapter(mRateNReviewAdapter);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cross_iv:
                getDialog().dismiss();

                if (mCallback != null) {
                    mCallback.onDialogDismiss();
                }
                break;

            case R.id.submit_tv:
                JSONObject obj = RateNReviewUtil.getJsonObject(getArguments().getString(INFO_STRING));
                if (IN_APP_RATING.equals(obj.optString(TARGET_SCREEN_KEY))) {
                    submitAppRatingApiCall((JSONArray) v.getTag());

                    if (RateNReviewUtil.getCurrentRatingValue((JSONArray) v.getTag())
                            >= DOPreferences.retrieveAppRatingThresholdValue(getActivity())) {
                        appRatingThresholdActions();
                    }

                } else {
                    submitRestaurantReviewApiCall((JSONArray) v.getTag());
                }
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().getAttributes().windowAnimations = R.style.RNRStyle;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void setRateNReviewCallback(RateNReviewCallbacks callback) {
        this.mCallback = callback;
    }

    public void setUploadBillRateNReviewCallback(UploadBillRateNReviewCallback callback) {
        this.mUploadBillCallback = callback;
    }

    @Override
    public void setSubmitButtonVisibility(boolean isEnabled) {
        if (getView() != null) {
            TextView submitTv = (TextView) getView().findViewById(R.id.submit_tv);

            // set enablity
            submitTv.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setSubmitButtonText() {
        if (getView() != null) {
            TextView submitTv = (TextView) getView().findViewById(R.id.submit_tv);
            int rating = RateNReviewUtil.getCurrentRatingValue((JSONArray) submitTv.getTag());
            String text = RateNReviewUtil.getSubmitButtonText(getArguments().getString(INFO_STRING), rating);
            if (TextUtils.isEmpty(text)) text = "Submit";

            // set text
            submitTv.setText(text);
        }
    }

    private void appRatingThresholdActions() {
        // store rating to preference
        int appRating = getCurrentRatingValue(mRateNReviewAdapter.getJsonArray());
        storeAppRatingFlagToPref(getActivity(), appRating);

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dineout.book")));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dineout.book")));
        }

        // dismiss the dialog
        getDialog().dismiss();

        // send dismiss callback
        if (mCallback != null) {
            mCallback.onReviewSubmission();
        }
    }

    private void submitAppRatingApiCall(JSONArray arr) {
        String ratingVal = String.valueOf(RateNReviewUtil.getCurrentRatingValue(arr));
        String reviewTags = RateNReviewUtil.getReviewTags(RateNReviewUtil.getCurrentRatingValue(arr), arr);
        String OSVersion = "";
        String deviceName = "";

        try {
            OSVersion = Build.VERSION.RELEASE;
            deviceName = Build.BRAND + " " + android.os.Build.MODEL;
        } catch (Exception e) {
            // exception
        }

        String errorMsg = "";
        if (Integer.valueOf(ratingVal) <= 0) {
            errorMsg = "Please select Rating";
        } else if (RateNReviewUtil.isValidateTags(getArguments().getString(INFO_STRING), Integer.valueOf(ratingVal))
                && TextUtils.isEmpty(reviewTags)) {
            errorMsg = "Please select what you liked or disliked";
        }

        if (TextUtils.isEmpty(errorMsg)) {
            showLoader();

            disableScreenClicks();

            sendAppRatingTracking(ratingVal);

            // send this data to api
            getNetworkManager().stringRequestPost(REQUEST_CODE_APP_RATING, AppConstant.RATING_FEEDBACK,
                    ApiParams.getAppRatingParams(reviewTags, ratingVal, OSVersion, deviceName), this, this, false);

        } else {
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void submitRestaurantReviewApiCall(JSONArray arr) {
        JSONObject obj = RateNReviewUtil.getJsonObject(getArguments().getString(INFO_STRING));
        String bId = obj.optString(BOOKING_ID);
        String restId = obj.optString(RESTAURANT_ID);
        String restName = obj.optString(RESTAURANT_NAME);
        String ratingVal = String.valueOf(RateNReviewUtil.getCurrentRatingValue(arr));
        String reviewDesc = RateNReviewUtil.getReviewText(arr);
        String reviewTags = RateNReviewUtil.getReviewTags(RateNReviewUtil.getCurrentRatingValue(arr), arr);
        String reviewId = obj.optString(REVIEW_ID);
        String action = obj.optString(REVIEW_ACTION, REVIEW_NONE_ACTION);

        String errorMsg = "";
        if (Integer.valueOf(ratingVal) <= 0) {
            errorMsg = "Please select Rating";
        } else if (RateNReviewUtil.isValidateTags(getArguments().getString(INFO_STRING), Integer.valueOf(ratingVal))
                && TextUtils.isEmpty(reviewTags)) {
            errorMsg = "Please select what you liked or disliked";
        }

        if (TextUtils.isEmpty(errorMsg)) {
            if (mUploadBillCallback != null) {
                JSONObject data = new JSONObject();
                try {
                    data.putOpt("b_id", bId);
                    data.putOpt("rating_food", ratingVal);
                    data.putOpt("rating_service", ratingVal);
                    data.putOpt("rating_ambience", ratingVal);
                    data.putOpt("rating_desc", reviewDesc);
                    data.putOpt("rest_id", restId);
                    data.putOpt("review_id", reviewId);
                    data.putOpt("tags", reviewTags);
                } catch (Exception e) {
                    // Exception
                }

                // send callback
                mUploadBillCallback.onReviewSubmitClick(data);

                // dismiss the dialog
                getDialog().dismiss();

                // tracking
                sendRestaurantRatingTracking(restId, restName, bId, ratingVal, reviewDesc);

                // send dismiss callback
                if (mCallback != null) {
                    mCallback.onDialogDismiss();
                }

            } else {
                showLoader();

                disableScreenClicks();

                sendRestaurantRatingTracking(restId, restName, bId, ratingVal, reviewDesc);

                getNetworkManager().stringRequestPost(REQUEST_CODE_SUBMIT_REVIEW, AppConstant.URL_SUBMIT_REVIEW,
                        ApiParams.getSubmitReviewParams(
                                DOPreferences.getDinerId(getContext()), bId, restId, ratingVal,
                                reviewDesc.trim(), reviewId, reviewTags, action), this, this, false);
            }
        } else {
            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResponse(Request<String> request, String responseString, Response<String> response) {
        if(getActivity() == null || getView() == null)
            return;

        hideLoader();

        enableScreenClicks();

        if (request.getIdentifier() == REQUEST_CODE_APP_RATING) {
            handleAppRatingResponse(responseString);

        } else if (request.getIdentifier() == REQUEST_CODE_SUBMIT_REVIEW) {
            handleRestaurantReviewResponse(responseString);
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        if(getActivity() == null || getView() == null)
            return;

        // hide loader
        hideLoader();

        // enabled clicks
        enableScreenClicks();

        // show toast
        if (!AppUtil.hasNetworkConnection(getActivity())) {
            Toast.makeText(getActivity(), getResources().getString(R.string.cb_no_internet),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAppRatingResponse(String responseString) {
        if (!TextUtils.isEmpty(responseString)) {
            try {
                JSONObject responseObject = new JSONObject(responseString);
                if (responseObject.optBoolean("status")) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");
                    if (outputParams != null) {
                        JSONObject dataObject = outputParams.optJSONObject("data");
                        if (dataObject != null) {
                            String msg = dataObject.optString("msg", "");

                            // dismiss the dialog
                            getDialog().dismiss();

                            // show thank you screen
                            showThankYouScreen(msg);

                            // store rating to preference
                            int appRating = getCurrentRatingValue(mRateNReviewAdapter.getJsonArray());
                            storeAppRatingFlagToPref(getActivity(), appRating);

                            // send callback for review submission
                            if (mCallback != null) {
                                mCallback.onReviewSubmission();
                            }
                        }
                    }
                } else {
                    String errMsg = responseObject.optString("error_msg", "");
                    if (!TextUtils.isEmpty(errMsg)) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }

                    String errorCode = responseObject.optString("error_code", "");
                    switch (errorCode) {
                        case LOGIN_SESSION_EXPIRE_CODE:
                            DOPreferences.deleteDinerCredentials(getActivity());

                            // send dismiss callback
                            if (mCallback != null) {
                                mCallback.onRNRError(responseObject);
                            }
                            break;
                    }

                    // dismiss the dialog
                    getDialog().dismiss();
                }
            } catch (Exception e) {
                // Exception
            }
        }
    }

    private void handleRestaurantReviewResponse(String responseString) {
        if (!TextUtils.isEmpty(responseString)) {
            try {
                JSONObject responseObject = new JSONObject(responseString);
                if (responseObject.optBoolean("status")) {
                    JSONObject outputParams = responseObject.optJSONObject("output_params");
                    if (outputParams != null) {
                        JSONObject dataObject = outputParams.optJSONObject("data");
                        if (dataObject != null) {
                            String msg = dataObject.optString("msg", "");

                            // dismiss the dialog
                            getDialog().dismiss();

                            // show thank you screen
                            showThankYouScreen(msg);

                            // send callback for review submission
                            if (mCallback != null) {
                                mCallback.onReviewSubmission();
                            }
                        }
                    }
                } else {
                    String errMsg = responseObject.optString("error_msg", "");
                    if (!TextUtils.isEmpty(errMsg)) {
                        Toast.makeText(getActivity(), errMsg, Toast.LENGTH_SHORT).show();
                    }

                    String errorCode = responseObject.optString("error_code", "");
                    switch (errorCode) {
                        case LOGIN_SESSION_EXPIRE_CODE:
                            DOPreferences.deleteDinerCredentials(getActivity());

                            // send dismiss callback
                            if (mCallback != null) {
                                mCallback.onRNRError(responseObject);
                            }
                            break;
                    }

                    // dismiss the dialog
                    getDialog().dismiss();
                }
            } catch (Exception e) {
                // Exception
            }
        }
    }

    private void showThankYouScreen(String msg) {
        RateNReviewThankYouDialog dialog = new RateNReviewThankYouDialog();

        Bundle bl = new Bundle();
        bl.putString(THANK_YOU_MSG_KEY, msg);
        dialog.setArguments(bl);
//        dialog.setCallback(mCallback);

        // Open Rate And Review Dialog
        showFragment(getActivity().getSupportFragmentManager(), dialog);
    }

    private void disableScreenClicks() {
        updateViewState(getView(), false);
    }

    private void enableScreenClicks() {
        updateViewState(getView(), true);
    }

    private void sendAppRatingTracking(String rating) {

        // star click tracking
        try {
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = getString(R.string.ga_rnr_action_app_rating_star_click);
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, rating);
        } catch (Exception e) {
            // Exception
        }

        // submit button tracking
        try {
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = getString(R.string.ga_rnr_action_app_rating_submit_click);
            String labelName = RateNReviewUtil.getSubmitButtonText(getArguments().getString(INFO_STRING), Integer.valueOf(rating));
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, labelName);
        } catch (Exception e) {
            // Exception
        }
    }

    private void sendRestaurantRatingTracking(String restId, String resName, String bID, String rating, String reviewText) {

        // star click tracking
        try {
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = getString(R.string.ga_rnr_action_restaurant_review_star_click);
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, rating);

            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",categoryName);
                hMap.put("action",actionName);
                hMap.put("label",rating);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);
        } catch (Exception e) {
            // Exception
        }

        // review text/type tracking
        try {
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = getString(R.string.ga_rnr_action_restaurant_review_type);
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, reviewText);
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",categoryName);
                hMap.put("label",reviewText);
                hMap.put("action",actionName);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);
        } catch (Exception e) {
            // Exception
        }

        // submit button tracking
        try {
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = getString(R.string.ga_rnr_action_restaurant_review_submit_click);
            String labelName = "";
            if (!TextUtils.isEmpty(resName) && !TextUtils.isEmpty(restId)) {
                labelName = resName + "_" + restId;
            } else {
                labelName = bID;
            }

            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, labelName);
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",categoryName);
                hMap.put("action",actionName);
                hMap.put("label",labelName);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);
        } catch (Exception e) {
            // Exception
        }
    }



    @Override
    public void tagSelectionCallback(String tagName) {
        // tag selection tracking
        try {
            JSONObject obj = RateNReviewUtil.getJsonObject(getArguments().getString(INFO_STRING));
            String categoryName = (getArguments() != null) ? getArguments().getString(GA_TRACKING_CATEGORY_NAME_KEY, "") : "";
            String actionName = IN_APP_RATING.equals(obj.optString(TARGET_SCREEN_KEY))
                    ? getString(R.string.ga_rnr_action_app_rating_tag_select)
                    : getString(R.string.ga_rnr_action_restaurant_review_tag_select);
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA(categoryName, actionName, tagName);
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            if(hMap!=null){
                hMap.put("category",categoryName);
                hMap.put("action",actionName);
                hMap.put("label",tagName);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(actionName,hMap);

        } catch (Exception e) {
            // Exception
        }
    }
}
