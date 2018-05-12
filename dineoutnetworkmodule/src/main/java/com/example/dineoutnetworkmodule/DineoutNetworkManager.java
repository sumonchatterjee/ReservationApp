package com.example.dineoutnetworkmodule;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.dineout.android.volley.DefaultRetryPolicy;
import com.dineout.android.volley.Request;
import com.dineout.android.volley.Request.Method;
import com.dineout.android.volley.RequestQueue;
import com.dineout.android.volley.RequestQueue.GlobalRequestQueueListener;
import com.dineout.android.volley.RequestQueue.RequestFilter;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.Response.ErrorListener;
import com.dineout.android.volley.Response.Listener;
import com.dineout.android.volley.RetryPolicy;
import com.dineout.android.volley.VolleyError;
import com.dineout.android.volley.toolbox.JsonObjectRequest;
import com.dineout.android.volley.toolbox.Volley;
import com.dineout.dineoutssl.DineoutSSLPinning;

import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class DineoutNetworkManager
        implements
        RequestFilter,
        GlobalRequestQueueListener<Object> {

    public static String NETWORK_TYPE;

    private static RequestQueue requestQueue;
    private static DineoutSSLPinning dineoutSSLPinning;
    private static String BASE_API_URL;
    private static String BASE_NODE_API_URL;
    private static HashMap<String, String> headers;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());



    private DineoutNetworkManager(Context context) {


        mContext = context;
        if (dineoutSSLPinning == null) {
            // Load SSL Certificate
            loadSSLCertificate();
        }

        // Check for RequestQueue
        if (requestQueue == null) {

            String baseApiPhpUrl=TextUtils.isEmpty(DOPreferences.getApiBaseUrl(mContext)) ? AppConstant.BASE_DOMAIN_URL :
                    DOPreferences.getApiBaseUrl(mContext);
            String baseApiNodeUrl=TextUtils.isEmpty(DOPreferences.getApiBaseUrlNode(mContext)) ? AppConstant.BASE_DOMAIN_URL_NODE :
                    DOPreferences.getApiBaseUrlNode(mContext);
            String versionPhpUrl=TextUtils.isEmpty(DOPreferences.getPhpBaseUrl(mContext)) ? AppConstant.BASE_PHP_URL :
                    DOPreferences.getPhpBaseUrl(mContext);
            String versionNodeUrl=TextUtils.isEmpty(DOPreferences.getNodeBaseUrl(mContext)) ? AppConstant.NODE_BASE_URL :
                    DOPreferences.getNodeBaseUrl(mContext);

            BASE_API_URL = baseApiPhpUrl+ versionPhpUrl;

            BASE_NODE_API_URL = baseApiNodeUrl+ versionNodeUrl;

           // BASE_REVIEW_API_URL = AppConstant.BASE_DOMAIN_URL_REVIEW + AppConstant.BASE_PHP_URL;

            requestQueue = Volley.newRequestQueue(context, null,
                    Volley.DEFAULT_CACHE_DIR, 3, true, false ,dineoutSSLPinning);

            requestQueue.setGlobalRequestQueueListener(this);

            requestQueue.start();
        }

        // Check for SSL Pinning

    }

    public static DineoutNetworkManager newInstance(Context context) {
        DineoutNetworkManager instance = new DineoutNetworkManager(context);
        return instance;
    }


    public static String generateGetUrl(String url, Map<String, String> params) {
        Builder uriBuilder = Uri.parse(url).buildUpon();

        if (params != null) {
            Set<String> keySet = new TreeSet<>(params.keySet());

            for (String key : keySet) {
                String value = params.get(key);

                if (TextUtils.isEmpty(value)) { // Check for Empty
                    value = "";

                    uriBuilder.appendQueryParameter(key, value);

                } else if (value.contains("|")) { // Check for multiple values
                    String[] values = value.split("\\|");

                    for (String val : values) {
                        if (!TextUtils.isEmpty(val)) {
                            uriBuilder.appendQueryParameter(key, val);
                        }
                    }
                } else {
                    uriBuilder.appendQueryParameter(key, value);
                }
            }
        }

        //uriBuilder.appendQueryParameter(APP_VERSION, appVersionName);
        return uriBuilder.toString();
    }

    private void loadSSLCertificate() {
        try {
            dineoutSSLPinning = DineoutSSLPinning.getInstance();
            dineoutSSLPinning.initiateSSLPinning(mContext);
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getHeaders(Context context) {
        headers = new HashMap<String, String>();
        PackageInfo pInfo;

        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            headers.put(AppConstant.DEFAULT_HEADER_APP_VERSION, pInfo.versionName);
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_ID, DOPreferences.getDeviceId(context));
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_ID_1, DOPreferences.getGoogleAdId(context));
//            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_ID_2, DOPreferences.getImei(context));
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_TOKEN, DOPreferences.getGcmRegistrationToken(context));
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_TYPE, AppConstant.DEVICE_TYPE);
            headers.put(AppConstant.DEFAULT_HEADER_CITY_ID, DOPreferences.getCityId(context));
            headers.put(AppConstant.DEFAULT_HEADER_DINER_ID, DOPreferences.getDinerId(context) != null ?
                    DOPreferences.getDinerId(context) : null);
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_LATITUDE, DOPreferences.getCurrentLatitude(context));
            headers.put(AppConstant.DEFAULT_HEADER_DEVICE_LONGITUDE, DOPreferences.getCurrentLongitude(context));
            headers.put(AppConstant.DEFAULT_HEADER_ENTITY_LATITUDE, DOPreferences.getELatitude(context));
            headers.put(AppConstant.DEFAULT_HEADER_ENTITY_LONGITUDE, DOPreferences.getELongitude(context));
            headers.put(AppConstant.ACCEPT_ENCODING, "gzip");
            headers.put(AppConstant.GPS_ENABLED, DOPreferences.isAutoMode(context) ? "1" : "0");

            if (!TextUtils.isEmpty(DOPreferences.getAuthKey(context))) {
                headers.put(AppConstant.DEFAULT_HEADER_AUTH_KEY, DOPreferences.getAuthKey(context));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return (HashMap<String, String>) DineoutNetworkManager.headers.clone();
    }

    public Request<?> jsonRequest(JsonObjectRequest request, int identifier, boolean shouldCache) {

        request.setIdentifier(identifier);
        request.setShouldCache(shouldCache);
        request.setTag(this);

        request.setHeaders((mContext != null)
                ? getHeaders(mContext)
                : DineoutNetworkManager.headers);

        request.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addRequest(request);

        return request;
    }



    public Request<?> multipartRequest(Request request, int identifier,
                                       RetryPolicy policy) {

        Map<String, String> header = (mContext != null)
                ? getHeaders(mContext)
                : null;

        Map<String, String> requestHeader = new HashMap<>();
        if (header != null) {
            requestHeader.put(AppConstant.DEFAULT_HEADER_DINER_ID, header.get(AppConstant.DEFAULT_HEADER_DINER_ID));
            requestHeader.put(AppConstant.DEFAULT_HEADER_AUTH_KEY,
                    header.get(AppConstant.DEFAULT_HEADER_AUTH_KEY));

            requestHeader.put(AppConstant.DEFAULT_HEADER_DEVICE_TYPE,
                    header.get(AppConstant.DEFAULT_HEADER_DEVICE_TYPE));
            requestHeader.put("content_type", request.getBodyContentType());
            requestHeader.put("content_length", request.getBodyString().length() + "");
        }
        request.setIdentifier(identifier);
        request.setTag(this);
        request.setShouldCache(false);
        request.setHeaders(requestHeader);

        request.setRetryPolicy(policy);

        addRequest(request);
        return request;
    }

    public void deleteRequest(Request<?> request) {
        request.setDeleteCache(true);
        addRequest(request);
    }

    public void addRequest(final Request<?> request) {
        if (!request.isDeleteCache() && request.hasHadResponseDelivered()) {
            throw new UnsupportedOperationException(
                    "Cannot reuse Request which has already served the request");
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                requestQueue.add(request);
            }
        });
    }

    public Request<?> jsonRequestPost(int identifier, String url,
                                      Map<String, String> requestMap, Listener<JSONObject> listener,
                                      ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(url)) {
            url = BASE_API_URL + url;
        }

        JSONObject jsonRequest = ((requestMap == null) ? null : new JSONObject(requestMap));
        // NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.USER_INITIATED_POST);

        DineoutJsonPostRequest postRequest = new DineoutJsonPostRequest(url, jsonRequest, listener, errorListener);

        return jsonRequest(postRequest, identifier, shouldCache);
    }

    public Request<?> jsonRequestGet(int identifier, String urlPath,
                                     Map<String, String> requestMap, Listener<JSONObject> listener,
                                     ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(urlPath)) {
            urlPath = BASE_API_URL + urlPath;
        }
        // NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.USER_INITIATED_GET);

        String url = generateGetUrl(urlPath, requestMap);

        return jsonRequest(identifier, url, listener, errorListener, shouldCache);
    }

    public Request<?> jsonRequestGetService(int identifier, String urlPath,
                                            Map<String, String> requestMap, Listener<JSONObject> listener,
                                            ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(urlPath)) {
            urlPath = BASE_API_URL + urlPath;
        }
        // NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.SERVICE_INITIATED);

        String url = generateGetUrl(urlPath, requestMap);

        return jsonRequest(identifier, url, listener, errorListener, shouldCache);
    }


    public Request<?> jsonWithoutBaseRequestGet(int identifier, String urlPath,
                                                Map<String, String> requestMap, Listener<JSONObject> listener,
                                                ErrorListener errorListener, boolean shouldCache) {


        String url = generateGetUrl(urlPath, requestMap);

        return jsonRequest(identifier, url, listener, errorListener,
                shouldCache);
    }

    // Shashank - To handle Node Requests
    public Request<?> jsonRequestGetNode(int identifier, String urlPath,
                                         Map<String, String> requestMap, Listener<JSONObject> listener,
                                         ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(urlPath)) {
            urlPath = BASE_NODE_API_URL + urlPath;
        }

        String url = generateGetUrl(urlPath, requestMap);
        //NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.USER_INITIATED_GET);

        JsonObjectRequest request = new JsonObjectRequest(url, null, listener,
                errorListener);

        return jsonRequest(request, identifier, shouldCache);
    }


    public Request<?> jsonRequestPostNode(int identifier, String urlPath,
                                          HashMap<String, Object> params, Listener<JSONObject> listener,
                                          ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(urlPath)) {
            urlPath = BASE_NODE_API_URL + urlPath;
        }

        JSONObject paramsJsonObject = new JSONObject(params);

        JsonObjectRequest postRequest = new JsonObjectRequest(urlPath, paramsJsonObject, listener, errorListener);

        return jsonRequest(postRequest, identifier, shouldCache);
    }




    public Request<?> jsonRequest(int identifier, String url,
                                  Listener<JSONObject> listener, ErrorListener errorListener,
                                  boolean shouldCache) {

        if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
            url = BASE_API_URL + url;
        }

        JsonObjectRequest request = new JsonObjectRequest(url, null, listener, errorListener);

        return jsonRequest(request, identifier, shouldCache);
    }

    public Request<?> stringRequestPostNode(int identifier, String url,
                                            Map<String, String> params, Listener<String> listener,
                                            ErrorListener errorListener, boolean shouldCache, String latitude, String longitude) {
        if (!TextUtils.isEmpty(url)) {
            url = BASE_NODE_API_URL + url;
        }
        DineoutPostRequest request = new DineoutPostRequest(Method.POST, url, params, listener, errorListener);

        request.setIdentifier(identifier);
        request.setShouldCache(shouldCache);
        request.setTag(this);
        Map<String, String> header = (mContext != null)
                ? getHeaders(mContext)
                : DineoutNetworkManager.headers;
        header.put(AppConstant.DEFAULT_HEADER_DEVICE_LATITUDE, latitude);
        header.put(AppConstant.DEFAULT_HEADER_DEVICE_LONGITUDE, longitude);

        try {
            if (!header.containsKey("content_length")) {
                header.put("content_length", request.getBodyString().length() + "");
            }

        } catch (Exception e) {

        }

        request.setHeaders(header);
        request.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addRequest(request);

        return request;
    }

    public Request<?> stringRequestPost(int identifier, String url,
                                        Map<String, String> params, Listener<String> listener,
                                        ErrorListener errorListener, boolean shouldCache) {

        if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {
            url = BASE_API_URL + url;
        }

        //NetworkAnalyzerConstants.trackNetworkCallType(NetworkAnalyzerConstants.USER_INITIATED_POST);

        DineoutPostRequest request = new DineoutPostRequest(Method.POST, url, params, listener, errorListener);

        request.setIdentifier(identifier);
        request.setShouldCache(shouldCache);
        request.setTag(this);

        request.setHeaders((mContext != null)
                ? getHeaders(mContext)
                : DineoutNetworkManager.headers);

        request.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 2,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        addRequest(request);

        return request;
    }

    public void cancel() {
        requestQueue.cancelAll(this);
    }

    @Override
    public boolean apply(Request<?> request) {
        return request.getTag() == this;
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {

    }

    @Override
    public void onResponse(Request request, Object responseObject, Response response) {
        final long startTimeInMillis = response.getStartTimeInMillis();
        final long endTimeInMillis = response.getEndTimeInMillis();
    }
}
