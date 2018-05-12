package com.dineout.livehandler.zoamtoHandler;

import android.content.Context;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.dineout.livehandler.AccessibilityMap;
import com.dineout.livehandler.AccessibilityPreference;
import com.dineout.livehandler.AccessibilityUtils;
import com.dineout.livehandler.IHandler;
import com.google.gson.JsonObject;
import com.squareup.otto.Bus;

import java.util.List;

/**
 * Created by sawai on 30/08/16.
 */
public class ZomatoHandlerWithOutIDs implements IHandler{
    private Context mContext;
    private static Bus mBus;

    @Override
    public boolean handleEvent(AccessibilityEvent event) {
        return handleEventVersion2(event) || handleEventVersion1(event);
    }

    @Override
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void setBus(Bus bus) {
        mBus = bus;
    }

    public boolean handleEventVersion1(AccessibilityEvent event) {
        boolean returnValue = false;

        String eventClass;
        if (event != null && (!TextUtils.isEmpty(event.getClassName()))) {
            eventClass = event.getClassName().toString();

            if (eventClass.contains(AccessibilityMap.ZOMATO)) {
                if (ZomatoHandler.restaurantPageList.get(0).equals(eventClass)) {
                    AccessibilityNodeInfo nodeInfo = event.getSource();

                    List<AccessibilityNodeInfo> buttonList;
                    if ((buttonList = nodeInfo.findAccessibilityNodeInfosByText(ZomatoHandler.reserveTableText)) != null
                            && buttonList.size() > 0 && buttonList.get(0) != null
                            && !TextUtils.isEmpty(buttonList.get(0).getText())) {
                        returnValue = processRootNode(nodeInfo);
                    }
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.HIDE_WINDOW_EVENT);
                    mBus.post(jsonObject);

                    returnValue = false;
                }
            } else if (ZomatoHandler.buttonClass.equals(eventClass)) {
                AccessibilityNodeInfo buttonNodeInfo = event.getSource();
                if (buttonNodeInfo != null && ZomatoHandler.reserveTableText.equals(buttonNodeInfo.getText())) {
                    AccessibilityNodeInfo rootNode = buttonNodeInfo;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNode(rootNode);
                }
            }
        }

        return returnValue;
    }

    public boolean handleEventVersion2(AccessibilityEvent event) {
        boolean returnValue = false;

        String eventClass;
        if (event != null && (!TextUtils.isEmpty(event.getClassName()))) {
            eventClass = event.getClassName().toString();

            if (eventClass.contains(AccessibilityMap.ZOMATO)) {
                if (ZomatoHandler.restaurantPageList.get(1).equals(eventClass)) {
                    AccessibilityNodeInfo nodeInfo = event.getSource();

                    AccessibilityNodeInfo scrollViewNode = null;
                    for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                        if (nodeInfo.getChild(i) != null
                                && "android.widget.ScrollView".equals(nodeInfo.getChild(i).getClassName())) {
                            scrollViewNode = nodeInfo.getChild(i);
                            break;
                        }
                    }
                    if (scrollViewNode != null) {
                        List<AccessibilityNodeInfo> bookATableNodeList;
                        if ((bookATableNodeList = scrollViewNode.findAccessibilityNodeInfosByText(ZomatoHandler.bookTableText)) != null
                                && bookATableNodeList.size() > 0 && bookATableNodeList.get(0) != null
                                && !TextUtils.isEmpty(bookATableNodeList.get(0).getText())) {
                            returnValue = processRootNode(scrollViewNode);
                        }
                    }
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.HIDE_WINDOW_EVENT);
                    mBus.post(jsonObject);

                    returnValue = false;
                }
            } else if (ZomatoHandler.buttonClass.equals(eventClass)) {
                AccessibilityNodeInfo buttonNodeInfo = event.getSource();
                if (buttonNodeInfo != null && ZomatoHandler.reserveTableText.equals(buttonNodeInfo.getText())) {
                    AccessibilityNodeInfo rootNode = buttonNodeInfo;
                    while(rootNode.getParent() != null) {
                        rootNode = rootNode.getParent();
                    }
                    returnValue = processRootNode(rootNode);
                }
            }
        }

        return returnValue;
    }

    private boolean processRootNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return false;

        boolean returnValue = false;

        String restaurantName;
        String restaurantAddress;
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo childNode;

            if ((childNode = nodeInfo.getChild(i)) != null && ZomatoHandler.textViewClass.equals(childNode.getClassName())
                    && !TextUtils.isEmpty(childNode.getText())
                    && containsIgnoreCase(AccessibilityPreference.getCityList(), childNode.getText().toString())) {
                AccessibilityNodeInfo previousNode;
                if (i > 0 && (previousNode = nodeInfo.getChild(i - 1)) != null
                        && !TextUtils.isEmpty(previousNode.getText())) {
                    restaurantName = previousNode.getText().toString();
                    restaurantAddress = childNode.getText().toString();

                    if (!TextUtils.isEmpty(restaurantName)
                            && !TextUtils.isEmpty(restaurantAddress)) {
                        String id = restaurantName + restaurantAddress;

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(AccessibilityUtils.EVENT_ID, AccessibilityUtils.RESTAURANT_VALIDATOR_EVENT);
                        jsonObject.addProperty(AccessibilityUtils.RESTAURANT_NAME, restaurantName);
                        jsonObject.addProperty(AccessibilityUtils.RESTAURANT_ADDRESS, restaurantAddress);
                        mBus.post(jsonObject);

                        returnValue = true;
                        break;
                    }
                }
            }
        }

        return returnValue;
    }

    private boolean containsIgnoreCase(String citiesArray, String value) {
        boolean returnValue = false;
        if (!TextUtils.isEmpty(citiesArray)) {
            String[] list = citiesArray.split("\\|");
            for (int i = 0; i < list.length; i++) {
                if (value.toLowerCase().contains(list[i].toLowerCase())) {
                    returnValue = true;
                    break;
                }
            }
        }

        return returnValue;
    }
}
