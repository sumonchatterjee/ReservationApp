package com.dineout.book.fragment.bookingflow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.dineout.book.fragment.mybookings.BookingDetailsFragment;
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

public class EventConfirmationFragment extends AuthenticationWrapperJSONReqFragment
        implements View.OnClickListener, ConfirmationAdapter.ConfirmationClickListener {

    private RecyclerView recyclerViewRestaurantDetails;
    private View confirmButton;
    private ConfirmationAdapter eventConfirmationAdapter;
    private Bundle arguments;

    private JSONArray ticketsArray;
    private String[] eventsIds;
    private String[] eventsQuantity;
    private JSONObject eventsObj;
    private TextView textViewTotalTicketAmount;

    public void setEventsObj(JSONObject eventsList) {
        this.eventsObj = eventsList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenToGA(getString(R.string.ga_screen_confirm_event));

        // Get Bundle
        arguments = getArguments();
        if (arguments == null)
            arguments = new Bundle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.deals_confrmation_modified_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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

        recyclerViewRestaurantDetails = (RecyclerView) getView().findViewById(R.id.recycler_view_deal_confirmation);

        confirmButton = getView().findViewById(R.id.bt_rest_book);
        textViewTotalTicketAmount = (TextView) getView().findViewById(R.id.textView_total_ticket_amount);
        TextView textViewPayNow = (TextView) getView().findViewById(R.id.textView_pay_now);
        textViewPayNow.setText(R.string.button_pay_now);

        ConfirmationDataUtils confirmationDataUtils = ConfirmationDataUtils.newInstance(getContext());
        if (eventsObj != null) {
            ticketsArray = eventsObj.optJSONArray("details");
        }

        eventConfirmationAdapter = new ConfirmationAdapter(getActivity(), confirmationDataUtils.getEventJSONArray(arguments, eventsObj, arguments.getString(AppConstant.BUNDLE_EXTRA_INFO)), "event");
        eventConfirmationAdapter.setConfirmationClickListener(this);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewRestaurantDetails.setLayoutManager(llm);
        recyclerViewRestaurantDetails.setAdapter(eventConfirmationAdapter);
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
                handleNavigationBack();
                break;

            case R.id.bt_rest_book:
                trackEventGA(getString(R.string.ga_screen_confirm_event), "Continue", null);
                initializePaymentScreen();
                break;

            default:
                break;
        }
    }

    private void handleNavigationBack() {
        popBackStack(getFragmentManager());
    }

    private void initializePaymentScreen() {
        try {
            if (ticketsArray != null && ticketsArray.length() > 0) {
                eventsIds = new String[ticketsArray.length()];
                eventsQuantity = new String[ticketsArray.length()];

                for (int i = 0; i < ticketsArray.length(); i++) {
                    JSONObject deals = ticketsArray.getJSONObject(i);
                    eventsIds[i] = deals.optString("eventID");
                    eventsQuantity[i] = String.valueOf(deals.optInt("quantity"));

                }
                if (eventsQuantity != null && eventsIds != null) {
                    if (eventsQuantity.length > 0 && eventsQuantity.length > 0) {
                        if (eventConfirmationAdapter.getTotalAmount() > 0) {
                            getBillAmount();
                        } else {
                            trackEventGA(getString(R.string.ga_screen_confirm_event), "FreeEventBought", eventsObj.optString("eventID"));
                            getFreeEventAmount();
                        }
                    }
                }
            }

        } catch (JSONException ex) {
            DOLog.d("error in initializing payment");
        }
    }

    private HashMap<String, String> getBillAmountParams( String phoneNo) {
        String userFullName = (DOPreferences.getDinerFirstName(getContext()) + " " + DOPreferences.getDinerLastName(getContext()));
        long dateTimeInSeconds = arguments.getLong(AppConstant.DATE); //date time in secs

        return ApiParams.getBillAmountParams(userFullName, phoneNo, DOPreferences.getDinerEmail(getContext()),
                eventsIds, eventsQuantity, arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID), Long.toString(dateTimeInSeconds), "event");
    }

    // get bill amount api hit
    private void getBillAmount() {
        showLoader();
        getNetworkManager().stringRequestPost(101, AppConstant.URL_BILL_PAYMENT,
                getBillAmountParams( eventConfirmationAdapter.getPhoneNumber()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        hideLoader();

                        if (getActivity() == null || getView() == null)
                            return;

                        try {
                            JSONObject resp = new JSONObject(responseObject);
                            if (resp.optBoolean("status")) {
                                // Save Diner Phone
                               // DOPreferences.setDinerPhone(MainApplicationClass.getInstance(), eventConfirmationAdapter.getPhoneNumber());

                                JSONObject outputParam = resp.optJSONObject("output_params");
                                if (outputParam != null) {

                                    JSONObject data = outputParam.optJSONObject("data");
                                    if(data!=null) {
                                        JSONArray extraInfoJsonArray = data.optJSONArray("section_1");


                                        navigateToEventsPayment(data.optString("do_wallet_amt"), data.optString("bill_amount"),
                                                data.optString("rest_wallet_amt"), data.optString("cashback_discount"),
                                                data.optString("max_cashback"), data.optString("restaurant_name"),
                                                extraInfoJsonArray, data.optString("cashback_text"),
                                                arguments.getString(AppConstant.BUNDLE_RESTAURANT_ID));

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



    // get free event amt hit
    private void getFreeEventAmount() {
        showLoader();
        getNetworkManager().stringRequestPost(102, AppConstant.URL_FREE_PAYMENT,
                getBillAmountParams( eventConfirmationAdapter.getPhoneNumber()),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(Request<String> request, String responseObject, Response<String> response) {
                        hideLoader();

                        if (getActivity() == null || getView() == null)
                            return;

                        try {
                            JSONObject resp = new JSONObject(responseObject);
                            if (resp.optBoolean("status")) {
                                // Save Diner Phone
//                                DOPreferences.setDinerPhone(MainApplicationClass.getInstance(), eventConfirmationAdapter.getPhoneNumber());

                                JSONObject outputParam = resp.optJSONObject("output_params");
                                if (outputParam != null) {

                                    // Navigate to free payment complete screen
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("status", 1);
                                    bundle.putInt("is_accept_payment", outputParam.optInt("is_accept_payment"));
                                    bundle.putInt("already_paid", outputParam.optInt("already_paid"));
                                    bundle.putInt("is_jp_booking", outputParam.optInt("is_jp_booking"));
                                    bundle.putString("message_1", outputParam.optString("success_message_1"));
                                    bundle.putString("message_2", outputParam.optString("success_message_2"));
                                    bundle.putString("rest_url", outputParam.optString("short_url"));
                                    bundle.putString("restaurant_locality", outputParam.optString("locality_name"));
                                    bundle.putInt("cnt_covers", outputParam.optInt("cnt_covers"));
                                    bundle.putString("res_name", outputParam.optString("res_name"));
                                    bundle.putString("res_id", outputParam.optString("res_id"));
                                    bundle.putString("booking_time", outputParam.optString("booking_time"));
                                    bundle.putString("booking_date", outputParam.optString("booking_date"));
                                    bundle.putString("guest_count", outputParam.optString("guest_count"));
                                    bundle.putString("disp_id", outputParam.optString("disp_id"));
                                    bundle.putString("b_id", outputParam.optString("b_id"));
                                    bundle.putString("type", "event");
                                    bundle.putInt("rating_dialog", outputParam.optInt("show_rating_dialog"));
                                    bundle.putString("events_title", eventsObj.optString("title"));
                                    bundle.putString("ticket_count", ticketsArray.length() + "");
                                    bundle.putString("previous_fragment", getResources().getString(R.string.events_confirmation));

//                                    HashMap<String, Object> props = new HashMap<String, Object>();
//                                    props.put("RestaurantName", bundle.getString("res_name", ""));
//                                    props.put("RestaurantId", bundle.getString("res_id", ""));
//                                    props.put("Amount", "0");
//                                    props.put("EventType", "Free");

                                    //trackMixPanelEvent("EventBought", props);

                                    BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();
                                    bookingDetailFragment.setArguments(bundle);

                                    addToBackStack(getActivity(), bookingDetailFragment);
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

    private void navigateToEventsPayment(String doWalletAmount, String billAmount, String restaurantWallet,
                                         String cashbackDiscount,String maxCashback,String restaurantName,
                                         JSONArray extraInfoJsonArray,String cashbackText,  String restaurantId) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstant.BUNDLE_BILL_AMOUNT, billAmount);
        bundle.putString("do_wallet_amount", doWalletAmount);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_WALLET_AMOUNT, restaurantWallet);
        bundle.putString(AppConstant.BUNDLE_CASHBACK_DISCOUNT, cashbackDiscount);
        bundle.putString(AppConstant.BUNDLE_MAX_CASHBACK, maxCashback);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_NAME_DETAIL, restaurantName);
        bundle.putString(AppConstant.BUNDLE_RESTAURANT_ID_DETAILS, restaurantId);
        bundle.putStringArray(AppConstant.BUNDLE_DEAL_IDS, eventsIds);
        bundle.putStringArray(AppConstant.BUNDLE_DEAL_QUANTITY, eventsQuantity);
        bundle.putLong(AppConstant.BUNDLE_DATE_TIME, arguments.getLong(AppConstant.DATE));
       // bundle.putString("spcl_request", eventConfirmationAdapter.getSpecialRequest());
        bundle.putString(AppConstant.BUNDLE_TYPE, "event");
        if (extraInfoJsonArray != null && extraInfoJsonArray.length() > 0) {
            bundle.putString(AppConstant.BUNDLE_EARNINGS_INFO, extraInfoJsonArray.toString());
        }
            bundle.putString(AppConstant.BUNDLE_CASHBACK_INFO, cashbackText);



        DealsBillAmountFragment dealsBillAmountFragment = DealsBillAmountFragment.getFragment(bundle);

        addToBackStack(getActivity(), dealsBillAmountFragment);
    }




    public void handleErrorResponse(JSONObject responseObject) {
        super.handleErrorResponse(responseObject);
        // This handles saving data and other stuff after User has logged in Successfully
        if (getActivity() != null) {
            // Hide Keyboard
            AppUtil.hideKeyboard(getActivity());
        }
    }

    @Override
    public void onCTAButtonUpdate(int totalAmount) {
        // Check UI
        if (getView() == null)
            return;

        if (totalAmount == 0) {
            textViewTotalTicketAmount.setText(R.string.text_free);

        } else {
            textViewTotalTicketAmount.setText(String.format(getString(R.string.text_total_amount_container), totalAmount));
        }
    }

    @Override
    public void onKnowMoreClick(JSONArray array) {

    }
}
