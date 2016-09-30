package com.abellimz.sgbusbuzz.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.fragments.NearbyFragment;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.utils.BusFormatUtils;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Abel on 9/19/2016.
 */

public class NearbyStopsAdapter extends BusStopsAdapter {

    NearbyFragment nearbyFragment;

    public NearbyStopsAdapter(BusStopFragment fragment, RecyclerView recyclerView, List<BusStop> busStops) {
        super(fragment, recyclerView, busStops);
    }

    @Override
    public BusStopsAdapter.BusStopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_section_nearby_stop, parent, false);
        return new NearbyBusStopHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BusStopViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        NearbyBusStopHolder nearbyHolder = (NearbyBusStopHolder) holder;
        String formattedDist = BusFormatUtils.getFormattedDistance(mBusStops.get(position).getDistance());
        nearbyHolder.distance.setText(formattedDist);
    }

    class NearbyBusStopHolder extends BusStopViewHolder {

        @BindView(R.id.list_section_distance)
        TextView distance;

        NearbyBusStopHolder(View itemView) {
            super(itemView);
        }
    }

}
