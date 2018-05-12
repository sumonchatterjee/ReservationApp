package com.dineout.book.fragment.master;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

public abstract class MasterDOViewPagerFragment extends MasterDOFragment
        implements OnPageChangeListener {

    private static final String SELECTED_PAGE_INDEX = "selected_page_index";
    private static final String KEY_VIEWPAGER_ID = "viewPagerRandomId";

    //private BaseViewPagerAdapter mPagerAdapter;
    private int selectedPageIndex = 0 ;

    public int getSelectedPageIndex() {
        return selectedPageIndex;
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        selectedPageIndex = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public abstract ViewPager getViewPager();


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_PAGE_INDEX, selectedPageIndex);
        super.onSaveInstanceState(outState);

    }

//    protected void setCurrentPage(int pageIndex, boolean smoothScroll) {
//        if (mPagerAdapter != null
//                && selectedPageIndex < mPagerAdapter.getCount()) {
//            getViewPager().setCurrentItem(pageIndex,
//                    smoothScroll);
//        }
//    }

    protected void setViewPagerAdapter(BaseViewPagerAdapter pagerAdapter) {
        //mPagerAdapter = pagerAdapter;

        getViewPager().setAdapter(pagerAdapter);

    }

    public abstract class BaseViewPagerAdapter
            extends
            FragmentStatePagerAdapter {



        protected BaseViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        final public MasterDOFragment getItem(int position) {
            MasterDOFragment fragment = getFragment(position);
            fragment.setChildFragment(true);
            return fragment;
        }

        protected abstract MasterDOFragment getFragment(int position);


        public abstract int getCount();

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


    }

}
