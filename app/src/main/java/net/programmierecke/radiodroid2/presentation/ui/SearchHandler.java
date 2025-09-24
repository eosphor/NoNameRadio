package net.programmierecke.radiodroid2.presentation.ui;

import net.programmierecke.radiodroid2.core.domain.interfaces.IStationRepository;

/**
 * Заглушка для SearchHandler - будет реализована позже при интеграции с ActivityMain
 */
public class SearchHandler {
    private final IStationRepository stationRepository;

    public SearchHandler(Object activity, IStationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public void setupSearchView() {
        // TODO: Implement search view setup
    }

    public void performSearch(String query) {
        // TODO: Implement search logic
    }

    public void expandSearch() {
        // TODO: Implement search expansion
    }

    public void collapseSearch() {
        // TODO: Implement search collapse
    }
}
