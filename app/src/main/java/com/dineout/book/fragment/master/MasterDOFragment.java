package com.dineout.book.fragment.master;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.analytics.tracker.AnalyticsHelper;
import com.dineout.android.volley.toolbox.ImageLoader;
import com.dineout.book.R;
import com.dineout.book.controller.FragmentTransactionManager;

import com.dineout.book.controller.DeeplinkParserManager;
import com.dineout.book.fragment.home.DOPlusFragment;
import com.dineout.book.fragment.home.DOPlusRestaurantFragment;
//import com.dineout.livehandler.AccessibilityPreference;
//import com.dineout.livehandler.AccessibilityUtils;
import com.dineout.recycleradapters.util.AppUtil;
import com.example.dineoutnetworkmodule.ApiParams;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.DineoutNetworkManager;
import com.example.dineoutnetworkmodule.ImageRequestManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.branch.referral.Branch;
import ly.count.android.sdk.Countly;

public abstract class MasterDOFragment extends DialogFragment {

    public static final int STYLE_NO_FRAME = DialogFragment.STYLE_NO_FRAME;

    private static final String TITLE = "base_title_key";
    private static final String IS_CHILD_FRAGMENT = "is_child_fragment";
    public static int NO_ANIMATION = -1;
    public AlertDialog smartpayDialog;
   // private NetworkErrorView networkErrorView;
    private Toolbar mToolbar;
    private boolean pageCurrent = true;
    private boolean childFragment = false;
    private DineoutNetworkManager networkManager;
    private ImageRequestManager imageRequestManager;


    /*
     * default handler for a fragment. *
     */
    private Handler mHandler;

    private ProgressBar progressBar;

    /**
     * Fragment replace methods **
     */
    public static void addToBackStack(FragmentActivity activity,
                                      MasterDOFragment fragment, boolean defaultAnimation) {
        addToBackStack(activity.getSupportFragmentManager(), fragment, defaultAnimation);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      MasterDOFragment fragment, boolean defaultAnimation) {
        if (defaultAnimation) {
            addToBackStack(manager, fragment);
        } else {
            replace(manager, R.id.fragment_base_container, fragment, 0, 0, 0, 0, true);
        }
    }

    public static void addToBackStack(FragmentActivity activity,
                                      MasterDOFragment fragment) {
        addToBackStack(activity.getSupportFragmentManager(), fragment);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      MasterDOFragment fragment) {
        addToBackStack(manager, fragment, R.id.fragment_base_container);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      MasterDOFragment fragment, int target) {
        replace(manager, target, fragment, NO_ANIMATION, NO_ANIMATION,
                NO_ANIMATION, NO_ANIMATION, true);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      MasterDOFragment fragment, int enter, int exit) {
        addToBackStack(manager, R.id.fragment_base_container, fragment, enter, exit);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      int targetId, MasterDOFragment fragment, int enter, int exit) {
        replace(manager, targetId, fragment, enter, exit, 0, 0, true);
    }

    public static void replace(FragmentActivity activity,
                               MasterDOFragment fragment) {
        replace(activity.getSupportFragmentManager(), R.id.fragment_base_container,
                fragment, NO_ANIMATION, NO_ANIMATION, NO_ANIMATION,
                NO_ANIMATION, true);
    }

    public static void removeTopAndAddToBackStack(FragmentActivity activity,
                                                  MasterDOFragment fragment) {
        popBackStack(activity.getSupportFragmentManager());
        replace(activity.getSupportFragmentManager(), R.id.fragment_base_container,
                fragment, NO_ANIMATION, NO_ANIMATION, NO_ANIMATION,
                NO_ANIMATION, true);
    }

    public static void replace(FragmentActivity activity, int targetId,
                               MasterDOFragment fragment) {
        replace(activity.getSupportFragmentManager(), targetId, fragment);
    }

    public static void replace(FragmentManager fragmentManager, int targetId,
                               MasterDOFragment fragment) {
        // may provide default animation
        replace(fragmentManager, targetId, fragment, NO_ANIMATION,
                NO_ANIMATION, NO_ANIMATION, NO_ANIMATION, false);
    }

    public static void replace(FragmentManager fragmentManager, int targetId,
                               MasterDOFragment fragment, boolean defaultAnimation) {
        // may provide default animation
        if (defaultAnimation) {
            replace(fragmentManager, targetId, fragment);
        } else {
            replace(fragmentManager, targetId, fragment, 0, 0, 0, 0, false);
        }
    }

    public static void addAllowingStateLoss(FragmentManager fragmentManager,
                                            MasterDOFragment fragment) {


        android.support.v4.app.FragmentTransaction transaction = fragmentManager
                .beginTransaction();
        transaction.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_NONE);

        try {
            transaction.replace(R.id.fragment_base_container, fragment,
                    fragment.getClass().getName());


            transaction.addToBackStack(fragment.getClass().getName());

            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void replace(FragmentManager fragmentManager, int targetId,
                               MasterDOFragment fragment, int enter, int exit, int popEnter,
                               int popExit, boolean isAddToBackStack) {

        FragmentTransactionManager.replace(fragmentManager, targetId,
                fragment,
                enter, exit, popEnter, popExit, isAddToBackStack);
    }

    public static void popBackStack(FragmentManager manager) {
        if (manager != null) {
            try {

                manager.popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void popBackStackWithAnimation(FragmentManager manager){
        if(manager!=null){
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(0,0, R.anim.enter_from_left, R.anim.exit_to_right);
            manager.popBackStack();


        }

    }

    public static void popBackStackImmediate(FragmentManager manager) {
        if (manager != null) {
            try {
                manager.popBackStackImmediate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void popToHome(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 1) {
            popBackStackTo(fragmentManager,
                    fragmentManager.getBackStackEntryAt(1));
        }
    }

    public static void popBackStackTo(FragmentManager manager,
                                      BackStackEntry entry) {
        FragmentTransactionManager.popBackStackTo(manager, entry,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void showFragment(FragmentManager manager, MasterDOFragment fragment) {
        FragmentTransaction ft = manager.beginTransaction();
        Fragment previousDialog = manager.findFragmentByTag("dialog");

        if (previousDialog != null) {
            ft.remove(previousDialog);
        }

        if (fragment != null) {
            fragment.show(manager, "dialog");
        }
    }

    /**
     * Checks if toolbar is to be shown. By default this is true.
     * If any fragment does not need a toolbar, override this function in respective
     * child class and return false.
     */

    protected boolean isToolbarNeeded() {
        return true;
    }

    /**
     * Inflates Toolbar in respective child classes
     */
    private void inflateToolbar() {

        mToolbar = (Toolbar) getView().findViewById(R.id.toolbar_fragment);

        if (mToolbar != null) {
            mToolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_navigation_arrow_back, null));
            mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleNavigation();
                }
            });
        }
    }

    public void handleNavigation() {
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            if (fragmentManager.getBackStackEntryCount() > 1) {
                popBackStackImmediate(getFragmentManager());
            }
        }
    }


    public Toolbar getToolbar() {
        return mToolbar;
    }



    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    /**
     * Fragment Life method start. **
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
        networkManager = DineoutNetworkManager.newInstance(getActivity());
        imageRequestManager = ImageRequestManager.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @CallSuper
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null || getActivity() == null)
            return;
        progressBar = (ProgressBar) getView().findViewById(R.id.dineoutLoader);

        if (isToolbarNeeded() && !isChildFragment()) {
            inflateToolbar();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }




    @Override
    public void onDestroyView() {
        // Cancel Network
        networkManager.cancel();

        super.onDestroyView();
    }


    public boolean isChildFragment() {
        return childFragment;
    }

    public void setChildFragment(boolean childFragment) {
        this.childFragment = childFragment;
    }


    /**
     * Called from child class to show loader if exist in xml layout.
     */
    protected void showLoader() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if(getActivity()!=null) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    /**
     * Called from child class to hide loader if exist in xml layout.
     */
    protected void hideLoader() {

        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        if(getActivity()!=null) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

    public DineoutNetworkManager getNetworkManager() {
        return networkManager;
    }

    public ImageLoader getImageLoader() {
        return imageRequestManager.getImageLoader();
    }

    /***
     * override this and return true if child is handling backpress (Toolbar and
     * device back skipTourButton)
     **/
    public boolean onPopBackStack() {
        return false;
    }

    /**
     * Called for visible fragment only
     *
     * @param connected (true if connectivity available)
     *
     *
     *
     **/
    public void onNetworkConnectionChanged(boolean connected) {

        // /if connected remove the error view and retry fail requests again
        if (connected) {
            onRemoveErrorView();

        }

        // call child fragment {@code onNetworkConnectionChanged}

    }

    public void onNetworkConnectionChanged(boolean connected, boolean isFromRetry){
        onNetworkConnectionChanged(connected);
        if(isFromRetry){
            FragmentManager childManager = getChildFragmentManager();
            //send to child fragment
            List<Fragment> fragments = childManager.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment != null
                            && fragment instanceof MasterDOFragment) {
                        ((MasterDOFragment) fragment)
                                .onNetworkConnectionChanged(connected);
                    }
                }
            }
        }
    }

    public void handleDeepLinks(Intent intent) {
        if (intent != null && intent.getAction() == Intent.ACTION_VIEW) {

            String ukey = intent.getStringExtra(DeeplinkParserManager.payloadUKey);

            if (!TextUtils.isEmpty(ukey)) {
                trackNotification(getActivity().getApplicationContext(), ukey);
            }

            String t = "";
            try {
                if (intent.getData() != null) {
                    t = URLDecoder.decode(intent.getData().toString(), "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


                MasterDOFragment frag = DeeplinkParserManager.getFragment(getActivity(), t);

                if (frag != null && (frag instanceof DOPlusFragment || frag instanceof DOPlusRestaurantFragment)) {
                    // Pop back to Home
                    popToHome(getActivity());

                    // Set Flag
                    DOPreferences.setHasDoPlusDeeplink(getActivity().getApplicationContext(), true);

                } else if (frag != null) {
                    addToBackStack(getActivity(), frag);
                }
//            }

            getActivity().setIntent(null);
        }
    }

    protected void trackNotification(Context context, String ukey) {
        // Take API Hit
        DineoutNetworkManager dineoutNetworkManager = DineoutNetworkManager
                .newInstance(context);
        dineoutNetworkManager.jsonRequestGet(101, AppConstant.URL_NOTIFICATION_TRACKING,
                ApiParams.getSetIsViewedNotificationTrackingParams(ukey), null, null, false);
    }




    public void onRemoveErrorView() {

    }


    public void trackEventCountly(String key,  Map<String, String> hMap){
        AnalyticsHelper.getAnalyticsHelper(getContext().getApplicationContext()).trackEventCountly(key,hMap);
    }


    public void trackScreenName(String screenName){
        AnalyticsHelper.getAnalyticsHelper(getContext()).trackScreenCountly(screenName);
        trackScreenToGA(screenName);
    }

    // track event for countly and GA
    public void trackEventForCountlyAndGA(String category, String action, String label,Map<String, String> hMap){

        if(hMap!=null){
            if(!TextUtils.isEmpty(category)) {
                hMap.put("category", category);
            }
            if(!TextUtils.isEmpty(label)){
                hMap.put("label",label);
            }
            if(!TextUtils.isEmpty(action)){
                hMap.put("action", action);
            }
        }

        trackEventCountly(action,hMap);
        trackEventGA(category,action,label);
    }

    //track event for qgraph,apsalar,branch
    public void trackEventQGraphApsalar(String action, HashMap<String, Object> hMap,
                                        boolean toBePushedInQgraph, boolean toBePushedInApsalar, boolean toBePushedInBranch) {
        if(hMap!=null) {
            HashMap<String, Object> map= DOPreferences.getGeneralEventParametersForQgraph(getContext());
            if(map!=null){
                hMap.putAll(map);
            }
            AnalyticsHelper.getAnalyticsHelper(getContext()).trackEventQGraphApsalar(action, hMap,
                    toBePushedInQgraph,toBePushedInApsalar);
        }

        if(toBePushedInBranch) {
            Branch.getInstance(getContext()).userCompletedAction(action);
        }
    }


    public void trackScreenToGA(String screenName) {
        AnalyticsHelper.getAnalyticsHelper(getContext())
                .trackScreen(screenName);
    }


    public void trackAdTechEvent(String k, String value) {
        AnalyticsHelper.getAnalyticsHelper(getContext()).trackAdTechEvent(k, value);
    }



    public void completeAdTechSesion() {
        AnalyticsHelper.getAnalyticsHelper(getContext()).completeAdTechSession();
    }


    public void trackEventGA(String category, String action, String label) {
        AnalyticsHelper.getAnalyticsHelper(getContext())
                .trackEventGA(category, action, label);
    }



    public void trackEventGA(String category, String action, String label, long value) {
        AnalyticsHelper.getAnalyticsHelper(getContext())
                .trackEventGA(category, action, label, value);
    }

    public void trackArrayAdTechEvent(String key, JSONArray cuisine) {

        if (cuisine != null) {
            for (int i = 0; i < cuisine.length(); i++) {

                trackAdTechEvent("int", key + ":" + cuisine.optString(i));
            }
        }
    }

    public void setToolbarTitle(String title) {
        if (mToolbar != null)
            mToolbar.setTitle(title);
    }

    public void setToolbarTitle(int title) {
        if (mToolbar != null)
            mToolbar.setTitle(title);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoader();
    }

    public AlertDialog getSmartpayDialog() {
        return smartpayDialog;
    }

    public void setSmartpayDialog(AlertDialog smartpayDialog) {
        this.smartpayDialog = smartpayDialog;
    }

    public void showSmartpayDialog() {
        // Check if Dialog instance is NULL
        if (getSmartpayDialog() == null) {
            // Get Smartpay View
            View smartPayView = LayoutInflater.from(getActivity().getApplicationContext()).
                    inflate(R.layout.smartpay_dialog, null, false);

            // Get Message from Pref
            JSONObject smartPayJsonObject = null;
            String smartPayMessage = DOPreferences.getSmartpayPopupMessage(getActivity().getApplicationContext());
            if (!AppUtil.isStringEmpty(smartPayMessage)) {
                try {
                    smartPayJsonObject = new JSONObject(smartPayMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Get Title
            TextView title = (TextView) smartPayView.findViewById(R.id.textView_smartpay_title);
            title.setText((smartPayJsonObject != null && !AppUtil.isStringEmpty(smartPayJsonObject.optString("title_1"))) ?
                    smartPayJsonObject.optString("title_1") : getString(R.string.text_smartPay));

            // Set Content 1
            TextView content1 = (TextView) smartPayView.findViewById(R.id.textView_smartpay_content_1);
            content1.setText((smartPayJsonObject != null && !AppUtil.isStringEmpty(smartPayJsonObject.optString("title_2"))) ?
                    smartPayJsonObject.optString("title_2") : getString(R.string.text_smartpay_content_1));

            // Set Content 2
            TextView content2 = (TextView) smartPayView.findViewById(R.id.textView_smartpay_content_2);
            content2.setText((smartPayJsonObject != null && !AppUtil.isStringEmpty(smartPayJsonObject.optString("title_3"))) ?
                    smartPayJsonObject.optString("title_3") : getString(R.string.text_smartpay_content_2));

            // Set Ok Button Click Listener
            smartPayView.findViewById(R.id.textView_smartpay_ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dismiss Dialog
                    getSmartpayDialog().dismiss();
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setView(smartPayView);

            // Set Dialog Instance
            setSmartpayDialog(builder.create());
        }

        // Show Alert Dialog
        getSmartpayDialog().show();
    }






    public interface OnFragmentDialogDismissListener {
        void onDismiss(MasterDOFragment fragment);
    }

    public static void addToBackStack(final FragmentManager manager,MasterDOFragment fragment,
                                      int enter, int exit, int popEnter, int popExit) {
        replace(manager, R.id.fragment_base_container, fragment, enter, exit, popEnter, popExit, true);
    }

    public static void addToBackStack(final FragmentManager manager,
                                      int targetId, MasterDOFragment fragment, int enter, int exit, int popEnter, int popExit) {
        replace(manager, targetId, fragment, enter, exit, popEnter, popExit, true);
    }
}
