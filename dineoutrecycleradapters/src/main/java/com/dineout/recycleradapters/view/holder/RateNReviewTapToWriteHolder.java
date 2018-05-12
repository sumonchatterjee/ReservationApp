package com.dineout.recycleradapters.view.holder;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.dineout.recycleradapters.R;
import com.dineout.recycleradapters.RateNReviewAdapter;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.util.RateNReviewUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_HINT_TEXT;
import static com.dineout.recycleradapters.util.RateNReviewUtil.REVIEW_TEXT;

/**
 * Created by sawai.parihar on 10/03/17.
 */

public class RateNReviewTapToWriteHolder extends RateNReviewHolderWrapper implements View.OnClickListener, TextWatcher {
    private Context mContext;
    private ViewGroup mTapToWriteEtLayout;
    private EditText mEt;

    public RateNReviewTapToWriteHolder(View itemView) {
        super(itemView);

        mContext = itemView.getContext();
        mTapToWriteEtLayout = (ViewGroup) itemView.findViewById(R.id.tap_to_write_et_wrapper_layout);
        mEt = (EditText) itemView.findViewById(R.id.tap_to_write_et);
    }

    public void bindData(JSONObject object, int position) {
        super.bindData(object, position);

        if (object.optString(RateNReviewUtil.TARGET_SCREEN_KEY).equals(RateNReviewUtil.IN_APP_RATING)) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(dpToPixel(mContext, 10), dpToPixel(mContext, 15),
                    dpToPixel(mContext, 10), dpToPixel(mContext, 15));
            mTapToWriteEtLayout.setLayoutParams(params);
            mTapToWriteEtLayout.setOnClickListener(null);

            // set hint
            mEt.setHint("");

            mEt.setEnabled(false);

            int ratingValue = ((RateNReviewAdapter) getAdapterInstance()).getCurrentRatingValue();
            JSONArray appThresholdTextArr = object.optJSONArray("appThresholdTextArr");
            if (appThresholdTextArr.length() >= ratingValue) {
                String thresholdText = appThresholdTextArr.optString(ratingValue - 1);
                if (TextUtils.isEmpty(thresholdText)) thresholdText = "";
                mEt.setText(thresholdText);
            }
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPixel(mContext, 100));
            params.setMargins(dpToPixel(mContext, 10), dpToPixel(mContext, 15),
                    dpToPixel(mContext, 10), dpToPixel(mContext, 15));
            mTapToWriteEtLayout.setLayoutParams(params);
            mTapToWriteEtLayout.setOnClickListener(this);

            // set hint
            String hintText = object.optString(REVIEW_HINT_TEXT, "");
            mEt.setHint(hintText);

            mEt.setEnabled(true);

            String reviewText = object.optString(REVIEW_TEXT);
            if (TextUtils.isEmpty(reviewText)) reviewText = "";
            mEt.setText(reviewText);
            mEt.addTextChangedListener(this);

            mEt.requestFocus();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tap_to_write_et_wrapper_layout) {
            mEt.requestFocus();
            AppUtil.showKeyBoard(((AppCompatActivity) mContext));
        }
    }

    private int dpToPixel(Context context, int dp) {
        return  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            getDataObject().put(REVIEW_TEXT, s.toString());
        } catch (Exception e) {
            // Exception
        }
    }
}
