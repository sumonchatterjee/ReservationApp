package com.dineout.book.fragment.bookingflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.DEAL_DATA;
import static com.example.dineoutnetworkmodule.AppConstant.SAVINGS_DATA;

public class DealTicketQuantityFragment extends MasterDOJSONReqFragment implements View.OnClickListener {

    private final int REQUEST_CODE_TICKET_SAVINGS = 202;

    private int singleDealPrice;
    private int dealsQuantity;
    private int dealSelectedQuantity;
    private int transactionDealLimit;
    private Bundle arguments;
    private JSONObject dealJsonObject;
    private TextView textViewDealsLeft;
    private TextView textViewDealAmount;
    private TextView textViewDealAmountQuantity;
    private ImageView imageViewDealTicketMinus;
    private TextView textViewDealTicketQuantity;
    private ImageView imageViewDealTicketPlus;
    private View relativeLayoutBuyDeal;
    private ProgressBar progressBarDealTicket;
    private JSONObject savingsDataJsonObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenName(getString(R.string.ga_screen_deal_popup));


        // Set Style
        AppUtil.setHoloLightTheme(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_deal_ticket_quantity, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initialize View
        initializeView();
    }

    private void initializeView() {
        // Get Bundle
        arguments = getArguments();

        if (arguments == null)
            arguments = new Bundle();

        // Get Deal Detail
        dealJsonObject = null;

        try {
            dealJsonObject = new JSONObject(arguments.getString(AppConstant.BUNDLE_OFFER_JSON));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (dealJsonObject == null)
            return;

        // Set Title
        TextView textViewDealTitle = (TextView) getView().findViewById(R.id.textView_deal_title);
        textViewDealTitle.setText(dealJsonObject.optString("title_2", ""));

        // Set Date Time Validity
        TextView textViewDealDateTime = (TextView) getView().findViewById(R.id.textView_deal_date_time);
        textViewDealDateTime.setText(dealJsonObject.optString("title_3", ""));

        // Set Deal Quantity
        dealsQuantity = dealJsonObject.optInt("dealsLeft", 0); // Deals Left
        showDealsLeft(dealsQuantity);

        // Set Deal Price
        singleDealPrice = dealJsonObject.optInt("title_4", 0);

        // Set Transaction Deal Limit
        transactionDealLimit = dealJsonObject.optInt("transactionDealLimit", 0);
        transactionDealLimit = ((transactionDealLimit <= dealsQuantity) ? transactionDealLimit : dealsQuantity);

        // Get Minus Button
        imageViewDealTicketMinus = (ImageView) getView().findViewById(R.id.imageView_deal_ticket_minus);
        imageViewDealTicketMinus.setOnClickListener(this);

        // Get Selected Deal Quantity
        textViewDealTicketQuantity = (TextView) getView().findViewById(R.id.textView_deal_ticket_quantity);
        int userSelectedDealQuantity = dealJsonObject.optInt(AppConstant.BUNDLE_SELECTED_DEAL_QUANTITY, 0);
        dealSelectedQuantity = ((userSelectedDealQuantity > 0) ? userSelectedDealQuantity : ((dealsQuantity > 0) ? 1 : 0));
        textViewDealTicketQuantity.setText(Integer.toString(dealSelectedQuantity));

        // Get Plus Button
        imageViewDealTicketPlus = (ImageView) getView().findViewById(R.id.imageView_deal_ticket_plus);
        imageViewDealTicketPlus.setOnClickListener(this);

        // Get Deal Quantity and Amount
        textViewDealAmount = (TextView) getView().findViewById(R.id.textView_deal_amount);
        textViewDealAmountQuantity = (TextView) getView().findViewById(R.id.textView_deal_amount_quantity);

        // Update Deal Quantity Amount
        updateDealAmountQuantity();

        // Update Deal Related UI
        updateDealRelatedUI();

        // Get Buy Deal
        relativeLayoutBuyDeal = getView().findViewById(R.id.relativeLayout_buy_deal);
        relativeLayoutBuyDeal.setOnClickListener(this);

        // Get Deal Info
        //ImageView imageViewDealInfo = (ImageView) getView().findViewById(R.id.imageView_deal_info);
        //imageViewDealInfo.setOnClickListener(this);

        // Get Outer View
        View textViewDealTicketOuter = getView().findViewById(R.id.textView_deal_ticket_outer);
        textViewDealTicketOuter.setOnClickListener(this);

        // Get Deal Ticket Progress Bar
        progressBarDealTicket = (ProgressBar) getView().findViewById(R.id.progressBar_deal_ticket);
    }

    private void showDealsLeft(int dealsLeft) {
        textViewDealsLeft = (TextView) getView().findViewById(R.id.textView_deals_left);

        if (dealsQuantity > 0) {
            textViewDealsLeft.setText(String.format(getString(R.string.text_container_deal_left), dealsLeft));
            textViewDealsLeft.setTextColor(getResources().getColor(R.color.grey_75));

        } else {
            textViewDealsLeft.setText(R.string.text_sold_out);
            textViewDealsLeft.setTextColor(getResources().getColor(R.color.red_cc));
        }
    }

    private void updateDealAmountQuantity() {
        int finalPrice = (singleDealPrice * dealSelectedQuantity);

        // Set Deal Price
        textViewDealAmount.setText(String.format(getString(R.string.container_rupee),
                Integer.toString(finalPrice)));

        // Set Deal Price Quantity
        textViewDealAmountQuantity.setText(
                String.format(getString(R.string.container_deal_quantity_amount),
                        dealSelectedQuantity, singleDealPrice));
    }

    @Override
    public void onClick(View view) {
        // Get View Id
        int viewId = view.getId();

        if (viewId == R.id.imageView_deal_ticket_minus) {
            handleMinusDealClick();

        } else if (viewId == R.id.imageView_deal_ticket_plus) {
            handlePlusDealClick();

        } else if (viewId == R.id.relativeLayout_buy_deal) {
            handleBuyDealClick();

        } else if (viewId == R.id.textView_deal_ticket_outer) {
            handleBackNavigation();
        }
    }

    private void handleBackNavigation() {
        popBackStack(getFragmentManager());
    }

    private void handleMinusDealClick() {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_deal_popup), getString(R.string.ga_action_minus_deal_popup), null);

        HashMap<String,String>hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_DealQuantity","DecreaseButtonClick","DecreaseButtonClick",hMap);

        // Check Deals Left
        if (dealSelectedQuantity > 1) {
            // Minus Deal Selected Quantity
            --dealSelectedQuantity;
        }

        // Update UI
        updateDealRelatedUI();
    }

    private void handlePlusDealClick() {
        // Track Event
       // trackEventGA(getString(R.string.ga_screen_deal_popup), getString(R.string.ga_action_plus_deal_popup), null);

        HashMap<String,String>hMap= DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_DealQuantity","IncreaseButtonClick","IncreaseButtonClick",hMap);


        // Check Deals Left
        int dealsLeft = (transactionDealLimit - dealSelectedQuantity);
        if (dealsLeft > 0) {
            // Plus Deal Selected Quantity
            ++dealSelectedQuantity;
        }

        // Update UI
        updateDealRelatedUI();
    }

    private void updateDealRelatedUI() {
        // Update Deal Selected Quantity
        textViewDealTicketQuantity.setText(Integer.toString(dealSelectedQuantity));

        // Set Enable for Minus Button
        imageViewDealTicketMinus.setEnabled((dealSelectedQuantity > 1));

        // Set Enable for Plus Button
        int dealsLeft = (transactionDealLimit - dealSelectedQuantity);
        imageViewDealTicketPlus.setEnabled((dealsLeft > 0));

        if (dealsLeft == 0) {
            UiUtil.showToastMessage(getActivity(), getString(R.string.deal_limit_reached));
        }

        // Update Deal Quantity Amount
        updateDealAmountQuantity();
    }


    private void updateSelectedDealQuantity() {
        // Check for NULL / Empty
        if (dealJsonObject != null) {
            try {
                dealJsonObject.put(AppConstant.BUNDLE_SELECTED_DEAL_QUANTITY, dealSelectedQuantity);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Add back to Bundle
            Bundle arguments = getArguments();
            if (arguments == null)
                arguments = new Bundle();

            arguments.putString(AppConstant.BUNDLE_OFFER_JSON, dealJsonObject.toString());
        }
    }


    private void handleBuyDealClick() {

        // Track Event
        //trackEventGA(getString(R.string.ga_screen_deal_popup), getString(R.string.ga_action_buy_now_deal_popup), null);


        HashMap<String, Object> props = new HashMap<String, Object>();
        if(dealJsonObject!=null){
            props.put("DealName", dealJsonObject.optString("title_2", ""));
            props.put("DealId", dealJsonObject.optString("id", ""));
            props.put("DealDetailURL", dealJsonObject.optString("url", ""));
        }
        
        props.put("RestaurantId", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_ID));
        props.put("RestaurantName", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_NAME));
        props.put("CuisinesList", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
        props.put("TagList", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));
        props.put("ResCuisine", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, ""));
        props.put("ResDeeplink", getArguments().getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK,""));
        props.put("Number of deals left", dealJsonObject.optInt("dealsLeft", 0));
        props.put("ticketGroups", getArguments().getString(AppConstant.BUNDLE_TICKET_GROUPS, ""));

//        trackEventGA(getString(R.string.deal_buy_initiated), props);
//
//        AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventGA("DealTicketQuantity",getString(R.string.deal_buy_initiated),getArguments().getString(AppConstant.BUNDLE_RESTAURANT_ID)+"_"
//                + getArguments().getString(AppConstant.BUNDLE_TICKET_GROUPS, ""));

        //track event

        HashMap<String,String>hMap=DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_DealQuantity","DealBuyNowClick",getArguments().getString(AppConstant.BUNDLE_RESTAURANT_NAME)+"_"
                +getArguments().getString(AppConstant.BUNDLE_RESTAURANT_ID),hMap);

        trackEventQGraphApsalar("DealBuyNowClick",new HashMap<String, Object>(),true,true,true);


        // Check selected Deal Quantity
        if (dealSelectedQuantity > 0) {
            getDealDetails();

        } else {
            UiUtil.showToastMessage(getActivity().getApplicationContext(), "Please select number of deals");
        }
    }

    private void setBuyDealButtonUI(boolean isRequestInProgress) {
        // Set Progress Bar
        progressBarDealTicket.setVisibility(isRequestInProgress ? View.VISIBLE : View.GONE);

        // Set Button
        relativeLayoutBuyDeal.setEnabled(!isRequestInProgress);
    }

    private void getDealDetails() {
        // Set Button UI
        setBuyDealButtonUI(true);

        // Take API Hit
        getNetworkManager().jsonRequestGetNode(REQUEST_CODE_TICKET_SAVINGS, AppConstant.NODE_TICKET_SAVINGS_URL,
                ApiParams.getTicketSavingsParams(
                        getArguments().getString(AppConstant.BUNDLE_RESTAURANT_ID),
                        getArguments().getString(AppConstant.BUNDLE_OFFER_ID),
                        dealSelectedQuantity), this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        // Set Button UI
        setBuyDealButtonUI(false);

        // Check for NULL
        if (responseObject != null) {
            // Get Status
            if (responseObject.optBoolean("status")) {
                // Get Data
                JSONObject dataJsonObject = responseObject.optJSONObject("data");

                if (dataJsonObject != null) {

                    // Handle Savings Details
                    handleSavingsDetails(dataJsonObject);
                }
            } else {
                // Handle Error
                handleError(responseObject.optString("error", ""));
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        setBuyDealButtonUI(false);

    }

    private void handleSavingsDetails(JSONObject dataJsonObject) {
        // Check for NULL
        if (dataJsonObject != null) {
            // Set Savings Data
            savingsDataJsonObject = dataJsonObject;
        }

        // Proceed with Deal Confirmation
        proceedWithBuyDeal();
    }

    private void handleError(String errorMsg) {
        // Check for Error Message
        if (AppUtil.isStringEmpty(errorMsg)) {
            errorMsg = getString(R.string.default_error_message);
        }

        // Show Toast
        UiUtil.showToastMessage(getActivity(), errorMsg);
    }

    private void proceedWithBuyDeal() {
        // Update Selected Deal Quantity
        updateSelectedDealQuantity();

        // Add Selected Tickets
        addSelectedTickets();

        DealsConfirmationFragment dealConfirmationFragment = DealsConfirmationFragment.newInstance(arguments);
        dealConfirmationFragment.setDealsArrays(getDealsArray());

        addToBackStack(getFragmentManager(), dealConfirmationFragment);
    }

    private JSONArray getDealsArray() {
        JSONArray dealJsonArray = new JSONArray();

        try {
            JSONObject jsonObject = new JSONObject();

            // Add Deal Data in JSON
            if (dealJsonObject != null) {
                jsonObject.put(DEAL_DATA, dealJsonObject);
            }

            // Add Savings Data in JSON
            if (savingsDataJsonObject != null) {
                jsonObject.put(SAVINGS_DATA, savingsDataJsonObject);
            }

            // Add JSON to Array
            dealJsonArray.put(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dealJsonArray;
    }

    private void addSelectedTickets() {
        // Check for NULL
        if (dealJsonObject != null) {
            try {
                dealJsonObject.put(AppConstant.BUNDLE_SELECTED_DEAL_COUNT, dealSelectedQuantity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        arguments = null;
        dealJsonObject = null;
        textViewDealsLeft = null;
        textViewDealAmount = null;
        textViewDealAmountQuantity = null;
        imageViewDealTicketMinus = null;
        textViewDealTicketQuantity = null;
        imageViewDealTicketPlus = null;
        relativeLayoutBuyDeal = null;
        progressBarDealTicket = null;
        savingsDataJsonObject = null;
    }
}
