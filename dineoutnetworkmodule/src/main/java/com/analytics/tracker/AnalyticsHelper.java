package com.analytics.tracker;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analytics.utilities.AnalyticsUtil;
import com.apsalar.sdk.Apsalar;
import com.crashlytics.android.Crashlytics;
import com.example.dineoutnetworkmodule.AppConstant;
import com.example.dineoutnetworkmodule.BuildConfig;
import com.example.dineoutnetworkmodule.DOPreferences;
import com.example.dineoutnetworkmodule.R;
import com.facebook.FacebookSdk;
import com.freshdesk.hotline.Hotline;
import com.freshdesk.hotline.HotlineUser;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.quantumgraph.sdk.QG;


import org.json.JSONException;
import org.json.JSONObject;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


import in.til.android.data.dmp.DMPIntegration;
import in.til.core.TilSDK;
import in.til.core.integrations.TILInterface;
import in.til.core.integrations.TILSDKExceptionDto;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import ly.count.android.sdk.Countly;

public class AnalyticsHelper {

    private static AnalyticsHelper analyticsHelper;
    private Tracker tracker;
    private Context mContext;
    private QG qg;

    private AnalyticsHelper(Context context) {
        mContext = context;
        initLibraries(context);
    }

    public static AnalyticsHelper getAnalyticsHelper(Context context) {
        if (analyticsHelper == null) {
            analyticsHelper = new AnalyticsHelper(context);
        }

        return analyticsHelper;
    }

    private void initLibraries(Context context) {
        initializeCrashlytics();
        initializeGA();
        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(context.getApplicationContext());
        initializeApsalar();
        //initializeMixPanel();
        initializeCountly(context);
        initializeHotline();
        initQGraph();
        initTilSDK();
        initializeBranchIO();
        sendUserAttributes();
    }

    public void trackAdTechEvent(String key,String value){

      //  Log.d("AdTech","KEY : "+key+">>>>>>>>Value: "+value);
      //  DmpManager.getInstance().addEvents(key,value);

        TilSDK.with().dmpEvent(key, value, new TILInterface() {
            @Override public void onSdkFailure(TILSDKExceptionDto tilsdkExceptionDto) {
                //onTILSDKFailure
            }
        });
    }


    public void completeAdTechSession(){
       // DmpManager.getInstance().completeSession();

        TilSDK.with().dmpcompleteSession(new TILInterface() {
            @Override
            public void onSdkFailure(TILSDKExceptionDto tilsdkExceptionDto) {
                // onTILSDKFailure
            }
        });

    }

    public void initializeCountly(Context mContext ){
        try {
           String url= AnalyticsUtil.IS_DEVELOPMENT ?
                    AppConstant.DINEOUT_COUNTLY_DEV_SDK_SERVER_URL : AppConstant.DINEOUT_COUNTLY_PRODUCTION_SDK_SERVER_URL;

            Countly.sharedInstance().init(mContext, url, "Dineout_Android");
        }catch (Exception ex){
            //exception
        }
    }

    public void startCountly(Activity activity){
        try {
            Countly.sharedInstance().onStart(activity);
        }catch (Exception ex){
            //exception
        }
    }

    public void stopCountly(){
        try {
            Countly.sharedInstance().onStop();
        }catch (Exception ex){
            //exception
        }
    }



    /**
     * initialization of Branch IO
     */

    public void initializeBranchIO() {

        Branch branch = Branch.getInstance();
        branch.initSession();
    }


    public void startAnalyticsTracker(Activity activity) {
        if (qg != null) {
            qg.onStart();
        }
        if(DOPreferences.isCountlyEnabled(activity.getApplicationContext())) {
            startCountly(activity);
        }
    }

    public void stopAnalyticsTracker(Activity activity) {
        if (qg != null) {
            qg.onStop();
        }

        if (DOPreferences.isCountlyEnabled(activity.getApplicationContext())){
            stopCountly();
    }
          completeAdTechSession();
        Branch.getInstance().closeSession();
    }


    /**
     * Initialization of QGraph
     */
    public void initQGraph() {
        //qg = QG.getInstance((Application) mContext.getApplicationContext(), mContext.getString(R.string.qgraph_app_id), mContext.getString(R.string.gcm_sender_id));

        //After updating sdk
        QG.initializeSdk((Application) mContext.getApplicationContext(), mContext.getString(R.string.qgraph_app_id), mContext.getString(R.string.gcm_sender_id));
        qg=QG.getInstance(mContext.getApplicationContext());

    }


    public void initTilSDK() {

       // DmpManager.initialize(mContext);

        HashMap<String,HashMap<String,String>> settings = new HashMap<String, HashMap<String,String>>();
        //Add settings for DMP
        HashMap<String,String> dmp = new HashMap<>();
        dmp.put("enabled","true");

        //Add dmp settings to TilSDK integration
        settings.put("tildmp",dmp);

        //Initialize DMP SDK via TIL SDK
        TilSDK tilSDK = new TilSDK.Builder(mContext)
                .use(DMPIntegration.FACTORY)//For enabling DMP
                .build(settings);
        TilSDK.setSingletonInstance(tilSDK);
    }

    /**
     * Initialization of Crashlytics
     */
    private void initializeCrashlytics() {
        if (BuildConfig.enableCrashlytics == true) {
            Fabric.with(mContext, new Crashlytics());
        }
    }


    /**
     * Initialization of google analytics
     */

    private void initializeGA() {
        GoogleAnalytics mGA = GoogleAnalytics.getInstance(mContext);
        mGA.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);

        tracker = mGA.newTracker(mContext.getResources().getString((AnalyticsUtil.IS_DEVELOPMENT) ?
                R.string.ga_tracking_id_testing : R.string.ga_tracking_id_production));
        tracker.enableAdvertisingIdCollection(true);

        int GA_DISPATCH_PERIOD = 60;
        mGA.setLocalDispatchPeriod(GA_DISPATCH_PERIOD);
        mGA.dispatchLocalHits();
    }

    private Tracker getGATracker() {
        return tracker;
    }

    /**
     * Initialization of Apsalar and start apsalar Session
     */

    private void initializeApsalar() {
        Apsalar.setFBAppId(mContext.getString(R.string.facebook_app_id));

        Apsalar.startSession(mContext, mContext
                .getString(R.string.apsalar_api_key), mContext.getString(R.string
                .apsalar_secret_key));
    }


    /**
     * Initialization of Clever Tap
     */

//    private void initializeMixPanel() {
//        try {
//
//            if(mixpanelAPI == null){
//                mixpanelAPI = MixpanelAPI.
//                        getInstance(mContext,
//                               mContext.getResources().getString((AnalyticsUtil.IS_DEVELOPMENT) ?
//                                       R.string.mixpanel_product_token_dev : R.string.mixpanel_product_token));
//                mixpanelAPI.identify(DOPreferences.getDeviceId(mContext));
//            }
////            //Get CleverTap API Instance
////            cleverTapAPI = CleverTapAPI.getInstance(mContext);
////
////            //Enable Personalization
////            cleverTapAPI.enablePersonalization();
//
//            //Set Mode
////            CleverTapAPI.setDebugLevel((AnalyticsUtil.IS_DEVELOPMENT) ? 1 : 0);
//
//        } catch (Exception e) {
//            DOLog.e("MixPanelException: ", e);
//        }
//    }




    /**
     * Initialization of Hotline
     */

    private void initializeHotline() {
        try {
            Hotline.getInstance(mContext).init(AnalyticsUtil.prepareHotlineConfig(mContext));
        }catch (Exception ex){

        }
    }

    public void onAppTerminated() {
        Apsalar.unregisterApsalarReceiver();

        Apsalar.endSession();
       // stopAnalyticsTracker();
    }

//    public void mixPanelPushGCM(String regId) {
//        try {
//
//            mixpanelAPI.getPeople().identify(DOPreferences.getDeviceId(mContext));
////            //Get Instance of CleverTapAPI
////            cleverTapAPI = CleverTapAPI.getInstance
////                    (context.getApplicationContext());
////
////            //Push GCM Registration ID
////            cleverTapAPI.data.pushGcmRegistrationId(regId, true);
//
//            mixpanelAPI.getPeople().setPushRegistrationId(regId);
//        } catch (Exception e) {
//            DOLog.e("MixPanel: ", e);
//        }
//    }

    public void sendUserAttributes() {
        // Get User Details in HashMap
        HashMap<String, Object> map = DOPreferences.getUserDataJSONObject(
                mContext);

        if (map != null && !map.isEmpty()) {


            // Hotline
            sendUserDataToHotline();

            //QGraph
            sendUserDataToQGraph(map);
        }
    }



//    private void sendUserDataToMixPanel(HashMap<String, Object> map) {
////        if (cleverTapAPI != null) {
////            cleverTapAPI.profile.push(map);
////        }
//
//        if(mixpanelAPI != null){
//            mixpanelAPI.getPeople().identify(DOPreferences.getDeviceId(mContext));
//            mixpanelAPI.getPeople().setMap(map);
//            try {
//                mixpanelAPI.getPeople().set(new JSONObject().put("$email",map.get("email")));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void sendUserDataToQGraph(HashMap<String, Object> map) {
        if (qg != null) {

            qg.setName((map.get("name")).toString());
            qg.setEmail((map.get("email")).toString());
            qg.setUserId((map.get("dinnerId")).toString());
            qg.setCity((map.get("cityId")).toString());
        }
    }

    public void trackScreen(String screenName) {
        // Set Screen Name for GA Events
        getGATracker().setScreenName(screenName);
        getGATracker().send(new HitBuilders.ScreenViewBuilder().build());
    }


    public void trackScreenCountly(String screenName){
        if(DOPreferences.isCountlyEnabled(mContext)) {
            try {
                Countly.sharedInstance().recordView(screenName);
            } catch (Exception ex) {
                //exception
            }
        }
    }


    public void trackEventQGraphApsalar(String eventName, HashMap<String, Object> map,
                                        boolean toBePushedInQgraph, boolean toBePushedInApsalar) {

        // Apsalar Event Tracking
        if(toBePushedInApsalar) {
            pushEventToApsalar(eventName, map);
        }

        // QGraph Event Tracking
        if(toBePushedInQgraph) {
            pushEventToQGraph(eventName, map);
        }

    }


    private void pushEventToApsalar(String eventName, HashMap<String, Object> map) {
        try {
            if (map == null || map.isEmpty()) {
                Apsalar.event(eventName);
            } else {
                Apsalar.eventJSON(eventName, new JSONObject(map));
                Log.d("apsalar","booking successful");
            }
        }catch (Exception ex){
            // exception
        }
    }



    public void pushEventToQGraph(String eventName, HashMap<String, Object> map) {
        if (qg != null) {
            if (map == null || map.isEmpty()) {
                qg.logEvent(eventName);
            } else {
                qg.logEvent(eventName, new JSONObject(map));
            }
        }
    }

    public void trackEventCountly(String key,  Map<String, String> hMap){
        if(DOPreferences.isCountlyEnabled(mContext)) {
            try {
                Countly.sharedInstance().recordEvent(key, hMap, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }



    public void trackEventGA(String category, String action, String label) {
        getGATracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel((TextUtils.isEmpty(label)) ? "" : label)
                .build());
    }

    public void trackEventGA(String category, String action, String label,long value) {
        getGATracker().send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel((TextUtils.isEmpty(label)) ? "" : label)
                .setValue(value)
                .build());

    }

    public void trackApsalarRevenueEvent(String restaurantId, String restaurantName, int guestCount) {
        int REVENUE_AMT = 40;
        Apsalar.event("__iap__",
                "ps", mContext.getString(R.string.apsalar_api_key),
                "pk", restaurantId,
                "pn", restaurantName,
                "pcc", "INR",
                "pq", guestCount,
                "pp", REVENUE_AMT,
                "r", (REVENUE_AMT * guestCount));
    }

    // Update User Details in Hotline
    public void sendUserDataToHotline() {
        try {
            // Update User
            HotlineUser hlUser = AnalyticsUtil.prepareHotlineUser(mContext);
            if (hlUser != null) {
                Hotline.getInstance(mContext).updateUser(hlUser);
            }

            // Update User Properties
            Map<String, String> userMeta = AnalyticsUtil.prepareHotlineUserProperties(mContext);
            if (userMeta != null && !userMeta.isEmpty()) {
                Hotline.getInstance(mContext).updateUserProperties(userMeta);
            }
        }catch (Exception ex){

        }
    }

    /**
     * Update gcm registration token on hotline
     */
    public void updateGcmRegIdOnHotline(String regId) {
        try {
            Hotline.getInstance(mContext).updateGcmRegistrationToken(regId);
        }catch (Exception ex){

        }
    }

//    public MixpanelAPI getMixPanelInstance(){
//
//       return mixpanelAPI;
//    }


    public void sendLoginAdTechEvent(){

        String gender="";
        String g=DOPreferences.getDinerGender(mContext);
        if(g.equalsIgnoreCase("M")){
            gender= "Male";
        }else if(g.equalsIgnoreCase("F")){
            gender="Female";
        }

        //source
        AnalyticsHelper.getAnalyticsHelper(mContext).trackAdTechEvent("Login", DOPreferences.getLoginSource(mContext));

        //gender
        AnalyticsHelper.getAnalyticsHelper(mContext).trackAdTechEvent("Gender" ,gender);

        //dob
        String dob="";
        String timeStampString = DOPreferences.getDinerDateOfbirth(mContext);
        if (!TextUtils.isEmpty(timeStampString)) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(timeStampString) * 1000);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            dob=(day + "/" + month + "/" + year);
        }
        AnalyticsHelper.getAnalyticsHelper(mContext).trackAdTechEvent("DOB" ,dob);

        System.out.println("XXXXX: "  + " event sent");
    }


    /**
     comparison=0 - same date
     comparison =1 - next day
     comparison=-1 - previous day
    */
    public void trackAdTechEventAppMaximize() {
        try {
            if (!TextUtils.isEmpty(DOPreferences.getDinerId(mContext))) {
                String currentLoginDate = AnalyticsUtil.getDateFormat(System.currentTimeMillis());

                if (!TextUtils.isEmpty(currentLoginDate)) {
                    // send on resume event if required
                    String previousLoginDateForAppOpen = DOPreferences.getLoginDateForFirstTimeAppOpen(mContext);
                    if (!TextUtils.isEmpty(previousLoginDateForAppOpen)
                            && currentLoginDate.compareTo(previousLoginDateForAppOpen) == 1) {
                        sendLoginAdTechEvent();
                        DOPreferences.setLoginDateForFirstTimeAppOpen(mContext, AnalyticsUtil.getDateFormat(System.currentTimeMillis()));
                    }
                }
            }
        } catch (Exception e) {
            // exception
        }
    }

    public void trackAdTechEventAppMinimize(){
        try {
            if (!TextUtils.isEmpty(DOPreferences.getDinerId(mContext))) {
                String currentLoginDate = AnalyticsUtil.getDateFormat(System.currentTimeMillis());

                if (!TextUtils.isEmpty(currentLoginDate)) {
                    // send on pause/stop event if required
                    String previousLoginDateForAppClose = DOPreferences.getLoginDateForFirstTimeAppClose(mContext);
                    if (!TextUtils.isEmpty(previousLoginDateForAppClose)
                            && currentLoginDate.compareTo(previousLoginDateForAppClose) == 1) {
                        sendLoginAdTechEvent();
                        DOPreferences.setLoginDateForFirstTimeAppClose(mContext, AnalyticsUtil.getDateFormat(System.currentTimeMillis()));
                    }
                }
            }
        } catch (Exception e) {
            // exception
        }
    }


}
