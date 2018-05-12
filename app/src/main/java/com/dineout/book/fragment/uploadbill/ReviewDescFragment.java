package com.dineout.book.fragment.uploadbill;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

//import com.dineout.book.BR;
import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;
import com.dineout.book.interfaces.DescriptionReviewHandler;
import com.dineout.book.model.uploadBill.viewmodel.DescriptionReviewModel;
import com.dineout.book.interfaces.ReviewDescriptionCallback;


public class ReviewDescFragment extends MasterDOFragment implements DescriptionReviewHandler {


    private DescriptionReviewModel descriptionReviewModel;

    private ReviewDescriptionCallback mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light);
    }

    public static ReviewDescFragment newInstance(String desc, ReviewDescriptionCallback callback){

        ReviewDescFragment fragment = new ReviewDescFragment();
        fragment.descriptionReviewModel = new DescriptionReviewModel(desc);
        fragment.mCallback = callback;

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review_desc, container, false);

        TextView doneTv = (TextView) view.findViewById(R.id.done_tv);
        EditText descEt = (EditText) view.findViewById(R.id.desc_et);

        doneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndClose(descriptionReviewModel.desc);
            }
        });

        descEt.setText(descriptionReviewModel.desc);
        descEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                descriptionReviewModel.afterTextChanged(s);
            }
        });

        return view;

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void saveAndClose(String desc) {

        dismissAllowingStateLoss();
        if(mCallback != null)
            mCallback.updateText(desc);
    }
}
