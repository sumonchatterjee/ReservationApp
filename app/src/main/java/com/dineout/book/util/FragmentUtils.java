package com.dineout.book.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.dineout.book.R;

import java.util.List;


public class FragmentUtils {

    public static Fragment getTopVisibleFragment(FragmentManager manager,
                                                 int containerId) {
        return manager.findFragmentById(containerId);
    }

    // return
    public static Fragment getTopFragment(FragmentManager manager) {
        return getTopVisibleFragment(manager, R.id.fragment_base_container);
    }

    public static Fragment getTopFragmentFromList(FragmentManager manager) {
        List<Fragment> fragments = manager.getFragments();
        if (fragments != null && fragments.size() > 0) {
            return fragments.get(fragments.size() - 1);
        }
        return null;
    }
}
