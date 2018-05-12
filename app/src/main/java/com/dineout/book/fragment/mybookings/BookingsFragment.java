package com.dineout.book.fragment.mybookings;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;

public class BookingsFragment extends MasterDOFragment implements TabLayout.OnTabSelectedListener {

    private TabLayout tabLayoutBookings;
    private int currentFragment = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenName(getString(R.string.countly_booking));
    }

    private int getCurrentFragment() {
        return currentFragment;
    }

    private void setCurrentFragment(int position) {
        currentFragment = position;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookings, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Title
        setToolbarTitle(R.string.title_my_bookings);

        // Initialize View
        initializeView();
    }

    private void initializeView() {
        if (getView() == null) {
            return;
        }

        // Set Tab
        tabLayoutBookings = (TabLayout) getView().findViewById(R.id.tabLayout_bookings);

        TabLayout.Tab upcomingBookingTab = tabLayoutBookings.newTab();
        upcomingBookingTab.setText(R.string.text_upcoming_tab);
        upcomingBookingTab.setTag(AppConstant.UPCOMING_BOOKING_TAB);

        TabLayout.Tab historyBookingTab = tabLayoutBookings.newTab();
        historyBookingTab.setText(R.string.text_history_tab);
        historyBookingTab.setTag(AppConstant.HISTORY_BOOKING_TAB);

        tabLayoutBookings.addTab(upcomingBookingTab);
        tabLayoutBookings.addTab(historyBookingTab);

        // Persist Select State
        persistTabSelection(upcomingBookingTab, historyBookingTab);

        // Set Tab Select Listener
        tabLayoutBookings.setOnTabSelectedListener(this);
    }

    private void persistTabSelection(TabLayout.Tab upcomingBookingTab, TabLayout.Tab historyBookingTab) {
        MasterDOFragment fragment = null;
        if (getCurrentFragment() == 0) {
            // Update Tab UI
            upcomingBookingTab.select();

            // Set Fragment to Load
            fragment = new UpcomingBookingFragment();

        } else {
            // Update Tab UI
            historyBookingTab.select();

            // Set Fragment to Load
            fragment = new HistoryBookingFragment();
        }

        // Load Tab Fragment
        loadTabFragment(fragment);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // Get Tag
        int tabTag = (int) tab.getTag();
        MasterDOFragment fragment = null;

        if (tabTag == AppConstant.UPCOMING_BOOKING_TAB) {

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_upcoming),getString(R.string.d_upcoming),hMap);

            fragment = new UpcomingBookingFragment();

        } else {

            //track event
            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
            trackEventForCountlyAndGA(getString(R.string.countly_booking),getString(R.string.d_history),getString(R.string.d_history),hMap);

            fragment = new HistoryBookingFragment();
        }

        // Load Tab Fragment
        loadTabFragment(fragment);
    }

    private void loadTabFragment(MasterDOFragment fragment) {
        // Set Fragment as Child
        fragment.setChildFragment(true);

        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout_bookings, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // Do Nothing...
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // Do Nothing...
    }

    @Override
    public void onStop() {
        super.onStop();

        setCurrentFragment(tabLayoutBookings.getSelectedTabPosition());
    }

    @Override
    public void onDestroyView() {
        // Remove Tab Listener
        tabLayoutBookings.setOnTabSelectedListener(null);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tabLayoutBookings = null;
    }
}
