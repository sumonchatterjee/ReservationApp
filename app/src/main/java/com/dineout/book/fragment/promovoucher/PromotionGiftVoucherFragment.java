package com.dineout.book.fragment.promovoucher;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.util.Constants;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.ArrayList;
import java.util.HashMap;

public class PromotionGiftVoucherFragment extends MasterDOFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<MasterDOFragment> fragmentList;
    private String deepLinkQuery;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calculate Number of Fragments/Pages
        setFragmentList();

        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
    }

    private void setFragmentList() {
        // Check for NULL
        if (fragmentList != null) {
            fragmentList.clear();
        } else {
            fragmentList = new ArrayList();
        }

        if (getArguments() != null) {
            deepLinkQuery = getArguments().getString(Constants.SEARCH_KEYWORD);
        }

        // Promotions Fragment
        RingFencingFragment ringFencingFragment = new RingFencingFragment();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.SEARCH_KEYWORD, deepLinkQuery);
        ringFencingFragment.setArguments(bundle);
        ringFencingFragment.setChildFragment(true);

        // Gift Vouchers Fragment
        GiftVouchersFragment giftVouchersFragment = new GiftVouchersFragment();
        giftVouchersFragment.setChildFragment(true);

        // Add Fragments to List
        fragmentList.add(ringFencingFragment); // Promotions
        fragmentList.add(giftVouchersFragment);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotion_gift_voucher, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
        trackScreenName("D_PromotionAndGiftVoucher");

        // Set Title
        setToolbarTitle(R.string.title_promotions_gift_vouchers);

        // Set TabLayout
        tabLayout = (TabLayout) getView().findViewById(R.id.tabLayout_promotion_gift);

        // Set View Pager
        viewPager = (ViewPager) getView().findViewById(R.id.viewPager_promotion_gift);
        viewPager.setOffscreenPageLimit(0);

        // Add Tabs
        setTabInfo();
    }

    private void setTabInfo() {
        // Promotions Tab
        TabLayout.Tab promotionsTab = tabLayout.newTab();
        tabLayout.addTab(promotionsTab);

        // Gift Vouchers Tab
        TabLayout.Tab giftVouchersTab = tabLayout.newTab();
        tabLayout.addTab(giftVouchersTab);

        // Set Adapter to View Pager
        if (viewPagerAdapter != null) {
            viewPager.setAdapter(viewPagerAdapter);
        }

        // Set Tab to View Pager
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tabLayout = null;
        viewPager = null;
        viewPagerAdapter = null;
        fragmentList = null;
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // Check for NULL
            if (fragmentList != null && fragmentList.size() >= position) {
                return fragmentList.get(position);
            }

            return null;
        }

        @Override
        public int getCount() {
            // Check for NULL
            if (fragmentList != null) {
                return fragmentList.size();
            }

            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.text_promotions);
            } else {
                return getString(R.string.text_gift_vouchers);
            }
        }
    }

    @Override
    public void handleNavigation() {
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA(getString(R.string.countly_promotion),getString(R.string.d_back_click),getString(R.string.d_back_click),hMap);
        super.handleNavigation();
    }
}
