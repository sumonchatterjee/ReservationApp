package com.dineout.book.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;

public class AboutFragment extends MasterDOFragment {

    int clickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewByIds();
        setToolbarTitle(getActivity().getResources().getString(R.string.about_str));
    }

    private void findViewByIds() {

        if (getView() == null) {
            return;
        }

//        TextView mSite = (TextView) getView().findViewById(R.id.dineout_site);

        TextView mVersion = (TextView) getView().findViewById(R.id.dineout_copyright);
        mVersion.setText(getResources().getString(R.string.dineout_copyright) + " " + AppUtil.getVersionName());



        mVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ++clickCounter;
                openDebugScreen();
            }
        });

        // On Long Press
        /*mVersion.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

               addToBackStack(getFragmentManager(),new DebugTestFragment());

                    return true;
                }


        });*/
    }

    private void openDebugScreen() {
        if (clickCounter == 10) {
            clickCounter = 0;
            addToBackStack(getFragmentManager(),new DebugTestFragment());
        }
    }
}
