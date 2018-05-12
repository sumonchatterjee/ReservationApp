/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dineout.android.volley.toolbox;

import com.dineout.android.volley.NetworkResponse;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;

import java.io.UnsupportedEncodingException;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class StringRequest extends Request<String> {
    private final Listener<String> mListener;

    /**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public StringRequest(int method, String url, Listener<String> listener,
            ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public StringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    protected void deliverResponse(Request<String> request,String response,Response<String> fullResponse) {
        mListener.onResponse(request,response,fullResponse);
    }
    
    @Override
    protected Response<String> parseNetworkResponseUnpacked(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.processedData, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.processedData);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }
}