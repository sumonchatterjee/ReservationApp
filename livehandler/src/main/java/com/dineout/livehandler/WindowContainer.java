package com.dineout.livehandler;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * Created by sawai on 20/08/16.
 */
public class WindowContainer {
    private Context mContext;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;
    private ViewGroup mView;

    private ViewGroup mWindowContainer;
    private RelativeLayout mMainContainer;

    public WindowContainer(Context context, WindowManager windowManager, LayoutInflater inflater) {
        mContext = context;
        mWindowManager = windowManager;
        mInflater = inflater;

        try {
            createContainer();
        } catch (Exception e) {
            // error during creation of container
        }
    }

    private void createContainer() {
        mWindowContainer = (ViewGroup) mInflater.inflate(R.layout.window_container, null, false);
        mMainContainer = (RelativeLayout) mWindowContainer.findViewById(R.id.main_container);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.dimAmount = 0.3f;
        mWindowManager.addView(mWindowContainer, params);
    }

    public void setView(IView iView) {
        if (mView != null) {
            mMainContainer.removeView(mView);
        }
        if (mMainContainer != null && iView != null && iView.getView() != null) {
            mView = iView.getView();
            mMainContainer.addView(mView);
        }

    }

    public void hideWindow() {
        if (mWindowContainer != null && mWindowContainer.getVisibility() == View.VISIBLE) {
            mWindowContainer.setVisibility(View.GONE);
        }
    }

    public void showWindow() {
        if (mWindowContainer != null && mWindowContainer.getVisibility() == View.GONE) {
            mWindowContainer.setVisibility(View.VISIBLE);
        }
    }
}
