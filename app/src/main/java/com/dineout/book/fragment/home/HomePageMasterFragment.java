package com.dineout.book.fragment.home;


import android.content.Intent;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.master.MasterDOTabsViewPageFragment;
import com.dineout.book.fragment.search.SearchFragment;
import com.dineout.book.widgets.HomePageViewPager;
import com.dineout.book.widgets.SlidingTabLayout;
//import com.dineout.livehandler.AccessibilityPreference;
//import com.dineout.livehandler.AccessibilityUtils;

import com.example.dineoutnetworkmodule.DOPreferences;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

public class HomePageMasterFragment extends MasterDOTabsViewPageFragment
        implements View.OnClickListener {

    private final static int HOME = 0;
    //private final static int DEALS = 1;
    private final static int DO_PLUS = 1;
    private final static int YOU = 2;
    Bundle bundle;
    private Toolbar mToolbar;
    private Intent intentFromDeeplink = null;
    private SlidingTabLayout slidingTabLayout;
    private HomePageViewPager viewPager;
    private HomePageTabAdapter mTabAdapter;
    private TextView textViewLocalityName;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_master, container, false);

    }

    private void inflateToolbar() {
        // Get Location View
        View mLocationDetailContainer = LayoutInflater.from(getActivity()).inflate(R.layout.home_title_view, null);

        // Set View in Toolbar
        mToolbar.addView(mLocationDetailContainer);

        // Set Location Click
        mLocationDetailContainer.findViewById(R.id.layout_location_section).setOnClickListener(this);

        // Set Global Search Click
        mLocationDetailContainer.findViewById(R.id.image_view_search).setOnClickListener(this);

        // Set notification icon click
        mLocationDetailContainer.findViewById(R.id.image_button_notification).setOnClickListener(this);

        // Get Locality Name
        textViewLocalityName = (TextView) mLocationDetailContainer.findViewById(R.id.text_view_locality_name);

        // Show Selected Location
        showSelectedLocation();
    }



    public Intent getIntentDeeplinks() {
        return intentFromDeeplink;
    }

    public void setIntentDeeplinks(Intent intentFromDeeplink) {
        this.intentFromDeeplink = intentFromDeeplink;
    }

    @Override
    public void handleDeepLinks(Intent intent) {
        if (intent != null && intent.getAction() == Intent.ACTION_VIEW) {

            if (getActivity() == null) {
                return;
            }

            String ukey = intent.getStringExtra(DeeplinkParserManager.payloadUKey);

            if (!TextUtils.isEmpty(ukey)) {
                trackNotification(getActivity().getApplicationContext(), ukey);
            }

            String t = "";
            try {
                if (intent.getData() != null) {
                    t = URLDecoder.decode(intent.getData().toString(), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

//            if (AccessibilityPreference.isAccessibilityFeatureEnabled() &&
//                    AccessibilityUtils.isAccessibilityWalkThroughDeepLink(t)) {
//                AccessibilityUtils.handleAccessibilityWalkThrough(getActivity());
//
//            } else {

                MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), t);

                if (frag != null && (frag instanceof DOPlusFragment || frag instanceof DOPlusRestaurantFragment)) {
                    if (getViewPager() != null) {
                        getViewPager().setCurrentItem(1);
                    }
                } else if (frag != null && getActivity() != null) {
                    addToBackStack(getActivity(), frag);
                }
           // }

            getActivity().setIntent(null);
            setIntentDeeplinks(null);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.image_view_search:
                onGlobalSearchClick();
                break;

            case R.id.layout_location_section:
                onLocationClick();
                break;

            case R.id.image_button_notification:
                onNotificationIconClick();
                break;

            default:
                break;
        }
    }

    private void onGlobalSearchClick() {
        //Track Search Event
        //trackEventGA(getString(R.string.ga_screen_home), getString(R.string.ga_action_search), null);

        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_search),getString(R.string.d_search_icon_click),getString(R.string.d_search_icon_click),hMap);

        SearchFragment searchFragment = new SearchFragment();
        //searchFragment.setArguments(bundle);
        addToBackStack(getActivity(), searchFragment);
    }

    private void onLocationClick() {
        //Track Location Change Event
       // trackEventGA(getString(R.string.ga_screen_home), getString(R.string.ga_action_select_location), null);

        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_select_location),getString(R.string.d_location_icon_click),getString(R.string.d_location_icon_click),hMap);

        // Attach Location Selection Screen
        addToBackStack(getFragmentManager(), new LocationSelectionFragment());
    }


    private void onNotificationIconClick(){

        String screenName="";

        if(viewPager!=null){
           int currentPage = viewPager.getCurrentItem();
            if(currentPage==0){
                screenName = "D_Home";
            }else if (currentPage ==1){
                String isDoPlusMember = DOPreferences.isDinerDoPlusMember(getContext());
                if(!TextUtils.isEmpty(isDoPlusMember)){
                    if("1".equalsIgnoreCase(isDoPlusMember)){
                        screenName = "D_DOPlusMember";
                    }else if("0".equalsIgnoreCase(isDoPlusMember)){
                        screenName = "D_DOPlusNonMember";
                    }
                }

            }
        }
        //event
        HashMap<String,String> hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("D_Notification","NotificationIconClick",screenName,hMap);


        //click on notification icon
        NotificationFragment notificationFragment = new NotificationFragment();
        addToBackStack(getActivity(), notificationFragment);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mToolbar = (Toolbar) getView().findViewById(R.id.toolbarHomePage);



        inflateToolbar();
        mTabAdapter = new HomePageTabAdapter(getChildFragmentManager());
        viewPager = (HomePageViewPager) getView().findViewById(R.id.viewPager);
        viewPager.setPagingEnabled(false);
        //viewPager.setOffscreenPageLimit(1);

        slidingTabLayout = (SlidingTabLayout) getView().findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.base_sliding_tab_indicator, R.id.text_view_tab_name);
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.colorPrimary);
            }
        });

        handleDeepLinks(getIntentDeeplinks());


            if(DOPreferences.isGIRFEnabled(getActivity().getApplicationContext())) {
                setTabsDistributeEvenly(true);
            }
            else {
                setTabsDistributeEvenly(false);
            }


            setViewPagerAdapter(mTabAdapter);
            viewPager.setCurrentItem(getSelectedPageIndex());
            trackPagerChildren(getSelectedPageIndex());



    }


    private void trackPagerChildren(int index){
        switch (index) {

            case 0:
                trackScreenName(getString(R.string.countly_home));
                break;

            case 1:
                if (DOPreferences.isDinerDoPlusMember(getContext()).equalsIgnoreCase("1")) {
                    //track
                    trackScreenName(getString(R.string.countly_doplus));
                }
                else {
                    trackScreenName(getString(R.string.countly_nondoplus));
                }
                break;

            case 2:
                trackScreenName(getString(R.string.countly_you));
                break;


        };
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if DO Plus Deeplink has come
        if (DOPreferences.hasDoPlusDeeplink(getActivity().getApplicationContext())) {
            if (viewPager != null) {
                viewPager.setCurrentItem(1);
            }

            // Set Flag
            DOPreferences.setHasDoPlusDeeplink(getActivity().getApplicationContext(), false);
        }
    }

    @Override
    public ViewPager getViewPager() {
        return viewPager;
    }

    @Override
    public SlidingTabLayout getSlidingTabLayout() {
        return slidingTabLayout;
    }



    // Show Selected Location
    private void showSelectedLocation() {
        String location = "";

        if (TextUtils.isEmpty(DOPreferences.getAreaName(getActivity().getApplicationContext()))) {
            location = DOPreferences.getCityName(getActivity().getApplicationContext());
        } else {
            location = DOPreferences.getAreaName(getActivity().getApplicationContext());
        }

        // Check Locality Name
        if (TextUtils.isEmpty(DOPreferences.getLocalityName(getActivity().getApplicationContext()))) {
            location = getString(R.string.text_all_localities) + ", " + location;
            textViewLocalityName.setText(location);
        } else {
            if (TextUtils.isEmpty(DOPreferences.getSuggestion(getActivity().getApplicationContext()))) {
                location = getString(R.string.text_all_localities) + ", " + location;
                textViewLocalityName.setText(location);
            } else {
                textViewLocalityName.setText(DOPreferences.getSuggestion(getActivity().getApplicationContext()));
            }
        }
    }



    public void onDestroyView() {
        super.onDestroyView();

        mTabAdapter=null;
        viewPager = null;
        //slidingTabLayout = null;
        mToolbar = null;
        removeAllChildFragments();
       // getChildFragmentManager().getFragments().clear();


    }






    private void removeAllChildFragments() {

            List<Fragment> childFragments = getChildFragmentManager().getFragments();
            if (childFragments != null && !childFragments.isEmpty()) {
                android.support.v4.app.FragmentTransaction fragmentTransaction =
                        getChildFragmentManager().beginTransaction();
                for (Fragment childFragment : childFragments) {
                    if (childFragment != null) {

                            fragmentTransaction.remove(childFragment);

                    }
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        }




        @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        switch (position) {
            case HOME:
                trackScreenName(getString(R.string.countly_home));

                break;

            case DO_PLUS:

                if(DOPreferences.isGIRFEnabled(getActivity())){
                    trackScreenToGA("GIRF_webview");
                }

                else {
                    if (DOPreferences.isDinerDoPlusMember(getContext()).equalsIgnoreCase("1")) {
                        //track
                        trackScreenName(getString(R.string.countly_doplus));

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        if(hMap!=null){
                            hMap.put("locationName",DOPreferences.getAreaName(getContext())+" "+DOPreferences.getCityName(getContext()));
                        }
                        trackEventForCountlyAndGA(getString(R.string.countly_doplus), "ListingView",
                                "DOPlus", hMap);

                        //track for qgraph
                        HashMap<String, Object> props = new HashMap<>();
                        props.put("label","DOPlus");
                        trackEventQGraphApsalar("ListingView", props, true, false, false);
                        trackAdTechEvent("ct", "DPUser");
                        //completeAdTechSesion();
                    } else {
                        trackScreenName(getString(R.string.countly_nondoplus));
                    }
                }
                break;


            case YOU:
                trackScreenName(getString(R.string.countly_you));
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                trackEventForCountlyAndGA(getString(R.string.countly_you), getString(R.string.d_you_view_click),
                        getString(R.string.d_you_view_click) , hMap);
                break;
        }
    }


    @Override
    public boolean onPopBackStack() {

        return false;
    }

    /**
     * Home Tabs Pager Adapter
     */
    public class HomePageTabAdapter extends BaseTabsViewPagerAdapter {
        protected HomePageTabAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        protected MasterDOFragment getFragment(int position) {
            if (position == HOME) {
                return new FeedsPageFragment();

            } else if (position == DO_PLUS) {

                if(DOPreferences.isGIRFEnabled(getActivity().getApplicationContext())){
                    Bundle bundle = new Bundle();
                    bundle.putString("url",DOPreferences.getGIRFURL(getActivity().getApplicationContext()));
                    GirfTabWebViewFragment webViewFragment =new GirfTabWebViewFragment();
                    webViewFragment.setArguments(bundle);
                    //webViewFragment.setChildFragment(true);
                    return webViewFragment;
                }
                else {
                    if (DOPreferences.isDinerDoPlusMember(getContext()).equalsIgnoreCase("1")) {
                        return new DOPlusRestaurantFragment();
                    } else {
                        return new DOPlusFragment();
                    }
                }
            } else {
                return new YouPageFragment();
            }
        }


        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == HOME) {
                return getString(R.string.text_tab_home);

            } else if (position == DO_PLUS) {
                if(DOPreferences.isGIRFEnabled(getActivity().getApplicationContext())){
                    return "GIRF";
                }
                else {
                    return getString(R.string.text_tab_dineout_plus);
                }

            } else {
                return getString(R.string.text_tab_you);
            }
        }


        @Override
        public Parcelable saveState() {
            return null;
        }


    }

}