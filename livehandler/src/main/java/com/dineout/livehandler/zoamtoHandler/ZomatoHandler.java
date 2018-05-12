package com.dineout.livehandler.zoamtoHandler;

import android.content.Context;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.dineout.livehandler.IHandler;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sawai on 19/08/16.
 */
public class ZomatoHandler implements IHandler {
    private Context mContext;
    private static ZomatoHandler mInstance;
    private static Bus mBus;

    private ZomatoHandlerWithIDs mZomatoHandlerWithIds;
//    private ZomatoHandlerWithOutIDs mZomatoHandlerWithOutIDs;

    public static final String textViewClass = "android.widget.TextView";
    public static final String buttonClass = "android.widget.Button";
    public static final String imageButtonClass = "android.widget.ImageButton";
    public static final String reserveTableText = "Reserve a Table";
    public static final String bookTableText = "Book a Table";
    public static final List<String> restaurantPageList = new ArrayList<>(Arrays.asList("com.application.zomato.activities.RestaurantPage",
            "com.application.zomato.restaurant.RestaurantPage"));

    public static ZomatoHandler getInstance() {
        if (mInstance == null) {
            mInstance = new ZomatoHandler();
        }
        return mInstance;
    }

    private ZomatoHandler() {
        mZomatoHandlerWithIds = new ZomatoHandlerWithIDs();
//        mZomatoHandlerWithOutIDs = new ZomatoHandlerWithOutIDs();
    }

    @Override
    public boolean handleEvent(AccessibilityEvent event) {
        boolean returnValue = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            returnValue = mZomatoHandlerWithIds.handleEvent(event);
        }
//        else {
//            returnValue = mZomatoHandlerWithOutIDs.handleEvent(event);
//        }

        return returnValue;
    }


    @Override
    public void setContext(Context context) {
        mContext = context;
        mZomatoHandlerWithIds.setContext(mContext);
//        mZomatoHandlerWithOutIDs.setContext(mContext);
    }

    @Override
    public void setBus(Bus bus) {
        mBus = bus;
        mZomatoHandlerWithIds.setBus(mBus);
//        mZomatoHandlerWithOutIDs.setBus(mBus);
    }
}
