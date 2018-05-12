package com.dineout.book.fragment.home;

import android.view.View;
import android.webkit.WebView;

import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.dialogs.NetworkErrorView;


public class GirfTabWebViewFragment extends WebViewFragment {
    @Override
    public void handleErrorMessage(NetworkErrorView networkErrorView, WebView webView) {
        if (networkErrorView != null) {
            networkErrorView.setVisibility(View.VISIBLE);

                networkErrorView.setCustomErrorMessage("Something went wrong. Please refresh..!! ");
                networkErrorView.setType(NetworkErrorView.ConditionalDialog.CUSTOM_ERROR);
                webView.clearHistory();
                webView.loadUrl("about:blank");



        }
    }
}
