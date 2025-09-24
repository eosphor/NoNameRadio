package net.programmierecke.radiodroid2.core.domain.interfaces;

import androidx.fragment.app.Fragment;

public interface INavigationManager {
    void navigateToStations();
    void navigateToPlayer();
    void navigateToHistory();
    void navigateToSettings();
    void navigateToStarred();
    void navigateToCategories();
    void replaceFragment(Fragment fragment, String tag);
    void addFragment(Fragment fragment, String tag);
    void popBackStack();
}

