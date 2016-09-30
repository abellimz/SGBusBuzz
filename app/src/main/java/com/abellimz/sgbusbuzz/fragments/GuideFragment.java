package com.abellimz.sgbusbuzz.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.activities.MainActivity;
import com.abellimz.sgbusbuzz.adapters.BusStopsAdapter;
import com.abellimz.sgbusbuzz.database.BusStopQueryHelper;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuideFragment extends Fragment {

    @BindView(R.id.search_view_guide)
    FloatingSearchView mSearchView;

    @BindView(R.id.recycler_view_guide)
    RecyclerView mRecyclerView;

    private BusStopsAdapter mAdapter;
    private List<BusStop> mSuggestionList;

    public GuideFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mFragment
        View view = inflater.inflate(R.layout.fragment_guide, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSearch();
        setupRecycler();
    }

    private void setupSearch() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {
                    getSuggestions(newQuery);
                }
            }
        });

//        TODO: set history items for search view
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
//                BusStop colorSuggestion = (BusStop) item;
                textView.setTextColor(Color.BLACK);
            }

        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });
    }

    private void setupRecycler() {
//        ((MainActivity) getActivity()).getSuggestions();
//        mAdapter = new BusStopsAdapter(getActivity(), );
//        mRecyclerView.setAdapter(new BusStopsAdapter());
    }

    private void getSuggestions(String newQuery) {
        ((MainActivity) getActivity()).getSuggestions(newQuery, new BusStopQueryHelper.QueryListener() {
            @Override
            public void onResults(List<BusStop> resultsList) {
                //this will swap the data and
                //render the collapse/expand animations as necessary
                mSuggestionList = resultsList;
                mSearchView.swapSuggestions(resultsList);
            }
        });
    }

    public interface BusStopSearchInterface {
        void getSuggestions(String query, BusStopQueryHelper.QueryListener listener);
    }
}
