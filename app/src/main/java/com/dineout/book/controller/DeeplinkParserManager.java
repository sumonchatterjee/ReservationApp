package com.dineout.book.controller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;


import com.dineout.book.fragment.myaccount.NewMyEarningFragment;
import com.dineout.book.fragment.payments.fragment.InvoiceFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.fragment.bookingflow.CloneBookingTimeSlotFragment;
import com.dineout.book.fragment.deals.DealsFragment;
import com.dineout.book.fragment.detail.RestaurantDetailFragment;
import com.dineout.book.fragment.home.DOPlusFragment;
import com.dineout.book.fragment.home.DOPlusRestaurantFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.myaccount.MyListFragment;
import com.dineout.book.fragment.myaccount.ReferEarnFragment;
import com.dineout.book.fragment.myaccount.YourBillsFragment;
import com.dineout.book.fragment.mybookings.BookingDetailsFragment;
import com.dineout.book.fragment.promovoucher.PromotionGiftVoucherFragment;
import com.dineout.book.fragment.search.RestaurantListFragment;
import com.dineout.book.fragment.settings.TalkToUsFragment;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.List;

public class DeeplinkParserManager {

    // uri host & syntaxes
    public static final String SCHEMA = "dineout://";

    // Deep-links that require Login
    public static final String hostWriteReview = "w_r";
    public static final String hostReferralRewards = "r_r";
    public static final String hostDinerWallet = "d_w";
    public static final String hostDinerPromotions = "d_p";
    public static final String hostFeedBack = "talk";
    public static final String hostMyList = "mylist";
    public static final String hostYourBill = "y_bill";

    public static final String hostSearch = "s";
    public static final String hostUpdateApp = "u_a";
    public static final String hostBookingDetail = "b_d";
    public static final String hostRestoDetail = "r_d";
    public static final String hostDOPlusZone = "dp_zone";
    public static final String hostDeals = "deals";
    public static final String hostWebView = "web";
    public static final String hostEarningHistory= "e_history";
    public static final String hostInvoice = "d_invoice";
    public static final String hostPaymentFailure = "d_translist";

    // Payload & their titles
    public static final String payloadTitle = "nt";
    public static final String payloadMessage = "nm";
    public static final String payloadURI = "uri";
    public static final String payloadUKey = "ukey";
    public static final String hostAccessibility = "acc_pm";
    public static final String hostSlots = "slots";

    private static String deepLinkQuery;
//    private static String deepLinkTag;
//    private static String deepLinkCuisine;
//    private static String deepLinkLocalityId;
//    private static String deepLinkCityId;
//    private static String deepLinkChainId;
    private static String deepLinkWebViewTitle;
    private static String deepLinkWebViewLink;

    public static MasterDOFragment getFragment(Activity context, String urlDeepLink) {

        Uri uri = null;
        if (!TextUtils.isEmpty(urlDeepLink)) {

            uri = Uri.parse(urlDeepLink);
            if (uri == null) {
                return null;

            }
        }

        String pageString="";
        if (!TextUtils.isEmpty(uri.getQueryParameter("p"))) {
            pageString = uri.getQueryParameter("p");

        } else {

            if(uri.getScheme()!=null) {
                if (uri.getScheme().equalsIgnoreCase("http")) {
                    List<String> pathSegments = uri.getPathSegments();
                    if (pathSegments != null) {
                        String firstPathSegment = pathSegments.get(0);
                        pageString = firstPathSegment;
                    } else {
                        pageString = "";
                    }
                } else {
                    pageString = uri.getHost();
                }
            }
        }

        if (!TextUtils.isEmpty(pageString)) {
            //Fetch QUERY
            deepLinkQuery = uri.getQueryParameter(Constants.DEEP_LINK_QUERY);


            if (pageString.equalsIgnoreCase(hostDOPlusZone)) {

                if (DOPreferences.isDinerDoPlusMember(context).equalsIgnoreCase("1")) {
                    return new DOPlusRestaurantFragment();
                } else {
                    return new DOPlusFragment();
                }

            } else if (pageString.equalsIgnoreCase(hostReferralRewards)) {
                return new ReferEarnFragment();

            } else if (pageString.equalsIgnoreCase(hostRestoDetail)) {
                RestaurantDetailFragment restaurantDetailFragment = new RestaurantDetailFragment();

                String tab = uri.getQueryParameter(Constants.DEEP_LINK_TAB);

                // Set Bundle
                Bundle restaurantBundle = new Bundle();
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID, deepLinkQuery); // Restaurant Id

                // Set Tab
                if (!AppUtil.isStringEmpty(tab)) {
                    restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_TAB, tab);
                }

                // AnalyticsHelper.getAnalyticsHelper(context).trackScreenToGA("Details_Reservation");
                restaurantDetailFragment.setArguments(restaurantBundle);

                return restaurantDetailFragment;

            } else if (pageString.equalsIgnoreCase(hostDinerPromotions)) {

                PromotionGiftVoucherFragment fragment = new PromotionGiftVoucherFragment();

                Bundle b = new Bundle();
                b.putString(Constants.SEARCH_KEYWORD, deepLinkQuery);
                fragment.setArguments(b);
                return fragment;

            } else if (pageString.equalsIgnoreCase(hostSearch)) {
                Bundle bundle = new Bundle();
                bundle.putString(AppConstant.PARAM_DEEPLINK, urlDeepLink); // Deeplink

                RestaurantListFragment fragment = new RestaurantListFragment();
                fragment.setArguments(bundle);
                return fragment;

            } else if (pageString.equalsIgnoreCase(hostDinerWallet)) {
                MasterDOFragment frag = new NewMyEarningFragment();
                frag.setArguments(new Bundle());
                return frag;

            } else if (pageString.equalsIgnoreCase(hostFeedBack)) {
                return new TalkToUsFragment();

            } else if (pageString.equalsIgnoreCase(hostUpdateApp)) {
                Intent intentDeepLink = new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id=" + context
                                .getApplicationContext().getPackageName()));
                context.startActivity(intentDeepLink);

            } else if (pageString.equalsIgnoreCase(hostBookingDetail)) {
                BookingDetailsFragment fragment = new BookingDetailsFragment();
                Bundle b = new Bundle();
                b.putString("b_id", deepLinkQuery);
                // b.putString("b_type", "upcoming");
                fragment.setArguments(b);
                return fragment;

            } else if (pageString.equalsIgnoreCase(hostDeals)) {
                return new DealsFragment();

            } else if (pageString.equalsIgnoreCase(hostMyList)) {
                return new MyListFragment();

            } else if (pageString.equalsIgnoreCase(hostWebView)) {
                WebViewFragment fragment = new WebViewFragment();
                Bundle bundle = new Bundle();
                deepLinkWebViewLink = uri.getQueryParameter(Constants.DEEP_LINK_WEB_LINK);
                deepLinkWebViewTitle = uri.getQueryParameter(Constants.DEEP_LINK_WEB_TITLE);
                bundle.putString("title", deepLinkWebViewTitle);
                bundle.putString("url", deepLinkWebViewLink);
                fragment.setArguments(bundle);
                return fragment;

            } else if (pageString.equalsIgnoreCase(hostYourBill)) {
                return YourBillsFragment.newInstance();

            } else if (pageString.equalsIgnoreCase(hostWriteReview)) {
                BookingDetailsFragment bookingDetailFragment = new BookingDetailsFragment();

                Bundle bundle = new Bundle();
                bundle.putString("b_id", deepLinkQuery);

                bookingDetailFragment.setArguments(bundle);
                return bookingDetailFragment;

            } else if (pageString.equalsIgnoreCase(hostSlots)) {
                CloneBookingTimeSlotFragment slotFragment = new CloneBookingTimeSlotFragment();

                // Set Bundle
                Bundle restaurantBundle = new Bundle();
                restaurantBundle.putString(AppConstant.BUNDLE_RESTAURANT_ID,
                        uri.getQueryParameter(Constants.DEEP_LINK_REST_ID)); // Restaurant Id

                restaurantBundle.putString(AppConstant.BUNDLE_DEAL_ID,
                        uri.getQueryParameter(Constants.DEEP_LINK_DEAL_ID)); // Offer Id

                restaurantBundle.putBoolean(AppConstant.BUNDLE_VIA_DEEPLINK, true); // Has Deeplink

                slotFragment.setArguments(restaurantBundle);

                return slotFragment;

            } else if (pageString.equalsIgnoreCase(hostEarningHistory)){

                MasterDOFragment frag = new NewMyEarningFragment();
                frag.setArguments(new Bundle());
                return frag;


            }else if (pageString.equalsIgnoreCase(hostInvoice)){

                InvoiceFragment invoiceFragment =  new InvoiceFragment();
                Bundle bundle = new Bundle();
                bundle.putString("d_invoice", deepLinkQuery);

                invoiceFragment.setArguments(bundle);
                return invoiceFragment;

            }else if (pageString.equalsIgnoreCase(hostPaymentFailure)){

                MasterDOFragment frag = new NewMyEarningFragment();
                frag.setArguments(new Bundle());
                return frag;
            }
        }

        return null;
    }
}
