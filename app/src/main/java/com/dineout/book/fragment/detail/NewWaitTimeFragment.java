package com.dineout.book.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.AppConstant;


public class NewWaitTimeFragment extends MasterDOJSONReqFragment implements View.OnClickListener {

    private Bundle bookingBundle;
    private String waitMsg;
    private String waitTitle;
    private WaitTimeListener waitTimeListener;

    public void setWaitingMsg(String waitMsg) {
        this.waitMsg = waitMsg;
    }

    public void setWaitTitle(String waitTitle) {
        this.waitTitle = waitTitle;
    }

    public void setWaitTimeListener(WaitTimeListener waitTimeListener) {
        this.waitTimeListener = waitTimeListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wait_time_new, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Toolbar
        setToolbarTitle(R.string.ga_screen_reservation);

        bookingBundle = getArguments();
        if (bookingBundle != null) {
            // Track Screen
            trackScreenToGA(getString(R.string.ga_screen_wait_time) + "_" +
                    bookingBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
        }

        TextView textViewWaitTimeTitle = (TextView) getView().findViewById(R.id.textView_wait_time_title);
        if (AppUtil.isStringEmpty(waitTitle)) {
            textViewWaitTimeTitle.setVisibility(View.GONE);
        } else {
            textViewWaitTimeTitle.setText(waitTitle);
            textViewWaitTimeTitle.setVisibility(View.VISIBLE);
        }

        TextView waitTimeText = (TextView) getView().findViewById(R.id.text_wait_time);
        waitTimeText.setText(waitMsg);

        Button continueButton = (Button) getView().findViewById(R.id.continue_wait_time);
        continueButton.setOnClickListener(this);

        Button cancelButton = (Button) getView().findViewById(R.id.cancel_wait_time);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancel_wait_time) {
            trackEventGA(getString(R.string.ga_screen_wait_time), getString(R.string.ga_action_cancel), bookingBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
            popBackStack(getFragmentManager());

        } else if (view.getId() == R.id.continue_wait_time) {
            trackEventGA(getString(R.string.ga_screen_wait_time), getString(R.string.ga_action_waittime_will_wait), bookingBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));

            popBackStackImmediate(getFragmentManager());

            // Check for NULL
            if (waitTimeListener != null) {
                waitTimeListener.onContinueWaitTime();
            }
        }
    }

    public interface WaitTimeListener {
        void onContinueWaitTime();
    }
}
