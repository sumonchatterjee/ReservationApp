package com.dineout.book.fragment.master;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.dineout.book.widgets.SlidingTabLayout;

public abstract class MasterDOTabsViewPageFragment extends MasterDOViewPagerFragment {


    private boolean tabsDistributeEvenly;
    private boolean isSmoothScroll;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }




    @Override
    public void onViewStateRestored(Bundle savedInstance) {
        super.onViewStateRestored(savedInstance);
    }

    @Override
    protected void setViewPagerAdapter(BaseViewPagerAdapter pagerAdapter) {
        super.setViewPagerAdapter(pagerAdapter);
        resetPagerAndListener(this, tabsDistributeEvenly);
    }

    public void setTabsDistributeEvenly(boolean distributeEvenly) {
        this.tabsDistributeEvenly = distributeEvenly;

    }





    public abstract SlidingTabLayout getSlidingTabLayout();

    private void resetPagerAndListener(OnPageChangeListener listener, boolean tabsDistributeEvenly) {
        getSlidingTabLayout().setOnPageChangeListener(listener);
        getSlidingTabLayout().setDistributeEvenly(tabsDistributeEvenly);
        getSlidingTabLayout().setViewPager(getViewPager());
    }




    public abstract class BaseTabsViewPagerAdapter extends BaseViewPagerAdapter {

        protected BaseTabsViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        protected abstract MasterDOFragment getFragment(int position);
    }

}
