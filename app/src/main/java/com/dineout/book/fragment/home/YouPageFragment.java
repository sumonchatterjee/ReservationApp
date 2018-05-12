package com.dineout.book.fragment.home;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.fragment.myaccount.DineoutPlusCardFragment;
import com.dineout.book.fragment.myaccount.MyListFragment;
import com.dineout.book.fragment.myaccount.NewMyAccountFragment;
import com.dineout.book.fragment.myaccount.NewMyEarningFragment;
import com.dineout.book.fragment.myaccount.ReferEarnFragment;
import com.dineout.book.fragment.myaccount.YourBillsFragment;
import com.dineout.book.fragment.mybookings.BookingsFragment;
import com.dineout.book.fragment.promovoucher.PromotionGiftVoucherFragment;
import com.dineout.book.fragment.settings.SettingsFragment;
import com.dineout.book.fragment.settings.TalkToUsFragment;
import com.dineout.book.fragment.webview.WebViewFragment;
import com.dineout.book.util.AppUtil;
import com.dineout.recycleradapters.YouTabRecyclerAdapter;
import com.dineout.recycleradapters.view.widgets.RoundedImageView;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.phonepe.android.sdk.api.PhonePe;
import com.phonepe.android.sdk.base.models.UserInfo;
import com.phonepe.android.sdk.domain.builders.UserInfoBuilder;

import java.util.HashMap;


public class YouPageFragment extends MasterDOFragment
        implements YouTabRecyclerAdapter.YouTabItemClickHandler {

    //public static final String TAG = YouPageFragment.class.getSimpleName();
    private final int ACCOUNT_CLICKED = 100;

    private RecyclerView youOptionsList;

    private RoundedImageView imageViewUserPic;
    private TextView textViewUsername, textViewSubTitle;
   // private CoordinatorLayout snackbarYou;
    private boolean isUserLoggedIn;


    //private boolean mIsOTPFlowRequired;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_you, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initializeView();

        initializeData();

        // Hide Keyboard
        AppUtil.hideKeyboard(getActivity());
    }

    // Initialize View
    private void initializeView() {
        // Set Flag for User is Logged In
        isUserLoggedIn = !TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()));

        imageViewUserPic = (RoundedImageView) getView().findViewById(R.id.image_view_user_pic);
        imageViewUserPic.setDefaultImageResId(R.drawable.img_profile_nav_default);
        textViewUsername = (TextView) getView().findViewById(R.id.text_view_user_name);
        textViewSubTitle = (TextView) getView().findViewById(R.id.text_view_sub_title);
        youOptionsList = (RecyclerView) getView().findViewById(R.id.list_you_options);
        //snackbarYou = (CoordinatorLayout) getView().findViewById(R.id.snackbar_you);

        // Define Layout Manager for List
        youOptionsList.setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    // Initialize Data
    private void initializeData() {
        // Set Adapter
        youOptionsList.setAdapter(new YouTabRecyclerAdapter(getActivity(), this));

        // Set User Details
        setUserDetails();

        getView().findViewById(R.id.relative_layout_you_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleClick(ACCOUNT_CLICKED);
            }
        });
    }

    // Set User Pic and Details
    private void setUserDetails() {

        if (getActivity() == null)
            return;

        // Reset User Pic
        imageViewUserPic.setImageUrl("", getImageLoader());

        // Set User Pic
        imageViewUserPic.setImageUrl((isUserLoggedIn ?
                DOPreferences.getDinerProfileImage(getContext()) : ""), getImageLoader());

        // Set User Name
        textViewUsername.setText(isUserLoggedIn ?
                (DOPreferences.getDinerFirstName(getContext()) +
                        " " + DOPreferences.getDinerLastName(getContext())) :
                getString(R.string.you_logged_out_title));

        // Set Sub Title
        textViewSubTitle.setText(
                getString(isUserLoggedIn ?
                        R.string.you_logged_in_subtitle : R.string.you_logged_out_subtitle));
    }

    // Handle Click
    private void handleClick(int position) {
        // Check Network
        //if (AppUtil.hasNetworkConnection(getActivity().getApplicationContext())) {

            // Check if User is Logged In
//            if (false && TextUtils.isEmpty(DOPreferences.getDinerId(getActivity()))
//                    && position != R.drawable.ic_you_settings
//                    && position != R.drawable.ic_you_smart_pay) {
//
//                // Ask User to Login
////                loginAndNavigate(position);
//
//            } else {
                switch (position) {

                    case ACCOUNT_CLICKED:
                        addToBackStack(getActivity(), new NewMyAccountFragment());
                        break;

                    case R.drawable.ic_you_doplus:
                        addToBackStack(getActivity(),new DineoutPlusCardFragment());
                        break;

                    case R.drawable.ic_you_refer:
                        addToBackStack(getActivity(), new ReferEarnFragment());
                        break;

                    case R.drawable.ic_you_smart_pay: {
                        WebViewFragment webViewFragment = new WebViewFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("url", AppConstant.DINEOUT_SMART_PAY_URL);
                        bundle.putString("title", getString(R.string.text_smartPay));

                        webViewFragment.setArguments(bundle);

                        addToBackStack(getActivity(), webViewFragment);
                    }
                    break;

                    case R.drawable.ic_you_favourites:
                        addToBackStack(getActivity(), new MyListFragment());
                        break;

                    case R.drawable.ic_you_bookings:
                        addToBackStack(getActivity(), new BookingsFragment());
                        break;

                    case R.drawable.ic_you_wallet:
                        MasterDOFragment frag = new NewMyEarningFragment();
                        frag.setArguments(new Bundle());
                        addToBackStack(getActivity(), frag);
                        break;

                    case R.drawable.ic_you_your_bills:
                        addToBackStack(getActivity(), YourBillsFragment.newInstance());
                        break;

//                    case R.drawable.ic_you_transaction:
//                        addToBackStack(getActivity(), new TransactionHistoryFragment());
//                        break;

                    case R.drawable.ic_you_promotions:
                        addToBackStack(getActivity(), new PromotionGiftVoucherFragment());
                        break;

                    case R.drawable.ic_you_talk_to_us:

                        addToBackStack(getActivity(), new TalkToUsFragment());

                        break;

                    case R.drawable.ic_you_settings:
                        addToBackStack(getActivity(), new SettingsFragment());
                        break;


                    case R.drawable.phonepeyouicon:

                        HashMap<String, String> hMap = DOPreferences.getGeneralEventParameters(getContext());
                        trackScreenName(getString(R.string.countly_phonepe));
                        trackEventForCountlyAndGA(getString(R.string.countly_phonepe),getString(R.string.d_phonepe_view),getString(R.string.d_phonepe_view),hMap);

                        UserInfo userInfo = new UserInfoBuilder() // Optional. Used for auto filling
                                .setUserId(DOPreferences.getDinerId(getContext())) // mandatory for linking
                        .setMobileNumber(DOPreferences.getDinerPhone(getContext()))
                        .setEmail(DOPreferences.getDinerEmail(getContext()))
                        .setShortName(DOPreferences.getDinerFirstName(getContext()))
                        .build();

                        PhonePe.showAccountDetails(DOPreferences.getDinerId(getContext()), userInfo);
                        break;

                    default:
                        break;
                }
           // }
        //}
    }



    @Override
    public void handleItemClick(int position) {
        handleClick(position);
    }
}
