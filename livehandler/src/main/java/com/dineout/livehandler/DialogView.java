package com.dineout.livehandler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.analytics.tracker.AnalyticsHelper;
import com.google.gson.JsonObject;

import static com.apsalar.sdk.ApSingleton.getContext;

/**
 * Created by sawai on 20/08/16.
 */
public class DialogView implements IView, View.OnClickListener,CompoundButton.OnCheckedChangeListener {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewGroup mView;

    private String mRestName;
    private String mRestId;
    private boolean mChecked;

    public DialogView(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;

        createView();
    }

    private void createView() {
        mView = (ViewGroup) this.mInflater.inflate(R.layout.dialog_layout, null);

        mView.findViewById(R.id.dialog_view_container).setOnClickListener(this);
        mView.findViewById(R.id.head_positive).setOnClickListener(this);
        mView.findViewById(R.id.head_negative).setOnClickListener(this);
        ((android.widget.CheckBox) mView.findViewById(R.id.never_show_checkbox)).setOnCheckedChangeListener(this);
    }

    public void setData(String restName, String restId) {
        this.mRestName = restName;
        this.mRestId = restId;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.head_positive) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!TextUtils.isEmpty(mRestId) && !mRestId.equalsIgnoreCase("na")) {
                intent.setData(Uri.parse("dineout://r_d?q=" + mRestId));
            } else {
                intent.setData(Uri.parse("dineout://"));
            }
            AccessibilityController.getInstance().hideWindow();
            mContext.startActivity(intent);

            // store flag for checkbox
            AccessibilityPreference.storeShowDialogFlag(mContext, !mChecked);

            // destroy the dialog
            AccessibilityController.getInstance().destroyDialogView();


            // ga tracking
            try {
                String category = mContext.getString(R.string.ga_booking_assistant_over_app_dialog);
                String action = mContext.getString(R.string.ga_booking_assistant_over_app_dialog_go_ahead);
                String neverShowDialogString = mChecked ? getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_true) : getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_false);
                long restId = Integer.parseInt(mRestId);

                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(category, action, neverShowDialogString, restId);
            } catch (Exception e) {
                // exception
            }

        } else if (i == R.id.head_negative) {
            AccessibilityController.getInstance().hideWindow();

            // store flag for checkbox
            AccessibilityPreference.storeShowDialogFlag(mContext, !mChecked);

            // destroy the dialog
            if (mChecked) {
                AccessibilityController.getInstance().destroyDialogView();
            }

            // ga tracking
            try {
                String category = mContext.getString(R.string.ga_booking_assistant_over_app_dialog);
                String action = mContext.getString(R.string.ga_booking_assistant_over_app_dialog_not_now);
                String neverShowDialogString = mChecked ? getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_true) : getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_false);
                long restId = Integer.parseInt(mRestId);

                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(category, action, neverShowDialogString, restId);
            } catch (Exception e) {
                // exception
            }

        } else if (i == R.id.dialog_view_container) {
            AccessibilityController.getInstance().hideWindow();

            // store flag for checkbox
            AccessibilityPreference.storeShowDialogFlag(mContext, !mChecked);

            // destroy the dialog
            if (mChecked) {
                AccessibilityController.getInstance().destroyDialogView();
            }

            // ga tracking
            try {
                String category = mContext.getString(R.string.ga_booking_assistant_over_app_dialog);
                String action = mContext.getString(R.string.ga_booking_assistant_over_app_dialog_not_now);
                String neverShowDialogString = mChecked ? getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_true) : getContext().getString(R.string.ga_booking_assistant_over_app_dialog_never_false);
                long restId = Integer.parseInt(mRestId);

                AnalyticsHelper.getAnalyticsHelper(getContext())
                        .trackEventGA(category, action, neverShowDialogString, restId);
            } catch (Exception e) {
                // exception
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        mChecked = isChecked;
    }

    @Override
    public ViewGroup getView() {
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                boolean returnValue;
                if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    AccessibilityController.getInstance().hideWindow();
                    returnValue = true;
                } else {
                    returnValue = false;
                }

                return returnValue;
            }
        });

        return mView;
    }

    public void setData(JsonObject jsonObject) {
        try {
            mRestId = jsonObject.get(AccessibilityUtils.RESTAURANT_ID).getAsString();

        } catch (Exception e) {
            mRestId = "";
        }
    }
}
