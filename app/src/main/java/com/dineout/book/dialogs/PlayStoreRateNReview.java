package com.dineout.book.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.example.dineoutnetworkmodule.DOPreferences;

public class PlayStoreRateNReview extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.play_store_rate_review_dialog, null);
        builder.setView(convertView);

        convertView.findViewById(R.id.btn_dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
                        getString(R.string.ga_action_never_ask_again_store_rating_dialog), null);

                DOPreferences.setPlayStoreDialogStatus(MainApplicationClass.getInstance(), false);
                dismiss();
            }
        });

        convertView.findViewById(R.id.btn_dialog_remind_later).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
                        getString(R.string.ga_action_remind_later_store_rating_dialog), null);

                dismiss();
            }
        });

        convertView.findViewById(R.id.btn_dialog_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Track Event
                AnalyticsHelper.getAnalyticsHelper(getActivity()).trackEventGA(getString(R.string.ga_screen_thankyou),
                        getString(R.string.ga_action_yes_store_rating_dialog), null);

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.dineout.book")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dineout.book")));
                }
                dismiss();
            }
        });
        return builder.create();
    }
}
