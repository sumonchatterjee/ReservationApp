package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;

import org.json.JSONArray;
import org.json.JSONObject;

public class DealSortByAdapter extends BaseRecyclerAdapter {

    public static String sortQuery;
    ImageLoader imageLoader;
    private JSONArray sortByList;
    private Context mContext;
    private LayoutInflater inflater;
    private View.OnClickListener mClickListener;


    public DealSortByAdapter(Context context, View.OnClickListener clickListener, ImageLoader imageLoader) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        mClickListener = clickListener;
        this.imageLoader = imageLoader;
    }

    public static String getSortQuery() {
        return sortQuery;
    }

    public static void setSortQuery(String query) {
        sortQuery = query;
    }

    public void setSortResult(JSONArray result) {
        this.sortByList = result;
        setJsonArray(result);
        notifyDataSetChanged();
    }


    public JSONObject getItem(int position) {
        return getJsonArray() != null && getJsonArray().length() > position ?
                getJsonArray().optJSONObject(position) : null;
    }

    public JSONObject getData(int position) {
        return sortByList.optJSONObject(position);
    }

    @Override
    protected int defineItemViewType(int position) {
        return 1;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new DealSortVH(
                inflater.inflate(R.layout.sort_row, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        ((DealSortVH) holder).bindView(position, getData(position));
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    class DealSortVH extends RecyclerView.ViewHolder {
        private TextView labelTv;
        private NetworkImageView checkIcon;


        public DealSortVH(View itemView) {
            super(itemView);
            labelTv = (TextView) itemView.findViewById(R.id.sortby_tv);
            checkIcon = (NetworkImageView) itemView.findViewById(R.id.sort_icon);
            itemView.setOnClickListener(mClickListener);


        }

        public void bindView(int position, JSONObject data) {
            itemView.setTag(data.optString("type"));
            labelTv.setText(data.optString("displayType"));
            labelTv.setTag(data.optString("type"));




//            if (!TextUtils.isEmpty(DOPreferences.getSortBy(mContext))) {
//                if (DOPreferences.getSortBy(mContext).equalsIgnoreCase(data.optString("type"))) {

            if (!TextUtils.isEmpty(getSortQuery())) {
                if (getSortQuery().equalsIgnoreCase(data.optString("type"))) {

                    labelTv.setTextColor(mContext.getResources().getColor(R.color.red));
                    String imageUrl = data.optString("iconUrl");
                    checkIcon.setImageUrl
                            (imageUrl, getImageLoader());
                } else {
                    labelTv.setTextColor(mContext.getResources().getColor(R.color.greys));
                    if (!TextUtils.isEmpty(data.optString("inActiveIconUrl"))) {
                        String imageUrl = data.optString("inActiveIconUrl");
                        checkIcon.setImageUrl
                                (imageUrl, getImageLoader());
                    }
                }

            }else{
                if (!TextUtils.isEmpty(data.optString("inActiveIconUrl"))){
                    checkIcon.setImageUrl
                            (data.optString("inActiveIconUrl"), getImageLoader());
                }
            }


        }


    }


}
