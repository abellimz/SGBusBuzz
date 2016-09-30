package com.abellimz.sgbusbuzz.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.abellimz.sgbusbuzz.R;
import com.abellimz.sgbusbuzz.activities.MainActivity;
import com.abellimz.sgbusbuzz.models.BusArrivalResponse;
import com.abellimz.sgbusbuzz.models.BusStop;
import com.abellimz.sgbusbuzz.network.LtaDataApiClient;
import com.abellimz.sgbusbuzz.utils.BusFormatUtils;
import com.abellimz.sgbusbuzz.utils.SntpClient;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Abel on 9/20/2016.
 */

public class BusBuzzService extends Service {
    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVE";
    private final String PREFIX_BUS_STOP_IN = "OUTFENCE_";
    private final String PREFIX_BUS_STOP_EXIT = "EXITFENCE";
    // For awareness
    private final int RADIUS_AWARE = 300;
    private final int DURATION_AWARE = 5000; // 5 Seconds

    private GoogleApiClient mGoogleClient;
    private PendingIntent mPendingIntent;
    private BusBuzzReceiver mFenceReceiver;
    private List<BusStop> mAwareBusStops;
    private DateTime mCurrentDt;
    private AsyncTask mTimeTask;

    @Override
    public void onCreate() {
        super.onCreate();
        setupGoogleApi();
        setupIntentBroadcast();
        mAwareBusStops = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            mAwareBusStops = intent.getParcelableArrayListExtra(MainActivity.KEY_EXTRA_AWARE_BUS_STOPS);
            setAwareBusStops();
        } else {
            onDestroy();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void setupGoogleApi() {
        mGoogleClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .build();
        mGoogleClient.connect();
    }

    private void setupIntentBroadcast() {
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        mFenceReceiver = new BusBuzzReceiver();
        registerReceiver(mFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    @SuppressWarnings("MissingPermission")
    public void setAwareBusStops() {
        FenceUpdateRequest.Builder fenceBuilder = new FenceUpdateRequest.Builder();
        fenceBuilder.removeFence(mPendingIntent);
        for (BusStop busStop : mAwareBusStops) {
            addFences(busStop, fenceBuilder);
        }
        Awareness.FenceApi.updateFences(mGoogleClient, fenceBuilder.build());
    }

    @SuppressWarnings("MissingPermission")
    private void addFences(BusStop busStop, FenceUpdateRequest.Builder fenceBuilder) {
//        AwarenessFence stationaryFence = DetectedActivityFence.stopping(DetectedActivityFence.ON_FOOT, DetectedActivityFence.IN_VEHICLE);
        AwarenessFence geoInFence = LocationFence.in(busStop.getLatitude(),
                busStop.getLongitude(), RADIUS_AWARE, DURATION_AWARE);
//        AwarenessFence busBuzzFence = AwarenessFence.and(stationaryFence, geoInFence);
        AwarenessFence geoExitFence = LocationFence.exiting(busStop.getLatitude(),
                busStop.getLongitude(), RADIUS_AWARE);
        fenceBuilder.addFence(PREFIX_BUS_STOP_IN + busStop.getBusStopCode(), geoInFence,
                mPendingIntent);
        fenceBuilder.addFence(PREFIX_BUS_STOP_EXIT + busStop.getBusStopCode(), geoExitFence,
                mPendingIntent);
    }

    private void busBuzz(BusStop busStop, List<com.abellimz.sgbusbuzz.models.Service> services) {
        if (mCurrentDt == null) {
            return;
        }
        String title = BusFormatUtils.getTruncatedContent(busStop);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(BusBuzzService.this)
                        .setSmallIcon(R.drawable.ic_bus)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                                R.mipmap.ic_launcher))
                        .setShowWhen(true)
                        .setContentTitle(title)
                        .setAutoCancel(true);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        String summary = BusFormatUtils.bindTimingsToInboxStyle(inboxStyle, mCurrentDt, services);
        if (summary == null) {
            return;
        }
        builder.setContentText(summary);
        builder.setStyle(inboxStyle);
        Notification noti = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Integer.valueOf(busStop.getBusStopCode()), noti);
    }

    private void dismissNotification(int id) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private void getCurrentTimeASync() {
        mTimeTask = new SntpClient.TimeAsyncTask() {
            @Override
            protected void onPostExecute(DateTime dateTime) {
                mCurrentDt = dateTime;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void getBusTimings(final BusStop busStop) {
        getCurrentTimeASync();
        enqueueTimingsCall(busStop.getBusStopCode(), new Callback<BusArrivalResponse>() {
            @Override
            public void onResponse(Call<BusArrivalResponse> call, Response<BusArrivalResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (response.errorBody() != null) {
                        try {
                            Log.e("ERROR", response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                syncWithCurrentTime(busStop, response.body().getServices());
            }

            @Override
            public void onFailure(Call<BusArrivalResponse> call, Throwable t) {
                Log.e("ERROR", t.getMessage());
            }
        });
    }

    private void enqueueTimingsCall(String busStopId, Callback<BusArrivalResponse> callback) {
        Call<BusArrivalResponse> call =
                LtaDataApiClient.buildBusArrivalCall(busStopId);
        call.enqueue(callback);
    }

    private void syncWithCurrentTime(final BusStop busStop, final List<com.abellimz.sgbusbuzz.models.Service> services) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                int count = 0;
                while (!mTimeTask.getStatus().equals(Status.FINISHED) && !mTimeTask.isCancelled()) {
                    try {
                        if (count >= 100) {
                            return null;
                        }
                        count++;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                busBuzz(busStop, services);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mFenceReceiver);
        } catch (IllegalArgumentException e) {
            super.onDestroy();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class BusBuzzReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
//            android.os.Debug.waitForDebugger();
            FenceState fenceState = FenceState.extract(intent);
            if (fenceState.getCurrentState() != FenceState.TRUE) {
                Log.e("failed state check:", fenceState.getFenceKey());
                return;
            }
            for (BusStop awareBusStop : mAwareBusStops) {
                if (!fenceState.getFenceKey().substring(9)
                        .equals(awareBusStop.getBusStopCode())) {
                    continue;
                }
                String key = fenceState.getFenceKey().substring(0, 9);
                String busStopCode = awareBusStop.getBusStopCode();
                switch (key) {
                    case PREFIX_BUS_STOP_IN:
                        getBusTimings(awareBusStop);
                        return;
                    case PREFIX_BUS_STOP_EXIT:
                        dismissNotification(Integer.valueOf(busStopCode));
                        return;
                }
            }
        }
    }
}
