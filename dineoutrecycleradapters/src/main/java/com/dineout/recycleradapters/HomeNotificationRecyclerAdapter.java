package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;



/**
 * Created by sumon.chatterjee on 10/04/17.
 */

public class HomeNotificationRecyclerAdapter extends BaseRecyclerAdapter implements View.OnClickListener {
    private Context mContext;
    private DineoutNetworkManager networkManager;
    private ImageLoader imageLoader;
    private JSONObject headerJsonObject;
    OnNotificationClickedListener onNotificationClickListener;

    private static final int ITEM_TYPE_REST_LIST = 101;


    public HomeNotificationRecyclerAdapter(Context context) {
        mContext = context;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager, ImageLoader imageLoader) {
        this.networkManager = networkManager;
        this.imageLoader = imageLoader;
    }


    public void setHeaderJsonObject(JSONObject headerJsonObject) {
        this.headerJsonObject = headerJsonObject;

        // Set Header Flag
        setHeader(headerJsonObject != null);
    }



    public OnNotificationClickedListener getOnCardClickedListener() {
        return onNotificationClickListener;
    }

    public void setOnCardClickedListener(OnNotificationClickedListener onNotificationsClickListener) {
        this.onNotificationClickListener = onNotificationsClickListener;
    }



    public void setData(JSONArray rest_arr, int startIndex, JSONObject headerJsonObject) {
        // Check for First Call
        if (startIndex == 0) {
            setJsonArray(rest_arr);
            setHeaderJsonObject(headerJsonObject);
            notifyDataSetChanged();

        } else {
            if (getJsonArray() != null) {
                JSONArray consolidatedArr = new JSONArray();

                for (int i = 0; i < getJsonArray().length(); i++) {
                    consolidatedArr.put(getJsonArray().opt(i));
                }

                for (int j = 0; j < rest_arr.length(); j++) {
                    consolidatedArr.put(rest_arr.opt(j));
                }

                setJsonArray(consolidatedArr);

                notifyItemRangeChanged(consolidatedArr.length() - rest_arr.length(), rest_arr.length());
            }
        }
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_REST_LIST) {
            return new NotificationViewHolder(LayoutInflater.from(mContext.getApplicationContext())
                    .inflate(R.layout.adapter_item_notification, parent, false));

        }
        return null;
    }

    @Override
    protected int defineItemViewType(int position) {
        if (getJsonArray() == null) {
            return ITEM_VIEW_TYPE_EMPTY;
        } else {
            return ITEM_TYPE_REST_LIST;
        }
    }


    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_REST_LIST) {
            inflateNotificationList(listItem, (HomeNotificationRecyclerAdapter.NotificationViewHolder) holder,position);

        }
    }


     // render notification item

    private void inflateNotificationList(JSONObject itemObject, HomeNotificationRecyclerAdapter.NotificationViewHolder viewHolder,int position) {

        if(itemObject!=null) {

            int isViewed=itemObject.optInt("is_viewed");
            if(isViewed == 1){
                viewHolder.getMainContainer().setBackgroundColor(mContext.getResources().getColor(R.color.grey_cb));
            }else{
                viewHolder.getMainContainer().setBackgroundColor(mContext.getResources().getColor(R.color.white));
            }
            try {
                itemObject.put("position", position);
            }catch (Exception ex){
                // exception
            }

            viewHolder.getNotificationIcon().setImageUrl(itemObject.optString("icon"), imageLoader);
            viewHolder.getNotificationTitleTextView().setText(itemObject.optString("title"));
            viewHolder.getNotificationDetailTextView().setText(itemObject.optString("message"));


            viewHolder.getCardView().setTag(itemObject);
            viewHolder.getCardView().setOnClickListener(this);

            long  timeInSec = itemObject.optLong("timestamp");
            long timeInmillis = timeInSec * 1000; //IST

            long diff =  System.currentTimeMillis() - timeInmillis;
            if(diff < 0){
                viewHolder.getNotificationTimeTextView().setVisibility(View.GONE);

            }else{
                viewHolder.getNotificationTimeTextView().setVisibility(View.VISIBLE);
                long oneDay = 1000 * 60 * 60 * 24;
                long oneHour = 1000 * 60 * 60;
                long oneMin = 1000 * 60;
                long oneSec = 1000;

                long days = (long) (diff / oneDay);
                long hours = (long) ((diff - days * oneDay) / oneHour);
                long mins = (long) ((diff - days * oneDay - hours * oneHour) / oneMin);
                long secs = (long) ((diff - days * oneDay - hours * oneHour - mins * oneMin) / oneSec);

                //rounding off time

                if (secs > 30) {
                    mins++;
                }
                if (mins > 30) {
                    hours++;
                }
                if (hours > 12) {
                    days++;
                }

                if (days > 0) {
                    viewHolder.getNotificationTimeTextView().setText(String.valueOf(days + " d ago"));
                } else if (hours > 0) {
                    viewHolder.getNotificationTimeTextView().setText(String.valueOf(hours + " hr"));
                } else if (mins > 0) {
                    viewHolder.getNotificationTimeTextView().setText(String.valueOf(mins + " m"));
                } else if (secs > 0) {
                    viewHolder.getNotificationTimeTextView().setText(String.valueOf(secs + " s"));
                }
            }



        }

    }


    public interface OnNotificationClickedListener {
        void onItemClicked(JSONObject jsonObject);
    }



    private class NotificationViewHolder extends RecyclerView.ViewHolder {
        private View cardView;
        private RelativeLayout relativeLayoutSearchCardParentLayout;
        private RelativeLayout mainContainer;

        private NetworkImageView notificationLogo;
        private TextView notificationTitle;
        private TextView notificationDetail;
        private TextView notificationTime;


        public NotificationViewHolder(View itemView) {
            super(itemView);
            cardView = itemView;
            instantiateViews();
        }

        protected void instantiateViews() {

            mainContainer = (RelativeLayout) cardView.findViewById(R.id.main_container);

            relativeLayoutSearchCardParentLayout = (RelativeLayout) cardView.findViewById(R.id.relativeLayoutSearchCardParentLayout);

            notificationLogo = (NetworkImageView) cardView.findViewById(R.id.notification_icon);

            notificationLogo.setVisibility(NetworkImageView.VISIBLE);

            //Set Featured Text
            notificationTitle = (TextView) cardView.findViewById(R.id.title_txtvw);

            //Set Title
            notificationDetail = (TextView) cardView.findViewById(R.id.detail_txtvw);

            notificationTime = (TextView) cardView.findViewById(R.id.time_txtvw);


        }


        public View getCardView() {
            return cardView;
        }

        public RelativeLayout getRelativeLayoutSearchCardParentLayout() {
            return relativeLayoutSearchCardParentLayout;
        }

        public RelativeLayout getMainContainer(){
            return mainContainer;
        }

        public NetworkImageView getNotificationIcon() {
            return notificationLogo;
        }


        public TextView getNotificationTitleTextView() {
            return notificationTitle;
        }

        public TextView getNotificationDetailTextView() {
            return notificationDetail;
        }

        public TextView getNotificationTimeTextView() {
            return notificationTime;
        }

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        JSONObject itemObject = (JSONObject) v.getTag();
        getOnCardClickedListener().onItemClicked(itemObject);
    }
}
