package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.view.widgets.MyReviewTextView;
import com.dineout.recycleradapters.view.widgets.OtherReviewTextView;
import com.dineout.recycleradapters.view.widgets.RoundedImageView;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestaurantReviewPageAdapter extends BaseRecyclerAdapter {

    private final int ITEM_TYPE_REVIEW = 102;

    private Context mContext;

    private RestaurantReviewsClickListener clickListener;
    private OtherReviewTextView.OtherReviewTextViewCallback mOtherReviewTextViewCallback;

    // Constructor
    public RestaurantReviewPageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setRestaurantReviewsClickListener(RestaurantReviewsClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        // Check on position
        return ITEM_TYPE_REVIEW;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check for Item Type
        return new ReviewViewHolder(LayoutInflater.from(mContext).
                inflate(R.layout.review_list_item1, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get View Type
        showReviewSection((ReviewViewHolder) holder, listItem, position);
    }


    private void showReviewSection(ReviewViewHolder viewHolder, JSONObject listItem, int position) {
        if (viewHolder != null && listItem != null) {
            // Get Data
            final JSONObject dataJsonObject = listItem.optJSONObject("data");

            if (dataJsonObject != null) {

                // Set Image
                viewHolder.getImageViewReviewAuthorImage().setDefaultImageResId(R.drawable.img_profile_nav_default_square);
//                if (!AppUtil.isStringEmpty(dataJsonObject.optString("profileImage"))) {
                    viewHolder.getImageViewReviewAuthorImage().setImageUrl(dataJsonObject.optString("profileImage"),
                            ImageRequestManager.getInstance(mContext).getImageLoader());
//                }

                // Set User Name
                String userNameText = dataJsonObject.optString("userName");
                if (TextUtils.isEmpty(userNameText)) userNameText = "";
                viewHolder.getTextViewReviewAuthorName().setText(userNameText);

                String visitedText = dataJsonObject.optString("vistedDateStr");
                if (TextUtils.isEmpty(visitedText)) visitedText = "";
                viewHolder.getTextViewReviewDateTime().setText(visitedText);

                // Set Rating
                // Check if Rating is greater than 0
                if (AppUtil.hasNoRating(dataJsonObject.optString("rating"))) {
                    // Hide Rating Layout
                    viewHolder.getTextViewReviewRating().setVisibility(RelativeLayout.GONE);
                } else {
                    // Show Rating Layout
                    viewHolder.getTextViewReviewRating().setVisibility(RelativeLayout.VISIBLE);

                    // Set Rating
                    viewHolder.getTextViewReviewRating().setRating(Float.valueOf(dataJsonObject.optString("rating")));
                    viewHolder.getTextViewReviewRating().setNumStars(5);
                }

                // Review Text
                String reviewText = dataJsonObject.optString("reviewText", "");
                if (getJsonArray() != null && position < getJsonArray().length()
                        && getJsonArray().optJSONObject(position) != null
                        && getJsonArray().optJSONObject(position).optJSONObject("data") != null
                        && getJsonArray().optJSONObject(position).optJSONObject("data").optBoolean("myReview")) {
                    viewHolder.getMyReviewTv().setVisibility(View.VISIBLE);
                    viewHolder.getOtherReviewTv().setVisibility(View.GONE);

                    viewHolder.getMyReviewTv().setText(reviewText);

                    viewHolder.getMyReviewTv().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            clickListener.onEditReviewClick(dataJsonObject);
                        }
                    });
                } else {
                    viewHolder.getMyReviewTv().setVisibility(View.GONE);
                    viewHolder.getOtherReviewTv().setVisibility(View.VISIBLE);

                    viewHolder.getOtherReviewTv().setText(reviewText);
                    viewHolder.getOtherReviewTv().setOtherReviewTextViewCallback(mOtherReviewTextViewCallback);
                }


                // review status
                if (dataJsonObject.optBoolean("myReview", false)) {
                    int reviewStatus = dataJsonObject.optInt("reviewStatus");
                    ReviewStatus rs = viewHolder.getReviewStatus(reviewStatus);

                    viewHolder.getTextViewReviewType().setText(rs.statusText);
                    viewHolder.getTextViewReviewType().setBackgroundResource(rs.statusBgColor);
                } else {
                    String reviewType = dataJsonObject.optString("reviewType");
                    try {
                        if ("Critic".equalsIgnoreCase(reviewType)) {
                            dataJsonObject.putOpt("reviewStatus", 3);
                        } else {
                            dataJsonObject.putOpt("reviewStatus", -1);
                        }
                    } catch (Exception e) {
                        // Exception
                    }


                    int reviewStatus = dataJsonObject.optInt("reviewStatus");
                    ReviewStatus rs = viewHolder.getReviewStatus(reviewStatus);

                    viewHolder.getTextViewReviewType().setText(rs.statusText);
                    viewHolder.getTextViewReviewType().setBackgroundResource(rs.statusBgColor);
                }


//                // Set Type
//                String reviewType = dataJsonObject.optString("reviewType");
//                if (AppUtil.isStringEmpty(reviewType)) {
//                    viewHolder.getTextViewReviewType().setVisibility(View.INVISIBLE);
//                } else {
//                    // Set Text
//                    viewHolder.getTextViewReviewType().setText(reviewType);
//
//                    // Set Background
//                    if (reviewType.equalsIgnoreCase(AppConstant.REVIEW_TYPE_CRITIC)) {
//                        viewHolder.getTextViewReviewType().setBackgroundResource(R.drawable.round_rect_critic_shape);
//                    } else if (reviewType.equalsIgnoreCase(AppConstant.REVIEW_TYPE_TRIP_ADVISOR)) {
//                        viewHolder.getTextViewReviewType().setBackgroundResource(R.drawable.round_rect_trip_advisor_shape);
//                    } else {
//                        viewHolder.getTextViewReviewType().setBackgroundResource(R.drawable.round_rect_authentic_shape);
//                    }
//                }

                // set the feedback like icon
                String thumbStatus = dataJsonObject.optString("tagLike", "0");
                int likeIcon = "1".equals(thumbStatus) ? R.drawable.thumb_up : R.drawable.thumb_down;
                viewHolder.getLikeStatusIv().setImageResource(likeIcon);

                // add the feedback tags
                viewHolder.getFeedbackTagsWrapper().removeAllViews();
                JSONArray reviewTags = dataJsonObject.optJSONArray("reviewTags");
                if (reviewTags != null && reviewTags.length() > 0) {
                    viewHolder.getLikeStatusIv().setVisibility(View.VISIBLE);

                    for (int i = 0; i < reviewTags.length(); i++) {
                        View view =  LayoutInflater.from(mContext).
                                inflate(R.layout.feedback_tag_item_layout, null, false);

                        ((TextView) view.findViewById(R.id.tags_tv)).setText(reviewTags.optJSONObject(i).optString("value"));

                        if (i == 0) {
                            view.findViewById(R.id.tag_dot).setVisibility(View.GONE);
                        } else {
                            view.findViewById(R.id.tag_dot).setVisibility(View.VISIBLE);
                        }

                        viewHolder.getFeedbackTagsWrapper().addView(view);
                    }
                } else {
                    viewHolder.getLikeStatusIv().setVisibility(View.GONE);
                }
            }
        }
    }

    public void setOtherReviewTextViewCallback(OtherReviewTextView.OtherReviewTextViewCallback otherReviewTextViewCallback) {
        this.mOtherReviewTextViewCallback = otherReviewTextViewCallback;
    }

    public interface RestaurantReviewsClickListener {
        void onEditReviewClick(JSONObject obj);
    }

    // Other Review
    private class ReviewViewHolder extends RecyclerView.ViewHolder {
        private RoundedImageView imageViewReviewAuthorImage;
        private TextView textViewReviewAuthorName;
        private TextView textViewReviewDateTime;
        private RatingBar ratingBar;
        private MyReviewTextView myReviewTv;
        private OtherReviewTextView otherReviewTv;
        private TextView textViewReviewType;
        private ImageView likeStatusIv;
        private LinearLayout feedbackTagsWrapper;

        ReviewViewHolder(View itemView) {
            super(itemView);

            imageViewReviewAuthorImage = (RoundedImageView) itemView.findViewById(R.id.imageView_review_author_image);
            textViewReviewAuthorName = (TextView) itemView.findViewById(R.id.textView_review_author_name);
            textViewReviewDateTime = (TextView) itemView.findViewById(R.id.textView_review_date_time);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
            myReviewTv = (MyReviewTextView) itemView.findViewById(R.id.textView_review_tv);
            otherReviewTv = (OtherReviewTextView) itemView.findViewById(R.id.textView_review_etv);
            textViewReviewType = (TextView) itemView.findViewById(R.id.textView_review_type);
            likeStatusIv = (ImageView) itemView.findViewById(R.id.like_status_iv);
            feedbackTagsWrapper = (LinearLayout) itemView.findViewById(R.id.feedback_tags_layout);
        }

        RoundedImageView getImageViewReviewAuthorImage() {
            return imageViewReviewAuthorImage;
        }

        TextView getTextViewReviewAuthorName() {
            return textViewReviewAuthorName;
        }

        TextView getTextViewReviewDateTime() {
            return textViewReviewDateTime;
        }

        RatingBar getTextViewReviewRating() {
            return ratingBar;
        }

        MyReviewTextView getMyReviewTv() {
            return myReviewTv;
        }

        OtherReviewTextView getOtherReviewTv() {
            return otherReviewTv;
        }

        TextView getTextViewReviewType() {
            return textViewReviewType;
        }

        ImageView getLikeStatusIv() {
            return likeStatusIv;
        }

        LinearLayout getFeedbackTagsWrapper() {
            return feedbackTagsWrapper;
        }

        ReviewStatus getReviewStatus(int status) {
            ReviewStatus returnValue;
            switch (status) {
                case 0:
                    returnValue = new ReviewStatus("Moderation", R.color.grey_84);
                    break;

                case 1:
                    returnValue = new ReviewStatus("Published", R.color.green_2f);
                    break;

                case 2:
                    returnValue = new ReviewStatus("Rejected", R.color.orange_fa);
                    break;

                case 3:
                    returnValue = new ReviewStatus("Critic", R.color.orange_fa);
                    break;

                default:
                    returnValue = new ReviewStatus("", R.color.transparent);
            }

            return returnValue;
        }
    }

    private class ReviewStatus {
        String statusText;
        int statusBgColor;

        ReviewStatus(String statusText, int statusBgColor) {
            this.statusText = statusText;
            this.statusBgColor = statusBgColor;
        }
    }
}
