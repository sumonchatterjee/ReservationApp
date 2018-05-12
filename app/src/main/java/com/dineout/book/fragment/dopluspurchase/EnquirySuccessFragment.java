package com.dineout.book.fragment.dopluspurchase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;


public class EnquirySuccessFragment extends MasterDOFragment {

    private TextView mEnquirySuccess, mHeading;
    private String header, content;

    public static EnquirySuccessFragment newInstance(String header, String content) {
        EnquirySuccessFragment fragment = new EnquirySuccessFragment();

        fragment.header = header;
        fragment.content = content;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track Screen
        trackScreenToGA(getString(R.string.ga_screen_do_plus_membership_enquiry_success));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.enquiry_request_submitted_success, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Title
        setToolbarTitle(getString(R.string.title_confirmation));

        mHeading = (TextView) getView().findViewById(R.id.textViewHeding);
        mEnquirySuccess = (TextView) getView().findViewById(R.id.textViewInfo);

        mHeading.setText(header);
        mEnquirySuccess.setText(content);
    }

    @Override
    public void onRemoveErrorView() {

    }
}
