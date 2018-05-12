package com.dineout.book.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dineout.book.GCMRegistrationIntentService;
import com.dineout.book.R;
import com.dineout.book.view.activity.DineoutMainActivity;
import com.example.dineoutnetworkmodule.DOPreferences;

public class SplashFragment extends MasterDOFragment {

    //private Handler mHandler;
    private boolean isLocationFetched;
    //private boolean isIntroShown = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Style
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);

        // Track Screen
        trackScreen(getString(R.string.ga_screen_splash));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash_screen, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set Cancellable
        setCancelable(false);
    }

    public void handleNavigation( boolean hasFetchedCurrentLocation) {
        // Initiate GCM Services
        initGcmServices();
        isLocationFetched = hasFetchedCurrentLocation;

        // Check if it is User's first launch of the App
        if (DOPreferences.checkFirstLaunch(getActivity()) ) {

            Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_base_container);
            if(fragment!=null && fragment instanceof IntroScreensPagerFragment) {
                 ((IntroScreensPagerFragment) fragment).setLocationFetched(isLocationFetched);

            }
        } else {
            if (!isLocationFetched) {


                showFragment(((DineoutMainActivity) getActivity()).getFragManager(),
                        new LocationSelectionFragment());
//
//
//                // dismissDialogWithDelay(2000);
//            }
            }
        }
        dismissDialogWithDelay(1000);


//
//                // Create Bundle setting flag
//                Bundle bundle = new Bundle();
//                bundle.putBoolean("isLocationFetched", isLocationFetched);
//
//                // Set Bundle as Arguments in Fragment
//                fragment.setArguments(bundle);
//
//                MasterDOFragment.showFragment(((DineoutMainActivity) getActivity())
//                        .getFragManager(), fragment);
//                isIntroShown = true;
               // dismissDialogWithDelay(20);
            //}
        }



    @Override
    public void onPause() {
        super.onPause();
        //isIntroShown = false;
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    // Initialize GCM Service
    public void initGcmServices() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(getActivity(), GCMRegistrationIntentService.class);
        getActivity().startService(intent);
    }
}
