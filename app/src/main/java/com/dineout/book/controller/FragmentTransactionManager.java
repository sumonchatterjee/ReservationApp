package com.dineout.book.controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;

import com.dineout.book.R;
import com.dineout.book.fragment.master.MasterDOFragment;




public class FragmentTransactionManager {


	public static void replace(final FragmentManager fragmentManager,
			final int targetId, final Fragment fragment,
			final int enter, final int exit, final int popEnter,
			final int popExit, final boolean isAddToBackStack) {




		Fragment currentFragment = fragmentManager
						.findFragmentById(R.id.fragment_base_container);


				android.support.v4.app.FragmentTransaction transaction = fragmentManager
						.beginTransaction();
				transaction.setTransition( android.support.v4.app.FragmentTransaction.TRANSIT_NONE);
				if (enter == MasterDOFragment.NO_ANIMATION
						&& exit == MasterDOFragment.NO_ANIMATION
						&& popEnter == MasterDOFragment.NO_ANIMATION
						&& popExit == MasterDOFragment.NO_ANIMATION) {


				} else {
					transaction.setCustomAnimations(enter, exit, popEnter,
							popExit);
				}

				try {
					transaction.replace(targetId, fragment,
							fragment.getClass().getName());
					if (isAddToBackStack) {

						transaction
								.addToBackStack(fragment.getClass().getName());
					} else {
						if (currentFragment != null) {
							transaction.remove(currentFragment);
						}
					}
					transaction.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}

	}

	public static void popBackStackTo(final FragmentManager manager,
			final BackStackEntry entry, final int flag) {


				manager.popBackStack(entry.getId(), flag);

	}



	public static void remove(final FragmentManager manager,
			final Fragment fragment) {

				try {
					manager.beginTransaction().remove(fragment).commit();
				} catch (IllegalStateException ex) {
					manager.beginTransaction().remove(fragment)
							.commitAllowingStateLoss();
				}

	}

	public static void removeAllFragments(final FragmentManager manager,
			final int flag) {


				if (manager != null && manager.getBackStackEntryCount() > 0) {
					BackStackEntry backStackEntry = manager
							.getBackStackEntryAt(0);
					manager.popBackStack(backStackEntry.getId(), flag);

				}

	}


}
