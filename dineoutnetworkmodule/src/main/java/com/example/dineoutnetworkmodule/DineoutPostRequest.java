package com.example.dineoutnetworkmodule;

import com.dineout.android.volley.AuthFailureError;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;
import com.dineout.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by vibhaschandra on 01/02/16.
 */
public class DineoutPostRequest extends StringRequest {
    Map<String, String> params;

    public DineoutPostRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public DineoutPostRequest(int method, String url, Map<String, String>
            params, Listener<String> listener, ErrorListener errorListener) {

        this(method, url, listener, errorListener);

        this.params = params;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }
}
