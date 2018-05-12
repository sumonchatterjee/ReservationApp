package com.dineout.recycleradapters.viewmodel;

import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;

import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public class DealAmountBreakUpStyle1ViewModel extends DealAmountBreakUpViewModelWrapper {
    private TextView mTvKey;
    private TextView mTvValue;

    public DealAmountBreakUpStyle1ViewModel(View itemView) {
        super(itemView);

        mTvKey = (TextView) itemView.findViewById(R.id.key);
        mTvValue = (TextView) itemView.findViewById(R.id.value);
    }

    @Override
    public void bind(int position, JSONObject data) {
        if (data != null) {
            String title = data.optString("title_1");
            mTvKey.setText(title);

            String value = data.optString("title_2");
            mTvValue.setText("Rs. " + value);
        }
    }


}
