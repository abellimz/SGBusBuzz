package com.abellimz.sgbusbuzz.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.PointF;

import com.abellimz.sgbusbuzz.models.BusStop;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import static com.abellimz.sgbusbuzz.utils.MapUtils.calculateDerivedPosition;

/**
 * Created by Abel on 9/14/2016.
 */

public class BusStopDbTable {
    public static final String DEFAULT_TABLE_NAME = "BUS_STOPS";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_BUS_STOP_CODE = "BusStopCode";
    public static final String COLUMN_NAME_ROAD_NAME = "RoadName";
    public static final String COLUMN_NAME_DESCRIPTION = "Description";
    public static final String COLUMN_NAME_LATITUDE = "Latitude";
    public static final String COLUMN_NAME_LONGITUDE = "Longitude";
    public static final String COLUMN_NAME_AWARENESS = "Awareness";
    private static final String DATABASE_NAME = "BusStops.db";
    private static final int DATABASE_VERSION = 1;
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    // SQL Statements for TABLE CREATE
    private static final String TABLE_NAME_FTS = "FTS";
    private static final String FTS_TABLE_CREATE =
            "CREATE VIRTUAL TABLE IF NOT EXISTS " + TABLE_NAME_FTS +
                    " USING fts3 (" +
                    COLUMN_NAME_ID + ", " +
                    COLUMN_NAME_BUS_STOP_CODE + ", " +
                    COLUMN_NAME_ROAD_NAME + ", " +
                    COLUMN_NAME_DESCRIPTION + ", " +
                    COLUMN_NAME_LATITUDE + ", " +
                    COLUMN_NAME_LONGITUDE + ")";

    private static final String TABLE_NAME_FAVOURITES = "FAVOURITES";
    private static final String FAV_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME_FAVOURITES + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_BUS_STOP_CODE + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_NAME_ROAD_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LATITUDE + DOUBLE_TYPE + COMMA_SEP +
                    COLUMN_NAME_LONGITUDE + DOUBLE_TYPE + COMMA_SEP +
                    COLUMN_NAME_AWARENESS + INTEGER_TYPE +
                    " )";

    private static final String INSERT_FTS =
            "INSERT INTO " + TABLE_NAME_FTS +
                    " SELECT * FROM " + DEFAULT_TABLE_NAME;
    private final int DEFAULT_VALUE_AWARENESS = 0;

    // Table instance variables
    private BusStopDbHelper mDbHelper;
    private int mQueryLimit;
    private int mRadius;

    public BusStopDbTable(Context context, int queryLimit, int nearbyRadius) {
        mDbHelper = new BusStopDbHelper(context);
        mQueryLimit = queryLimit;
        mRadius = nearbyRadius;
    }

    public Cursor getAllBusStops() {
        return null;
    }

    public Cursor getFavourites() {
        return query(TABLE_NAME_FAVOURITES, null, null, null, null);
    }

    public Boolean removeFromFavourite(BusStop busStop) {
        SQLiteDatabase writableDB = mDbHelper.getWritableDatabase();
        String busStopCode = busStop.getBusStopCode();
        String selection = COLUMN_NAME_BUS_STOP_CODE + " = ?";
        String[] args = new String[]{busStopCode};
        return writableDB.delete(TABLE_NAME_FAVOURITES, selection, args) > 0;
    }

    public Boolean setAwareness(BusStop busStop, boolean isAware) {
        SQLiteDatabase writableDB = mDbHelper.getWritableDatabase();
        String selection = COLUMN_NAME_BUS_STOP_CODE + "= ?";
        String[] args = new String[]{busStop.getBusStopCode()};
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_AWARENESS, isAware ? 1 : 0);
        return writableDB.update(TABLE_NAME_FAVOURITES, contentValues, selection, args) > 0;
    }

    public Boolean addToFavourite(BusStop busStop) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME_BUS_STOP_CODE,
                busStop.getBusStopCode());
        contentValues.put(COLUMN_NAME_ROAD_NAME,
                busStop.getRoadName());
        contentValues.put(COLUMN_NAME_DESCRIPTION,
                busStop.getDescription());
        contentValues.put(COLUMN_NAME_LATITUDE,
                busStop.getLatitude());
        contentValues.put(COLUMN_NAME_LONGITUDE,
                busStop.getLongitude());
        contentValues.put(COLUMN_NAME_AWARENESS,
                DEFAULT_VALUE_AWARENESS);
        // Write to db
        SQLiteDatabase writableDB = mDbHelper.getWritableDatabase();
        return writableDB.insertWithOnConflict(TABLE_NAME_FAVOURITES, null, contentValues, SQLiteDatabase.CONFLICT_FAIL) != -1;
    }

    public Cursor findNearby(double lat, double lon) {
        PointF center = new PointF((float) lat, (float) lon);
        final double mult = 1; // mult = 1.1; is more reliable
        PointF p1 = calculateDerivedPosition(center, mult * mRadius, 0);
        PointF p2 = calculateDerivedPosition(center, mult * mRadius, 90);
        PointF p3 = calculateDerivedPosition(center, mult * mRadius, 180);
        PointF p4 = calculateDerivedPosition(center, mult * mRadius, 270);
        // Setup query params
        String nearbySelection = COLUMN_NAME_LATITUDE + " > ?" + " AND "
                + COLUMN_NAME_LATITUDE + " < ?" + " AND "
                + COLUMN_NAME_LONGITUDE + " < ?" + " AND "
                + COLUMN_NAME_LONGITUDE + " > ?";
        String[] selectionArgs = new String[]{
                String.valueOf(p3.x), String.valueOf(p1.x), String.valueOf(p2.y),
                String.valueOf(p4.y)};
        Cursor cursor = query(DEFAULT_TABLE_NAME, nearbySelection, selectionArgs, null,
                mQueryLimit);
        return cursor;
    }

    public Cursor getSearchSuggestions(String query, String[] columns) {
        String descSelection = COLUMN_NAME_DESCRIPTION + " MATCH ?";
        String[] selectionArgs = new String[]{query + "*"};

        Cursor cursor = query(TABLE_NAME_FTS, descSelection, selectionArgs, columns,
                mQueryLimit);
        int limit = mQueryLimit;
        int diff = limit;
        if (cursor == null || (diff = limit - cursor.getCount()) > 0) {
            String roadNameSelection = COLUMN_NAME_ROAD_NAME + " MATCH ?";
            cursor = new MergeCursor(new Cursor[]{
                    cursor, query(TABLE_NAME_FTS, roadNameSelection, selectionArgs,
                    columns, diff)
            });
            if ((diff = limit - cursor.getCount()) > 0) {
                String codeSelection = COLUMN_NAME_BUS_STOP_CODE + " MATCH ?";
                cursor = new MergeCursor(new Cursor[]{
                        cursor, query(TABLE_NAME_FTS, codeSelection, selectionArgs,
                        columns, diff)
                });
            }
        }
        return cursor;
    }

    private Cursor query(String tableName, String selection, String[] selectionArgs, String[] columns, Integer limit) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tableName);
        String limitString = String.valueOf(limit);
        if (limit == null) {
            limitString = null;
        }
        Cursor cursor = builder.query(mDbHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null,
                null, limitString);
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private static class BusStopDbHelper extends SQLiteAssetHelper {

        BusStopDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!isTableExists(db, TABLE_NAME_FTS)) {
                db.execSQL(FTS_TABLE_CREATE);
                db.execSQL(INSERT_FTS);
            }
            db.execSQL(FAV_TABLE_CREATE);
        }

        private boolean isTableExists(SQLiteDatabase db, String tableName) {
            if (tableName == null || db == null || !db.isOpen()) {
                return false;
            }
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName});
            if (!cursor.moveToFirst()) {
                cursor.close();
                return false;
            }
            int count = cursor.getInt(0);
            cursor.close();
            return count > 0;
        }
    }
}
