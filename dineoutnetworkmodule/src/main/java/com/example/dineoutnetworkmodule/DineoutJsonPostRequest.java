package com.example.dineoutnetworkmodule;

import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;
import com.dineout.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * Created by vibhaschandra on 01/02/16.
 */
public class DineoutJsonPostRequest extends JsonObjectRequest {
    // Constructor
    public DineoutJsonPostRequest(String url, JSONObject jsonRequest, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }
}
