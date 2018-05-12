package com.dineout.book.fragment.bookingflow;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.analytics.utilities.DOLog;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.ConfirmationDataUtils;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.payments.fragment.DealsBillAmountFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.dialogs.MoreDialog;
import com.dineout.recycleradapters.ConfirmationAdapter;
import com.dineout.recycleradapters.util.DateTimeSlotUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.example.dineoutnetworkmodule.AppConstant.BUNDLE_RESTAURANT_NAME;

public class DealsConfirmationFragment extends AuthenticationWrapperJSONReqFragment
        implements View.OnClickListener, ConfirmationAdapter.ConfirmationClickListener {

    public static final String RESTAURANT_ID = "RESTAURANT_ID";
    public static final String BOOKING_ID = "BOOKING_ID";

    private final int REQUEST_CODE_EDIT_DEAL = 107;

    private ConfirmationDataUtils confirmationDataUtils;
    private Bundle arguments;
    private long dateTimeInMilliSeconds;
    private JSONArray dealsArrays;
    private String[] dealIds;
    private String[] dealQuantity;
    private View confirmButton;
    private RecyclerView recyclerViewResturantDetails;
    private ConfirmationAdapter adapter;
    private Bundle dealBundle;
    private TextView textViewPayNow;
    private TextView textViewTotalTicketAmount;

    public static DealsConfirmationFragment newInstance(Bundle bundle) {
        DealsConfirmationFragment dealConfirmationFragment = new DealsConfirmationFragment();
        dealConfirmationFragment.setArguments(bundle);
        return dealConfirmationFragment;
    }

    public void setDealsArrays(JSONArray dealsArrays) {
        this.dealsArrays = dealsArrays;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Arguments
        arguments = getArguments();

        if (arguments != null) {
            dealBundle = arguments;
            dateTimeInMilliSeconds = (arguments.getLong(AppConstant.BUNDLE_DATE_TIMESTAMP) * 1000);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deals_confrmation_modified_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Track Screen
        trackScreenToGA(getString(R.string.ga_screen_confirm_deal));

        // Initialize View
        initializeView();
    }

    private void initializeView() {
        // Get Back
        ImageView imageViewAllOfferBack = (ImageView) getView().findViewById(R.id.imageView_all_offer_back);
        imageViewAllOfferBack.setOnClickListener(this);

        // Set Title
        TextView textViewTitle = (TextView) getView().findViewById(R.id.textView_all_offer_title);
        textViewTitle.setText(getBookingDateTime());

        // Set Sub Title
        TextView textViewSubTitle = (TextView) getView().findViewById(R.id.textView_all_offer_sub_title);
        textViewSubTitle.setText(arguments.getString(BUNDLE_RESTAURANT_NAME, ""));

        recyclerViewResturantDetails = (RecyclerView) getView().findViewById(R.id.recycler_view_deal_confirmation);

        confirmButton = getView().findViewById(R.id.bt_rest_book);
        textViewPayNow = (TextView) getView().findViewById(R.id.textView_pay_now);
        textViewTotalTicketAmount = (TextView) getView().findViewById(R.id.textView_total_ticket_amount);


            textViewPayNow.setText(R.string.button_pay_now);


        confirmationDataUtils = ConfirmationDataUtils.newInstance(getContext());
        adapter = new ConfirmationAdapter(getActivity(), confirmationDataUtils.getDealsJSONArray(dealBundle, dealsArrays, arguments.getString(AppConstant.BUNDLE_EXTRA_INFO)), "deal");
        adapter.setConfirmationClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewResturantDetails.setLayoutManager(llm);
        recyclerViewResturantDetails.setAdapter(adapter);
        confirmButton.setOnClickListener(this);
    }

    private String getBookingDateTime() {
        if (arguments != null) {
            return DateTimeSlotUtil.getDateTimeSubTitle(
                    arguments.getString(AppConstant.BUNDLE_SELECTED_DATE, ""),
                    arguments.getString(AppConstant.BUNDLE_SELECTED_TIME, ""));
        }

        return "";
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imageView_all_offer_back:
                handleNavigation();
                break;

            case R.id.bt_rest_book:
                if ((textViewPayNow.getText().toString().equalsIgnoreCase("EDIT DEAL"))) {
                    trackEventGA(getString(R.string.ga_screen_confirm_deal), getString(R.string.ga_action_edit), null);
                    processEditDeal();

                } else {
                   // trackEventGA(getString(R.string.ga_screen_confirm_deal), getString(R.string.ga_action_buy_now), null);
                    try {

                        HashMap<String, Object> props = new HashMap<String, Object>();

                        props.put("DealId", dealBundle.getString(AppConstant.BUNDLE_OFFER_ID));
                        props.put("RestaurantId", dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
                        props.put("RestaurantName", dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_NAME));
                        JSONObject dealJsonObject = new JSONObject(dealBundle.getString(AppConstant.BUNDLE_OFFER_JSON));

                        if(dealJsonObject!=null){
                            props.put("DealName",dealJsonObject.optString("title_2"));
                            props.put("DealDetailURL",dealJsonObject.optString("url"));

                        }
                        props.put("ResDeeplink",dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_DEEPLINK));
                        props.put("ResCuisine", dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINE_NAME, ""));
                        props.put("CuisinesList", dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_CUISINELIST, "[]"));
                        props.put("TagList", dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_TAGLIST, "[]"));

                      //  trackEventGA(getString(R.string.deal_buy_confirmation), props);
                    }catch (JSONException ex){

                    }

                    initializePaymentScreen();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void handleNavigation() {
        popBackStack(getFragmentManager());
    }

    private HashMap<String, String> getBillAmountParams(String phoneNo) {
        String userFullName = (DOPreferences.getDinerFirstName(getContext()) + " " +
                DOPreferences.getDinerLastName(getContext()));
        long unixTime = dateTimeInMilliSeconds / 1000L;
        return ApiParams.getBillAmountParams(userFullName, phoneNo, DOPreferences.getDinerEmail(getContext()), dealIds, dealQuantity, dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID), Long.toString(unixTime), "deal");
    }

    // get bill amount api hit
    private void getBillAmount() {
        showLoader();
        getNetworkManager().stringRequestPost(101, AppConstant.URL_BILL_PAYMENT
                , getBillAmountParams(adapter.getPhoneNumber()), new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        hideLoader();

                        if (getActivity() == null || getView() == null)
                            return;

                        try {
                            JSONObject resp = new JSONObject(responseObject);
                            if (resp.optBoolean("status")) {
                                hideLoader();

                                // Save Diner Phone
//                                DOPreferences.setDinerPhone(MainApplicationClass.getInstance(), adapter.getPhoneNumber());

                                JSONObject outputParam = resp.optJSONObject("output_params");
                                if (outputParam != null) {

                                    JSONObject data = outputParam.optJSONObject("data");
                                    if(data!=null) {

                                        JSONArray extraInfoJsonArray = data.optJSONArray("section_1");


                                        navigateToDealsPayment(data.optString("do_wallet_amt"), data.optString("bill_amount"), data.optString("rest_wallet_amt"),
                                                data.optString("cashback_discount"), data.optString("max_cashback"), data.optString("restaurant_name"),extraInfoJsonArray,
                                                data.optString("cashback_text"),dealBundle.getString(AppConstant.BUNDLE_RESTAURANT_ID));
                                    }
                                }
                            } else {
                                handleErrorResponse(new JSONObject(responseObject));
                            }

                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Some error in initializing payment", Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(Request request, VolleyError error) {

                        hideLoader();
                        UiUtil.showToastMessage(getContext(), "Something went wrong");
                    }
                }, false);
    }


    // navigate to bill payment screen
    private void initializePaymentScreen() {
        try {

            JSONObject dealDetail=null;
            JSONObject dealData=dealsArrays.getJSONObject(0);
            if(dealData!=null){
                dealDetail = dealData.optJSONObject(AppConstant.DEAL_DATA);
            }
            int length=1;

            dealIds = new String[length];
            dealQuantity = new String[length];

                dealIds[0] = dealDetail.optString("id");
                dealQuantity[0] = String.valueOf(dealDetail.optInt(AppConstant.BUNDLE_SELECTED_DEAL_COUNT));


            //since multiple deals and multiple ticket cannot be purchased, hence commented this logic

//            dealIds = new String[dealsArrays.length()];
//            dealQuantity = new String[dealsArrays.length()];
//
//            for (int i = 0; i < dealsArrays.length(); i++) {
//                JSONObject deals = dealsArrays.getJSONObject(i);
//                dealIds[i] = deals.optString("id");
//                dealQuantity[i] = String.valueOf(deals.optInt(AppConstant.BUNDLE_SELECTED_DEAL_COUNT));
//
//            }

            if (getContext() != null && adapter != null) {
                DOPreferences.setTempDinerPhone(getContext(), adapter.getPhoneNumber().toString().trim());
            }

            if (dealQuantity != null && dealIds != null) {
                if (dealQuantity.length > 0 && dealQuantity.length > 0) {
                    getBillAmount();
                }
            }
        } catch (JSONException ex) {
            DOLog.d("error in initializing payment");
        }
    }

    private void navigateToDealsPayment(String doWallemtAmount, String billAmount, String restaurantWallet, String cashbackDiscount,String maxCashback,
                                        String restaurantName,JSONArray extraInfoJsonArray,String cashbackTxt, String restaurantId) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_BILL_AMOUNT, billAmount);
        bundle.putString("do_wallet_amount", doWallemtAmount);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_WALLET_AMOUNT, restaurantWallet);
        bundle.putString(AppConstant.BUNDLE_CASHBACK_DISCOUNT, cashbackDiscount);
        bundle.putString(AppConstant.BUNDLE_MAX_CASHBACK, maxCashback);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME_DETAIL, restaurantName);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID_DETAILS, restaurantId);
        bundle.putStringArray(AppConstant.BUNDLE_DEAL_IDS, dealIds);
        bundle.putStringArray(AppConstant.BUNDLE_DEAL_QUANTITY, dealQuantity);
        bundle.putLong(AppConstant.BUNDLE_DATE_TIME, dateTimeInMilliSeconds);
        //bundle.putString("spcl_request", adapter.getSpecialRequest());
        bundle.putString(AppConstant.BUNDLE_TYPE, "deal");
        if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
            bundle.putString(AppConstant.BUNDLE_EARNINGS_INFO, extraInfoJsonArray.toString());
        }
            bundle.putString(AppConstant.BUNDLE_CASHBACK_INFO, cashbackTxt);


        DealsBillAmountFragment dealsBillAmountFragment = DealsBillAmountFragment.getFragment(bundle);
        addToBackStack(getActivity(), dealsBillAmountFragment);
    }

    // edit deal
    private void processEditDeal() {
        showEditBookingDialog();
    }

    private void showEditBookingDialog() {
        MoreDialog dialog = new MoreDialog("Edit Deal?", getResources().getString(R.string.booking_edit_msg), getActivity(), true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();

                // Track Event
                trackEventGA(getString(R.string.confirm_edit_booking),
                        getString(R.string.ga_action_edit_warning), null);

                // Show Loading Dialog
                showLoader();

                getNetworkManager().stringRequestPost(REQUEST_CODE_EDIT_DEAL, AppConstant.URL_EDIT_BOOKING,
                        ApiParams.editDeal(
                                dealBundle.getString(AppConstant.BOOKING_ID),
                                AppUtil.convertMillisecondsToSeconds(dateTimeInMilliSeconds)

                        ), new Response.Listener<String>() {
                            @Override
                            public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                                try {
                                    parseJSON(new JSONObject(responseObject));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, DealsConfirmationFragment.this, false);
            }
        });

        dialog.show(getFragmentManager(), "booking_detail");
    }

    private void parseJSON(JSONObject responseObject) {
        // Hide Loading Dialog
        hideLoader();

        if (getActivity() == null || getView() == null)
            return;

        if (responseObject != null) {
            if (responseObject.optBoolean("status")) {

                MoreDialog dialog = new MoreDialog(getResources().getString(R.string.edit_booking), getActivity().getResources().getString(R.string.edit_booking_status), getActivity(), false);
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // Dismiss Dialog
                        dialog.dismiss();

                        // Pop
                        popBackStack(getFragmentManager());
                    }
                });

                dialog.show(getFragmentManager(), "booking_detail");

            } else {
                handleErrorResponse(responseObject);
//                MoreDialog dialog = new MoreDialog(getResources().getString(R.string.edit_booking), getErrorMessage(responseObject), getActivity(), false);
//                dialog.show(getFragmentManager(), "booking_detail");
            }
        }
    }

    private String getErrorMessage(JSONObject jsonObject) {
        String errorMessage = "";

        if (jsonObject != null) {
            errorMessage = jsonObject.optString("error_msg");
        }

        //check other message
        if (TextUtils.isEmpty(errorMessage)) {
            errorMessage = "Unknown Error";
        }

        return errorMessage;
    }


    public void handleErrorResponse(JSONObject responseObject) {
        super.handleErrorResponse(responseObject);
    }


    @Override
    public void onCTAButtonUpdate(int totalAmount) {
        // Check UI
        if (getView() == null)
            return;

        textViewTotalTicketAmount.setText(String.format(getString(R.string.text_total_amount_container), totalAmount));
    }

    @Override
    public void onKnowMoreClick(JSONArray array) {
        if (getView() == null) {
            return;
        }

       // trackEventGA(getString(R.string.ga_screen_confirm_deal), getString(R.string.ga_action_know_more), null);
        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
        trackEventForCountlyAndGA("B_Confirmation",
                "DealSavingsKnowMoreClick",
                "", hMap);

        Bundle bundle = new Bundle();
        bundle.putString("title", getBookingDateTime());
        bundle.putString("subtitle", arguments.getString(BUNDLE_RESTAURANT_NAME, ""));

        DealAmountBreakUpFragment fragment = new DealAmountBreakUpFragment();
        fragment.setArguments(bundle);
        fragment.setData(array);

        addToBackStack(getFragmentManager(), fragment, R.anim.f_enter, R.anim.f_exit);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        confirmationDataUtils = null;
        arguments = null;
        dealsArrays = null;
        dealIds = null;
        dealQuantity = null;
        confirmButton = null;
        recyclerViewResturantDetails = null;
        adapter = null;
        dealBundle = null;
        textViewPayNow = null;
        textViewTotalTicketAmount = null;
    }
}
