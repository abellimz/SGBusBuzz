package com.abellimz.sgbusbuzz.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.activities.MainActivity;
import com.abellimz.sgbusbuzz.adapters.BusStopsAdapter;
import com.abellimz.sgbusbuzz.adapters.FavouriteStopsAdapter;
import com.abellimz.sgbusbuzz.animations.BusStopAnimator;
import com.abellimz.sgbusbuzz.database.BusStopQueryHelper;
import com.abellimz.sgbusbuzz.models.BusStop;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends BusStopsAdapter.BusStopFragment {

    @BindDrawable(R.drawable.ic_awareness)
    public Drawable setAwareIcon;
    @BindDrawable(R.drawable.ic_awareness_white)
    public Drawable unsetAwareIcon;
    @BindDrawable(R.drawable.ic_heart_filled_white)
    public Drawable whiteHeartIcon;
    @BindDrawable(R.drawable.ic_heart_outline_white)
    public Drawable whiteHeartOutlineIcon;
    @BindString(R.string.message_awareness_not_deleted)
    public String notDeletedMsg;
    @BindColor(R.color.colorBusBuzz)
    public int colorBusAware;
    @BindString(R.string.btn_text_undo)
    String undoText;
    @BindString(R.string.message_undo_delete)
    String undoMessage;
    @BindView(R.id.recycler_view_fav)
    RecyclerView mRecyclerView;

    private BusStopsAdapter mAdapter;
    private List<BusStop> mBusStops;

    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecycler();
        setupFavourites();
    }

    private void setupRecycler() {
        mBusStops = new ArrayList<>();
        mAdapter = new FavouriteStopsAdapter(this, mRecyclerView, mBusStops);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemAnimator animator = new BusStopAnimator(new AccelerateDecelerateInterpolator());
        animator.setAddDuration(400);
        mRecyclerView.setItemAnimator(animator);
    }

    public void refreshTab() {
        mRecyclerView.smoothScrollToPosition(0);
        if (mRecyclerView.computeVerticalScrollOffset() == 0) {
            setupFavourites();
            return;
        }
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int scrollY = recyclerView.computeVerticalScrollOffset();
                if (scrollY == 0) {
                    setupFavourites();
                    mRecyclerView.removeOnScrollListener(this);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void setupFavourites() {
        ((MainActivity) getActivity()).getFavourites(new BusStopQueryHelper.QueryListener() {
            @Override
            public void onResults(List<BusStop> resultsList) {
                mAdapter.setBusStops(resultsList);
                ((MainActivity) getActivity()).registerFencesWithPerms(resultsList);
            }
        });
    }

    // Assumes only delete operations
    @Override
    public void toggleFavourite(final BusStop busStop, final boolean setToFav) {
        if (busStop.isAware()) {
            Snackbar.make(ButterKnife.findById(getActivity(), R.id.activity_main),
                    notDeletedMsg, Snackbar.LENGTH_LONG).show();
            return;
        }
        BusStopQueryHelper.EditListener listener = new BusStopQueryHelper.EditListener() {
            @Override
            public void onResults(boolean isSuccessful) {
                if (!setToFav && isSuccessful) {
                    final int index = mBusStops.indexOf(busStop);
                    mBusStops.remove(index);
                    mAdapter.notifyItemRemoved(index);
//                    String formattedMsg = String.format(undoMessage, busStop.getDescription());
//                    Snackbar snackbar = Snackbar.make(
//                            ButterKnife.findById(getActivity(), R.id.activity_main),
//                            formattedMsg , Snackbar.LENGTH_LONG);
//                    snackbar.setAction(undoText, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            undoRemove(busStop, index);
//                        }
//                    });
//                    snackbar.show();
                }
            }
        };
        if (setToFav) {
            ((MainActivity) getActivity()).addFavourite(busStop, listener);
        } else {
            ((MainActivity) getActivity()).removeFavourite(busStop, listener);
        }
    }

    public void toggleAwareness(final BusStop busStop, final boolean isAware) {
        BusStopQueryHelper.EditListener listener = new BusStopQueryHelper.EditListener() {
            @Override
            public void onResults(boolean isSuccessful) {
                ((MainActivity) getActivity()).registerFencesWithPerms(mBusStops);
            }
        };
        ((MainActivity) getActivity()).setAwareness(busStop, isAware, listener);
    }

    private void undoRemove(final BusStop busStop, final int oldPos) {
        BusStopQueryHelper.EditListener listener = new BusStopQueryHelper.EditListener() {
            @Override
            public void onResults(boolean isSuccessful) {
                if (isSuccessful) {
                    mBusStops.add(oldPos, busStop);
                    mAdapter.notifyItemInserted(oldPos);
                }
            }
        };
        ((MainActivity) getActivity()).addFavourite(busStop, listener);
    }
}
