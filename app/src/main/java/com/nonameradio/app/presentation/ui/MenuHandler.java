package com.nonameradio.app.presentation.ui;

import android.view.MenuItem;
import com.nonameradio.app.presentation.navigation.NavigationManager;

/**
 * Заглушка для MenuHandler - будет реализована позже при интеграции с ActivityMain
 */
public class MenuHandler {
    private final NavigationManager navigationManager;

    public MenuHandler(Object activity, NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }

    public void setupMenu() {
        // TODO: Implement menu setup
    }

    public boolean handleMenuItemClick(MenuItem item) {
        // TODO: Implement menu item handling
        return false;
    }
}
