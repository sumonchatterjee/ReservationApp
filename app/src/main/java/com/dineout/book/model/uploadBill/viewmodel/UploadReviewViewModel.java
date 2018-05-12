package com.dineout.book.model.uploadBill.viewmodel;


import android.text.TextUtils;
import android.widget.RatingBar;

import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.interfaces.ReviewDescriptionCallback;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONException;
import org.json.JSONObject;


public class UploadReviewViewModel implements ReviewDescriptionCallback {




    public String ratingDesc;
    public int ratingValue;
    public String imageUrl;
    public String name;
    public String billId;
    private JSONObject data = new JSONObject();
    public String path;
    private UpdateDescCallback mUpdateDescCallback;

    public UploadReviewViewModel(JSONObject data,
                                String id,String path, UpdateDescCallback updateDescCallback){

        this.path = path;

        this.billId = id;
        this.mUpdateDescCallback = updateDescCallback;
        if(data != null){
            this.data = data;
            this.name = "@"+data.optString("rest_name","");
            this.imageUrl = data.optString("rest_url","");
            this.ratingValue = data.optInt("rating");
            this.ratingDesc = data.optString("reviewText");
//            ratingValue.set(data.optInt("rating"));
//            ratingDesc.set(data.optString("reviewText"));
        }
    }
    public static void setImageResource(NetworkImageView view, String  url){

        view.setDefaultImageResId(R.drawable.default_list);
        if(!TextUtils.isEmpty(url))
        view.setImageUrl(url,
                ImageRequestManager.getInstance(view.getContext()).getImageLoader());
    }



    public JSONObject getData(){

        if(this.data != null){
            this.data.remove("rest_name");
            this.data.remove("rest_url");
            this.data.remove("rating");
            this.data.remove("reviewText");

            try {

                data.put("rating_desc",ratingDesc);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        return this.data;
    }

    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

        try {
            data.put("rating_food",rating);
            data.put("rating_service",rating);
            data.put("rating_ambience",rating);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ReviewDescriptionCallback getDescriptionCallback(){
        return this;
    }

    @Override
    public void updateText(String desc) {

        if(!TextUtils.isEmpty(desc.trim())){
            ratingDesc = desc;
        }
        else
            ratingDesc = "";

        if (mUpdateDescCallback != null) {
            mUpdateDescCallback.onDescUpdate(ratingDesc);
        }

    }

    public interface UpdateDescCallback {
        void onDescUpdate(String desc);
    }
}
