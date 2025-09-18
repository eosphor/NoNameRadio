package com.eosphor.nonameradio.interfaces;

import com.eosphor.nonameradio.station.StationsFilter;

public interface IFragmentSearchable {
    void Search(StationsFilter.SearchStyle searchStyle, String query);
}
