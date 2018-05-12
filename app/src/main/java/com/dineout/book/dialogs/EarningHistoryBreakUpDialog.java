package com.dineout.book.dialogs;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOStringReqFragment;
import com.dineout.recycleradapters.EarningHistoryBreakUpAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.dineout.recycleradapters.util.AppUtil.setTextViewInfo;

/**
 * Created by sawai.parihar on 09/03/17.
 */

public class EarningHistoryBreakUpDialog extends MasterDOStringReqFragment implements View.OnClickListener {
    public static final String INFO_STRING = "earning_history_break_up_info_string";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earning_history_break_up, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setDimAmount(new Float(0.85));
        getDialog().setCanceledOnTouchOutside(false);
        setCancelable(false);

        // set dialog width
        Rect displayRectangle = new Rect();
        Window window = getDialog().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        getDialog().getWindow().setLayout((int) (displayRectangle.width() * .88), WindowManager.LayoutParams.WRAP_CONTENT);


        ImageView crossIv = (ImageView) view.findViewById(R.id.cross_iv);
        crossIv.setOnClickListener(this);

        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);

        EarningHistoryBreakUpAdapter adapter = new EarningHistoryBreakUpAdapter(getActivity());
        try {
            if (getArguments() != null) {
                JSONObject balanceData = new JSONObject(getArguments().getString(INFO_STRING));

                // set amount
                setTextViewInfo(getView().findViewById(R.id.earning_amount_tv),
                        balanceData.optJSONObject("amount"));

                // set amount text
                setTextViewInfo(getView().findViewById(R.id.earning_balance_tv),
                        balanceData.optJSONObject("amount_text"));

                // set list data
                adapter.setJsonArray(balanceData.optJSONArray("amount_breakup"));
                rv.setAdapter(adapter);

            }
        } catch (Exception e) {
            // Exception
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cross_iv:
                getDialog().dismiss();
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().getAttributes().windowAnimations = R.style.RNRStyle;
    }
}
