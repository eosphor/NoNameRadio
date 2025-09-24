package com.nonameradio.app.presentation.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.nonameradio.app.ActivityMain;
import com.nonameradio.app.FavouriteManager;
import com.nonameradio.app.FragmentTabs;
import com.nonameradio.app.HistoryManager;
import com.nonameradio.app.NoNameRadioApp;
import com.nonameradio.app.R;
import com.nonameradio.app.Utils;
import com.nonameradio.app.alarm.TimePickerFragment;
import com.nonameradio.app.presentation.navigation.NavigationManager;

/**
 * Handler for managing menu items visibility and click handling in ActivityMain
 */
public class MenuHandler {
    private final ActivityMain activity;
    private final NavigationManager navigationManager;

    // Menu items
    private MenuItem menuItemSearch;
    private MenuItem menuItemDelete;
    private MenuItem menuItemSleepTimer;
    private MenuItem menuItemSave;
    private MenuItem menuItemLoad;
    private MenuItem menuItemAddAlarm;
    private MenuItem menuItemMpd;

    private SearchView mSearchView;
    private SharedPreferences sharedPref;
    private int selectedMenuItem;

    public MenuHandler(ActivityMain activity, NavigationManager navigationManager) {
        this.activity = activity;
        this.navigationManager = navigationManager;
        this.sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        this.selectedMenuItem = sharedPref.getInt("last_selectedMenuItem", -1);
    }

    /**
     * Setup menu items and their initial visibility
     */
    public void setupMenu(Menu menu) {
        final Toolbar myToolbar = activity.findViewById(R.id.my_awesome_toolbar);

        // Find menu items
        menuItemSleepTimer = menu.findItem(R.id.action_set_sleep_timer);
        menuItemSearch = menu.findItem(R.id.action_search);
        menuItemDelete = menu.findItem(R.id.action_delete);
        menuItemSave = menu.findItem(R.id.action_save);
        menuItemLoad = menu.findItem(R.id.action_load);
        menuItemAddAlarm = menu.findItem(R.id.action_add_alarm);
        menuItemMpd = menu.findItem(R.id.action_mpd);

        // Setup search view
        if (menuItemSearch != null) {
            mSearchView = (SearchView) menuItemSearch.getActionView();
            if (mSearchView != null) {
                setupSearchView();
            }
        }

        // Initially hide all menu items
        setAllMenuItemsVisible(false);

        // Ensure selectedMenuItem reflects current UI (fallback to stations)
        if (selectedMenuItem == -1) {
            selectedMenuItem = R.id.nav_item_stations;
        }

        // Show items based on current fragment
        updateMenuVisibility();

        // Force menu refresh to apply visibility on toolbar
        activity.invalidateOptionsMenu();
    }

    /**
     * Update menu visibility based on current selected menu item
     */
    public void updateMenuVisibility() {
        // Hide all items first
        setAllMenuItemsVisible(false);

        // Show items based on current fragment
        switch (selectedMenuItem) {
            case R.id.nav_item_stations:
                showStationsMenuItems();
                break;
            case R.id.nav_item_starred:
                showFavoritesMenuItems();
                break;
            case R.id.nav_item_history:
                showHistoryMenuItems();
                break;
            case R.id.nav_item_alarm:
                showAlarmMenuItems();
                break;
            case R.id.nav_item_settings:
                // No menu items for settings
                break;
        }

        // Show sleep timer except on Alarm screen (оставим место под кнопку добавления будильника)
        if (menuItemSleepTimer != null) {
            boolean showSleep = selectedMenuItem != R.id.nav_item_alarm;
            menuItemSleepTimer.setVisible(showSleep);
        }

        // Show MPD item if available
        if (menuItemMpd != null) {
            boolean mpd_is_visible = false;
            NoNameRadioApp app = (NoNameRadioApp) activity.getApplication();
            if (app != null) {
                mpd_is_visible = sharedPref.getBoolean("mpd_visible", false);
            }
            menuItemMpd.setVisible(mpd_is_visible);
        }
    }

    private void showStationsMenuItems() {
        if (menuItemSearch != null) menuItemSearch.setVisible(true);
        if (menuItemSave != null) menuItemSave.setVisible(true);
        if (menuItemLoad != null) menuItemLoad.setVisible(true);
    }

    private void showFavoritesMenuItems() {
        if (menuItemDelete != null) menuItemDelete.setVisible(true);
    }

    private void showHistoryMenuItems() {
        if (menuItemDelete != null) menuItemDelete.setVisible(true);
    }

    private void showAlarmMenuItems() {
        if (menuItemAddAlarm != null) {
            menuItemAddAlarm.setVisible(true);
            menuItemAddAlarm.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
    }

    private void setAllMenuItemsVisible(boolean visible) {
        if (menuItemSleepTimer != null) menuItemSleepTimer.setVisible(visible);
        if (menuItemSearch != null) menuItemSearch.setVisible(visible);
        if (menuItemDelete != null) menuItemDelete.setVisible(visible);
        if (menuItemSave != null) menuItemSave.setVisible(visible);
        if (menuItemLoad != null) menuItemLoad.setVisible(visible);
        if (menuItemAddAlarm != null) menuItemAddAlarm.setVisible(visible);
        if (menuItemMpd != null) menuItemMpd.setVisible(visible);
    }

    private void setupSearchView() {
        // Configure SearchView to always be expanded (like original NoNameRadio)
        mSearchView.setIconifiedByDefault(false);

        // Ensure SearchView gets focus when it's shown
        mSearchView.requestFocus();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                activity.SearchStations(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                activity.SearchStations(newText);
                return true;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d("MenuHandler", "SearchView has focus");
                // Hide tabs when searching
                if (activity.tabsView != null) {
                    activity.tabsView.setVisibility(android.view.View.GONE);
                }
                // Show keyboard when SearchView gets focus
                activity.showSoftKeyboard(mSearchView);
            } else {
                // Restore tabs visibility
                if (activity.tabsView != null) {
                    activity.tabsView.setVisibility(android.view.View.VISIBLE);
                }
            }
        });
    }

    /**
     * Handle menu item clicks
     */
    public boolean handleMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (activity.mDrawerLayout != null) {
                    activity.mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;

            case R.id.action_save:
                if (Utils.verifyStoragePermissions(activity, ActivityMain.PERM_REQ_STORAGE_FAV_SAVE)) {
                    activity.SaveFavourites();
                }
                return true;

            case R.id.action_load:
                if (Utils.verifyStoragePermissions(activity, ActivityMain.PERM_REQ_STORAGE_FAV_LOAD)) {
                    activity.LoadFavourites();
                }
                return true;

            case R.id.action_set_sleep_timer:
                activity.changeTimer();
                return true;

            case R.id.action_mpd:
                activity.selectMPDServer();
                return true;

            case R.id.action_delete:
                handleDeleteAction();
                return true;


            case R.id.action_add_alarm:
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setCallback(activity);
                newFragment.show(activity.getSupportFragmentManager(), "timePicker");
                return true;

            case R.id.action_legal_notice:
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.legal_notice_title)
                        .setMessage(R.string.legal_notice_content)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
                return true;
        }
        return false;
    }

    private void handleDeleteAction() {
        if (selectedMenuItem == R.id.nav_item_history) {
            new AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.alert_delete_history))
                    .setCancelable(true)
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, id) -> {
                        NoNameRadioApp app = (NoNameRadioApp) activity.getApplication();
                        HistoryManager historyManager = app.getHistoryManager();
                        historyManager.clear();
                        Toast.makeText(activity.getApplicationContext(),
                                activity.getString(R.string.notify_deleted_history),
                                Toast.LENGTH_SHORT).show();
                        activity.recreate();
                    })
                    .setNegativeButton(activity.getString(R.string.no), null)
                    .show();
        } else if (selectedMenuItem == R.id.nav_item_starred) {
            new AlertDialog.Builder(activity)
                    .setMessage(activity.getString(R.string.alert_delete_favorites))
                    .setCancelable(true)
                    .setPositiveButton(activity.getString(R.string.yes), (dialog, id) -> {
                        NoNameRadioApp app = (NoNameRadioApp) activity.getApplication();
                        FavouriteManager favouriteManager = app.getFavouriteManager();
                        favouriteManager.clear();
                        Toast.makeText(activity.getApplicationContext(),
                                activity.getString(R.string.notify_deleted_favorites),
                                Toast.LENGTH_SHORT).show();
                        activity.recreate();
                    })
                    .setNegativeButton(activity.getString(R.string.no), null)
                    .show();
        }
    }

    public void setSelectedMenuItem(int selectedMenuItem) {
        this.selectedMenuItem = selectedMenuItem;
        sharedPref.edit().putInt("last_selectedMenuItem", selectedMenuItem).apply();
    }

    public int getSelectedMenuItem() {
        return selectedMenuItem;
    }

    public SearchView getSearchView() {
        return mSearchView;
    }
}