package com.dineout.book.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.VolleyError;
import com.dineout.book.R;
import com.dineout.book.model.webservice.UberResponse;
import com.dineout.book.util.AppUtil;
import com.dineout.book.util.Constants;
import com.dineout.book.util.JsonParser;
import com.dineout.book.util.UiUtil;
import com.dineout.book.fragment.master.MasterDOJSONReqFragment;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class UberCabsDialog extends MasterDOJSONReqFragment
        implements View.OnClickListener {

    public static final String START_LAT = "start_lat";
    public static final String START_LNG = "start_lng";
    public static final String END_LAT = "end_lat";
    public static final String END_LNG = "end_lng";
    public static final String LOC_NAME = "loc_name";

    private int REQUEST_CODE_UBER_PRICE = 101;
    private int REQUEST_CODE_UBER_TIME = 102;

    private String startLat;
    private String startLng;
    private String endLat;
    private String endLng;
    private String locationName;
    private JSONObject uberPriceJsonObject;
    private ListView mListView;
    private RelativeLayout progressLayout;
    private RelativeLayout relativeLayoutUberDetailSection;

    public static UberCabsDialog newInstance(String startLatitude, String startLongitude,
                                             String endLatitude, String endLongitude, String locName) {
        UberCabsDialog uberCabsDialog = new UberCabsDialog();
        uberCabsDialog.setArguments(bundle(startLatitude, startLongitude, endLatitude, endLongitude, locName));
        return uberCabsDialog;
    }

    public static Bundle bundle(String startLatitude, String startLongitude,
                                String endLatitude, String endLongitude, String locName) {

        Bundle bundle = new Bundle();
        bundle.putString(START_LAT, startLatitude);
        bundle.putString(START_LNG, startLongitude);
        bundle.putString(END_LAT, endLatitude);
        bundle.putString(END_LNG, endLongitude);
        bundle.putString(LOC_NAME, locName);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (getArguments() != null) {
            startLat = bundle.getString(START_LAT);
            startLng = bundle.getString(START_LNG);
            endLat = bundle.getString(END_LAT);
            endLng = bundle.getString(END_LNG);
            locationName = bundle.getString(LOC_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uber_dialog_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeView();
    }

    private void initializeView() {
        progressLayout = (RelativeLayout) getView().findViewById(R.id.progress_layout);
        relativeLayoutUberDetailSection = (RelativeLayout) getView().findViewById(R.id.relativeLayout_uber_detail_section);
        mListView = (ListView) getView().findViewById(R.id.lv_timing_list);

        Button mDismiss = (Button) getView().findViewById(R.id.btn_dismiss);
        mDismiss.setOnClickListener(this);
        fetchUberCabs();
    }

    private void fetchUberCabs() {
        progressLayout.setVisibility(ProgressBar.VISIBLE);
        relativeLayoutUberDetailSection.setVisibility(RelativeLayout.GONE);

        getNetworkManager().jsonWithoutBaseRequestGet(REQUEST_CODE_UBER_PRICE, AppConstant.UBER_PRICE_URL,
                ApiParams.getFetchUberPriceParams(Constants.UBER_SERVER_TOKEN,
                        startLat,
                        startLng,
                        endLat,
                        endLng), this, this, false);
    }

    private void fetchUberCabsArrivalTime() {

        getNetworkManager().jsonWithoutBaseRequestGet(REQUEST_CODE_UBER_TIME, AppConstant.UBER_TIME_URL,
                ApiParams.getFetchUberCabsArrivalTimeParams(Constants.UBER_SERVER_TOKEN,
                        startLat,
                        startLng), this, this, false);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {
        super.onResponse(request, responseObject, response);

        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == REQUEST_CODE_UBER_PRICE) {
            if (responseObject != null && AppUtil.isStringEmpty(responseObject.optString("code"))) {
                uberPriceJsonObject = responseObject;

                fetchUberCabsArrivalTime();
            }
        } else if (request.getIdentifier() == REQUEST_CODE_UBER_TIME) {
            progressLayout.setVisibility(ProgressBar.GONE);
            relativeLayoutUberDetailSection.setVisibility(RelativeLayout.VISIBLE);

            if (responseObject != null && AppUtil.isStringEmpty(responseObject.optString("code"))) {
                showUberDialog(responseObject);
            }
        }
    }

    @Override
    public void onErrorResponse(Request request, VolleyError error) {
        super.onErrorResponse(request, error);

        progressLayout.setVisibility(ProgressBar.GONE);

        if (request.getIdentifier() == REQUEST_CODE_UBER_PRICE) {
            try {
                String response = new String(error.networkResponse.networkData);
                UberResponse uberResponse = JsonParser.getInstance().fromJson(response, UberResponse.class);

                if (uberResponse != null && uberResponse.getMessage() != null) {
                    UiUtil.showToastMessage(getContext(), uberResponse.getMessage());

                } else {
                    UiUtil.showToastMessage(getContext(), getString(R.string.text_no_uber_cabs_found));
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), getString(R.string.text_no_uber_cabs_found));
            }
        } else if (request.getIdentifier() == REQUEST_CODE_UBER_TIME) {
            try {
                String response = new String(error.networkResponse.networkData);
                UberResponse uberResponse = JsonParser.getInstance().fromJson(response, UberResponse.class);

                if (uberResponse != null && uberResponse.getMessage() != null) {
                    UiUtil.showToastMessage(getContext(), uberResponse.getMessage());

                } else {
                    UiUtil.showToastMessage(getContext(), getString(R.string.text_no_uber_cabs_found));
                }
            } catch (Exception e) {
                UiUtil.showToastMessage(getContext(), getString(R.string.text_no_uber_cabs_found));
            }
        }

        // Dismiss Dialog
        dismissAllowingStateLoss();
    }

    private void showUberDialog(JSONObject responseObject) {
        JSONArray priceJsonArray = uberPriceJsonObject.optJSONArray("prices");
        JSONArray timeJsonArray = responseObject.optJSONArray("times");

        if (priceJsonArray != null && timeJsonArray != null) {
            ArrayList<String> cabList = new ArrayList<>();
            int priceListSize = priceJsonArray.length();
            int timeListSize = timeJsonArray.length();

            String priceProdId="";
            String timeProdId="";

            for (int indexPrice = 0; indexPrice < priceListSize; indexPrice++) {
                JSONObject priceJsonObject = (JSONObject) priceJsonArray.opt(indexPrice);
                if (priceJsonObject != null  ) {
                    priceProdId = priceJsonObject.optString("product_id");
                }
                int countMatch=0;

                for(int indexTime=0;indexTime< timeListSize;indexTime++) {


                    JSONObject timeJsonObject = (JSONObject) timeJsonArray.opt(indexTime);

                    if (timeJsonObject != null) {
                        timeProdId = timeJsonObject.optString("product_id");
                    }
                    //int time = 0;
                    if (timeProdId.equalsIgnoreCase(priceProdId) &&
                            !TextUtils.isEmpty(priceProdId)) {
                        countMatch=1;

                        int time = (timeJsonObject.optInt("estimate", 0) / 60);

                        cabList.add(priceJsonObject.optString("localized_display_name", "") +
                                " (" + priceJsonObject.optString("estimate", "") + ") in " +
                                time + " mins");
                        break;
                    }




                }// end of inner for loop
                if(countMatch==0){
                           cabList.add(priceJsonObject.optString("localized_display_name", ""));
                }

            }

            if (!AppUtil.isCollectionEmpty(cabList)) {
                setUpTimingList(priceJsonArray, cabList);

            } else {
                UiUtil.showSnackbar(getView(), getString(R.string.text_no_uber_cabs_found), 0);
            }

        } else {
            UiUtil.showSnackbar(getView(), getString(R.string.text_no_uber_cabs_found), 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_dismiss:
                dismiss();
                break;
        }
    }

    private void setUpTimingList(final JSONArray uberPriceList, ArrayList<String> uberDetails) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.gen_text_option, R.id.textViewOption, uberDetails);

        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (uberPriceList != null) {
                    JSONObject priceJsonObject = (JSONObject) uberPriceList.opt(position);

                    if (priceJsonObject != null && getActivity() != null) {
                        // Get Package Manager
                        PackageManager packageManager = getContext().getPackageManager();

                        try {
                            // Dismiss Dialog
                            dismissAllowingStateLoss();

                            packageManager.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);

                            String broadCastUrl = "uber://?client_id=" + Constants.UBER_CLIENT_ID + "&action=setPickup&" +
                                    "pickup=my_location&pickup[latitude]=" + startLat + "&" +
                                    "pickup[longitude]=" + startLng + "&" +
                                    "dropoff[latitude]=" + endLat + "&" +
                                    "dropoff[longitude]=" + endLng + "&" +
                                    "dropoff[nickname]=" + locationName + "&" +
                                    "product_id=" + priceJsonObject.optString("product_id", "");

                            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(broadCastUrl));
                            getActivity().startActivity(intent);

                        } catch (PackageManager.NameNotFoundException e) {
                            String url = "http://www.uber.com/go/dineoutindia";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mListView.setOnItemClickListener(null);

        startLat = null;
        startLng = null;
        endLat = null;
        endLng = null;
        locationName = null;
        uberPriceJsonObject = null;
        mListView = null;
        progressLayout = null;
        relativeLayoutUberDetailSection = null;
    }
}
