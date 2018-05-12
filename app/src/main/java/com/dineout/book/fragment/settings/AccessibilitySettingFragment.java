//package com.dineout.book.fragment.settings;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.SwitchCompat;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CompoundButton;
//
//import com.dineout.book.R;
//import com.dineout.book.fragment.master.MasterDOFragment;
////import com.dineout.livehandler.AccessibilityPreference;
////import com.dineout.livehandler.AccessibilityUtils;
//
//
//public class AccessibilitySettingFragment extends MasterDOFragment implements CompoundButton.OnCheckedChangeListener {
//    SwitchCompat mSwitchCompat;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_accessibility_setting, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        mSwitchCompat = ((SwitchCompat) view.findViewById(R.id.toggle_button));
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        setToolbarTitle(getActivity().getResources().getString(R.string.accessibility_setting));
//    }
//
//    @Override
//    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
////        AccessibilityPreference.storeShowDialogFlag(getActivity(), b);
////        if (b) {
////            AccessibilityUtils.handleAccessibilityWalkThrough(getActivity());
////        }
//
//        // ga tracking event
//        //trackEventGA(getString(R.string.ga_booking_assistant_setting), getString(R.string.ga_booking_assistant_setting_toggle), String.valueOf(b));
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        if (mSwitchCompat != null && getActivity() != null) {
////            mSwitchCompat.setChecked(AccessibilityPreference.shouldShowDialogRaw(getActivity()));
////
////            mSwitchCompat.setOnCheckedChangeListener(this);
////        }
//    }
//}
