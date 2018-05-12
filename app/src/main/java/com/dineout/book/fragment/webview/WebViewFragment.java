package com.dineout.book.fragment.webview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dineout.book.R;
import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.util.AppUtil;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.dialogs.NetworkErrorView;
import com.example.dineoutnetworkmodule.DOPreferences;

import java.util.HashMap;

public class WebViewFragment extends MasterDOFragment {

    private String url = "";
    private WebView webView;
    private View networkErrorView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            url = getArguments().getString("url");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        findViewByIds();



            if (getArguments() != null) {

                    if (!isChildFragment()) {
                        getView().findViewById(R.id.toolbar_fragment).setVisibility(View.VISIBLE);
                        setToolbarTitle(getArguments().getString("title"));

                        if(getArguments().getString("title").equalsIgnoreCase(getString(R.string.text_smartPay))){
                            trackScreenName(getString(R.string.countly_smartpay));

                            //track event
                            HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                            trackEventForCountlyAndGA(getString(R.string.countly_smartpay),
                                    getString(R.string.d_smart_pay_view),getString(R.string.d_smart_pay_view),hMap);
                        }
                    } else {
                        getView().findViewById(R.id.toolbar_fragment).setVisibility(View.GONE);
                    }


                if (AppUtil.hasNetworkConnection(getActivity().getApplicationContext())) {
                    onNetworkConnectionChanged(true);
                } else {
                    showNetworkErrorView();
                }

            }

    }


    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        if (connected) {
            if (networkErrorView != null) {
                networkErrorView.setVisibility(View.GONE);

                webView.loadUrl(url);
                showLoader();

            }
        }
    }

    public void showNetworkErrorView() {
        if (networkErrorView != null) {
            networkErrorView.setVisibility(View.VISIBLE);
            if (networkErrorView instanceof NetworkErrorView) {
                ((NetworkErrorView) networkErrorView)
                        .setType(NetworkErrorView.ConditionalDialog.INTERNET_CONNECTION);

            }
        }
    }

    private void findViewByIds() {

        webView = (WebView) getView().findViewById(R.id.webview);
        networkErrorView = getView().findViewById(R.id.networkErrorView);
        webView.setWebViewClient(new DoWebViewClient());
        webView.setScrollContainer(true);

        setCookieManagerForWebview(url);

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);

        webView.setFocusableInTouchMode(true);

        // webView.freeMemory();
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setCookieManagerForWebview(String loadUrl) {
        CookieManager cookieManager = CookieManager.getInstance();

        if (getActivity() == null || getView() == null)
            return;

        cookieManager.setCookie(loadUrl, "ak=" + DOPreferences.getAuthKey(getActivity()));
        cookieManager.setCookie(loadUrl, "diner_id=" + DOPreferences.getDinerId(getActivity()));
        cookieManager.setCookie(loadUrl, "user_agent=" + "android_webview");
        cookieManager.setCookie(loadUrl, "by_city="+DOPreferences.getSfByCity(getActivity()));
        cookieManager.setCookie(loadUrl, "arr_locarea="+DOPreferences.getSfArrLocarea(getActivity()));
        cookieManager.setCookie(loadUrl, "arr_area="+DOPreferences.getSfArrArea(getActivity()));
        cookieManager.setCookie(loadUrl, "elat="+DOPreferences.getELatitude(getActivity()));
        cookieManager.setCookie(loadUrl, "elng="+DOPreferences.getELongitude(getActivity()));

    }


    private class DoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.contains(DeeplinkParserManager.SCHEMA)) {
                MasterDOFragment masterDOFragment = DeeplinkParserManager.getFragment(getActivity(), url);
                if (masterDOFragment != null && getActivity() != null) {
                    addToBackStack(getActivity().getSupportFragmentManager(), masterDOFragment);
                } else if (url.contains(DeeplinkParserManager.hostAccessibility)) {
                   // AppUtil.redirectToAccessibilityWalkthrough(getActivity(), url);
                }
            } else {

                showLoader();
                setCookieManagerForWebview(url);
                view.loadUrl(url);
            }

            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Please press Continue to proceed..!!");
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideLoader();
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {

                handleErrorMessage((NetworkErrorView) networkErrorView,webView);

        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            // Handle the error
            handleErrorMessage((NetworkErrorView) networkErrorView,webView);
        }

    }

    public void handleErrorMessage(NetworkErrorView networkErrorView,WebView webview){

    }

    @Override
    public boolean onPopBackStack() {
        if (webView.isFocused() && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            return super.onPopBackStack();
        }

    }

    @Override
    public void handleNavigation() {
        if (webView.isFocused() && webView.canGoBack()) {
            webView.goBack();
        } else {
           super.handleNavigation();
        }
    }
}