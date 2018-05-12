package com.dineout.book.widgets;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.dineout.book.R;
import com.dineout.recycleradapters.EarningFilterPopUpAdapter;
import com.dineout.recycleradapters.util.AppUtil;

import org.json.JSONObject;

import static com.dineout.recycleradapters.util.AppUtil.setTextViewInfo;

/**
 * Created by sawai.parihar on 09/05/17.
 */

public class EarningFilterPopUpWindow extends PopupWindow implements EarningFilterPopUpAdapter.FilterPopUpDismissCallback {
    private Context mContext;
    private String mInfoString;
    private EarningFilterPopUpAdapter.FilterCallback mCallback;

    public EarningFilterPopUpWindow(Context context) {
        super(context);
        this.mContext = context;
    }

    public void setInfoString(String infoString) {
        this.mInfoString = infoString;
    }

    public void setFilterCallback(EarningFilterPopUpAdapter.FilterCallback callback) {
        this.mCallback = callback;
    }

    public void show(View anchor) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.earning_filter_screen_layout, null);

        try {
            if (!TextUtils.isEmpty(mInfoString)) {
                JSONObject filterData = new JSONObject(mInfoString);

                // set title
                setTextViewInfo(view.findViewById(R.id.earning_amount_tv),
                        filterData.optJSONObject("sub_title"));

                // set up recycler and adapter
                RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view);
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                rv.setLayoutManager(llm);

                EarningFilterPopUpAdapter adapter = new EarningFilterPopUpAdapter(mContext);
                adapter.setJsonArray(filterData.optJSONArray("filter_list"));
                adapter.setFilterCallback(mCallback);
                adapter.setFilterPopUpDimissCallback(this);
                rv.setAdapter(adapter);
            }
        } catch (Exception e) {
            // Exception
        }

        // set dialog height
        setHeight(AppUtil.dpToPx(350, mContext.getResources()));

        // set dialog width
        setWidth(AppUtil.dpToPx(200, mContext.getResources()));

        // to remove black boundary
        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // to dismiss pop when user click outside
        setFocusable(true);

        // set up view
        setContentView(view);

        // set style
//        setAnimationStyle(R.style.filterPopupStyle);

        int[] location = new int[2];
        ((FragmentActivity) mContext).findViewById(R.id.filter_right_text_tv).getLocationOnScreen(location);

        Point p = new Point();
        p.x = location[0];
        p.y = location[1];
        int OFFSET_X = AppUtil.dpToPx(5, mContext.getResources());
        int OFFSET_Y = AppUtil.dpToPx(35, mContext.getResources());

        // show
        showAtLocation(anchor, Gravity.LEFT | Gravity.TOP, p.x - OFFSET_X, p.y + OFFSET_Y);
    }

    @Override
    public void onDismiss() {
        dismiss();
    }
}
