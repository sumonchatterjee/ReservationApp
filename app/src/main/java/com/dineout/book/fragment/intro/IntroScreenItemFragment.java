package com.dineout.book.fragment.intro;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;

public class IntroScreenItemFragment extends MasterDOFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro_screen_items, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        int background = args.getInt("background");

        if (getView() == null)
            return;
        ImageView bannnerImageView = (ImageView) getView().findViewById(R.id.imgIntroPage);

        Drawable backgroundDrawable = getResources().getDrawable(background);

        bannnerImageView.setImageResource(background);
    }
}
