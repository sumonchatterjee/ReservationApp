package com.dineout.book.fragment.payments.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.dineout.book.R;


@SuppressLint("NewApi")
public class PaymentLoaderDialog extends DialogFragment {

    private static final String BACK_PRESSED = "ON_BACK_PRESSED";
    private View mView;
    private boolean onBackPressedCancel = false;

    public static PaymentLoaderDialog newInstance(boolean onBackPressedCancel) {
        PaymentLoaderDialog fragment = new PaymentLoaderDialog();
        Bundle args = new Bundle();
        args.putBoolean(BACK_PRESSED, onBackPressedCancel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            onBackPressedCancel = getArguments().getBoolean(BACK_PRESSED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Drawable d = new ColorDrawable();
        d.setColorFilter(Color.TRANSPARENT, Mode.CLEAR);
        getDialog().getWindow().setBackgroundDrawable(d);

        getDialog().setCanceledOnTouchOutside(false);

        mView = inflater.inflate(R.layout.progress_screen_loader, container);

        return mView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (onBackPressedCancel)
            getActivity().onBackPressed();
    }
}
