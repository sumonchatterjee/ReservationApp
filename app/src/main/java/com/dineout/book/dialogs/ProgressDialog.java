package com.dineout.book.dialogs;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;


public class ProgressDialog extends DialogFragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo);

        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.progress_dialog,container,false);
        Drawable d = new ColorDrawable();
        d.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        getDialog().getWindow().setBackgroundDrawable(d);
        return v;
    }


}
