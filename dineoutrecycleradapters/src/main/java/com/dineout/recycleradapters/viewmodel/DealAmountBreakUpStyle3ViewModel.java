package com.dineout.recycleradapters.viewmodel;

import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;

import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public class DealAmountBreakUpStyle3ViewModel extends DealAmountBreakUpViewModelWrapper {
    private TextView mTvValue;

    public DealAmountBreakUpStyle3ViewModel(View itemView) {
        super(itemView);

        mTvValue = (TextView) itemView.findViewById(R.id.value);
    }

    @Override
    public void bind(int position, JSONObject data) {
        if (data != null) {
            String value = data.optString("title_2");
            mTvValue.setText(value);
        }
    }


}
