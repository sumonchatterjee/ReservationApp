package com.dineout.book.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.R;

import org.json.JSONArray;
import org.json.JSONObject;


public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private JSONArray mNotificationArray;
    public NotificationAdapter(Context context,JSONArray array){

        this.mContext = context;
        this.mNotificationArray = array;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationHolder(LayoutInflater.from(mContext).inflate(R.layout.notification_item,null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder != null){
            ((NotificationHolder)holder).updateData
                    (mNotificationArray.optJSONObject(position));
        }
    }

    @Override
    public int getItemCount() {

        if(this.mNotificationArray != null)
        return this.mNotificationArray.length();

        return 0;
    }




    private class NotificationHolder extends RecyclerView.ViewHolder{

        public NotificationHolder(View itemView) {
            super(itemView);
        }

        public void updateData(JSONObject data){

        }
    }
}
