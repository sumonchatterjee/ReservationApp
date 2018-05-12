package com.dineout.book.dialogs;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.fragment.home.FeedsPageFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE;
import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE_POP_UP;
import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;

/**
 * Created by sawai.parihar on 30/03/17.
 */

public class RestaurantReviewAskUserFeedbackDialog extends MasterDOFragment implements View.OnClickListener {
    private JSONObject mData;
    private FeedsPageFragment mCallbackFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && !TextUtils.isEmpty(getArguments().getString(INFO_STRING))) {
            try {
                mData = new JSONObject(getArguments().getString(INFO_STRING));
            } catch (Exception e) {
                // Exception
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rate_n_review_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(new Float(0.85));
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);

        // set dialog width
        Rect displayRectangle = new Rect();
        Window window = getDialog().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        getDialog().getWindow().setLayout((int) (displayRectangle.width() * .88), WindowManager.LayoutParams.WRAP_CONTENT);

        ImageView crossIv = (ImageView) view.findViewById(R.id.cross_iv);
        crossIv.setOnClickListener(this);

        inflateData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().getAttributes().windowAnimations = R.style.RNRStyle;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cross_iv:
                getDialog().cancel();
                break;

            case R.id.left_tv:
                String actionKey = getActionKey(1);
                JSONObject value = getActionData(actionKey);
                addValueToJsonObject(value, REVIEW_ACTION, actionKey);
                addValueToJsonObject(value, BOOKING_ID, getBookingId());

                RateNReviewRestaurantNotVisitedDialog notVisitedDialog = new RateNReviewRestaurantNotVisitedDialog();
                Bundle bl = new Bundle();
                bl.putString(ASK_USER_DIALOG_TYPE, ASK_USER_DIALOG_TYPE_POP_UP);
                bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, getString(R.string.ga_rnr_category_home));
                bl.putString(INFO_STRING, value.toString());
                notVisitedDialog.setArguments(bl);
                notVisitedDialog.setRateNReviewCallback(mCallbackFragment);

                showFragment(getActivity().getSupportFragmentManager(), notVisitedDialog);

                getDialog().dismiss();
                break;

            case R.id.right_tv:
                actionKey = getActionKey(0);
                value = getActionData(actionKey);
                addValueToJsonObject(value, REVIEW_ACTION, actionKey);
                addValueToJsonObject(value, BOOKING_ID, getBookingId());

                RateNReviewDialog rnrDialog = new RateNReviewDialog();
                bl = new Bundle();
                bl.putString(ASK_USER_DIALOG_TYPE, ASK_USER_DIALOG_TYPE_POP_UP);
                bl.putString(INFO_STRING, value.toString());
                rnrDialog.setArguments(bl);
                rnrDialog.setRateNReviewCallback(mCallbackFragment);

                showFragment(getActivity().getSupportFragmentManager(), rnrDialog);

                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(getString(R.string.ga_rnr_category_home),
                                getString(R.string.ga_rnr_action_recon_pop_up_yes_click), null);

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("category",getString(R.string.ga_rnr_category_home));
                    hMap.put("action", getString(R.string.ga_rnr_action_recon_pop_up_yes_click));
                    hMap.put("label","");
                }
                AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly(getString(R.string.ga_rnr_action_recon_pop_up_yes_click),hMap);

                getDialog().dismiss();
                break;
        }
    }

    private String getActionKey(int pos) {
        String returnValue = null;
        try {
            if (mData != null && mData.optJSONArray("button") != null) {
                int returnVal = mData.optJSONArray("button").getJSONObject(pos).optInt("action");
                returnValue = String.valueOf(returnVal);
            }
        } catch (Exception e) {}

        return returnValue;
    }

    private JSONObject getActionData(String key) {
        JSONObject returnValue = new JSONObject();
        if (mData != null && mData.optJSONObject("review_box") != null) {
            RateNReviewUtil.appendObject(returnValue, mData.optJSONObject("review_box").optJSONObject(key));
            RateNReviewUtil.appendObject(returnValue, mData.optJSONObject("review_data"));
        }

        return returnValue;
    }

    public void inflateData() {
        try {
            if (mData != null) {
                View view = getView();
                if (view != null) {
                    TextView inputTv = (TextView) view.findViewById(R.id.restaurant_visit_text_1);
                    String visitedText = mData.optString("visit_text");
                    if (TextUtils.isEmpty(visitedText)) {
                        inputTv.setVisibility(GONE);
                    } else {
                        inputTv.setVisibility(VISIBLE);
                        inputTv.setText(visitedText);
                    }


                    JSONArray buttonArray = mData.optJSONArray("button");
                    if (buttonArray != null) {
                        TextView rightTv = (TextView) view.findViewById(R.id.right_tv);
                        if (buttonArray.length() > 0) {
                            String rightBtnText = buttonArray.getJSONObject(0).optString("text");
                            if (TextUtils.isDigitsOnly(rightBtnText)) {
                                rightTv.setVisibility(GONE);

                            } else {
                                rightTv.setVisibility(VISIBLE);
                                rightTv.setText(rightBtnText);
                                rightTv.setOnClickListener(this);
                            }
                        } else {
                            rightTv.setVisibility(GONE);
                        }


                        TextView leftTv = (TextView) view.findViewById(R.id.left_tv);
                        if (buttonArray.length() > 1) {
                            String leftBtnText = buttonArray.getJSONObject(1).optString("text");
                            if (TextUtils.isDigitsOnly(leftBtnText)) {
                                leftTv.setVisibility(GONE);

                            } else {
                                leftTv.setVisibility(VISIBLE);
                                leftTv.setText(leftBtnText);
                                leftTv.setOnClickListener(this);
                            }
                        } else {
                            leftTv.setVisibility(GONE);
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    private String getBookingId() {
        String returnValue = "";
        if (mData != null) {
            returnValue = mData.optString(BOOKING_ID);
        }

        return returnValue;
    }

    public void setCallback(FeedsPageFragment fragment) {
        this.mCallbackFragment = fragment;
    }
}
