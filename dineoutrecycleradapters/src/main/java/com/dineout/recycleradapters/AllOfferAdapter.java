package com.dineout.recycleradapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONObject;

public class AllOfferAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private final int ITEM_TYPE_ALL_OFFER = 1;

    private Context context;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private AllOfferClickListener allOfferClickListener;
    private boolean isInstantBooking=false;

    public AllOfferAdapter(Context context, boolean isInstantBooking) {
        this.context = context;
        this.isInstantBooking=isInstantBooking;
        layoutInflater = LayoutInflater.from(context);

    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void setAllOfferClickListener(AllOfferClickListener allOfferClickListener) {
        this.allOfferClickListener = allOfferClickListener;
    }

    @Override
    protected int defineItemViewType(int position) {
        return ITEM_TYPE_ALL_OFFER;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        // Check Item Type
        if (viewType == ITEM_TYPE_ALL_OFFER) {
            return new AllOfferViewHolder(layoutInflater.inflate(R.layout.all_offer_list_item, parent, false));
        }

        return null;
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        // Get Item View Type
        int viewType = holder.getItemViewType();

        // Check View Type
        if (viewType == ITEM_TYPE_ALL_OFFER) {
            showAllOffer((AllOfferViewHolder) holder, listItem, position);
        }
    }

    private void showAllOffer(AllOfferViewHolder viewHolder, JSONObject jsonObject, int position) {
        if (viewHolder != null && jsonObject != null) {
            // Get Availability
            //int availability = jsonObject.optInt("availability", 1);
           // boolean isAvailable = (availability == 1);
            boolean showCTA = (jsonObject.optInt("showCTA", 0) == 1);

            String type = jsonObject.optString("type", "");
            int enabledOffer = jsonObject.optInt("enabled",1);
            boolean isOfferEnabled= (enabledOffer==1);

            // Set Heading
            String heading = jsonObject.optString("title_1", "");
            if (!AppUtil.isStringEmpty(heading)) {
                viewHolder.getTextViewOfferHeading().setText(heading);
                viewHolder.getTextViewOfferHeading().setVisibility(View.VISIBLE);

                // Set Top Divider
                viewHolder.getViewTopDivider().setVisibility((position == 0) ? View.GONE : View.VISIBLE);
                viewHolder.getViewBottomDivider().setVisibility(View.GONE);

            } else {
                viewHolder.getTextViewOfferHeading().setVisibility(View.GONE);
                viewHolder.getViewBottomDivider().setVisibility(View.VISIBLE);
                viewHolder.getViewTopDivider().setVisibility(View.GONE);
            }
            viewHolder.getTextViewOfferHeading().setEnabled(isOfferEnabled);

            // Set Title
            String title = jsonObject.optString("title_2", "");
            if (!AppUtil.isStringEmpty(title)) {
                title = AppUtil.renderRupeeSymbol(title).toString();
                viewHolder.getTextViewOfferTitle().setText(AppUtil.getColoredText(title,
                        ((isOfferEnabled) ? context.getResources().getColor(R.color.green) :
                                context.getResources().getColor(R.color.grey_4D))).toString());
            }
            viewHolder.getTextViewOfferTitle().setEnabled(isOfferEnabled);

            // Set Message
            String message = jsonObject.optString("title_3", "");
            if (!AppUtil.isStringEmpty(message)) {
                message = AppUtil.renderRupeeSymbol(message).toString();
                viewHolder.getTextViewOfferText().setText(AppUtil.getColoredText(message,
                        ((isOfferEnabled) ? context.getResources().getColor(R.color.green) :
                                context.getResources().getColor(R.color.grey_4D))).toString());
                viewHolder.getTextViewOfferText().setVisibility(View.VISIBLE);

            } else {
                viewHolder.getTextViewOfferText().setVisibility(View.GONE);
            }
            viewHolder.getTextViewOfferText().setEnabled(isOfferEnabled);
            if (isOfferEnabled) {
                viewHolder.getTextViewOfferText().setTextColor(context.getResources().getColor(R.color.green));
            }

            if ("additionalOffer".equalsIgnoreCase(type)) {
                String textColor = jsonObject.optString("textColor", "");
                try {
                    if (!TextUtils.isEmpty(textColor)) {
                        viewHolder.getTextViewOfferText().setTextColor(Color.parseColor(textColor));
                    } else {
                        viewHolder.getTextViewOfferText().setTextColor(ContextCompat.getColor(context,R.color.grey_85));
                    }
                } catch (Exception e) {
                    // Exception
                }

            }

            // Set Price
            int price = jsonObject.optInt("title_4", 0);
            if (price > 0) {
                viewHolder.getTextViewOfferPrice().setText(
                        String.format(context.getString(R.string.container_rupee_with_slash), Integer.toString(price)));
                viewHolder.getTextViewOfferPrice().setVisibility(View.VISIBLE);

            } else {
                viewHolder.getTextViewOfferPrice().setVisibility(View.GONE);
            }
            viewHolder.getTextViewOfferPrice().setEnabled(isOfferEnabled);

            String seeMoreText=jsonObject.optString("title_5");
            if(!AppUtil.isStringEmpty(seeMoreText)){
                viewHolder.getSeeMoreTextView().setVisibility(View.VISIBLE);
                viewHolder.getSeeMoreTextView().setText(seeMoreText);

            }
            else {
                viewHolder.getSeeMoreTextView().setVisibility(View.GONE);
            }

            // Set Offer Logo
            String offerLogo = jsonObject.optString("logoImgUrl", "");
            if (!AppUtil.isStringEmpty(offerLogo) && imageLoader != null) {
                viewHolder.getImageViewOfferLogo().setImageUrl(offerLogo, imageLoader);
                viewHolder.getImageViewOfferLogo().setVisibility(View.VISIBLE);

            } else {
                viewHolder.getImageViewOfferLogo().setVisibility(View.GONE);
            }

            // Set Background Image
            /*String backgroundImage = jsonObject.optString("backgroundImg", "");
            if (!AppUtil.isStringEmpty(backgroundImage) && imageLoader != null) {
                viewHolder.getImageViewBackground().setImageUrl(backgroundImage, imageLoader);
            }*/

            // Set Button
            int isPaid = jsonObject.optInt("is_paid", -1);
            if (isPaid == 0) {
                // Reserve or Get Offers Text based on Instant booking or not
                if(isInstantBooking)
                    viewHolder.getButtonOffer().setText(R.string.text_get_offer);
                else
                    viewHolder.getButtonOffer().setText(R.string.text_reserve);
                    viewHolder.getButtonOffer().setVisibility(View.VISIBLE);

            } else if (isPaid == 1) {
                viewHolder.getButtonOffer().setText(R.string.text_buy);
                viewHolder.getButtonOffer().setVisibility(View.VISIBLE);

            } else {
                viewHolder.getButtonOffer().setVisibility(View.GONE);
            }
            viewHolder.getButtonOffer().setEnabled(isOfferEnabled);
            viewHolder.getButtonOffer().setVisibility(showCTA ? View.VISIBLE : View.GONE);

            try {
                if (jsonObject != null) {
                    jsonObject.put("position", position);
                }
            }catch (Exception ex){

            }

            // Set Tag
            viewHolder.getButtonOffer().setTag(jsonObject);
            viewHolder.getButtonOffer().setOnClickListener((isOfferEnabled && showCTA) ? this : null);

            // Set Tag
            viewHolder.getFrameLayoutOfferSection().setTag(jsonObject);
            viewHolder.getFrameLayoutOfferSection().setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.frameLayout_offer_section) {
            handleCardClick((JSONObject) view.getTag());

        } else if (viewId == R.id.button_offer) {
            handleOfferButtonClick((JSONObject) view.getTag());
        }
    }

    private void handleCardClick(JSONObject jsonObject) {
        // Check for NULL
        if (allOfferClickListener != null) {
            allOfferClickListener.onCardClick(jsonObject);
        }
    }

    private void handleOfferButtonClick(JSONObject jsonObject) {
        // Check for NULL
        if (allOfferClickListener != null) {
            allOfferClickListener.onOfferButtonClick(jsonObject);
        }
    }

    public interface AllOfferClickListener {
        void onCardClick(JSONObject jsonObject);

        void onOfferButtonClick(JSONObject jsonObject);
    }

    private class AllOfferViewHolder extends RecyclerView.ViewHolder {
        private View viewTopDivider;
        private FrameLayout frameLayoutOfferSection;
        private NetworkImageView imageViewBackground;
        private TextView textViewOfferHeading;
        private NetworkImageView imageViewOfferLogo;
        private TextView textViewOfferPrice;
        private TextView buttonOffer;
        private TextView textViewOfferTitle;
        private TextView textViewOfferText;
        private View viewBottomDivider;
        private TextView seeMoreTextView;

        public AllOfferViewHolder(View itemView) {
            super(itemView);

            viewTopDivider = itemView.findViewById(R.id.view_top_divider);
            frameLayoutOfferSection = (FrameLayout) itemView.findViewById(R.id.frameLayout_offer_section);
            frameLayoutOfferSection.setClickable(true);
            imageViewBackground = (NetworkImageView) itemView.findViewById(R.id.imageView_background);
            textViewOfferHeading = (TextView) itemView.findViewById(R.id.textView_offer_heading);
            textViewOfferPrice = (TextView) itemView.findViewById(R.id.textView_offer_price);
            imageViewOfferLogo = (NetworkImageView) itemView.findViewById(R.id.imageView_offer_logo);
            buttonOffer = (TextView) itemView.findViewById(R.id.button_offer);
            textViewOfferTitle = (TextView) itemView.findViewById(R.id.textView_offer_title);
            textViewOfferText = (TextView) itemView.findViewById(R.id.textView_offer_text);
            viewBottomDivider = itemView.findViewById(R.id.view_bottom_divider);
            seeMoreTextView= (TextView) itemView.findViewById(R.id.textView_offer_seemore);
        }

        public View getViewTopDivider() {
            return viewTopDivider;
        }

        public FrameLayout getFrameLayoutOfferSection() {
            return frameLayoutOfferSection;
        }

        public NetworkImageView getImageViewBackground() {
            return imageViewBackground;
        }

        public TextView getTextViewOfferHeading() {
            return textViewOfferHeading;
        }

        public TextView getTextViewOfferPrice() {
            return textViewOfferPrice;
        }

        public NetworkImageView getImageViewOfferLogo() {
            return imageViewOfferLogo;
        }

        public TextView getButtonOffer() {
            return buttonOffer;
        }

        public TextView getTextViewOfferTitle() {
            return textViewOfferTitle;
        }

        public TextView getTextViewOfferText() {
            return textViewOfferText;
        }

        public TextView getSeeMoreTextView() {
            return seeMoreTextView;
        }



        public View getViewBottomDivider() {
            return viewBottomDivider;
        }
    }
}
