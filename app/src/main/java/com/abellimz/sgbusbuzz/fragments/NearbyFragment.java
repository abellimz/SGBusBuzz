package com.abellimz.sgbusbuzz.fragments;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.activities.MainActivity;
import com.abellimz.sgbusbuzz.adapters.BusStopsAdapter;
import com.abellimz.sgbusbuzz.adapters.NearbyStopsAdapter;
import com.abellimz.sgbusbuzz.animations.BusStopAnimator;
import com.abellimz.sgbusbuzz.database.BusStopQueryHelper;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.utils.MapUtils;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.ResultCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyFragment extends BusStopsAdapter.BusStopFragment {

    @BindString(R.string.fab_map_transition)
    String fabTransitionName;

    @BindView(R.id.search_view_nearby)
    FloatingSearchView mSearchView;

    @BindView(R.id.swipe_refresh_nearby)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recycler_view_nearby)
    RecyclerView mRecyclerView;

    private BusStopsAdapter mAdapter;
    private List<BusStop> mBusStops;
    private LinearLayoutManager mLayoutManager;

    public NearbyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this mFragment
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupSearch();
        setupRecycler();
        refreshNearby();
    }

    public List<BusStop> getBusStops() {
        return mBusStops;
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
                BusStop busStop = (BusStop) searchSuggestion;
                double lat = busStop.getLatitude();
                double lon = busStop.getLongitude();
                findNearby(lat, lon);
            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });
    }

    private void setupRecycler() {
        mBusStops = new ArrayList<>();
        mAdapter = new NearbyStopsAdapter(this, mRecyclerView, mBusStops);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemAnimator animator = new BusStopAnimator(new AccelerateDecelerateInterpolator());
        animator.setAddDuration(400);
        animator.setRemoveDuration(400);
        mRecyclerView.setItemAnimator(animator);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshNearby();
            }
        });
        swipeRefreshLayout.setColorSchemeColors(((MainActivity) getActivity()).colorPrimary);
    }

    public void showTimingAtPosition(final int position) {
        mLayoutManager.scrollToPositionWithOffset(position, 0);
        RecyclerView.ViewHolder existingHolder = mRecyclerView
                .findViewHolderForAdapterPosition(position);
        if (existingHolder != null) {
            existingHolder.itemView.performClick();
            return;
        }
        mAdapter.setViewHolderBindListener(new BusStopsAdapter.OnViewHolderBindListener() {
            @Override
            public void OnBind(final RecyclerView.ViewHolder holder) {
                if (holder.getAdapterPosition() == position) {
                    mAdapter.removeViewHolderListener();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.itemView.performClick();
                        }
                    }, 500);
                }
            }
        });
    }

    private void getSuggestions(String newQuery) {
        ((MainActivity) getActivity()).getSuggestions(newQuery, new BusStopQueryHelper.QueryListener() {
            @Override
            public void onResults(List<BusStop> resultsList) {
                //this will swap the data and
                //render the collapse/expand animations as necessary
                mSearchView.swapSuggestions(resultsList);
            }

        });
    }

    private void findNearby(final double lat, final double lon) {
        ((MainActivity) getActivity()).findNearby(lat, lon, new BusStopQueryHelper.QueryListener() {
            @Override
            public void onResults(List<BusStop> resultsList) {
                Collections.sort(resultsList, new Comparator<BusStop>() {
                    @Override
                    public int compare(BusStop b1, BusStop b2) {
                        Integer b1Dist = b1.getDistance();
                        if (b1Dist == null) {
                            b1Dist = MapUtils.calcDistance(b1.getLatitude(), b1.getLongitude(),
                                    lat, lon);
                            b1.setDistance(b1Dist);
                        }
                        Integer b2Dist = b2.getDistance();
                        if (b2Dist == null) {
                            b2Dist = MapUtils.calcDistance(b2.getLatitude(), b2.getLongitude(),
                                    lat, lon);
                            b2.setDistance(b2Dist);
                        }
                        return b1Dist - b2Dist;
                    }
                });
                mAdapter.setBusStops(resultsList);
                swipeRefreshLayout.setRefreshing(false);
            }

        });
    }

    public void refreshTab() {
        if (mRecyclerView.computeVerticalScrollOffset() == 0) {
            swipeRefreshLayout.setRefreshing(true);
            refreshNearby();
        } else {
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void refreshNearby() {
        MainActivity main = (MainActivity) getActivity();
        ResultCallback<LocationResult> callback = new ResultCallback<LocationResult>() {
            @Override
            public void onResult(@NonNull LocationResult locationResult) {
                if (!locationResult.getStatus().isSuccess()) {
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                Location location = locationResult.getLocation();
                findNearby(location.getLatitude(), location.getLongitude());
            }
        };
        main.getUserLocation(callback);
    }

    @Override
    public void toggleFavourite(BusStop busStop, boolean setToFav) {
        BusStopQueryHelper.EditListener listener = new BusStopQueryHelper.EditListener() {
            @Override
            public void onResults(boolean isSuccessful) {

            }
        };
        if (setToFav) {
            ((MainActivity) getActivity()).addFavourite(busStop, listener);
        } else {
            ((MainActivity) getActivity()).removeFavourite(busStop, listener);
        }
    }
}
