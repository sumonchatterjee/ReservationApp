package com.dineout.book.widgets;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.dialogs.RateNReviewDialog;
import com.dineout.book.dialogs.RateNReviewRestaurantNotVisitedDialog;
import com.dineout.book.fragment.home.FeedsPageFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.util.RateNReviewUtil;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE;
import static com.dineout.recycleradapters.util.RateNReviewUtil.ASK_USER_DIALOG_TYPE_STICKY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.BOOKING_ID;
import static com.dineout.recycleradapters.util.RateNReviewUtil.GA_TRACKING_CATEGORY_NAME_KEY;
import static com.dineout.recycleradapters.util.RateNReviewUtil.INFO_STRING;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_ACTION;
import static com.dineout.recycleradapters.util.RateNReviewUtil.addValueToJsonObject;

/**
 * Created by sawai.parihar on 23/03/17.
 */

public class RestaurantReviewAskUserFeedback extends RelativeLayout implements View.OnClickListener {
    private Context mContext;
    private JSONObject mData;
    private FeedsPageFragment mCallbackFragment;

    public RestaurantReviewAskUserFeedback(Context context) {
        super(context);

        init(context);
    }

    public RestaurantReviewAskUserFeedback(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public RestaurantReviewAskUserFeedback(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }


    private void init(Context context) {
        this.mContext = context;

        View view = LayoutInflater.from(mContext).inflate(R.layout.restaurant_review_ask_user_layout, null, false);

        this.removeAllViews();
        addView(view);
    }

    public void setData(JSONObject data) {
        this.mData = data;
    }

    public void inflateData() {
        try {
            if (mData != null) {
                View view;
                if (getChildCount() > 0 && ((view = getChildAt(0)) != null)) {
                    TextView inputTv = (TextView) view.findViewById(R.id.input_tv);
                    String visitedText = mData.optString("visit_text");
                    if (TextUtils.isEmpty(visitedText)) {
                        inputTv.setVisibility(GONE);
                    } else {
                        inputTv.setVisibility(VISIBLE);
                        inputTv.setText(visitedText);
                    }


                    JSONArray buttonArray = mData.optJSONArray("button");
                    if (buttonArray != null) {
                        TextView leftTv = (TextView) view.findViewById(R.id.left_tv);
                        if (buttonArray.length() > 0) {
                            String leftBtnText = buttonArray.getJSONObject(0).optString("text");
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


                        TextView rightTv = (TextView) view.findViewById(R.id.right_tv);
                        if (buttonArray.length() > 1) {
                            String rightBtnText = buttonArray.getJSONObject(1).optString("text");
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
                    }

                    // set bottom up animation
                    getChildAt(0).setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.bottom_review_box_anim));
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_tv:
                String actionKey = getActionKey(0);
                JSONObject value = getActionData(actionKey);
                addValueToJsonObject(value, REVIEW_ACTION, actionKey);
                addValueToJsonObject(value, BOOKING_ID, getBookingId());

                RateNReviewDialog rnrDialog = new RateNReviewDialog();
                Bundle bl = new Bundle();
                bl.putString(ASK_USER_DIALOG_TYPE, ASK_USER_DIALOG_TYPE_STICKY);
                bl.putString(GA_TRACKING_CATEGORY_NAME_KEY, mContext.getString(R.string.ga_rnr_category_home));
                bl.putString(INFO_STRING, value.toString());
                rnrDialog.setArguments(bl);
                rnrDialog.setRateNReviewCallback(mCallbackFragment);

                MasterDOFragment.showFragment(((FragmentActivity) mContext).getSupportFragmentManager(), rnrDialog);

                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(mContext.getString(R.string.ga_rnr_category_home),
                                mContext.getString(R.string.ga_rnr_action_recon_sticky_yes_click), null);
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("category",mContext.getString(R.string.ga_rnr_category_home));
                    hMap.put("action",  mContext.getString(R.string.ga_rnr_action_recon_sticky_yes_click));
                    hMap.put("label","");
                }
                AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly( mContext.getString(R.string.ga_rnr_action_recon_sticky_yes_click),hMap);


                setVisibility(GONE);
                break;

            case R.id.right_tv:
                actionKey = getActionKey(1);
                value = getActionData(actionKey);
                addValueToJsonObject(value, REVIEW_ACTION, actionKey);
                addValueToJsonObject(value, BOOKING_ID, getBookingId());

                RateNReviewRestaurantNotVisitedDialog notVisitedDialog = new RateNReviewRestaurantNotVisitedDialog();
                bl = new Bundle();
                bl.putString(ASK_USER_DIALOG_TYPE, ASK_USER_DIALOG_TYPE_STICKY);
                bl.putString(INFO_STRING, value.toString());
                notVisitedDialog.setArguments(bl);
                notVisitedDialog.setRateNReviewCallback(mCallbackFragment);

                MasterDOFragment.showFragment(((FragmentActivity) mContext).getSupportFragmentManager(), notVisitedDialog);


                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(mContext.getString(R.string.ga_rnr_category_home),
                                mContext.getString(R.string.ga_rnr_action_recon_sticky_no_click), null);
                HashMap<String, String> hMaps = DOPreferences.getGeneralEventParameters(getContext());
                if(hMaps!=null){
                    hMaps.put("category",mContext.getString(R.string.ga_rnr_category_home));
                    hMaps.put("action",  mContext.getString(R.string.ga_rnr_action_recon_sticky_no_click));
                    hMaps.put("label","");
                }
                AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventCountly( mContext.getString(R.string.ga_rnr_action_recon_sticky_no_click),hMaps);

                setVisibility(GONE);
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
