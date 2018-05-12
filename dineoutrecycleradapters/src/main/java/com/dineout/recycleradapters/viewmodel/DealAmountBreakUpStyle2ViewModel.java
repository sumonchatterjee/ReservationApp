package com.dineout.recycleradapters.viewmodel;

import android.view.View;
import android.widget.TextView;

import com.dineout.recycleradapters.R;

import org.json.JSONObject;

/**
 * Created by sawai on 10/01/17.
 */

public class DealAmountBreakUpStyle2ViewModel extends DealAmountBreakUpViewModelWrapper {
    private TextView mTvValue;

    public DealAmountBreakUpStyle2ViewModel(View itemView) {
        super(itemView);

        mTvValue = (TextView) itemView.findViewById(R.id.value);
    }

    @Override
    public void bind(int position, JSONObject data) {
        String value = data.optString("title_2");
        mTvValue.setText(value);
    }


}
