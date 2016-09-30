package com.abellimz.sgbusbuzz.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.fragments.FavouritesFragment;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.utils.AnimUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abel on 9/19/2016.
 */

public class FavouriteStopsAdapter extends BusStopsAdapter {

    private FavouritesFragment mFragment;

    public FavouriteStopsAdapter(BusStopFragment fragment, RecyclerView recyclerView, List<BusStop> busStops) {
        super(fragment, recyclerView, busStops);
        mFragment = (FavouritesFragment) fragment;
    }

    @Override
    public BusStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_section_favourites_stop, parent, false);
        return new FavBusStopHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BusStopViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final BusStop busStop = mBusStops.get(holder.getAdapterPosition());
        final FavBusStopHolder favHolder = (FavBusStopHolder) holder;
        setupButtons(favHolder, busStop);
        setTexts(favHolder, busStop);
        favHolder.detailsLayout.setVisibility(busStop.isAware() ? View.INVISIBLE : View.VISIBLE);
        favHolder.detailsLayoutAware.setVisibility(!busStop.isAware() ? View.INVISIBLE : View.VISIBLE);
    }

    private void setupButtons(final FavBusStopHolder holder, final BusStop busStop) {
        View.OnClickListener awareClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAwareness(busStop, holder);
            }
        };
        holder.awareBtn.setOnClickListener(awareClickListener);
        holder.awareBtnAware.setOnClickListener(awareClickListener);
        View.OnClickListener favClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(ButterKnife.findById(mFragment.getActivity(), R.id.activity_main),
                        mFragment.notDeletedMsg,
                        Snackbar.LENGTH_SHORT).show();
            }
        };
        holder.favBtnAware.setOnClickListener(favClickListener);
        holder.favBtnAware.setImageDrawable(busStop.isFavourite() ? mFragment.whiteHeartIcon :
                mFragment.whiteHeartOutlineIcon);
    }


    private void setTexts(FavBusStopHolder holder, BusStop busStop) {
        holder.busStopCodeAware.setText(busStop.getBusStopCode());
        holder.roadNameAware.setText(busStop.getRoadName());
        holder.stopNameAware.setText(busStop.getDescription());
    }

    private void toggleAwareness(BusStop busStop, FavBusStopHolder holder) {
        boolean setToAware = !busStop.isAware();
        busStop.setAware(setToAware);
        animateAwareToggle(holder, setToAware);
        mFragment.toggleAwareness(busStop, setToAware);
    }

    private void animateAwareToggle(final FavBusStopHolder holder, final boolean isAware) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    notifyItemChanged(holder.getAdapterPosition());
                }
            };
            holder.detailsLayoutAware.bringToFront();
            if (isAware) {
                AnimUtils.circularReveal(holder.awareBtn,
                        holder.detailsLayoutAware, holder.detailsLayoutAware,
                        500, listener);
            } else {
                holder.detailsLayout.setVisibility(View.VISIBLE);
                AnimUtils.circularExit(holder.detailsLayoutAware, holder.awareBtn,
                        500, listener);
            }
        } else {
            notifyItemChanged(holder.getAdapterPosition());
        }
    }

    public class FavBusStopHolder extends BusStopViewHolder {

        @BindView(R.id.list_section_aware_enable)
        RelativeLayout detailsLayoutAware;
        @BindView(R.id.list_section_aware_disable)
        RelativeLayout detailsLayout;
        @BindView(R.id.list_section_fav_btn_aware)
        ImageView favBtnAware;
        @BindView(R.id.list_section_bus_stop_code_aware)
        TextView busStopCodeAware;
        @BindView(R.id.list_section_road_name_aware)
        TextView roadNameAware;
        @BindView(R.id.list_section_stop_name_aware)
        TextView stopNameAware;
        @BindView(R.id.list_section_awareness_btn_aware)
        ImageView awareBtnAware;
        @BindView(R.id.list_section_awareness_btn)
        ImageView awareBtn;

        FavBusStopHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
