package com.dineout.livehandler;

import android.content.Context;

import com.dineout.livehandler.zoamtoHandler.ZomatoHandler;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sawai on 19/08/16.
 */
public class AccessibilityMap {
    public static final String ZOMATO = "com.application.zomato";

    private static List<String> mRestaurantList = new ArrayList<>(Arrays.asList(ZOMATO));

    public static IHandler getHandler(String packageName, Context context, Bus bus) throws ClassNotFoundException {
        if (mRestaurantList.contains(packageName)) {
            IHandler handler = getHandler(packageName);
            handler.setContext(context);
            handler.setBus(bus);

            return handler;
        } else {
            throw new ClassNotFoundException();
        }
    }

    public static IHandler getHandler(String packageName) throws ClassNotFoundException {
        IHandler handler;
        switch (packageName) {
            case ZOMATO:
                handler = ZomatoHandler.getInstance();
                break;

            default:
                handler = null;
        }

        if (handler != null) {
            return handler;
        } else {
            throw new ClassNotFoundException();
        }
    }
}
