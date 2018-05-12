package com.dineout.book.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
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
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ReviewDialogView extends RelativeLayout implements Response.Listener<JSONObject>,
        Response.ErrorListener, UserAuthenticationController.LoginFlowCompleteCallbacks {

    Context mContext;
    private RatingBar mRatingSeekBar;
    private EditText mReviewText;
    private String mRestName;
    private ScrollView mScrollReview;
    private TextView mDone;
    private TextView mMessage;
    private String mBookingId = "-1";
    private TextView mLater;
    private Activity activity;
    private ProgressDialog progress;

    private DialogListener mReviewDialogListener = new DialogListener() {
        @Override
        public void onPositiveButtonClick(AlertDialog alertDialog) {
            alertDialog.dismiss();
            setVisibility(View.GONE);
        }

        @Override
        public void onNegativeButtonClick(AlertDialog alertDialog) {
            alertDialog.dismiss();
        }
    };

    public ReviewDialogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(attrs, defStyle);
    }

    public ReviewDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(attrs, 0);
    }

    public ReviewDialogView(Context context) {
        super(context);
        initViews(null, 0);
    }

    public void setActivityInstance(Activity activity) {
        this.activity = activity;
    }

    private void initViews(AttributeSet attrs, int defStyle) {

        LayoutInflater.from(getContext()).inflate(R.layout.review_dialog, this, true);

        if (attrs != null) {
            initAttributes(attrs, defStyle);
        }

        mScrollReview = (ScrollView) findViewById(R.id.in_rate_layout);
        mRatingSeekBar = (RatingBar) findViewById(R.id.sb_rating);
        mReviewText = (EditText) findViewById(R.id.et_review);
        mReviewText.setHint(getResources().getString(R.string.rate_review_head_title));
        mDone = (TextView) findViewById(R.id.done);
        mMessage = (TextView) findViewById(R.id.tv_rr_title);
        mLater = (TextView) findViewById(R.id.tv_later);
        mLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.GONE);
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v) {
                AppUtil.hideKeyboard(mReviewText, activity);
                mScrollReview.setScrollY(1);
                if (mRatingSeekBar.getRating() > 0) {
                    setUserRating();
                } else {
                    UiUtil.showToastMessage(mContext, mContext.getString(R.string.review_rating_error));
                }
            }
        });

        mReviewText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mScrollReview.setScrollY(0);
                }
            }
        });

        mReviewText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    if (mRatingSeekBar.getRating() > 0) {
                        setUserRating();
                    } else {
                        UiUtil.showToastMessage(mContext, mContext.getString(R.string.review_rating_error));
                    }
                    return true;
                }
                return false;
            }
        });

        mMessage.setText(String.format(getResources().getString(R.string.rating_info_header), mRestName));

        setup_radius_seekbar();

        setVisibility(View.GONE);

        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {

    }

    private void setUserRating() {

        String ratingVal = mRatingSeekBar.getRating() + "";
        String reviewText = mReviewText.getText().toString();

        progress = ProgressDialog.show(activity, "Please wait", "Submitting your feedback...", true);

        // Take API Hit
        DineoutNetworkManager dineoutNetworkManager = DineoutNetworkManager.newInstance(activity);

        dineoutNetworkManager.stringRequestPost(101, AppConstant.URL_SUBMIT_BOOKING_REVIEW,
                ApiParams.getSubmitBookingReviewParams(DOPreferences.getDinerId(activity.getApplicationContext()),
                        mBookingId, mBookingId, ratingVal, reviewText.trim(), null, ""),
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

    //Record Restaurant Reviewed Event
    private void recordRestaurantReviewedEvent() {

        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put("RestaurantId", "");
        props.put("RestaurantName", mRestName);
        props.put("RatingValue", mRatingSeekBar.getRating() + "");
        props.put("BookingId", mBookingId);

        AnalyticsHelper.getAnalyticsHelper(mContext)
                .trackEventQGraphApsalar(mContext.getString(R.string.push_label_restaurant_reviewed), props,true,true);

        // Track Event
        AnalyticsHelper.getAnalyticsHelper(mContext)
                .trackEventGA(mContext.getString(R.string.ga_screen_write_review),
                        mContext.getString(R.string.ga_action_review_under_moderation),
                        mRatingSeekBar.getRating() + "");

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


    public void setData(String mBookingId, String mRestName, Context context) {
        this.mContext = context;
        this.mBookingId = mBookingId;
        this.mRestName = mRestName;

        mMessage.setText(String.format(getResources().getString(R.string.rating_info_header), mRestName));
        mReviewText.setHint(getResources().getString(R.string.rate_review_head_title));
    }

    private void showLoginDialog() {

        // initiate login flow
        UserAuthenticationController.getInstance((FragmentActivity) mContext).startLoginFlow(null, this);

    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        progress.dismiss();
        UiUtil.showToastMessage(mContext, mContext.getString(R.string.text_no_feedback_error));
    }

    private void handleSubmitBookingReviewResponse(JSONObject responseObject) {
        progress.dismiss();

        //Record Restaurant Reviewed Event
        recordRestaurantReviewedEvent();

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {

                JSONObject params = responseObject.optJSONObject("output_params");
                if (params != null) {

                    JSONObject data = params.optJSONObject("data");
                    AnalyticsHelper.getAnalyticsHelper(mContext).trackAdTechEvent("ua", "ReviewPosted");
                    //AnalyticsHelper.getAnalyticsHelper(mContext).completeAdTechSession();
                    if (data != null && !TextUtils.isEmpty(data.optString("msg"))) {
                        Bundle bundle = new Bundle();
                        bundle.putString(AppConstant.BUNDLE_DIALOG_DESCRIPTION,
                                mContext.getString(R.string.review_submitted_dialog));
                        bundle.putString(AppConstant.BUNDLE_DIALOG_POSITIVE_BUTTON_TEXT, "OKAY");
                        bundle.putString(AppConstant.BUNDLE_DIALOG_HEADER, mContext.getString(R.string.review_dialog_header));
                        UiUtil.showCustomDialog(activity, bundle, mReviewDialogListener);
                    } else {

                        Toast.makeText(getContext(), "Rating submitted successfully", Toast.LENGTH_LONG).show();
                        setVisibility(View.GONE);

                    }
                }


            } else {
                if (responseObject.optString("error_type").contains("duplicate")) {
                    UiUtil.showToastMessage(mContext, responseObject.optString("error_msg"));
                } else if (!responseObject.optJSONObject("res_auth").optBoolean("status")) {
                    showLoginDialog();
                } else if (!TextUtils.isEmpty(responseObject.optString("error_msg"))) {
                    UiUtil.showToastMessage(mContext, responseObject.optString("error_msg"));
                } else {
                    UiUtil.showToastMessage(mContext, activity.getString(R.string.text_no_feedback_error));
                }
            }
        }
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {

    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject object) {
        if (mContext != null) {
            setUserRating();
        }
    }

    @Override
    public void loginFlowCompleteFailure(JSONObject loginFlowCompleteFailureObject) {
        if (mContext != null && mContext.getApplicationContext() != null&& loginFlowCompleteFailureObject != null) {

            String type = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_TYPE);
            if (LoginFlowBaseFragment.LoginType.NONE_CANCELLED.equalsIgnoreCase(type)) {
                return;
            }

            String cause = loginFlowCompleteFailureObject.optString(AuthenticationWrapperJSONReqFragment.API_RESPONSE_ERROR_MSG);
            // Show Error Message
            if (!AppUtil.isStringEmpty(cause)) {
                UiUtil.showToastMessage(mContext.getApplicationContext(), cause);
            }
        }
    }
}
