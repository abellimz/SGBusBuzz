package com.abellimz.sgbusbuzz.database;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.abellimz.sgbusbuzz.models.BusStop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abel on 9/14/2016.
 */

public class BusStopQueryHelper {

    private BusStopDbTable mTable;
    private List<BusStop> mFavourites;

    public BusStopQueryHelper(Context context, int queryLimit, int nearbyRadius) {
        mTable = new BusStopDbTable(context, queryLimit, nearbyRadius);
    }

    public void getSuggestions(String queryText, QueryListener listener) {
        SuggestionsAsyncTask task = new SuggestionsAsyncTask(listener);
        task.execute(queryText);
    }

    public void findNearby(double lat, double lon, QueryListener listener) {
        NearbyAsyncTask task = new NearbyAsyncTask(listener);
        task.execute(lat, lon);
    }

    public void getFavourites(QueryListener listener) {
        FavouritesAsyncTask favAsyncTask = new FavouritesAsyncTask(listener);
        favAsyncTask.execute();
    }

    public void addToFavourite(BusStop busStop, EditListener listener) {
        AddFavAsyncTask task = new AddFavAsyncTask(listener);
        task.execute(busStop);
    }

    public void removeFromFavourite(BusStop busStop, EditListener listener) {
        RemoveFavAsyncTask task = new RemoveFavAsyncTask(listener);
        task.execute(busStop);
    }

    public void setAwareness(BusStop busStop, boolean isAware, EditListener listener) {
        SetAwareAsyncTask task = new SetAwareAsyncTask(listener, isAware);
        task.execute(busStop);
    }

    private List<BusStop> cursorToBusStops(Cursor cursor) {
        if (cursor == null) {
            return new ArrayList<>();
        }
        cursor.moveToFirst();
        int codeIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_BUS_STOP_CODE);
        int roadNameIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_ROAD_NAME);
        int descIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_DESCRIPTION);
        int latIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_LATITUDE);
        int lonIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_LONGITUDE);
        int awareIdx = cursor.getColumnIndex(BusStopDbTable.COLUMN_NAME_AWARENESS);
        ArrayList<BusStop> resultsList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            BusStop busStop = new BusStop(
                    cursor.getString(codeIdx),
                    cursor.getString(roadNameIdx),
                    cursor.getString(descIdx),
                    cursor.getDouble(latIdx),
                    cursor.getDouble(lonIdx)
            );
            // For favourite bus stops, set awareness and favourite boolean value
            if (awareIdx != -1) {
                busStop.setAware(cursor.getInt(awareIdx) == 1);
                busStop.setIsFavourite(true);
            }
            if (mFavourites != null) {
                for (BusStop favStop : mFavourites) {
                    if (favStop.getBusStopCode().equals(busStop.getBusStopCode())) {
                        busStop.setIsFavourite(true);
                    }
                }
            }
            resultsList.add(busStop);
        }
        return resultsList;
    }

    public interface QueryListener {
        void onResults(List<BusStop> resultsList);
    }

    public interface EditListener {
        void onResults(boolean isSuccessful);
    }

    private class SetAwareAsyncTask extends EditorAsyncTask<BusStop, Void, Boolean> {
        private boolean isAware;

        SetAwareAsyncTask(EditListener listener, boolean isAware) {
            super(listener);
            this.isAware = isAware;
        }

        @Override
        protected Boolean doInBackground(BusStop... busStops) {
            return mTable.setAwareness(busStops[0], isAware);
        }
    }

    private class AddFavAsyncTask extends EditorAsyncTask<BusStop, Void, Boolean> {

        AddFavAsyncTask(EditListener listener) {
            super(listener);
        }

        @Override
        protected Boolean doInBackground(BusStop... busStops) {
            return mTable.addToFavourite(busStops[0]);
        }
    }

    private class RemoveFavAsyncTask extends EditorAsyncTask<BusStop, Void, Boolean> {

        RemoveFavAsyncTask(EditListener listener) {
            super(listener);
        }

        @Override
        protected Boolean doInBackground(BusStop... busStops) {
            return mTable.removeFromFavourite(busStops[0]);
        }
    }

    private class FavouritesAsyncTask extends ListenerAsyncTask<Void, Void, List<BusStop>> {

        FavouritesAsyncTask(QueryListener listener) {
            super(listener);
        }

        @Override
        protected List<BusStop> doInBackground(Void... voids) {
            Cursor cursor = mTable.getFavourites();
            mFavourites = cursorToBusStops(cursor);
            return mFavourites;
        }
    }

    private class NearbyAsyncTask extends ListenerAsyncTask<Double, Void, List<BusStop>> {

        NearbyAsyncTask(QueryListener listener) {
            super(listener);
        }

        @Override
        protected List<BusStop> doInBackground(Double... coordinates) {
            if (coordinates.length != 2) {
                return null;
            }
            Cursor cursor = mTable.findNearby(coordinates[0], coordinates[1]);
            return cursorToBusStops(cursor);
        }
    }

    private class SuggestionsAsyncTask extends ListenerAsyncTask<String,
            Void, List<BusStop>> {
        QueryListener mListener;

        SuggestionsAsyncTask(QueryListener listener) {
            super(listener);
        }

        @Override
        protected List<BusStop> doInBackground(String... params) {
            switch (params.length) {
                case 0:
                    return null;
                case 1:
                    Cursor cursor = mTable.getSearchSuggestions(params[0], null);
                    return cursorToBusStops(cursor);
                default:
                    return null;
            }
        }
    }

    private abstract class ListenerAsyncTask<Params, Progress, Results>
            extends AsyncTask<Params, Progress, List<BusStop>> {
        QueryListener mListener;

        ListenerAsyncTask(QueryListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPostExecute(List<BusStop> result) {
            if (mListener == null) {
                return;
            }
            mListener.onResults(result);
        }
    }

    private abstract class EditorAsyncTask<Params, Progress, Results>
            extends AsyncTask<Params, Progress, Boolean> {
        EditListener mListener;

        EditorAsyncTask(EditListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPostExecute(Boolean isSuccessful) {
            if (mListener == null) {
                return;
            }
            if (isSuccessful) {
                getFavourites(null);
            }
            mListener.onResults(isSuccessful);
        }
    }
}
