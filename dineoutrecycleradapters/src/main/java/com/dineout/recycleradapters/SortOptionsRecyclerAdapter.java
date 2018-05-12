package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SortOptionsRecyclerAdapter extends BaseRecyclerAdapter
        implements View.OnClickListener {

    private final int ITEM_VIEW_TYPE_SORT = 1;

    private Context context;
    private LayoutInflater layoutInflater;
    private SortOptionHandler sortOptionHandler;
    private Drawable iconDrawable;

    // Constructor
    public SortOptionsRecyclerAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    private InputStream getInputStream(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.connect();
        return connection.getInputStream();
    }

    public void setSortOptionHandler(SortOptionHandler sortOptionHandler) {
        this.sortOptionHandler = sortOptionHandler;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_VIEW_TYPE_SORT;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new SortOptionViewHolder(layoutInflater.inflate(R.layout.sort_option_layout, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        showSortOption((SortOptionViewHolder) holder, listItem);
    }

    private void showSortOption(SortOptionViewHolder viewHolder, JSONObject listItem) {
        if (viewHolder != null && listItem != null) {
            // Set Sort Icon
            String selectedIconUrl = listItem.optString("url_selected");
            //selectedIconUrl = "http://d3tfancs2fcmmi.cloudfront.net/assets/images/uploads/misc/2016/Jun/20/ic_parking.png";

            String deSelectedIconUrl = listItem.optString("url_deselected");
            //deSelectedIconUrl = "http://d3tfancs2fcmmi.cloudfront.net/assets/images/uploads/misc/2016/Jun/20/ic_alcohol.png";

            boolean isApplied = (listItem.optInt("applied", 0) == 1);

            // Check if Icon URL is available
            if (AppUtil.isStringEmpty(selectedIconUrl) || AppUtil.isStringEmpty(deSelectedIconUrl)) {
                // Hide Icon
                viewHolder.getImageViewSortIcon().setVisibility(View.GONE);

            } else {
                // Set Current Icon
                viewHolder.getImageViewSortIcon().
                        setImageUrl(((isApplied) ? selectedIconUrl : deSelectedIconUrl),
                                ImageRequestManager.getInstance(context).getImageLoader());

                // Show Icon
                viewHolder.getImageViewSortIcon().setVisibility(View.VISIBLE);
            }

            // Set Sort Name
            String sortName = listItem.optString("name");
            if (!AppUtil.isStringEmpty(sortName)) {
                viewHolder.getTextViewSortName().setText(sortName);
            }

            // Set Selected
            viewHolder.getTextViewSortName().setSelected(isApplied);
            viewHolder.getImageViewSortIcon().setSelected(isApplied);

            // Set On Click
            viewHolder.getRootView().setTag(listItem);
            viewHolder.getRootView().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.linearLayout_sort_option) {
            handleSortOptionClick((JSONObject) view.getTag());
        }
    }

    private void handleSortOptionClick(JSONObject listItem) {
        if (sortOptionHandler != null) {
            sortOptionHandler.onSortOptionClick(listItem);
        }
    }

    public interface SortOptionHandler {
        void onSortOptionClick(JSONObject listItem);
    }

    private class SortOptionViewHolder extends RecyclerView.ViewHolder {
        private View rootView;
        private NetworkImageView imageViewSortIcon;
        private TextView textViewSortName;

        public SortOptionViewHolder(View itemView) {
            super(itemView);

            rootView = itemView.findViewById(R.id.linearLayout_sort_option);
            imageViewSortIcon = (NetworkImageView) itemView.findViewById(R.id.imageView_sort_icon);
            textViewSortName = (TextView) itemView.findViewById(R.id.textView_sort_name);
        }

        public View getRootView() {
            return rootView;
        }

        public NetworkImageView getImageViewSortIcon() {
            return imageViewSortIcon;
        }

        public TextView getTextViewSortName() {
            return textViewSortName;
        }
    }
}
