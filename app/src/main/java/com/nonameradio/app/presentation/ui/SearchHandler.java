package com.nonameradio.app.presentation.ui;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.FragmentTabs;
import com.nonameradio.app.R;
import com.nonameradio.app.core.domain.interfaces.IStationRepository;
import com.nonameradio.app.interfaces.IFragmentSearchable;
import com.nonameradio.app.station.StationsFilter;

/**
 * Handler for managing search functionality in ActivityMain
 */
public class SearchHandler {
    private final ActivityMain activity;
    private final IStationRepository stationRepository;
    private final FragmentManager fragmentManager;

    private SearchView searchView;

    public SearchHandler(ActivityMain activity, IStationRepository stationRepository) {
        this.activity = activity;
        this.stationRepository = stationRepository;
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    /**
     * Setup search view with listeners and behavior
     */
    public void setupSearchView(SearchView searchView) {
        this.searchView = searchView;

        if (searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Optional: implement real-time search
                // For now, only submit on enter
                return false;
            }
        });

        searchView.setFocusableInTouchMode(true);

        // Setup focus change listener to hide/show tabs
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d("SearchHandler", "SearchView has focus");
                // Hide tabs when searching
                if (activity.tabsView != null) {
                    activity.tabsView.setVisibility(View.GONE);
                }
                if (activity.isRunningOnTVPublic()) {
                    activity.showSoftKeyboardPublic(searchView);
                }
            } else {
                // Restore tabs visibility
                if (activity.tabsView != null) {
                    activity.tabsView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Perform search with the given query
     */
    public void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return;
        }

        Log.d("SearchHandler", "Performing search for: " + query);

        // Try to search in current fragment if it supports search
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof IFragmentSearchable) {
            ((IFragmentSearchable) currentFragment).Search(StationsFilter.SearchStyle.ByName, query);
        } else {
            // If current fragment doesn't support search, switch to stations tab and search there
            performSearchInStationsTab(query);
        }
    }

    /**
     * Expand search view
     */
    public void expandSearch() {
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(false);
            searchView.requestFocus();
        }
    }

    /**
     * Collapse search view
     */
    public void collapseSearch() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
            searchView.clearFocus();
        }
    }

    /**
     * Clear search and reset to default state
     */
    public void clearSearch() {
        collapseSearch();

        // Reset current fragment if it supports clearing search
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof FragmentTabs) {
            ((FragmentTabs) currentFragment).clearSearch();
        }
    }

    private void performSearchInStationsTab(String query) {
        Log.d("SearchHandler", "Switching to stations tab for search");

        // Create new FragmentTabs and perform search
        FragmentTabs stationsFragment = new FragmentTabs();

        // Replace current fragment with stations fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (activity.useBottomNavigationPublic()) {
            transaction.replace(activity.getContainerViewId(), stationsFragment).commit();
            if (activity.mBottomNavigationView != null) {
                activity.mBottomNavigationView.getMenu().findItem(R.id.nav_item_stations).setChecked(true);
            }
        } else {
            transaction.replace(activity.getContainerViewId(), stationsFragment)
                      .addToBackStack(String.valueOf(R.id.nav_item_stations))
                      .commit();
            if (activity.mNavigationView != null) {
                activity.mNavigationView.getMenu().findItem(R.id.nav_item_stations).setChecked(true);
            }
        }

        // Perform search after fragment is ready
        stationsFragment.Search(StationsFilter.SearchStyle.ByName, query);

        // Update selected menu item
        activity.invalidateOptionsMenu();
    }

    private Fragment getCurrentFragment() {
        try {
            return fragmentManager.getFragments().get(fragmentManager.getFragments().size() - 1);
        } catch (Exception e) {
            Log.e("SearchHandler", "Error getting current fragment", e);
            return null;
        }
    }

    /**
     * Check if search is currently active
     */
    public boolean isSearchActive() {
        return searchView != null && !searchView.isIconified() && searchView.hasFocus();
    }

    /**
     * Get current search query
     */
    public String getCurrentQuery() {
        return searchView != null ? searchView.getQuery().toString() : "";
    }
}