package com.nonameradio.app.interfaces;

import com.nonameradio.app.station.StationsFilter;

public interface IFragmentSearchable {
    void Search(StationsFilter.SearchStyle searchStyle, String query);
    void clearSearch();
}
