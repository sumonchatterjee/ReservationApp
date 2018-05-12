package com.dineout.book.adapter;

import android.content.Context;
//import android.databinding.DataBindingUtil;
//import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import com.dineout.book.BR;
import com.dineout.book.R;
import com.dineout.book.model.YourBillItem;

import org.json.JSONArray;
import org.json.JSONObject;


public class YourBillAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;
    private JSONArray mData;

    public YourBillAdapter(Context context, JSONArray array){

        this.mContext = context;
        this.mData = array;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.item_your_bill, parent, false));
//        return new ViewBinder(DataBindingUtil.inflate(LayoutInflater.from(mContext),R.layout.item_your_bill,null,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        JSONObject data = mData.optJSONObject(position);
        if(data != null) {
            ((MyViewHolder) holder).setData(data);
        }
    }

    @Override
    public int getItemCount() {

        if(mData != null)
            return mData.length();
        return 0;
    }

    public void updateData(JSONArray data,int startIndex){

        if (startIndex == 0) {
            this.mData = data;
            notifyDataSetChanged();

        } else {
            if (this.mData != null) {
                JSONArray consolidatedArr = new JSONArray();

                for (int i = 0; i < this.mData.length(); i++) {
                    consolidatedArr.put(this.mData.opt(i));
                }

                for (int j = 0; j < data.length(); j++) {
                    consolidatedArr.put(data.opt(j));
                }


                this.mData = consolidatedArr;
                notifyItemRangeChanged(consolidatedArr.length() - data.length()
                        , data.length());
            }
        }
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout parentContainer;
        TextView restaurantTv;
        TextView statusTv;
        TextView dateTv;
        TextView cashBackTv;

        MyViewHolder(View itemView) {
            super(itemView);

            parentContainer = (RelativeLayout) itemView.findViewById(R.id.parent_container);
            restaurantTv = (TextView) itemView.findViewById(R.id.restaurant_tv);
            statusTv = (TextView) itemView.findViewById(R.id.status_tv);
            dateTv = (TextView) itemView.findViewById(R.id.date_tv);
            cashBackTv = (TextView) itemView.findViewById(R.id.cash_back_tv);
        }

        public void setData(JSONObject data){
            YourBillItem billItem = new YourBillItem(mContext, data);

            parentContainer.setOnClickListener(billItem.clickListener);
            restaurantTv.setText(billItem.getRestaurant());
            statusTv.setText(billItem.getStatusText());
            YourBillItem.setImageResource(statusTv, billItem.getStatus());
            dateTv.setText(billItem.getDateText());
            cashBackTv.setText(billItem.getCashbackText());
            cashBackTv.setVisibility(billItem.getCashback() ? View.VISIBLE : View.GONE);

        }
    }


}
