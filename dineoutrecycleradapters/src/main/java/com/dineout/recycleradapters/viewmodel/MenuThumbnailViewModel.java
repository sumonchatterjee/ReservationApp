package com.dineout.recycleradapters.viewmodel;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.R;
import com.example.dineoutnetworkmodule.ImageRequestManager;

/**
 * Created by prateek.aggarwal on 8/24/16.
 */
public class MenuThumbnailViewModel {


    private String imageUrl;
    public boolean isOverlayEnabled = false;
    public String count = "";

    public MenuThumbnailViewModel(View view, String url, int count){
       this.imageUrl = url;
        this.count = "+"+count+" more";

        isOverlayEnabled = true;
        initializeView(view);
    }

    public MenuThumbnailViewModel(View v,String url){

        this.imageUrl = url;
        isOverlayEnabled = false;
        initializeView(v);
    }

    private void initializeView(View v){

        TextView overlayView =(TextView) v.findViewById(R.id.thumbnail_overlay);
        overlayView.setVisibility(isOverlayEnabled ? View.VISIBLE: View.GONE);
        overlayView.setText(this.count);

        setImage(((NetworkImageView) v.findViewById(R.id.thumbnail_image)),imageUrl);

    }


    private void setImage(NetworkImageView view,String url){


        Context context =  view.getContext();
        ImageLoader loader = ImageRequestManager.getInstance(context).getImageLoader();
        view.setImageUrl(url,loader);
    }
}
