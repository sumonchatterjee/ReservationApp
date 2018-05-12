package com.dineout.book.fragment.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImagePagerFragment extends MasterDOFragment implements View.OnClickListener {

    public static final String EXTRA_IMAGE = "position";
    List<String> menus = new ArrayList<String>();
    int position = 0;
    private ViewPager mPager;
    private TextView showingImageNumber;
    private Toolbar mToolbar;
    private ImageButton back;
    private boolean isMenu;
    private String detail;
    private String restaurantCategory;
    private String restaurantType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.restaurant_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
       // trackScreenToGA(getString(R.string.ga_screen_photo_menu));
        initializeData();
    }

    private void initializeData() {

        menus = getArguments().getStringArrayList("menus");
        position = getArguments().getInt("position", 0);
        isMenu = getArguments().getBoolean("isMenu");
        detail=getArguments().getString("detail");
        restaurantCategory=getArguments().getString("restaurantCategory");
        restaurantType = getArguments().getString("restaurantType");

        showingImageNumber = ((TextView) getView().findViewById(R.id.tv_showing_menu_image));
        back = (ImageButton) getView().findViewById(R.id.ib_menu_back);
        back.setOnClickListener(this);

        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        if(hMap!=null){
            hMap.put("ImageName",menus.get(position));
        }
        if(!TextUtils.isEmpty(detail)){
            trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantImageView",
                    Integer.toString(position), hMap);
        }else{
            if(isMenu) {
                trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantMenuImageView",
                        Integer.toString(position), hMap);
            }
        }


        showingImageNumber.setText(String.format(getString(R.string.gallery_counter), position + 1, menus.size()));

        ImagePagerAdapter mAdapter = new ImagePagerAdapter(getActivity().getSupportFragmentManager(), menus.size());
        mPager = (ViewPager) getView().findViewById(R.id.pagerFullScreen);
        //mPager.setPagingEnabled(true);

        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
        //mPager.setOffscreenPageLimit(2);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                if(hMap!=null){
                    hMap.put("ImageName",menus.get(arg0));
                }

                showingImageNumber.setText(String.format(getString(R.string.gallery_counter), arg0 + 1, menus.size()));

                if(!TextUtils.isEmpty(detail)){
                    trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantImageView",
                            Integer.toString(arg0), hMap);
                }else{
                    if(isMenu) {
                        trackEventForCountlyAndGA(getString(R.string.countly_reaturant_detail)+"_"+restaurantType+"_"+restaurantCategory, "RestaurantMenuImageView",
                                Integer.toString(arg0), hMap);
                    }
                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        // Set up activity to go full screen
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the current item based on the extra passed in to this activity
         int extraCurrentItem = getArguments().getInt(EXTRA_IMAGE, -1);
        if (extraCurrentItem != -1) {
            mPager.setCurrentItem(extraCurrentItem);
        }

        // showToolbar();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_menu_back:
                popBackStack(getActivity().getSupportFragmentManager());
                break;
        }
    }


    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.newInstance(menus.get(position));
        }
    }
}
