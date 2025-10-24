package com.nonameradio.app;
import androidx.lifecycle.Lifecycle;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.nonameradio.app.interfaces.IFragmentRefreshable;
import com.nonameradio.app.interfaces.IFragmentSearchable;
import com.nonameradio.app.station.FragmentStations;
import com.nonameradio.app.station.StationsFilter;

import java.util.ArrayList;
import java.util.List;

public class FragmentTabs extends Fragment implements IFragmentRefreshable, IFragmentSearchable {
    private final String itsAdressWWWLocal = "json/stations/bycountryexact/internet?order=clickcount&reverse=true";
    private final String itsAdressWWWTopClick = "json/stations/topclick/100";
    private final String itsAdressWWWTopVote = "json/stations/topvote/100";
    private final String itsAdressWWWChangedLately = "json/stations/lastchange/100";
    private final String itsAdressWWWCurrentlyHeard = "json/stations/lastclick/100";
    private final String itsAdressWWWTags = "json/tags";
    private final String itsAdressWWWCountries = "json/countrycodes";
    private final String itsAdressWWWLanguages = "json/languages";

    // Note: the actual order of tabs is defined
    // further down when populating the ViewPagerAdapter
    private static final int IDX_LOCAL = 0;
    private static final int IDX_TOP_CLICK = 1;
    private static final int IDX_TOP_VOTE = 2;
    private static final int IDX_CHANGED_LATELY = 3;
    private static final int IDX_CURRENTLY_HEARD = 4;
    private static final int IDX_TAGS = 5;
    private static final int IDX_COUNTRIES = 6;
    private static final int IDX_LANGUAGES = 7;
    private static final int IDX_SEARCH = 8;

    public static ViewPager2 viewPager;
    private TabLayoutMediator tabLayoutMediator;

    private String queuedSearchQuery; // Search may be requested before onCreateView so we should wait
    private StationsFilter.SearchStyle queuedSearchStyle;

    private final Fragment[] fragments = new Fragment[9];
    private final String[] addresses = new String[]{
            itsAdressWWWLocal,
            itsAdressWWWTopClick,
            itsAdressWWWTopVote,
            itsAdressWWWChangedLately,
            itsAdressWWWCurrentlyHeard,
            itsAdressWWWTags,
            itsAdressWWWCountries,
            itsAdressWWWLanguages,
            ""
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View x = inflater.inflate(R.layout.layout_tabs, null);
        final TabLayout tabLayout = getActivity().findViewById(R.id.tabs);
        viewPager = x.findViewById(R.id.viewpager);

        ViewPagerAdapter adapter = setupViewPager(viewPager);

        // Setup TabLayout with ViewPager2 using TabLayoutMediator
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getPageTitle(position));
        });
        tabLayoutMediator.attach();

        if (queuedSearchQuery != null) {
            Log.d("TABS", "do queued search by name:"+ queuedSearchQuery);
            Search(queuedSearchStyle, queuedSearchQuery);
            queuedSearchQuery = null;
            queuedSearchStyle = StationsFilter.SearchStyle.ByName;
        }

        return x;
    }

    @Override
    public void onResume() {
        super.onResume();

        final TabLayout tabLayout = getActivity().findViewById(R.id.tabs);
        tabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        final TabLayout tabLayout = getActivity().findViewById(R.id.tabs);
        tabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        // Detach TabLayoutMediator to prevent memory leaks
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
        }
        super.onDestroyView();
    }

    private String getCountryCode() {
        Context ctx = getContext();
        String countryCode = null;
        if (ctx != null) {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            countryCode = tm.getNetworkCountryIso();
            Log.d("MAIN", "Network country code: '" + countryCode + "'");
            if (countryCode != null && countryCode.length() == 2) {
                return countryCode;
            }
            countryCode = tm.getSimCountryIso();
            Log.d("MAIN", "Sim country code: '" + countryCode + "'");
            if (countryCode != null && countryCode.length() == 2) {
                return countryCode;
            }
            countryCode = ctx.getResources().getConfiguration().getLocales().get(0).getCountry();
            addresses[IDX_LOCAL] = "json/stations/bycountrycodeexact/?order=clickcount&reverse=true";
            Log.d("MAIN", "Locale: '" + countryCode + "'");
            if (countryCode != null && countryCode.length() == 2) {
                return countryCode;
            }
        }
        return null;
    }

    private ViewPagerAdapter setupViewPager(ViewPager2 viewPager) {
        String countryCode = getCountryCode();
        if (countryCode != null){
            addresses[IDX_LOCAL] = "json/stations/bycountrycodeexact/" + countryCode + "?order=clickcount&reverse=true";
        }

        fragments[IDX_LOCAL] = new FragmentStations();
        fragments[IDX_TOP_CLICK] = new FragmentStations();
        fragments[IDX_TOP_VOTE] = new FragmentStations();
        fragments[IDX_CHANGED_LATELY] = new FragmentStations();
        fragments[IDX_CURRENTLY_HEARD] = new FragmentStations();
        fragments[IDX_TAGS] = new FragmentCategories();
        fragments[IDX_COUNTRIES] = new FragmentCategories();
        fragments[IDX_LANGUAGES] = new FragmentCategories();
        fragments[IDX_SEARCH] = new FragmentStations();

        for (int i=0;i<fragments.length;i++) {
            Bundle bundle = new Bundle();
            bundle.putString("url", addresses[i]);

            // Enable search for the dedicated search tab
            if (i == IDX_SEARCH) {
                bundle.putBoolean(FragmentStations.KEY_SEARCH_ENABLED, true);
            }
            // Enable search for all station fragments on TV devices for better navigation
            else if (fragments[i] instanceof FragmentStations && Utils.isRunningOnTV(getContext())) {
                bundle.putBoolean(FragmentStations.KEY_SEARCH_ENABLED, true);
            }

            fragments[i].setArguments(bundle);
        }

        ((FragmentCategories) fragments[IDX_TAGS]).EnableSingleUseFilter(true);
        ((FragmentCategories) fragments[IDX_TAGS]).SetBaseSearchLink(StationsFilter.SearchStyle.ByTagExact);
        ((FragmentCategories) fragments[IDX_COUNTRIES]).SetBaseSearchLink(StationsFilter.SearchStyle.ByCountryCodeExact);
        ((FragmentCategories) fragments[IDX_LANGUAGES]).SetBaseSearchLink(StationsFilter.SearchStyle.ByLanguageExact);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        if (countryCode != null){
            adapter.addFragment(fragments[IDX_LOCAL], R.string.action_local);
        }
        adapter.addFragment(fragments[IDX_TOP_CLICK], R.string.action_top_click);
        adapter.addFragment(fragments[IDX_TOP_VOTE], R.string.action_top_vote);
        adapter.addFragment(fragments[IDX_CHANGED_LATELY], R.string.action_changed_lately);
        adapter.addFragment(fragments[IDX_CURRENTLY_HEARD], R.string.action_currently_playing);
        adapter.addFragment(fragments[IDX_TAGS], R.string.action_tags);
        adapter.addFragment(fragments[IDX_COUNTRIES], R.string.action_countries);
        adapter.addFragment(fragments[IDX_LANGUAGES], R.string.action_languages);
        adapter.addFragment(fragments[IDX_SEARCH], R.string.action_search);
        viewPager.setAdapter(adapter);
        
        return adapter;
    }

    public void Search(StationsFilter.SearchStyle searchStyle, final String query) {
        Log.d("TABS","Search = "+ query + " searchStyle="+searchStyle);
        if (viewPager != null) {
            Log.d("TABS","a Search = "+ query);
            viewPager.setCurrentItem(IDX_SEARCH, false); // ViewPager2 supports same API
            ((IFragmentSearchable)fragments[IDX_SEARCH]).Search(searchStyle, query);
        } else {
            Log.d("TABS","b Search = "+ query);
            queuedSearchQuery = query;
            queuedSearchStyle = searchStyle;
        }
    }

    public void clearSearch() {
        Log.d("TABS","clearSearch");
        if (fragments[IDX_SEARCH] instanceof IFragmentSearchable) {
            ((IFragmentSearchable)fragments[IDX_SEARCH]).clearSearch();
        }
        queuedSearchQuery = null;
        queuedSearchStyle = null;
    }

    @Override
    public void Refresh() {
        Fragment fragment = fragments[viewPager.getCurrentItem()];
        if (fragment instanceof FragmentBase) {
            ((FragmentBase) fragment).DownloadUrl(true);
        }
    }

    /**
     * Get the currently visible fragment for TV remote navigation
     * @return the currently visible fragment
     */
    public Fragment getCurrentFragment() {
        if (viewPager != null && viewPager.getCurrentItem() < fragments.length) {
            return fragments[viewPager.getCurrentItem()];
        }
        return null;
    }

    class ViewPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Integer> mFragmentTitleList = new ArrayList<Integer>();

        public ViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, int title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            Resources res = getResources();
            return res.getString(mFragmentTitleList.get(position));
        }
    }
}
