package com.dineout.book.dialogs;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.recycleradapters.util.RateNReviewUtil.RateNReviewCallbacks;

/**
 * Created by sawai.parihar on 30/03/17.
 */

public class RateNReviewThankYouDialog extends MasterDOFragment implements View.OnClickListener {
    public static final String THANK_YOU_MSG_KEY = "thank_you_message_key";
    private RateNReviewCallbacks mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rate_n_review_thank_you_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(new Float(0.85));
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);

        // set dialog width
        Rect displayRectangle = new Rect();
        Window window = getDialog().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        getDialog().getWindow().setLayout((int) (displayRectangle.width() * .88), WindowManager.LayoutParams.WRAP_CONTENT);

        // set cross icon listener
        ImageView crossIv = (ImageView) view.findViewById(R.id.cross_iv);
        crossIv.setOnClickListener(this);

        // set title text
        TextView restaurantVisitTv1 = (TextView) view.findViewById(R.id.restaurant_visit_text_1);
        restaurantVisitTv1.setText("Thank You");

        // set sub title text
        TextView restaurantVisitTv2 = (TextView) view.findViewById(R.id.restaurant_visit_text_2);
        String resText2;
        if (getArguments() != null) {
            resText2 = getArguments().getString(THANK_YOU_MSG_KEY);
        } else {
            resText2 = "";
        }
        restaurantVisitTv2.setText(resText2);
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

                if (mCallback != null) {
                    mCallback.onDialogDismiss();
                }
                break;
        }
    }

    public void setCallback(RateNReviewCallbacks callback) {
        this.mCallback = callback;
    }
}
