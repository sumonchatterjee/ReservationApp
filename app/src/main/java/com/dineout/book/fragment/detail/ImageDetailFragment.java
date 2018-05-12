package com.dineout.book.fragment.detail;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.widgets.ZoomImageView;


public class ImageDetailFragment extends MasterDOFragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ZoomImageView mImageView;


    public ImageDetailFragment() {
    }


    public static ImageDetailFragment newInstance(String imageUrl) {
        ImageDetailFragment f = new ImageDetailFragment();

        Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);

        f.setArguments(args);

        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate( R.layout.zoomable, container, false);
        mImageView = (ZoomImageView) v.findViewById(R.id.imageView);


        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String imageUrl = getPathFromUrl(mImageUrl);

        mImageView.setEnabled(true);

        mImageView.setNetworkImage(R.drawable.default_list,
                imageUrl, getImageLoader());

    }

    private String getPathFromUrl(String url){
        String finalUrl=url;
        String[] urlParts=url.split("\\?");

        if(urlParts!=null){
            finalUrl=urlParts[0];
        }

        return finalUrl;
    }
}
