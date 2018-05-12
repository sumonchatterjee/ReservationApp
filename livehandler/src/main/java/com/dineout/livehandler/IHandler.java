package com.dineout.livehandler;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import com.squareup.otto.Bus;

/**
 * Created by sawai on 19/08/16.
 */
public interface IHandler {
    boolean handleEvent(AccessibilityEvent event);
    void setContext(Context context);
    void setBus(Bus bus);
}
