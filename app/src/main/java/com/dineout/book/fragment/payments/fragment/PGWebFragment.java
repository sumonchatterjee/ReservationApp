package com.dineout.book.fragment.payments.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.util.AppUtil;
import com.dineout.book.dialogs.NetworkErrorView;

import org.json.JSONObject;

public class PGWebFragment extends DOBaseFragment {

    private String url = "";
    private WebView webView;
    private View networkErrorView;
    private String postData = "";

    public  static PGWebFragment newInstance(String url,String data){

        PGWebFragment fragment = new PGWebFragment();
        fragment.postData = data;
        fragment.url = url;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        networkErrorView = getView().findViewById(R.id.networkErrorView);

        setToolbarTitle("Payment");
        initWebView();
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);

    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        if (connected) {
            if (networkErrorView != null) {
                networkErrorView.setVisibility(View.GONE);
                if(TextUtils.isEmpty(postData))
                    webView.loadUrl(url);
                else {
                    try{


                        webView.postUrl(url, postData.getBytes());
                    }catch (Exception e){
                        // Exception
                    }
                }
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

    private void initWebView() {

        if (getView() == null) {
            return;
        }



        webView = (WebView) getView().findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webView.setWebViewClient(new DoWebViewClient());
        webView.setWebChromeClient(new DOChromClient());
        if (AppUtil.hasNetworkConnection(getActivity().getApplicationContext())) {
            onNetworkConnectionChanged(true);
        } else {
            showNetworkErrorView();
        }


    }


    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }

    private class DOChromClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return true;
        }
    }


    private class DoWebViewClient extends WebViewClient {


        @Override
        public void onPageFinished(WebView view, String url) {

            hideLoader();

            view.clearHistory();

            if (getContext() != null) {
                if (!TextUtils.isEmpty(url) && url.contains("freecharge_response")) {
                    String response = Uri.parse(url).getQueryParameter("response");
                    handlePGResponse(response);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().flush();
            }else{
                CookieSyncManager.getInstance().sync();
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //  showLoadingDialog(true);
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
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }



    private void handlePGResponse(String resp){
        try {
            if (!TextUtils.isEmpty(resp)) {
                JSONObject response = new JSONObject(resp);

                String next = response.optString("next");
                if(!TextUtils.isEmpty(next)){
                    if(next.equalsIgnoreCase("get_payment_option")){
                        ((AddMoneyFragment) getTargetFragment()).
                                handleFailureResponse(response.optString("error_msg"));
                    } else {
                        boolean isSuccessFromFreeCharge = "1".equals(response.optString("is_success"));
                        ((AddMoneyFragment) getTargetFragment()).handlePaymentSuccess(isSuccessFromFreeCharge, response.optString("trans_id"), response.optString("amount"));
                    }
                }
            }
        }catch (Exception e){
            // Exception
        }
    }
}
