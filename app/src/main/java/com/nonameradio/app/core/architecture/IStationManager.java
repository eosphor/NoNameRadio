package com.nonameradio.app.core.architecture;

import com.nonameradio.app.station.DataRadioStation;

import java.util.List;

/**
 * Interface for managing radio station collections.
 * Provides operations for managing stations (add, remove, reorder, etc.)
 * without persistence concerns.
 */
public interface IStationManager {

    /**
     * Adds a station to the collection
     */
    void add(DataRadioStation station);

    /**
     * Adds multiple stations to the collection
     */
    void addMultiple(List<DataRadioStation> stations);

    /**
     * Replaces existing stations with new ones based on UUID
     */
    void replaceList(List<DataRadioStation> stations);

    /**
     * Adds a station to the front of the collection
     */
    void addFront(DataRadioStation station);

    /**
     * Adds all stations without saving
     */
    void addAll(List<DataRadioStation> stations);

    /**
     * Gets the last station in the collection
     */
    DataRadioStation getLast();

    /**
     * Gets the first station in the collection
     */
    DataRadioStation getFirst();

    /**
     * Gets a station by its UUID
     */
    DataRadioStation getById(String id);

    /**
     * Gets the next station after the given ID
     */
    DataRadioStation getNextById(String id);

    /**
     * Gets the previous station before the given ID
     */
    DataRadioStation getPreviousById(String id);

    /**
     * Moves a station from one position to another without notification
     */
    void moveWithoutNotify(int fromPos, int toPos);

    /**
     * Moves a station from one position to another with notification
     */
    void move(int fromPos, int toPos);

    /**
     * Finds the best name match for a query
     */
    DataRadioStation getBestNameMatch(String query);

    /**
     * Removes a station by ID and returns its position
     */
    int remove(String id);

    /**
     * Restores a station at a specific position
     */
    void restore(DataRadioStation station, int pos);

    /**
     * Clears all stations
     */
    void clear();

    /**
     * Gets the size of the collection
     */
    int size();

    /**
     * Checks if the collection is empty
     */
    boolean isEmpty();

    /**
     * Checks if a station exists by ID
     */
    boolean has(String id);

    /**
     * Gets an unmodifiable view of the station list
     */
    List<DataRadioStation> getList();

    /**
     * Refreshes stations from server (async operation)
     */
    void refreshStationsFromServer();
}
