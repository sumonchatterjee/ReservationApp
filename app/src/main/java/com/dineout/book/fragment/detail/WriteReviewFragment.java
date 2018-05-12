package com.dineout.book.fragment.detail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.interfaces.DialogListener;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.login.LoginFlowBaseFragment;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WriteReviewFragment extends MasterDOJSONReqFragment
        implements View.OnClickListener, UserAuthenticationController.LoginFlowCompleteCallbacks {

    public static final String BOOKING_ID = "b_id";
    public static final String RESTAURANT_NAME = "rest_name";
    public static final String RESTAURANT_ID = "resId";
    protected TextView mMessage, mDone;
    private CoordinatorLayout snackbarRatenReviewPosition;
    private RatingBar mRatingSeekBar;
    private EditText mReviewText;
    private String mRestName, mResId = "-1";
    private ProgressDialog progress;
    private String mBookingId = "-1";
    private int reviewId;
    private int REQUEST_CODE_RESTAURANT_DETAILS = 101;
    private int REQUEST_CODE_SUBMIT_BOOKING_REVIEW = 102;

    private WriteReviewListener reviewListener;

    public void setWriteReviewListener(WriteReviewListener reviewListener) {
        this.reviewListener = reviewListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenToGA(getString(R.string.ga_screen_write_review));

        Bundle bundle = getArguments();

        if (bundle != null) {
            mRestName = bundle.getString(RESTAURANT_NAME);
            mResId = bundle.getString(RESTAURANT_ID);
            mBookingId = bundle.getString(BOOKING_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.rate_and_review_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeView();

        initializeData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                popBackStack(getActivity().getSupportFragmentManager());
                break;

            case R.id.done:

                if (isRatingDone()) {
                    setUserRating();
                }
                break;
        }
    }

    protected boolean isRatingDone() {
        AppUtil.hideKeyboard(mReviewText, getActivity());
        if (mRatingSeekBar.getRating() > 0) {
            mDone.setEnabled(true);
            return true;
        } else {
            UiUtil.showSnackbar(snackbarRatenReviewPosition, getString(R.string.review_rating_error), R.color.snackbar_error_background_color);
            return false;
        }
    }

    protected void initializeView() {
        snackbarRatenReviewPosition = (CoordinatorLayout) getView().findViewById(R.id.snackbarRatenReviewPosition);
        mRatingSeekBar = (RatingBar) getView().findViewById(R.id.sb_rating);
        mRatingSeekBar.setFocusable(true);
        mRatingSeekBar.setFocusableInTouchMode(true);
        mReviewText = (EditText) getView().findViewById(R.id.et_review);

        ImageView mBack = (ImageView) getView().findViewById(R.id.back);
        mDone = (TextView) getView().findViewById(R.id.done);
        mMessage = (TextView) getView().findViewById(R.id.tv_rr_title);
        mBack.setColorFilter(getResources().getColor(R.color.app_primary_shade_one));

        mBack.setOnClickListener(this);
        mDone.setOnClickListener(this);

        mReviewText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (mRatingSeekBar.getRating() > 0) {
                        setUserRating();
                    } else {
                        UiUtil.showSnackbar(snackbarRatenReviewPosition, getString(R.string.review_rating_error), R.color.snackbar_error_background_color);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    protected void initializeData() {
        String bookingId = mBookingId;

        mReviewText.setHint(R.string.rate_review_head_title);
        mMessage.setText(String.format(getResources().getString(R.string.rating_info_header), mRestName));

        presetReview();

        setup_radius_seekbar();

        if (bookingId == null || bookingId.equalsIgnoreCase("-1")) {
            getBookingIdFromRestDetails();
        }
    }

    private void presetReview() {
        // Get Bundle
        Bundle bundle = getArguments();

        if (bundle != null) {
            // Get Review Id
            reviewId = bundle.getInt(AppConstant.BUNDLE_REVIEW_ID, 0);
            mBookingId = bundle.getString(AppConstant.BUNDLE_BOOKING_ID);
            // Get Rating
            int rating = bundle.getInt(AppConstant.BUNDLE_REVIEW_RATING, 0);
            if (rating > 0) {
                mRatingSeekBar.setRating(Integer.valueOf(rating).floatValue());
            }

            // Get Review Text
            String review = bundle.getString(AppConstant.BUNDLE_REVIEW_TEXT, "");
            if (!AppUtil.isStringEmpty(review)) {
                mReviewText.setText(review);
            }
        }
    }

    void setup_radius_seekbar() {
        mRatingSeekBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating > 0) {
                    mDone.setEnabled(true);
                }
            }
        });
    }

    private void getBookingIdFromRestDetails() {
        String mRestId = getArguments().getString("resId");
        showLoader();

        getNetworkManager().jsonRequestGet(REQUEST_CODE_RESTAURANT_DETAILS, AppConstant.URL_REST_DETAILS,
                ApiParams.getRestaurantDetailsParams(DOPreferences.getDinerId
                        (getContext()), mRestId, null, null), this, null, false);
    }

    private void setUserRating() {
        String ratingVal = mRatingSeekBar.getRating() + "";
        String reviewText = mReviewText.getText().toString();

        progress = ProgressDialog.show(getActivity(), "Please wait",
                "Submitting your feedback...", true);

        if (!AppUtil.hasNetworkConnection(getActivity())) {
            progress.dismiss();
            mDone.setEnabled(true);
            return;
        }

        showLoader();

        getNetworkManager().stringRequestPost(REQUEST_CODE_SUBMIT_BOOKING_REVIEW, AppConstant.URL_SUBMIT_BOOKING_REVIEW,
                ApiParams.getSubmitBookingReviewParams(DOPreferences.getDinerId(getContext()),
                        getArguments().getString("b_id"), mBookingId, ratingVal, reviewText.trim(), mResId, Integer.toString(reviewId)),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        try {
                            handleSubmitBookingReviewResponse(new JSONObject(responseObject));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                this, false);
    }

    private void handleSubmitBookingReviewResponse(JSONObject responseObject) {
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {
                recordRestaurantReviewedEvent();

                JSONObject params = responseObject.optJSONObject("output_params");
                if (params != null) {

                    JSONObject data = params.optJSONObject("data");
                    trackAdTechEvent("ua", "Review-Posted");
                    //completeAdTechSesion();
                    if (data != null && !TextUtils.isEmpty(data.optString("msg"))) {
                        showSuccessDialog(responseObject.optJSONObject("output_params").optJSONObject("data").optString("msg"));
                    } else {

                        Toast.makeText(getContext(), "Rating submitted successfully", Toast.LENGTH_LONG).show();

                        if (reviewListener != null) {
                            reviewListener.onReviewSubmit();
                        }

                        popBackStack(getActivity().getSupportFragmentManager());
                    }
                }

                mDone.setEnabled(true);

            } else {
                if (responseObject.optString("error_type").contains("duplicate")) {
                    UiUtil.showSnackbar(snackbarRatenReviewPosition, responseObject.optString("error_msg"), R.color.snackbar_error_background_color);
                } else if (!responseObject.optJSONObject("res_auth").optBoolean("status")) {
                    showLoginDialog();
                } else if (!TextUtils.isEmpty(responseObject.optString("error_msg"))) {
                    UiUtil.showSnackbar(snackbarRatenReviewPosition, responseObject.optString("error_msg"), R.color.snackbar_error_background_color);
                } else {
                    UiUtil.showSnackbar(snackbarRatenReviewPosition, getString(R.string.text_no_feedback_error), R.color.snackbar_error_background_color);
                }
            }
        }

        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAILS) {
            if (responseObject != null && responseObject.optBoolean("status")) {
                JSONObject outputParams = responseObject.optJSONObject("output_params");

                if (outputParams != null) {
                    JSONArray data = outputParams.optJSONArray("data");

                    if (data != null && data.length() > 0) {
                        mBookingId = ((JSONObject) data.opt(0)).optJSONObject(
                                ("leave_review")).optString("booking_id");

                        if (mBookingId.equalsIgnoreCase("") || null == mBookingId) {
                            mBookingId = "-1";
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        hideLoader();

        if (request.getIdentifier() == REQUEST_CODE_RESTAURANT_DETAILS) {
            getBookingIdFromRestDetails();

        } else if (request.getIdentifier() == REQUEST_CODE_SUBMIT_BOOKING_REVIEW) {
            // Dismiss Progress Dialog
            if (progress != null) {
                progress.dismiss();
            }

            mDone.setEnabled(true);

            // Show Error Message
            UiUtil.showSnackbar(snackbarRatenReviewPosition,
                    getString(R.string.text_no_feedback_error),
                    R.color.snackbar_error_background_color);
        }
    }

    private void showLoginDialog() {

        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    private void recordRestaurantReviewedEvent() {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("RestaurantId", mResId);
        props.put("RestaurantName", mRestName);
        props.put("RatingValue", mRatingSeekBar.getRating() + "");
        props.put("BookingId", mBookingId);

        trackEventQGraphApsalar(getString(R.string.push_label_restaurant_reviewed), props,true,true,false);

        // Track Event
        trackEventGA(getString(R.string.ga_screen_home),
                getString(R.string.ga_action_review_under_moderation),
                mRatingSeekBar.getRating() + "");
    }

    private void showSuccessDialog(String msg) {
        Bundle bundle = new Bundle();

        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                TextUtils.isEmpty(msg) ? getString(R.string.review_submitted_dialog) :
                        msg.replaceAll("#", "\n"));

        bundle.putString(AppConstant.BUNDLE_DIALOG_TITLE, getString(R.string.review_dialog_header));
        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OKAY");

        UiUtil.showCustomDialog(getActivity(), bundle, new DialogListener() {
            @Override
            public void onPositiveButtonClick(AlertDialog alertDialog) {
                alertDialog.dismiss();

                if (reviewListener != null) {
                    reviewListener.onReviewSubmit();
                }

                popBackStack(getActivity().getSupportFragmentManager());
            }

            @Override
            public void onNegativeButtonClick(AlertDialog alertDialog) {
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject object) {
        if (getActivity() != null) {
            setUserRating();
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        if (getActivity() != null && loginFlowCompleteFailureObject != null) {

            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                return;
            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
            if (!AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(getActivity().getApplicationContext(), cause);
            }
        }
    }


    public interface WriteReviewListener {
        void onReviewSubmit();
    }
}



