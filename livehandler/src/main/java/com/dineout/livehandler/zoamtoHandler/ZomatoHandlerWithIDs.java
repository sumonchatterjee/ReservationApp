package com.dineout.livehandler.zoamtoHandler;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.dineout.livehandler.AccessibilityMap;
import com.dineout.livehandler.AccessibilityUtils;
import com.dineout.livehandler.IHandler;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;

import java.util.List;

/**
 * Created by sawai on 30/08/16.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ZomatoHandlerWithIDs implements IHandler {
    private Context mContext;
    private static Bus mBus;

    public static final String bottomFabButtonId = "com.application.zomato:id/fabtoolbar_fab";
    public static final String bottomFabButtonId2 = "com.application.zomato:id/open_table_button";
    public static final String restIdVersion1 = "com.application.zomato:id/rest_name";
    public static final String restAddVersion1 = "com.application.zomato:id/rest_full_address";
    public static final String restIdVersion2 = "com.application.zomato:id/restaurant_header_name";
    public static final String restAddVersion2 = "com.application.zomato:id/restaurant_header_address";

    @Override
    public boolean handleEvent(AccessibilityEvent event) {
        return handleEventVersionAPI18Version2(event) || handleEventVersionAPI18Version1(event);
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void setBus(Bus bus) {
        mBus = bus;
    }

    // for old zomato version
    private boolean handleEventVersionAPI18Version1(AccessibilityEvent event) {
        boolean returnValue = false;
        String eventClass;
        if (event != null && (!TextUtils.isEmpty(event.getClassName()))) {
            eventClass = event.getClassName().toString();

            if (eventClass.contains(AccessibilityMap.ZOMATO)) {
                if (ZomatoHandler.restaurantPageList.get(0).equals(eventClass)) {
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    returnValue = processRootNodeWithIdsVersion1(nodeInfo);

                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.HIDE_WINDOW_EVENT);
                    mBus.post(jsonObject);

                    returnValue = false;
                }
            } else if (ZomatoHandler.imageButtonClass.equals(eventClass)) {
                AccessibilityNodeInfo imageButtonNode;
                if ((imageButtonNode = event.getSource()) != null
//                        && bottomFabButtonId.equals(imageButtonNode.getViewIdResourceName())
                        ) {
                    AccessibilityNodeInfo rootNode = imageButtonNode;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNodeWithIdsVersion1(rootNode);
                }
            } else if (ZomatoHandler.buttonClass.equals(eventClass)) {
                AccessibilityNodeInfo buttonNode;
                if ((buttonNode = event.getSource()) != null
                        &&
                        (   bottomFabButtonId2.equals(buttonNode.getViewIdResourceName())
                            || ZomatoHandler.reserveTableText.equals(buttonNode.getText())
                        )
                        ) {
                    AccessibilityNodeInfo rootNode = buttonNode;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNodeWithIdsVersion1(rootNode);
                }
            }
        }
        return returnValue;
    }

    private boolean handleEventVersionAPI18Version2(AccessibilityEvent event) {
        boolean returnValue = false;
        String eventClass;
        if (event != null && (!TextUtils.isEmpty(event.getClassName()))) {
            eventClass = event.getClassName().toString();

            if (eventClass.contains(AccessibilityMap.ZOMATO)) {
                if (ZomatoHandler.restaurantPageList.get(1).equals(eventClass)) {
                    AccessibilityNodeInfo nodeInfo = event.getSource();
                    returnValue = processRootNodeWithIdsVersion2(nodeInfo);

                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.HIDE_WINDOW_EVENT);
                    mBus.post(jsonObject);

                    returnValue = false;
                }
            } else if (ZomatoHandler.imageButtonClass.equals(eventClass)) {
                AccessibilityNodeInfo imageButtonNode;
                if ((imageButtonNode = event.getSource()) != null
//                        && bottomFabButtonId.equals(imageButtonNode.getViewIdResourceName())
                        ) {
                    AccessibilityNodeInfo rootNode = imageButtonNode;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNodeWithIdsVersion2(rootNode);
                }
            } else if (ZomatoHandler.buttonClass.equals(eventClass)) {
                AccessibilityNodeInfo buttonNode;
                if ((buttonNode = event.getSource()) != null
                        &&
                        (   bottomFabButtonId2.equals(buttonNode.getViewIdResourceName())
                                || ZomatoHandler.reserveTableText.equals(buttonNode.getText())
                        )) {
                    AccessibilityNodeInfo rootNode = buttonNode;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNodeWithIdsVersion2(rootNode);
                }
            }
        }
        return returnValue;
    }

    private boolean processRootNodeWithIdsVersion1(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return false;

        boolean returnValue = false;

        String restaurantName = "";
        String restaurantAddress = "";

        List<AccessibilityNodeInfo> nameNodes = nodeInfo.findAccessibilityNodeInfosByViewId(restIdVersion1);
        if (nameNodes != null && nameNodes.size() > 0 && nameNodes.get(0) != null
                && !TextUtils.isEmpty(nameNodes.get(0).getText())) {
            restaurantName = nameNodes.get(0).getText().toString();
        }

        List<AccessibilityNodeInfo> addressNodes = nodeInfo.findAccessibilityNodeInfosByViewId(restAddVersion1);
        if (addressNodes != null && addressNodes.size() > 0 && addressNodes.get(0) != null
                && !TextUtils.isEmpty(addressNodes.get(0).getText())) {
            restaurantAddress = addressNodes.get(0).getText().toString();
        }

        if (!TextUtils.isEmpty(restaurantName) && !TextUtils.isEmpty(restaurantAddress)) {
            restaurantAddress = restaurantAddress.split(",")[0];
            String id = restaurantName + restaurantAddress;

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.RESTAURANT_VALIDATOR_EVENT);
            jsonObject.addProperty(AccessibilityUtils.RESTAURANT_NAME, restaurantName);
            jsonObject.addProperty(AccessibilityUtils.RESTAURANT_ADDRESS, restaurantAddress);
            mBus.post(jsonObject);

            returnValue = true;
        }

        return returnValue;
    }

    private boolean processRootNodeWithIdsVersion2(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return false;

        boolean returnValue = false;

        String restaurantName = "";
        String restaurantAddress = "";

        List<AccessibilityNodeInfo> nameNodes = nodeInfo.findAccessibilityNodeInfosByViewId(restIdVersion2);
        if (nameNodes != null && nameNodes.size() > 0 && nameNodes.get(0) != null
                && !TextUtils.isEmpty(nameNodes.get(0).getText())) {
            restaurantName = nameNodes.get(0).getText().toString();
        }

        List<AccessibilityNodeInfo> addressNodes = nodeInfo.findAccessibilityNodeInfosByViewId(restAddVersion2);
        if (addressNodes != null && addressNodes.size() > 0 && addressNodes.get(0) != null
                && !TextUtils.isEmpty(addressNodes.get(0).getText())) {
            restaurantAddress = addressNodes.get(0).getText().toString();
        }

        if (!TextUtils.isEmpty(restaurantName) && !TextUtils.isEmpty(restaurantAddress)) {
            restaurantAddress = restaurantAddress.split(",")[0];
            String id = restaurantName + restaurantAddress;

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.RESTAURANT_VALIDATOR_EVENT);
            jsonObject.addProperty(AccessibilityUtils.RESTAURANT_NAME, restaurantName);
            jsonObject.addProperty(AccessibilityUtils.RESTAURANT_ADDRESS, restaurantAddress);
            mBus.post(jsonObject);

            returnValue = true;
        }

        return returnValue;
    }
}
