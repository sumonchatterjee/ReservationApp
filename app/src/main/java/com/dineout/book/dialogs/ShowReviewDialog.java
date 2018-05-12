package com.dineout.book.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.AppConstant;

public class ShowReviewDialog extends MasterDOFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.show_review_dialog_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize View
        initializeView();
    }

    @Override
    public void onRemoveErrorView() {

    }

    private void initializeView() {
        // Get Bundle
        Bundle bundle = getArguments();

        if (bundle != null) {
            // Set Text
            TextView reviewText = (TextView) getView().findViewById(R.id.textView_review_text);
            reviewText.setText(bundle.getString(AppConstant.BUNDLE_REVIEW_TEXT, ""));

            // Get Button
            Button dismissButton = (Button) getView().findViewById(R.id.button_review_dismiss);
            dismissButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
    }
}
