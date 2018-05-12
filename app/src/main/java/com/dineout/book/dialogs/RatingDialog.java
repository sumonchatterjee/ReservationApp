package com.dineout.book.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;

/**
 * Created by sawai on 02/09/16.
 */
public class RatingDialog extends MasterDOFragment implements Response.Listener<String>, Response.ErrorListener {
    private TextView mExperienceTV;
    private RatingBar mRatingBar;
    private TextView mRatingLevelTV;
    private TextView mRatingResponseFirstTV;
    private TextView mRatingResponseSecondTV;
    private EditText mFeedbackET;
    private LinearLayout mBottomButtonsLayoutLT;
    private Button mLeftButton;
    private Button mRightButton;
    private TextView mNoThanksTV;

    private final static int REQUEST_CODE_RATING_FEEDBACK = 0;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.app_rating,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mExperienceTV = (TextView) view.findViewById(R.id.app_rating_experience_id);
        mRatingBar = (RatingBar) view.findViewById(R.id.app_rating);
        mRatingLevelTV = (TextView) view.findViewById(R.id.rating_level_id);
        mRatingResponseFirstTV = (TextView) view.findViewById(R.id.rating_response_1_id);
        mRatingResponseSecondTV = (TextView) view.findViewById(R.id.rating_response_2_id);
        mFeedbackET = (EditText) view.findViewById(R.id.feedback_id);
        mBottomButtonsLayoutLT = (LinearLayout) view.findViewById(R.id.rating_button_layout);
        mLeftButton = (Button) view.findViewById(R.id.left_button);
        mRightButton = (Button) view.findViewById(R.id.right_button);
        mNoThanksTV = (TextView) view.findViewById(R.id.no_thanks_id);

        // set up basic functionality
        setUpRatingBarListener();
        showFirstDialog();
    }

    private void setUpRatingBarListener() {
        final int minimumValue = 2;
        final int threshold = DOPreferences.retrieveAppRatingThresholdValue(getActivity());

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {

                //track event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA("B_BookingReviewPopUp",
                        "RatingClick",Float.toString(rating),hMap);

                if (rating < 1.0f) {
                    mRatingBar.setRating(1.0f);
                } else {
                    if (rating < threshold) {
                        if (rating <= minimumValue) {
                            // set up rating response first
                            String text = getActivity().getResources().getString(R.string.app_rating_sorry_text);
                            mRatingResponseFirstTV.setText(text);

                            // set up rating response second
                            text = getActivity().getResources().getString(R.string.app_rating_what_went_wrong_text);
                            mRatingResponseSecondTV.setText(text);
                        } else {
                            // set up rating response first
                            String text = getActivity().getResources().getString(R.string.app_rating_thanks_for_feedback_text);
                            mRatingResponseFirstTV.setText(text);

                            // set up rating response second
                            text = getActivity().getResources().getString(R.string.app_rating_how_can_improve_app_text);
                            mRatingResponseSecondTV.setText(text);
                        }

                        showSecondDialog();
                    } else {
                        // set up rating response first
                        String text = getActivity().getResources().getString(R.string.app_rating_glad_text);
                        mRatingResponseFirstTV.setText(text);

                        // set up rating response second
                        text = getActivity().getResources().getString(R.string.app_rating_go_to_play_store_text);
                        mRatingResponseSecondTV.setText(text);

                        showThirdDialog();
                    }
                }
            }
        });
    }

    private void showFirstDialog() {
        mExperienceTV.setVisibility(View.VISIBLE);
        mRatingBar.setVisibility(View.VISIBLE);
        mRatingLevelTV.setVisibility(View.GONE);
        mRatingResponseFirstTV.setVisibility(View.GONE);
        mRatingResponseSecondTV.setVisibility(View.GONE);
        mFeedbackET.setVisibility(View.GONE);
        mBottomButtonsLayoutLT.setVisibility(View.GONE);
        mLeftButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.GONE);
        mNoThanksTV.setVisibility(View.GONE);

        mExperienceTV.setText(getActivity().getResources().getString(R.string.app_rating_experience_text));
        mRatingBar.setRating(0);
    }

    private void showSecondDialog() {
        mExperienceTV.setVisibility(View.GONE);
        mRatingBar.setVisibility(View.VISIBLE);
        mRatingLevelTV.setVisibility(View.VISIBLE);
        mRatingResponseFirstTV.setVisibility(View.VISIBLE);
        mRatingResponseSecondTV.setVisibility(View.VISIBLE);
        mFeedbackET.setVisibility(View.VISIBLE);
        mBottomButtonsLayoutLT.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);
        mNoThanksTV.setVisibility(View.VISIBLE);

        // disable rating
//        mRatingBar.setIsIndicator(true);

        // set up rating
        int value = (int) mRatingBar.getRating();
        if (value > 0) {
            String ratingLevelText = getActivity().getResources().getStringArray(R.array.app_rating_array)[value - 1];
            mRatingLevelTV.setText(ratingLevelText);
        }

        // edit text request focus
        mFeedbackET.requestFocus();

        // left button text
        String text = getActivity().getResources().getString(R.string.app_rating_submit_button_text);
        mLeftButton.setText(text);

        // right button text
        text = getActivity().getResources().getString(R.string.app_rating_later_button_text);
        mRightButton.setText(text);

        // set up left button listener
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                // show toast
                String thankYouText = getActivity().getResources().getString(R.string.app_rating_submit_toast);
                Toast.makeText(getActivity(), thankYouText, Toast.LENGTH_SHORT).show();

//                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_submit_store_rating_dialog), null);


                // Track Event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                trackEventForCountlyAndGA("B_BookingReviewPopUp",
                        "RateButtonClick","RateButtonClick",hMap);

                // call api
                sendFeedBackToServer();

            }
        });

        // set up right button listener
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_remind_later_store_rating_dialog), null);
            }
        });

        // no thanks listener
        text = getActivity().getResources().getString(R.string.app_rating_no_thanks_text);
        mNoThanksTV.setText(text);

        mNoThanksTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                // set flag for show dialog next time
                DOPreferences.setPlayStoreDialogStatus(getActivity(), false);

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_never_ask_again_store_rating_dialog), null);
            }
        });
    }

    private void showThirdDialog() {
        mExperienceTV.setVisibility(View.GONE);
        mRatingBar.setVisibility(View.VISIBLE);
        mRatingLevelTV.setVisibility(View.VISIBLE);
        mRatingResponseFirstTV.setVisibility(View.VISIBLE);
        mRatingResponseSecondTV.setVisibility(View.VISIBLE);
        mFeedbackET.setVisibility(View.GONE);
        mBottomButtonsLayoutLT.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);
        mNoThanksTV.setVisibility(View.VISIBLE);

        // disable rating
//        mRatingBar.setIsIndicator(true);

        // set up rating
        int value = (int) mRatingBar.getRating();
        if (value > 0) {
            String ratingLevelText = getActivity().getResources().getStringArray(R.array.app_rating_array)[value - 1];
            mRatingLevelTV.setText(ratingLevelText);
        }

        // left button text
        String text = getActivity().getResources().getString(R.string.app_rating_rate_button_text);
        mLeftButton.setText(text);

        // right button text
        text = getActivity().getResources().getString(R.string.app_rating_later_button_text);
        mRightButton.setText(text);

        // set up left button listener
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dineout.book")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dineout.book")));
                }

                dismiss();

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_yes_store_rating_dialog), null);
            }
        });
// submit_storeR
        // set up right button listener
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_remind_later_store_rating_dialog), null);
            }
        });

        // no thanks listener
        text = getActivity().getResources().getString(R.string.app_rating_no_thanks_text);
        mNoThanksTV.setText(text);

        mNoThanksTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();

                // set flag for show dialog next time
                DOPreferences.setPlayStoreDialogStatus(getActivity(), false);

                // Track Event
//                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
//                        getString(R.string.ga_action_never_ask_again_store_rating_dialog), null);
            }
        });

        // hide keyboard
        AppUtil.hideKeyboard(mFeedbackET, getActivity());
    }

    private void sendFeedBackToServer() {
        String feedback = "";
        String rating = "-1";
        String OSVersion = "";
        String deviceName = "";

        if (mFeedbackET != null && !TextUtils.isEmpty(mFeedbackET.getText())) {
            feedback = mFeedbackET.getText().toString();
        }

        if (mRatingBar != null) {
            rating = ((int) mRatingBar.getRating()) + "";
        }

        try {
            OSVersion = Build.VERSION.RELEASE;
            deviceName = Build.BRAND + " " + android.os.Build.MODEL;
        } catch (Exception e) {
            // exception
        }

        // send this data to api
        getNetworkManager().stringRequestPost(REQUEST_CODE_RATING_FEEDBACK, AppConstant.RATING_FEEDBACK,
                ApiParams.getRatingFeedbackParams(feedback, rating, OSVersion, deviceName), this, this, true);
    }


    @Override
    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
        // success
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        // failure
    }
}
