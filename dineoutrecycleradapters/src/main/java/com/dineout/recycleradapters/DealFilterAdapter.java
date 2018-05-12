package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.analytics.utilities.DOLog;
import com.dineout.recycleradapters.util.JSONUtil;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DealFilterAdapter extends BaseRecyclerAdapter {

    public static String filterQuery;
    public static int dinerCount;
    private Context mContext;
    private DineoutNetworkManager networkManager;
    private LayoutInflater inflater;
    private JSONObject category;
    private List<String> selectedCategories;

    public DealFilterAdapter(Context context, List<String> selectedCategories) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.selectedCategories = selectedCategories;
    }

    public static String getFilterQuery() {
        return filterQuery;
    }

    public static void setFilterQuery(String query) {
        filterQuery = query;
    }

    public static int getDinerCount() {
        return dinerCount;
    }

    public static void setDinerCount(int dinerCount) {
        dinerCount = dinerCount;
    }

    public void setNetworkManager(DineoutNetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void setData(JSONObject responseObject) {
        JSONArray filters = null;
        category = null;

        JSONObject filterData = JSONUtil.getFilters(responseObject);
        if (filterData != null && !filterData.isNull("byCategory")) {
            JSONObject byCategory = filterData.optJSONObject("byCategory");
            category = byCategory.optJSONObject("category");
            filters = byCategory.optJSONArray("categoryArray");
        }

        setJsonArray(filters);
        notifyDataSetChanged();
    }


    @Override
    protected int defineItemViewType(int position) {
        return 1;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new DealSearchVH(
                inflater.inflate(R.layout.deal_filter, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((DealSearchVH) holder).bindView(position);
    }

    class DealSearchVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView labelTv;
        private ImageView checkIcon;
        private String categoryName;

        public DealSearchVH(View itemView) {
            super(itemView);
            labelTv = (TextView) itemView.findViewById(R.id.tv_label);
            checkIcon = (ImageView) itemView.findViewById(R.id.check_icon);
            checkIcon.setImageResource(R.drawable.check_icon);
        }

        public void bindView(int position) {

            int filterCount = 0;
            categoryName = getJsonArray().optString(position);

            try {
                filterCount = category.optInt(categoryName.toString());
            } catch (Exception ex) {
                DOLog.d("Parsing:", ex);
            }

            if (filterCount == 0) {
                labelTv.setText(categoryName);
                labelTv.setEnabled(false);

            } else {
                labelTv.setText(categoryName + " (" + filterCount + ")");
                labelTv.setEnabled(true);
                labelTv.setOnClickListener(this);
            }

            setTickMark();
        }

        private void setTickMark() {
            checkIcon.setVisibility(selectedCategories.contains(categoryName) ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View v) {
            if (selectedCategories.contains(categoryName)) {
                selectedCategories.remove(categoryName);


            } else {
                selectedCategories.add(categoryName);
                AnalyticsHelper.getAnalyticsHelper(mContext).trackEventGA(mContext.getString(R.string.ga_screen_deal_filters),mContext.getString(R.string.ga_action_tag_added),categoryName);

            }
            setTickMark();

        }
    }
}
