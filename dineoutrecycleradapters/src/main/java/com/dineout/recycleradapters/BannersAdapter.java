package com.dineout.recycleradapters;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class BannersAdapter extends PagerAdapter {
    DineoutNetworkManager networkManager;
    ImageLoader imageLoader;
    private JSONArray array;
    private DynamicFeedsHomePageAdapter.BannerClickListener bannerClickListener;

    public BannersAdapter(DynamicFeedsHomePageAdapter.BannerClickListener bannerClickListener) {
        this.bannerClickListener = bannerClickListener;
    }

    @Override
    public int getCount() {
        return array != null ? array.length() : 0;
    }

    public JSONArray getArray() {
        return array;
    }

    public void setArray(JSONArray array) {
        this.array = array;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager,
                                  ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = LayoutInflater.from(container.getContext())
                .inflate(R.layout.banners_networkimageview, container, false);

        NetworkImageView imageView =
                (NetworkImageView) itemView.findViewById(R.id.bannersImageView);
        imageView.setDefaultImageResId(R.drawable.default_list);

        final JSONObject pageJsonObject = getArray().optJSONObject(position);

        imageView.setImageUrl(pageJsonObject.optString("img_url"), getImageLoader());
        container.addView(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bannerClickListener.onBannerClick(pageJsonObject,position);
            }
        });

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        View view = (View) object;
        if (view != null) {
            container.removeView(view);
        }
    }
}
