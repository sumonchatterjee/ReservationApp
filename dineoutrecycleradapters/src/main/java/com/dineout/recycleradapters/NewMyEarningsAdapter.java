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

import org.json.JSONObject;

import static com.dineout.recycleradapters.util.AppUtil.setTextViewInfo;

public class NewMyEarningsAdapter extends BaseRecyclerAdapter {

    private static final int ITEM_TYPE_EARNING = 0;
    private NewMyEarningsAdapterCallback mCallback;
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private JSONObject mSectionsData;

    public NewMyEarningsAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public interface NewMyEarningsAdapterCallback {
        void openDeepLink(JSONObject object);
    }

    @Override
    protected int defineItemViewType(int position) {
        int returnValue;
        if (getJsonArray() != null && getJsonArray().optJSONObject(position) != null
                && getJsonArray().optJSONObject(position).optString("section_type").equalsIgnoreCase("list_row")) {
            returnValue = ITEM_TYPE_EARNING;

        } else {
            returnValue = ITEM_VIEW_TYPE_EMPTY;
        }

        return returnValue;
    }


    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder returnValue;
        if (viewType == ITEM_TYPE_EARNING) {
            View view = mInflater.inflate(R.layout.new_my_earnings_item1, parent, false);
            returnValue = new MyEarningViewHolder(view);

        } else {
            returnValue = null;
        }

        return returnValue;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        ((MyEarningViewHolder) holder).bindData(listItem, position);
    }

    private JSONObject getSectionDataDetails(String sectionKey) {
        // Check on Section Key
        if (!TextUtils.isEmpty(sectionKey) && mSectionsData != null) {
            return mSectionsData.optJSONObject(sectionKey);
        } else {
            return null;
        }
    }

    private class MyEarningViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ViewGroup wrapper;
        TextView titleTv;
        TextView subTitleTv1;
        TextView subTitleTv2;
        TextView earningAmountTv;
        NetworkImageView earningBreakUpIv;
        TextView expireTv;
        TextView earningStatusTv;
        NetworkImageView earningStatusIv;

        MyEarningViewHolder(View itemView) {
            super(itemView);

            wrapper = (ViewGroup) itemView.findViewById(R.id.wrapper_layout);
            titleTv = (TextView) itemView.findViewById(R.id.title_text_tv);
            subTitleTv1 = (TextView) itemView.findViewById(R.id.sub_title_1_text_tv);
            subTitleTv2 = (TextView) itemView.findViewById(R.id.sub_title_2_text_tv);
            earningAmountTv = (TextView) itemView.findViewById(R.id.earning_amount_text_tv);
            earningBreakUpIv = (NetworkImageView) itemView.findViewById(R.id.earning_break_up_icon_iv);
            expireTv = (TextView) itemView.findViewById(R.id.expire_text_tv);
            earningStatusTv = (TextView) itemView.findViewById(R.id.earning_status_text_tv);
            earningStatusIv = (NetworkImageView) itemView.findViewById(R.id.earning_status_icon_iv);
        }

        public void bindData(JSONObject itemObj, int pos) {

            if (itemObj != null) {
                String sectionKey = itemObj.optString("section_key");
                if (getSectionDataDetails(sectionKey) != null) {
                    JSONObject sectionData = getSectionDataDetails(sectionKey).optJSONObject("data");

                    if (sectionData != null) {
                        // title text
                        setTextViewInfo(titleTv, sectionData.optJSONObject("title"));
//                        titleTv.setText(String.valueOf(pos));

                        // subtitle 1 text
                        setTextViewInfo(subTitleTv1, sectionData.optJSONObject("sub_title_1"));

                        // subtitle 2 text
                        setTextViewInfo(subTitleTv2, sectionData.optJSONObject("sub_title_2"));

                        // earning amount text
                        setTextViewInfo(earningAmountTv, sectionData.optJSONObject("amount"));

                        // status text
                        setTextViewInfo(earningStatusTv, sectionData.optJSONObject("status_text"));

                        // status icon
                        String statusUrl = sectionData.optString("status_icon");
                        if (TextUtils.isEmpty(statusUrl)) {
                            earningStatusIv.setVisibility(View.GONE);
                        } else {
                            earningStatusIv.setVisibility(View.VISIBLE);
                            earningStatusIv.setImageUrl(statusUrl, mImageLoader);
                        }

                        // expire text
                        setTextViewInfo(expireTv, sectionData.optJSONObject("expiry"));

                        // more info available or not
                        if (sectionData.optInt("cta", 0) != 0) {
                            earningBreakUpIv.setVisibility(View.VISIBLE);
                            earningBreakUpIv.setImageResource(R.drawable.new_earning_right_arrow);
                            wrapper.setTag(sectionData.optJSONObject("cta_data"));
                            wrapper.setOnClickListener(this);

                        } else {
                            earningBreakUpIv.setVisibility(View.GONE);
                            wrapper.setOnClickListener(null);
                        }
                    }
                }
            }
        }

        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.wrapper_layout) {
                if (mCallback != null && v.getTag() != null) {
                    mCallback.openDeepLink((JSONObject) v.getTag());
                }
            }
        }
    }

    public JSONObject getSectionsData() {
        return this.mSectionsData;
    }
    public void setSectionsData(JSONObject sectionsData) {
        this.mSectionsData = sectionsData;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.mImageLoader = imageLoader;
    }
    public void setCallback(NewMyEarningsAdapterCallback callback) {
        this.mCallback = callback;
    }
}
