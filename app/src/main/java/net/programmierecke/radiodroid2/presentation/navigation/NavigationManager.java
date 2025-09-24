package net.programmierecke.radiodroid2.presentation.navigation;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import net.programmierecke.radiodroid2.FragmentCategories;
import net.programmierecke.radiodroid2.FragmentHistory;
import net.programmierecke.radiodroid2.FragmentPlayerFull;
import net.programmierecke.radiodroid2.FragmentPlayerSmall;
import net.programmierecke.radiodroid2.FragmentServerInfo;
import net.programmierecke.radiodroid2.FragmentSettings;
import net.programmierecke.radiodroid2.FragmentStarred;
import net.programmierecke.radiodroid2.FragmentTabs;
import net.programmierecke.radiodroid2.R;

public class NavigationManager implements net.programmierecke.radiodroid2.core.domain.interfaces.INavigationManager {
    private final FragmentManager fragmentManager;
    private final Context context;

    public NavigationManager(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    @Override
    public void navigateToStations() {
        replaceFragment(new FragmentTabs(), "stations");
    }

    @Override
    public void navigateToPlayer() {
        replaceFragment(new FragmentPlayerFull(), "player");
    }

    @Override
    public void navigateToHistory() {
        replaceFragment(new FragmentHistory(), "history");
    }

    @Override
    public void navigateToSettings() {
        replaceFragment(new FragmentSettings(), "settings");
    }

    @Override
    public void navigateToStarred() {
        replaceFragment(new FragmentStarred(), "starred");
    }

    @Override
    public void navigateToCategories() {
        replaceFragment(new FragmentCategories(), "categories");
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void addFragment(Fragment fragment, String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, fragment, tag);
        transaction.addToBackStack(tag);
        transaction.commit();
    }

    @Override
    public void popBackStack() {
        fragmentManager.popBackStack();
    }
}

