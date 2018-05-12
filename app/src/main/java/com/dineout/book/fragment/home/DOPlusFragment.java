package com.dineout.book.fragment.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dineout.android.volley.Request;
import com.dineout.android.volley.Response;
import com.dineout.android.volley.toolbox.NetworkImageView;
import com.dineout.book.R;
import com.dineout.book.application.MainApplicationClass;
import com.dineout.book.controller.UserAuthenticationController;
import com.dineout.book.fragment.dopluspurchase.MemberShipDetailFragment;
import com.dineout.book.fragment.login.AuthenticationWrapperJSONReqFragment;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.dopluspurchase.MembershipEnquiryFragment;
import com.dineout.book.dialogs.GenericShareDialog;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;


public class DOPlusFragment extends AuthenticationWrapperJSONReqFragment
        implements View.OnClickListener{

    public static final String TAG_NAME = DOPlusFragment.class.getSimpleName();
    public static final String CITY_ARRAY_KEY = "city_array";
    public static final String CALL_TIME_ARRAY = "time_array";
    private final int DO_CONVERSION_REQUEST_CODE = 0x40;
    private TextView mDOPaymentView;
    private LinearLayout mImageContainer;
    private LinearLayout mHowItWorksContainer;

    private View mIntroContainer;
    private String mCityListArray;
    private String mCallTimeArray;
    private JSONObject shareContent;
    private TextView mRestaurantCountFirstSection;
    private TextView mRestaurantCountThirdSection;
    private View mDoNotAvailable;

    public static DOPlusFragment newInstance() {
        DOPlusFragment fragment = new DOPlusFragment();
        //fragment.setChildFragment(true);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_do_plus_tab_intro, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //track
        //trackScreenName(getString(R.string.countly_nondoplus));


        findViewByIDS();

        if (!DOPreferences.isDinerDoPlusMember(getContext()).equalsIgnoreCase("1")) {
            callConverApi();
        }
    }

    private void findViewByIDS() {

        mDoNotAvailable = getView().findViewById(R.id.imageViewDOPlusNotAvailable);
        mRestaurantCountFirstSection = (TextView) getView().findViewById(R.id.do_first_restaurant);
        mRestaurantCountThirdSection = (TextView) getView().findViewById(R.id.do_refer_restaurant);
        mIntroContainer = getView().findViewById(R.id.intro_container);
        mHowItWorksContainer = (LinearLayout) getView().findViewById(R.id.linearLayoutDOPlusStepSection);
        mImageContainer = (LinearLayout) getView().findViewById(R.id.do_partner_image_container);
        mDOPaymentView = (TextView) getView().findViewById(R.id.amount_info_do_plus);

        getView().findViewById(R.id.section_one).findViewById(R.id.btn_buy_now_do_plus).setOnClickListener(this);
        getView().findViewById(R.id.section_one).findViewById(R.id.btn_tell_more_do_plus).setOnClickListener(this);

        getView().findViewById(R.id.section_two).findViewById(R.id.btn_buy_now_do_plus).setOnClickListener(this);
        getView().findViewById(R.id.section_two).findViewById(R.id.btn_tell_more_do_plus).setOnClickListener(this);

        getView().findViewById(R.id.section_three).findViewById(R.id.btn_buy_now_do_plus).setOnClickListener(this);
        getView().findViewById(R.id.section_three).findViewById(R.id.btn_tell_more_do_plus).setOnClickListener(this);

        getView().findViewById(R.id.btn_share_it_do).setOnClickListener(this);
    }

    private void callConverApi() {
        String cityId = DOPreferences.getCityId(getActivity());

        if (TextUtils.isEmpty(cityId)) {
            return;
        }

        mIntroContainer.setVisibility(View.GONE);

        showLoader();

        // Take API Hit
        getNetworkManager().jsonRequestGet(DO_CONVERSION_REQUEST_CODE, AppConstant.URL_DOPLUS_CONVERSION_DETAIL,
                ApiParams.getDoplusConversionDetailParams(cityId), this, this, true);
    }

    @Override
    public void onResponse(Request<JSONObject> request, JSONObject responseObject, Response<JSONObject> response) {

       // super.onResponse(request, responseObject, response);
        if (getActivity() == null || getView() == null)
            return;

        if (request.getIdentifier() == DO_CONVERSION_REQUEST_CODE) {

             hideLoader();
            if (responseObject != null && !responseObject.optBoolean("status") && responseObject.optInt("error_code") == 101) {
                    mDoNotAvailable.setVisibility(View.VISIBLE);
                   return;

            }
            else if(responseObject!=null && !responseObject.optBoolean("status")){
                super.onResponse(request,responseObject,response);
            }
            else if(responseObject != null && responseObject.optBoolean("status")) {
                mIntroContainer.setVisibility(View.VISIBLE);
                JSONObject outputParamsJsonObject = responseObject.optJSONObject("output_params");

                if (outputParamsJsonObject != null) {
                    JSONObject dataJsonObject = outputParamsJsonObject.optJSONObject("data");

                    if (dataJsonObject != null) {
                        prepareConversionDetailScreen(dataJsonObject);

                        shareContent = dataJsonObject.optJSONObject("share_message");
                        mCityListArray = dataJsonObject.optJSONArray("city_list") != null ?
                                dataJsonObject.optJSONArray("city_list").toString() : "";
                        mCallTimeArray = dataJsonObject.optJSONArray("best_time_to_call") != null ?
                                dataJsonObject.optJSONArray("best_time_to_call").toString() : "";
                    }
                }
            }

        }
    }

    private void prepareConversionDetailScreen(JSONObject data) {
        if (getActivity() == null) {
            return;
        }
        String desc = getString(R.string.label_dineout_section_one_desc)
                .replace("###", data.optString("doplus_offer"))
                .replace("***", data.optString("doplus_restaurant_count"));
        mRestaurantCountFirstSection.setText(desc);

        String desc2 = String.format(getString(R.string.doplus_refer_restaurant),
                data.optString("doplus_restaurant_count")).replace("###", data.optString("doplus_offer"));
        mRestaurantCountThirdSection.setText(desc2);
        mImageContainer.removeAllViews();
        mImageContainer.invalidate();
        JSONArray imgArray = data.optJSONArray("dining_destination_images");
        for (int i = 0; i < imgArray.length(); i++) {
            String url = imgArray.optString(i);
            String finalUrl = getPathFromUrl(url);
            View imageView = getEliteRestaurantView(finalUrl);
            if (imageView != null) {
                mImageContainer.addView(imageView);
            }
        }
        mDOPaymentView.setText(String.format(getString(R.string.label_do_plus_section_three_desc),
                data.optString("doplus_cost", "0")));
        prepareHowItWorksSteps(data.optJSONArray("how_it_works"));
    }

    private String getPathFromUrl(String url) {
        String finalUrl = url;
        String[] urlParts = url.split("\\?");

        if (urlParts != null) {
            finalUrl = urlParts[0];
        }

        return finalUrl;
    }

    private View getEliteRestaurantView(String url) {
        NetworkImageView imageView = null;

        if (getActivity() != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            if (layoutInflater != null) {
                imageView = (NetworkImageView) layoutInflater.inflate(R.layout.do_plus_image_view, null, false);

                if (imageView != null) {
                    // Set Default Image
                    imageView.setDefaultImageResId(R.drawable.img_default_hotel);

                    if (!TextUtils.isEmpty(url)) {

                        imageView.setImageUrl(url, getImageLoader());
                    }
                }
            }
        }

        return imageView;
    }

    private void launchMemberShipEnquiryForm() {

        String cityArray = createCityArray();
        String[] timeArray = createCallTimeArray();

        if (!TextUtils.isEmpty(cityArray) && timeArray != null) {
            Bundle bundle = new Bundle();
            bundle.putString(CITY_ARRAY_KEY, createCityArray());
            bundle.putStringArray(CALL_TIME_ARRAY, createCallTimeArray());
            MasterDOFragment fragment = MembershipEnquiryFragment.newInstance(bundle);
            addToBackStack(getActivity(), fragment);
        }
    }

    private void prepareHowItWorksSteps(JSONArray steps) {
        if (steps != null && steps.length() > 0) {

            mHowItWorksContainer.removeAllViews();

            int stepsSize = steps.length();

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            for (int index = 0; index < stepsSize; index++) {
                View stepView = layoutInflater.inflate(R.layout.do_plus_step_layout, null, false);

                String step = steps.optString(index);

                TextView textViewStepNumber = (TextView) stepView.findViewById(R.id.step_number);
                textViewStepNumber.setText(String.valueOf(index + 1));

                TextView textViewStepDescription = (TextView) stepView.findViewById(R.id.step_description);
                textViewStepDescription.setText(step);

                if (index == (stepsSize - 1)) {
                    View viewStepLine = stepView.findViewById(R.id.viewStepLine);
                    viewStepLine.setVisibility(View.GONE);
                }

                mHowItWorksContainer.addView(stepView);
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_buy_now_do_plus:

                //track event
                HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());

                trackEventForCountlyAndGA(getString(R.string.countly_nondoplus), getString(R.string.d_restaurant_buy_now_click),
                        getString(R.string.d_restaurant_buy_now_click) , hMap);

                // track for qgraph, apsalar
                trackEventQGraphApsalar(getString(R.string.d_restaurant_buy_now_click),new HashMap<String, Object>(),true,false,false);
                launchBuyNowScreen();

                break;
            case R.id.btn_tell_more_do_plus:

                //track for countly and ga
                HashMap<String, String> hMaps = DOPreferences.getGeneralEventParameters(getContext());

                trackEventForCountlyAndGA(getString(R.string.countly_nondoplus), getString(R.string.d_tellmemore_click),
                        getString(R.string.d_tellmemore_click) , hMaps);

                 // track for qgraph, apsalar
                trackEventQGraphApsalar(getString(R.string.d_tellmemore_click),new HashMap<String, Object>(),true,false,false);
                launchMemberShipEnquiryForm();

                break;
            case R.id.btn_share_it_do:

                //track event
                HashMap<String, String> hMaps1 = DOPreferences.getGeneralEventParameters(getContext());

                trackEventForCountlyAndGA(getString(R.string.countly_nondoplus), getString(R.string.d_do_share_it_click),
                        getString(R.string.d_do_share_it_click) , hMaps1);

                showShareCard();
                break;
        }
    }

    private void launchBuyNowScreen() {

        if (TextUtils.isEmpty(DOPreferences.getDinerId(getContext()))) {
            // Ask User to Login
            askUserToLogin();

        }else {
            String key = createCityArray();

            if (!TextUtils.isEmpty(key)) {
                Bundle bundle = new Bundle();
                bundle.putString(CITY_ARRAY_KEY, createCityArray());
                MasterDOFragment fragment = MemberShipDetailFragment.newInstance(bundle);
                addToBackStack(getActivity(), fragment);
            }
        }
    }

    // Ask User to Login
    private void askUserToLogin() {

        // initiate login flow
        UserAuthenticationController.getInstance(getActivity()).startLoginFlow(null, this);
    }

    private String createCityArray() {

        try {
            JSONArray target = new JSONArray();
            JSONArray array = new JSONArray(mCityListArray);
            JSONObject sample = new JSONObject();
            sample.put("value", "Select City");
            sample.put("id", "-1");
            target.put(sample);
            for (int i = 0; i < array.length(); i++) {
                target.put(array.opt(i));
            }
            return target.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String[] createCallTimeArray() {
        try {
            JSONArray target = new JSONArray();
            JSONArray array = new JSONArray(mCallTimeArray);

            String selectValue = "Best Time to Call";
            String[] values = new String[array.length() + 1];
            target.put(selectValue);
            values[0] = selectValue;
            for (int i = 0; i < array.length(); i++) {
                int j = i + 1;
                values[j] = array.optString(i);
                target.put(array.opt(i));
            }

            return values;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void showShareCard() {
        if (shareContent != null) {
            GenericShareDialog dialog = GenericShareDialog.getInstance(shareContent.toString());
            dialog.show(getActivity().getSupportFragmentManager(), DOPlusFragment.class.getSimpleName());
        }
    }

//    @Override
//    public void loginSuccess(LoginFlowBaseFragment.LoginResult detail, DialogFragment dialog) {
//        if (dialog != null)
//            dialog.dismiss();
//
//        if (getActivity() != null) {
//            if (!DOPreferences.isDinerDoPlusMember
//                    (MainApplicationClass.getInstance()).equalsIgnoreCase("1")) {
//
//                String key = createCityArray();
//
//                if (!TextUtils.isEmpty(key)) {
//                    Bundle bundle = new Bundle();
//                    bundle.putString(CITY_ARRAY_KEY, createCityArray());
//
//                    MasterDOFragment fragment = MemberShipDetailFragment.newInstance(bundle);
//
//                    addToBackStack(getActivity(), fragment);
//                }
//
//                return;
//            }
//
//            if (getParentFragment() != null) {
//                ((HomePageMasterFragment) getParentFragment()).refreshViewPager();
//            }
//        }
//    }
//
//    @Override
//    public void loginFailure(LoginFlowBaseFragment.LoginError cause, DialogFragment dialog) {
//        if (getActivity() != null) {
//            if (cause.getType() != null &&
//                    cause.getType().equalsIgnoreCase(LoginFlowBaseFragment.LoginType.NONE_CANCELLED)) {
//
//                // Dismiss Dialog
//                if (dialog != null)
//                    dialog.dismiss();
//
//                return;
//            }
//
//            // Show Error Message
//            if (!AppUtil.isStringEmpty(cause.getCause())) {
//                UiUtil.showToastMessage(getActivity().getApplicationContext(), cause.getCause());
//            }
//        }
//    }

    @Override
    public void loginFlowCompleteSuccess(JSONObject object) {
        if (getActivity() != null) {
            if (!DOPreferences.isDinerDoPlusMember
                    (MainApplicationClass.getInstance()).equalsIgnoreCase("1")) {

                String key = createCityArray();
                if (!TextUtils.isEmpty(key)) {
                    Bundle bundle = new Bundle();
                    bundle.putString(CITY_ARRAY_KEY, createCityArray());

                    MasterDOFragment fragment = MemberShipDetailFragment.newInstance(bundle);

                    addToBackStack(getActivity(), fragment);
                }
                return;
            }
//            else {
//
//                if (getParentFragment() != null) {
//                    ((HomePageMasterFragment) getParentFragment()).refreshViewPager();
//                }
//            }
        }
    }

    protected void showLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoader() {
        ProgressBar progressBar;

        if (getView() != null &&
                (progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader)) != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

}
