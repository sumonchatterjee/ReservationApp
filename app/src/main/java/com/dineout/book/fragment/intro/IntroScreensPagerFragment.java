package com.dineout.book.fragment.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.home.LocationSelectionFragment;
import com.dineout.book.fragment.home.HomePageMasterFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOViewPagerFragment;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;

public class IntroScreensPagerFragment extends MasterDOViewPagerFragment
        implements OnClickListener, ViewPager.OnPageChangeListener {

    ViewPager viewPager;
    RadioGroup radioGroup;
    Button skipTourButton;
    private IntroScreenAdapter pagerAdapter;
    private boolean isLocationFetched = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen

        trackScreenName(getString(R.string.countly_first_launch));

        // Set Style
        AppUtil.setHoloLightTheme(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro_screens, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(false);

        pagerAdapter = new IntroScreenAdapter(getChildFragmentManager());
        viewPager = (ViewPager) getView().findViewById(R.id.pager_intro_option);
        viewPager.setOnPageChangeListener(this);

        radioGroup = (RadioGroup) getView().findViewById(R.id.radio_group_intro_indicator);
        setViewPagerAdapter(pagerAdapter);

        skipTourButton = (Button) getView().findViewById(R.id.button_intro_skip_tour);
        skipTourButton.setOnClickListener(this);

        setupIndicators();

        radioGroup.check(getSelectedPageIndex() + 1);
    }

    @Override
    public void onRemoveErrorView() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private void setupIndicators() {
        int screensCount = pagerAdapter.getCount();
        if (screensCount > 0) {
            if (radioGroup == null)
                return;

            radioGroup.removeAllViews();

            for (int i = 0; i < screensCount; i++) {
                ViewGroup vg = (ViewGroup) View
                        .inflate(getActivity(), R.layout.intro_screen_indicator_layout, null);
                RadioButton radioButton = (RadioButton) vg.getChildAt(0);
                radioButton.setId(i + 1);
                radioGroup.addView(vg);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
    }

    @Override
    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);

        if (pagerAdapter.getCount() == position + 1) {
            skipTourButton.setText(R.string.text_got_it);
            skipTourButton.setBackgroundResource(R.drawable.rectangle_primary_button_selector);

        } else {
            skipTourButton.setText(R.string.text_skip_tour);
            skipTourButton.setBackgroundResource(R.drawable.rectangle_black_selector);
        }

        radioGroup.check(position + 1);
    }

    public void setLocationFetched(boolean isLocationFetched) {
        this.isLocationFetched = isLocationFetched;
        //DOPreferences.setFirstLaunch(getActivity().getApplicationContext(), false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_intro_skip_tour) {

            int currentPage=viewPager.getCurrentItem();
            String label="";
            switch (currentPage){
                case 0:
                    label= "Explore";
                    break;


                case 1:
                    label="Reserve";
                    break;


                case 2:
                    label="Discounts";
                    break;


                case 3:
                    label="DineoutEarnings";
                    break;


                case 4:
                    label="UploadBill";
                    break;

            }

            //track screen for countly and GA
            HashMap<String,String> hMap=DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_first_launch),getString(R.string.d_skip_introclick),label,hMap);

            //track event for qgraph and apsalar
            HashMap<String, Object> props = new HashMap<>();
            props.put("label", label);
            trackEventQGraphApsalar(getString(R.string.d_skip_introclick),props,true,true,true);
            AnalyticsHelper.getAnalyticsHelper(getActivity())
                    .trackEventGA(getString(com.dineout.book.R.string.ga_screen_first_launch),
                            getString(com.dineout.book.R.string.ga_action_skip_intro), null);

            popBackStack(getFragmentManager());


            DOPreferences.setFirstLaunch(getActivity().getApplicationContext(), false);
            HomePageMasterFragment homePageMasterFragment = new HomePageMasterFragment();
            addToBackStack(getActivity(), homePageMasterFragment);


            if (!isLocationFetched) {
                addToBackStack(getActivity(), new
                        LocationSelectionFragment());

            }
            //handleDeepLinks(((DineoutMainActivity) getActivity()).getBranchDeeplinks());

        }


    }


    @Override
    public void onDetach() {
        super.onDetach();

        viewPager = null;
        radioGroup = null;
        pagerAdapter = null;
    }


    /**
     * Intro Screen Page Adapter
     */
    class IntroScreenAdapter extends BaseViewPagerAdapter {

        IntroScreenAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public MasterDOFragment getFragment(int i) {

            IntroScreenItemFragment fragment = new IntroScreenItemFragment();
            Bundle bundle = new Bundle();
            int backgroundDrawable;

            if (i == 0) {
                backgroundDrawable = R.drawable.intro001;

            } else if (i == 1) {
                backgroundDrawable = R.drawable.intro002;

            } else if (i == 2) {
                backgroundDrawable = R.drawable.intro003;

            } else if(i == 3) {
                backgroundDrawable = R.drawable.intro004;
            }else{
                backgroundDrawable = R.drawable.intro005;
            }

            bundle.putInt("background", backgroundDrawable);

            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }
    }
}
