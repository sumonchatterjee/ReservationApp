package com.dineout.recycleradapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.recycleradapters.util.AppUtil;
import com.dineout.recycleradapters.view.widgets.NetworkRoundCornerImageView;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RestaurantDetailEventPageAdapter extends BaseRecyclerAdapter implements View.OnClickListener {

    private final int ITEM_TYPE_EVENTS = 1;
    private final String TICKET_COUNT = "TICKET_COUNT";
    private final String EVENT_POSITION = "EVENT_POSITION";
    private String source = "";

    private Context context;
    private LayoutInflater layoutInflater;
    private EventClickHandler eventClickHandler;

    // Constructor
    public RestaurantDetailEventPageAdapter(Context context, EventClickHandler eventClickHandler) {
        this.context = context;
        this.eventClickHandler = eventClickHandler;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setData(JSONArray eventsJsonArray) {
        setJsonArray(eventsJsonArray);
    }


    public void setSource(String source) {
        this.source = source;
    }


    @Override
    protected int defineItemViewType(int position) {
        // Check for List if valid
        if (getJsonArray() != null && getJsonArray().length() > 0) {
            return ITEM_TYPE_EVENTS;
        }

        return ITEM_VIEW_TYPE_EMPTY;
    }

    @Override
    protected RecyclerView.ViewHolder defineViewHolder(ViewGroup parent, int viewType) {
        return new EventsViewHolder(layoutInflater.inflate(R.layout.event_list_item, parent, false));
    }

    @Override
    protected void renderListItem(RecyclerView.ViewHolder holder, JSONObject listItem, int position) {
        renderEvents((EventsViewHolder) holder, listItem, position);
    }

    private void renderEvents(EventsViewHolder viewHolder, JSONObject jsonObject, int position) {
        if (jsonObject != null && viewHolder != null) {
            // Get Media
            JSONObject mediaJsonObject = jsonObject.optJSONObject("media");

            if (mediaJsonObject != null) {
                // Get Ticket Images
                JSONArray ticketJsonArray = mediaJsonObject.optJSONArray("ticketImages");

                if (ticketJsonArray != null && ticketJsonArray.length() > 0) {
                    // Get First Object
                    JSONObject ticketJsonObject = ticketJsonArray.optJSONObject(0);

                    if (ticketJsonObject != null) {
                        String imageUrl = ticketJsonObject.optString("imageUrl", "");
                        if (!AppUtil.isStringEmpty(imageUrl)) {
                            // Set Background Image
                            viewHolder.getImageViewEvent().setImageUrl(imageUrl,
                                    ImageRequestManager.getInstance(context).getImageLoader());
                        } else {
                            // Reset Background Image
                            viewHolder.getImageViewEvent().setImageUrl("",
                                    ImageRequestManager.getInstance(context).getImageLoader());
                        }
                    } else {
                        // Reset Background Image
                        viewHolder.getImageViewEvent().setImageUrl("",
                                ImageRequestManager.getInstance(context).getImageLoader());
                    }
                } else {
                    // Reset Background Image
                    viewHolder.getImageViewEvent().setImageUrl("",
                            ImageRequestManager.getInstance(context).getImageLoader());
                }
            } else {
                // Reset Background Image
                viewHolder.getImageViewEvent().setImageUrl("",
                        ImageRequestManager.getInstance(context).getImageLoader());
            }

            // Set Event Name
            String eventName = jsonObject.optString("title", "");
            if (!AppUtil.isStringEmpty(eventName)) {
                viewHolder.getTextViewEventName().setText(eventName);
            }

            // Set Event Date Time
            String eventDateTime = jsonObject.optString("displayTime", "");
            if (!AppUtil.isStringEmpty(eventDateTime)) {
                viewHolder.getTextViewEventDateTime().setText(eventDateTime);
            }

            boolean isAvailable = true;
            JSONObject availabilityJsonObject = jsonObject.optJSONObject("availability");
            if (availabilityJsonObject != null) {
                int status = availabilityJsonObject.optInt("status");
                isAvailable = (status == 1);
            }

            // Set Tickets
            JSONArray ticketJsonArray = jsonObject.optJSONArray("details");
            renderEventTickets(viewHolder.getLinearLayoutTickets(), ticketJsonArray, position, isAvailable);

            // Show Free Event Text
            int isPaymentRequired = jsonObject.optInt("paymentRequired", 0);
            if (isPaymentRequired == 0) {
                String freeEventText = jsonObject.optString("typeText", "");
                if (!AppUtil.isStringEmpty(freeEventText)) {
                    viewHolder.getTextViewEventFree().setText(freeEventText);
                    viewHolder.getTextViewEventFree().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getTextViewEventFree().setVisibility(View.GONE);
                }
            } else {
                viewHolder.getTextViewEventFree().setVisibility(View.GONE);
            }

            // Handle Click on Card
            viewHolder.getImageViewEvent().setTag(jsonObject);
            viewHolder.getImageViewEvent().setOnClickListener(this);

            // Handle Share button Click
            JSONObject shareJsonObject = jsonObject.optJSONObject("share");
            if (shareJsonObject == null) {
                viewHolder.getImageViewShare().setVisibility(View.GONE);
            } else {
                viewHolder.getImageViewShare().setVisibility(View.VISIBLE);
                try {
                    if (shareJsonObject != null) {
                        shareJsonObject.put("event_id", jsonObject.optString("eventID"));
                        shareJsonObject.put("event_name", jsonObject.optString("title"));
                    }
                } catch (Exception ex) {

                }
                viewHolder.getImageViewShare().setTag(shareJsonObject);
                viewHolder.getImageViewShare().setOnClickListener(this);
            }
        }
    }

    private void renderEventTickets(LinearLayout linearLayoutEventTicketContainer,
                                    JSONArray ticketJsonArray, int position, boolean isAvailable) {
        if (linearLayoutEventTicketContainer != null
                && ticketJsonArray != null && ticketJsonArray.length() > 0) {
            int ticketSize = ticketJsonArray.length();

            linearLayoutEventTicketContainer.removeAllViews();
            for (int index = 0; index < ticketSize; index++) {
                final JSONObject ticketJsonObject = ticketJsonArray.optJSONObject(index);

                if (ticketJsonObject != null) {
                    View ticketView = layoutInflater.inflate(R.layout.event_ticket_list_item, null, false);

                    // Add Item Position
                    try {
                        ticketJsonObject.put(EVENT_POSITION, position);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Set Ticket Name
                    String ticketName = ticketJsonObject.optString("title", "");
                    if (!AppUtil.isStringEmpty(ticketName)) {
                        TextView textViewTicketName = (TextView) ticketView.findViewById(R.id.textView_ticket_name);
                        textViewTicketName.setText(ticketName);
                    }

                    // Set Ticket Description
                    String ticketDescription = ticketJsonObject.optString("description");
                    TextView textViewTicketDesc = (TextView) ticketView.findViewById(R.id.textView_ticket_desc);
                    if (AppUtil.isStringEmpty(ticketDescription)) {
                        textViewTicketDesc.setText("");
                        textViewTicketDesc.setVisibility(View.GONE);
                    } else {
                        textViewTicketDesc.setText(ticketDescription);
                        textViewTicketDesc.setVisibility(View.VISIBLE);
                    }

                    // Set Ticket Price
                    String ourTicketPriceText = ticketJsonObject.optString("ourPrice", "0");
                    TextView textViewEventPrice = (TextView) ticketView.findViewById(R.id.textView_event_price);

                    String actualTicketPriceText = ticketJsonObject.optString("actualPrice", "0");
                    TextView textViewEventActualPrice = (TextView) ticketView.findViewById(R.id.textView_event_actual_price);

                    if (!AppUtil.isStringEmpty(ourTicketPriceText)) {
                        if (Integer.parseInt(ourTicketPriceText) == 0) {
                            textViewEventPrice.setText(context.getString(R.string.text_free));
                        } else {
                            textViewEventPrice.setText(
                                    String.format(context.getString(R.string.container_rupee),
                                            ourTicketPriceText));
                        }
                    }

                    if (AppUtil.isStringEmpty(actualTicketPriceText) ||
                            Integer.parseInt(actualTicketPriceText) == 0) { // FREE TICKET
                        textViewEventActualPrice.setVisibility(View.GONE);
                    } else {
                        textViewEventActualPrice.setText(
                                AppUtil.getStrikedText(
                                        String.format(context.getString(R.string.container_rupee),
                                                actualTicketPriceText)));
                    }

                    // Set Tickets Left
                    String ticketsLeftText = ticketJsonObject.optString("dealsLeft", "0");
                    int ticketsLeftCount = ((AppUtil.isStringEmpty(ticketsLeftText)) ? 0 : Integer.parseInt(ticketsLeftText));

                    TextView textViewSoldOut = (TextView) ticketView.findViewById(R.id.textView_event_sold_out);
                    RelativeLayout relativeLayoutTicketSection = (RelativeLayout) ticketView.findViewById(R.id.relativeLayout_ticket_section);
                    TextView textViewTicketsLeft = (TextView) ticketView.findViewById(R.id.textView_deal_status);

                    if (isAvailable == false || ticketsLeftCount == 0) {
                        textViewSoldOut.setVisibility(View.VISIBLE);
                        relativeLayoutTicketSection.setVisibility(View.GONE);
                        textViewTicketsLeft.setVisibility(View.GONE);

                    } else {
                        textViewSoldOut.setVisibility(View.GONE);
                        relativeLayoutTicketSection.setVisibility(View.VISIBLE);
                        textViewTicketsLeft.setVisibility(View.VISIBLE);
                        textViewTicketsLeft.setText(String.format(context.getString(R.string.text_container_deal_left), ticketsLeftCount));

                        int ticketCount = ticketJsonObject.optInt(TICKET_COUNT, 0);
                        int transactionDealLimit = ticketJsonObject.optInt("transactionDealLimit", 0);

                        // Get Ticket Count
                        TextView textViewTicketQuantity = (TextView) ticketView.findViewById(R.id.textView_ticket_quantity);
                        textViewTicketQuantity.setText(Integer.toString(ticketCount));

                        // Get Minus Button
                        ImageView minusButton = (ImageView) ticketView.findViewById(R.id.imageView_ticket_minus);
                        minusButton.setTag(ticketJsonObject);
                        minusButton.setOnClickListener(this);
                        minusButton.setEnabled(!(ticketCount == 0));

                        // Get Plus Button
                        ImageView plusButton = (ImageView) ticketView.findViewById(R.id.imageView_ticket_plus);
                        plusButton.setTag(ticketJsonObject);
                        plusButton.setOnClickListener(this);

                        if (transactionDealLimit > 0) {
                            plusButton.setEnabled(!(ticketCount == transactionDealLimit));
                        }
                    }

                    // Remove Divider
                    if ((index + 1) == ticketSize) {
                        ticketView.findViewById(R.id.divider_event_ticket).setVisibility(View.GONE);
                    }

                    linearLayoutEventTicketContainer.addView(ticketView);
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.imageView_event_share) {
            eventClickHandler.onShareCardClick((JSONObject) view.getTag());

        } else if (viewId == R.id.imageView_ticket_minus) {
            handleTicketMinusButtonClick((JSONObject) view.getTag());

        } else if (viewId == R.id.imageView_ticket_plus) {
            handleTicketPlusButtonClick((JSONObject) view.getTag());

        } else if (viewId == R.id.image_view_event) {
            handleEventCardClick((JSONObject) view.getTag());
        }
    }

    private void handleTicketMinusButtonClick(JSONObject ticketJsonObject) {
        if (ticketJsonObject != null) {

            if (source.equalsIgnoreCase("AllOffers")) {

                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(context);
                AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("EventQuantitySubtract", hMap);
                if(hMap!=null){
                    hMap.put("category","B_SelectEvent");
                    hMap.put("label",ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));
                    hMap.put("action","RestaurantEventQuantitySubtract");
                }
                AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("EventQuantitySubtract", hMap);
                AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("B_SelectEvent", "EventQuantitySubtract", ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));

                //track event for qgraph, apsalar and branch
                AnalyticsHelper.getAnalyticsHelper(context).trackEventQGraphApsalar("EventQuantitySubtract",new HashMap<String, Object>(),true,false);

            } else {

                 // track for ga,countly
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(context);
                if(hMap!=null){
                    hMap.put("category","B_SelectEvent");
                    hMap.put("label",ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));
                    hMap.put("action","RestaurantEventQuantitySubtract");
                }
                AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("RestaurantEventQuantitySubtract", hMap);
                AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("D_restaurantDetail", "RestaurantEventQuantitySubtract", ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));

                //track for qgraph, apsalar and branch

                HashMap<String, Object> props = new HashMap<>();
                props.put("eventID",ticketJsonObject.optString("id"));
                props.put("eventDetailsURL",ticketJsonObject.optString("eventUrl"));
                AnalyticsHelper.getAnalyticsHelper(context).trackEventQGraphApsalar("RestaurantOfferCTAClick",props,true,false);

            }

            // Get Ticket Count
            int ticketCount = ticketJsonObject.optInt(TICKET_COUNT, 0);

            if (ticketCount > 0) {
                // Reduce Count
                --ticketCount;

                // Remove Key
                ticketJsonObject.remove(TICKET_COUNT);

                // Add Key
                try {
                    ticketJsonObject.put(TICKET_COUNT, ticketCount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                notifyItemChanged(ticketJsonObject.optInt(EVENT_POSITION));
            }
        }
    }

    private void handleTicketPlusButtonClick(JSONObject ticketJsonObject) {
        if (ticketJsonObject != null) {

            if (source.equalsIgnoreCase("AllOffers")) {
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(context);

                if(hMap!=null){
                    hMap.put("action","EventQuantityAdd");
                    hMap.put("category","B_SelectEvent");
                    hMap.put("label",ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));
                }


                AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("EventQuantityAdd", hMap);
                AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("B_SelectEvent", "EventQuantityAdd", ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));

                //track qgraph, apsalar
                AnalyticsHelper.getAnalyticsHelper(context).trackEventQGraphApsalar("EventQuantityAdd",new HashMap<String, Object>(),true,false);

            } else {

                //track for countly
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(context);

                if(hMap!=null){
                    hMap.put("action","RestaurantEventQuantityAdd");
                    hMap.put("category","B_SelectEvent");
                    hMap.put("label",ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));
                }

                AnalyticsHelper.getAnalyticsHelper(context).trackEventCountly("RestaurantEventQuantityAdd", hMap);
                AnalyticsHelper.getAnalyticsHelper(context).trackEventGA("D_restaurantDetail", "RestaurantEventQuantityAdd", ticketJsonObject.optString("title") + "_" + ticketJsonObject.optString("eventID"));

                //track for qgraph, apsalar and branch

                HashMap<String, Object> props = new HashMap<>();
                props.put("eventID",ticketJsonObject.optString("id"));
                props.put("eventDetailsURL",ticketJsonObject.optString("eventUrl"));
                AnalyticsHelper.getAnalyticsHelper(context).trackEventQGraphApsalar("RestaurantOfferCTAClick",props,true,false);

            }

            // Get Ticket Count
            int ticketCount = ticketJsonObject.optInt(TICKET_COUNT, 0);

            // Increase Count
            ++ticketCount;

            // Get Transaction Limit
            int transactionLimit = ticketJsonObject.optInt("transactionDealLimit", 0);

            if (transactionLimit > 0 && ticketCount == transactionLimit) {
                Toast.makeText(context, "Maximum limit reached", Toast.LENGTH_LONG).show();

            }

            // Remove Key
            ticketJsonObject.remove(TICKET_COUNT);

            // Add Key
            try {
                ticketJsonObject.put(TICKET_COUNT, ticketCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            notifyItemChanged(ticketJsonObject.optInt(EVENT_POSITION));
        }
    }

    private void handleEventCardClick(JSONObject eventJsonObject) {
        if (eventJsonObject != null) {
            eventClickHandler.onEventCardClick(eventJsonObject.optString("title", ""),
                    eventJsonObject.optString("eventUrl", ""));
        }
    }

    public JSONArray getSelectedEvents() {
        JSONArray selectedEventJsonArray = new JSONArray();

        JSONArray eventJsonArray = getJsonArray();

        if (eventJsonArray != null && eventJsonArray.length() > 0) {
            int eventSize = eventJsonArray.length();
            for (int index = 0; index < eventSize; index++) {
                JSONObject eventJsonObject = eventJsonArray.optJSONObject(index);

                if (eventJsonObject != null) {
                    boolean isTicketSelected = false;
                    JSONArray detailsJsonArray = eventJsonObject.optJSONArray("details");
                    JSONArray selTicketJsonArray = new JSONArray();

                    if (detailsJsonArray != null && detailsJsonArray.length() > 0) {
                        int detailSize = detailsJsonArray.length();
                        for (int count = 0; count < detailSize; count++) {
                            JSONObject ticketJsonObject = detailsJsonArray.optJSONObject(count);

                            if (ticketJsonObject != null) {
                                int ticketCount = ticketJsonObject.optInt(TICKET_COUNT, 0);

                                if (ticketCount > 0) {
                                    // Set Flag
                                    isTicketSelected = true;

                                    // Add Ticket JSON Object to Array
                                    selTicketJsonArray.put(getSelectedTicketJsonObject(ticketJsonObject));
                                }
                            }
                        }
                    }

                    if (isTicketSelected) {
                        JSONObject selEventJsonObject = getSelectedEventJsonObject(eventJsonObject);

                        try {
                            selEventJsonObject.putOpt("details", selTicketJsonArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // Add Event JSON Object to Array
                        selectedEventJsonArray.put(selEventJsonObject);
                    }
                }
            }
        }

        return selectedEventJsonArray;
    }

    private JSONObject getSelectedTicketJsonObject(JSONObject ticketJsonObject) {
        JSONObject selTicketJsonObject = new JSONObject();

        try {
            selTicketJsonObject.put("eventID", ticketJsonObject.optString("eventID", ""));
            selTicketJsonObject.put("title", ticketJsonObject.optString("title", ""));
            selTicketJsonObject.put("description", ticketJsonObject.optString("description", ""));
            selTicketJsonObject.put("ourPrice", ticketJsonObject.optInt("ourPrice", 0));
            selTicketJsonObject.put("quantity", ticketJsonObject.optInt(TICKET_COUNT, 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return selTicketJsonObject;
    }

    private JSONObject getSelectedEventJsonObject(JSONObject eventJsonObject) {
        JSONObject selEventJsonObject = new JSONObject();

        try {
            selEventJsonObject.put("eventID", eventJsonObject.optString("eventID", ""));
            selEventJsonObject.put("title", eventJsonObject.optString("title", ""));
            selEventJsonObject.put("toDate", eventJsonObject.optString("toDate", ""));
            selEventJsonObject.put("time", eventJsonObject.optString("time", ""));//displayTime
            selEventJsonObject.put("validity", eventJsonObject.optString("validity", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return selEventJsonObject;
    }

    public interface EventClickHandler {
        void onEventCardClick(String title, String url);

        void onShareCardClick(JSONObject jsonObject);
    }

    private class EventsViewHolder extends RecyclerView.ViewHolder {
        private NetworkRoundCornerImageView imageViewEvent;
        private TextView textViewEventName;
        private TextView textViewEventDateTime;
        private ImageView imageViewShare;
        private LinearLayout linearLayoutTickets;
        private TextView textViewEventFree;

        public EventsViewHolder(View itemView) {
            super(itemView);

            imageViewEvent = (NetworkRoundCornerImageView) itemView.findViewById(R.id.image_view_event);
            imageViewEvent.setDefaultImageResId(R.drawable.default_list);
            textViewEventName = (TextView) itemView.findViewById(R.id.text_view_event_name);
            textViewEventDateTime = (TextView) itemView.findViewById(R.id.text_view_event_date_time);
            imageViewShare = (ImageView) itemView.findViewById(R.id.imageView_event_share);
            linearLayoutTickets = (LinearLayout) itemView.findViewById(R.id.linearLayout_event_tickets);
            textViewEventFree = (TextView) itemView.findViewById(R.id.textView_event_free);
        }

        public NetworkRoundCornerImageView getImageViewEvent() {
            return imageViewEvent;
        }

        public TextView getTextViewEventName() {
            return textViewEventName;
        }

        public TextView getTextViewEventDateTime() {
            return textViewEventDateTime;
        }

        public ImageView getImageViewShare() {
            return imageViewShare;
        }

        public LinearLayout getLinearLayoutTickets() {
            return linearLayoutTickets;
        }

        public TextView getTextViewEventFree() {
            return textViewEventFree;
        }
    }
}
