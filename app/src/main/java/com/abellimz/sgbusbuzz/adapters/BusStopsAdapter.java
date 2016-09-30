package com.abellimz.sgbusbuzz.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.models.Bus;
import com.abellimz.sgbusbuzz.models.BusArrivalResponse;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.models.Service;
import com.abellimz.sgbusbuzz.network.LtaDataApiClient;
import com.abellimz.sgbusbuzz.utils.AnimUtils;
import com.abellimz.sgbusbuzz.utils.BusFormatUtils;
import com.abellimz.sgbusbuzz.utils.SntpClient;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abel on 9/16/2016.
 */

public abstract class BusStopsAdapter extends RecyclerView.Adapter<BusStopsAdapter.BusStopViewHolder> {

    protected SparseBooleanArray loadingItems;
    protected SparseBooleanArray expandedItems;
    protected Context mContext;
    protected BusStopFragment mFragment;
    protected RecyclerView mRecyclerView;
    protected List<List<Service>> mServicesList;
    protected List<BusStop> mBusStops;
    protected AsyncTask mTimeTask;
    protected DateTime mCurrentDt;
    protected OnViewHolderBindListener viewHolderBindListener;

    public BusStopsAdapter(BusStopFragment fragment, RecyclerView recyclerView, List<BusStop> busStops) {
        mContext = fragment.getActivity();
        mRecyclerView = recyclerView;
        mBusStops = busStops;
        mFragment = fragment;
        ButterKnife.bind(fragment.getActivity());
    }

    @Override
    public void onBindViewHolder(final BusStopViewHolder holder, int position) {
        // For rebinding of data on recycle, just display data loaded if any
        setContainerParams(holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTimings(holder);
            }
        });
        final BusStop busStop = mBusStops.get(holder.getAdapterPosition());
        holder.busStopCode.setText("(" + busStop.getBusStopCode() + ")");
        holder.stopName.setText(busStop.getDescription());
        holder.roadName.setText(busStop.getRoadName());
        holder.favButton.setImageDrawable(busStop.isFavourite() ? mFragment.heartFilledIcon
                : mFragment.heartOutlineIcon);
        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean setToFav = !busStop.isFavourite();
                busStop.setIsFavourite(setToFav);
                AnimUtils.animateTouchView(holder.favButton, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                });
                mFragment.toggleFavourite(busStop, setToFav);
            }
        });
        if (viewHolderBindListener != null) {
            viewHolderBindListener.OnBind(holder);
        }
    }

    public void setViewHolderBindListener(OnViewHolderBindListener listener) {
        viewHolderBindListener = listener;
    }

    public void removeViewHolderListener() {
        viewHolderBindListener = null;
    }

    public void setBusStops(List<BusStop> busStops) {
        int originalCount = mBusStops.size();
        mBusStops.clear();
        notifyItemRangeRemoved(0, originalCount);
        mBusStops.addAll(busStops);
        notifyItemRangeInserted(0, mBusStops.size());
        mServicesList = new ArrayList<>(busStops.size());
        for (int i = 0; i < busStops.size(); i++) {
            mServicesList.add(null);
        }
        loadingItems = new SparseBooleanArray();
        expandedItems = new SparseBooleanArray();
    }

    protected void toggleTimings(final BusStopViewHolder holder) {
        if (holder.containerLayout.isActivated()) {
            closeTimings(holder);
        } else {
            getTimings(holder);
        }
    }

    protected void setContainerParams(BusStopViewHolder holder) {
        int position = holder.getAdapterPosition();
        boolean isExpanded = expandedItems.get(position);
        boolean isLoading = loadingItems.get(position);
        List<Service> services = mServicesList.get(position);
        bindTimingsToViews(holder, services, mCurrentDt);
        holder.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.containerLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
    }

    protected void closeTimings(BusStopViewHolder holder) {
        expandedItems.put(holder.getAdapterPosition(), false);
//        TransitionManager.beginDelayedTransition(mRecyclerView);
        notifyItemChanged(holder.getAdapterPosition());
    }

    protected void showTimings(BusStopViewHolder holder) {
        expandedItems.put(holder.getAdapterPosition(), true);
//        TransitionManager.beginDelayedTransition(mRecyclerView);
        notifyItemChanged(holder.getAdapterPosition());
    }

    protected void getCurrentTime() {
        mCurrentDt = null;
        // this should be your date
        mTimeTask = new SntpClient.TimeAsyncTask() {
            @Override
            protected void onPostExecute(DateTime result) {
                mCurrentDt = result;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void syncWithCurrentTime(final BusStopViewHolder sectionHolder) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                while (!mTimeTask.getStatus().equals(Status.FINISHED) && !mTimeTask.isCancelled()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                setLoadingPosition(sectionHolder.getAdapterPosition(), false);
                showTimings(sectionHolder);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void bindTimingsToViews(BusStopViewHolder sectionHolder, List<Service> services, DateTime currentDt) {
        if (services == null) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        sectionHolder.containerLayout.removeAllViews();
        for (int i = 0; i < services.size(); i++) {
            Service service = services.get(i);
            PercentRelativeLayout itemView = (PercentRelativeLayout) inflater.inflate(R.layout.list_item_bus_timing,
                    sectionHolder.containerLayout, false);
            bindServiceToView(service, itemView, currentDt);
            sectionHolder.containerLayout.addView(itemView, i);
        }
    }

    private void bindServiceToView(Service service, PercentRelativeLayout itemView, DateTime currentDt) {
        BusTimingViewHolder itemHolder = new BusTimingViewHolder(itemView);
        String serviceNo = service.getServiceNo();
        itemHolder.busNo.setText(serviceNo);
        boolean isOperating = setStatusText(service, itemHolder.statusField);
        if (!isOperating) {
            return;
        }
        Bus bus1 = service.getNextBus();
        Bus bus2 = service.getSubsequentBus();
        Bus bus3 = service.getSubsequentBus3();
        setArrivalTimes(bus1, itemHolder.busTime1, currentDt);
        setArrivalTimes(bus2, itemHolder.busTime2, currentDt);
        setArrivalTimes(bus3, itemHolder.busTime3, currentDt);
    }

    /**
     * This method sets the text for bus service status accordingly
     *
     * @param service     service to check for
     * @param statusField text view to set text
     * @return whether service is operating
     */
    private boolean setStatusText(Service service, TextView statusField) {
        boolean isOperating = BusFormatUtils.isOperating(service);
        if (!isOperating) {
            statusField.setText(BusFormatUtils.getDisplayMsgNotOperating());
        }
        return isOperating;
    }

    private void setArrivalTimes(Bus bus, TextView timingText, DateTime currentDt) {
        String minsToArrival = bus.getMinsToArrival();
        if (minsToArrival == null) {
            minsToArrival = BusFormatUtils.getFormattedMinsToBus(currentDt,
                    bus.getEstimatedArrival()
            );
            bus.setMinsToArrival(minsToArrival);
        }
        timingText.setText(minsToArrival);
        int loadIndex = BusFormatUtils.getLoadIndex(bus.getLoad());
        switch (loadIndex) {
            case 0:
                timingText.setTextColor(mFragment.colorLowLoad);
                return;
            case 1:
                timingText.setTextColor(mFragment.colorMedLoad);
                return;
            case 2:
                timingText.setTextColor(mFragment.colorHighLoad);
                return;
            default:
                timingText.setTextColor(Color.WHITE);
                break;
        }
    }

    protected void getTimings(final BusStopViewHolder holder) {
        boolean isLoading = loadingItems.get(holder.getAdapterPosition());
        if (isLoading) {
            return;
        }
        setLoadingPosition(holder.getAdapterPosition(), true);
        BusStop busStop = mBusStops.get(holder.getAdapterPosition());
        getCurrentTime();
        enqueueTimingsCall(busStop.getBusStopCode(), new Callback<BusArrivalResponse>() {
            @Override
            public void onResponse(Call<BusArrivalResponse> call, Response<BusArrivalResponse> response) {
                if (response.body() == null) {
                    try {
                        Log.e("Error", response.errorBody().string());
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                BusArrivalResponse arrivalResponse = response.body();
                List<Service> services = arrivalResponse.getServices();
                mServicesList.set(holder.getAdapterPosition(), services);
                syncWithCurrentTime(holder);
            }

            @Override
            public void onFailure(Call<BusArrivalResponse> call, Throwable t) {
                setLoadingPosition(holder.getAdapterPosition(), false);
            }
        });
    }

    private void enqueueTimingsCall(String busStopId, Callback<BusArrivalResponse> callback) {
        Call<BusArrivalResponse> call =
                LtaDataApiClient.buildBusArrivalCall(busStopId);
        call.enqueue(callback);
    }

    private void setLoadingPosition(int position, boolean isLoading) {
        loadingItems.put(position, isLoading);
        notifyItemChanged(position);
        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Override
    public int getItemCount() {
        return mBusStops.size();
    }

    public interface OnViewHolderBindListener {
        void OnBind(RecyclerView.ViewHolder holder);
    }

    public static abstract class BusStopFragment extends Fragment {

        @BindDrawable(R.drawable.ic_heart_filled)
        public Drawable heartFilledIcon;
        @BindDrawable(R.drawable.ic_heart_outline)
        public Drawable heartOutlineIcon;
        @BindColor(R.color.colorLowLoad)
        public int colorLowLoad;
        @BindColor(R.color.colorMedLoad)
        public int colorMedLoad;
        @BindColor(R.color.colorHighLoad)
        public int colorHighLoad;

        public abstract void toggleFavourite(BusStop busStop, boolean setToFav);

    }

    class BusStopViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_section_progressbar)
        SmoothProgressBar progressBar;
        @BindView(R.id.list_section_fav_btn)
        ImageView favButton;
        @BindView(R.id.list_section_bus_stop_code)
        TextView busStopCode;
        @BindView(R.id.list_section_stop_name)
        TextView stopName;
        @BindView(R.id.list_section_road_name)
        TextView roadName;
        @BindView(R.id.list_section_container_layout)
        LinearLayout containerLayout;

        BusStopViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class BusTimingViewHolder {

        @BindView(R.id.list_item_bus_no)
        TextView busNo;
        @BindView(R.id.list_item_bus_status)
        TextView statusField;
        @BindView(R.id.list_item_bus_timing1)
        TextView busTime1;
        @BindView(R.id.list_item_bus_timing2)
        TextView busTime2;
        @BindView(R.id.list_item_bus_timing3)
        TextView busTime3;

        BusTimingViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
