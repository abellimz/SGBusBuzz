package com.abellimz.sgbusbuzz.models;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * Created by Abel on 9/14/2016.
 */

public class BusStop implements SearchSuggestion {

    public static final Creator<BusStop> CREATOR = new Creator<BusStop>() {
        @Override
        public BusStop createFromParcel(Parcel parcel) {
            return new BusStop(parcel);
        }

        @Override
        public BusStop[] newArray(int i) {
            return new BusStop[0];
        }
    };
    private String mBusStopCode;
    private String mRoadName;
    private String mDescription;
    private Integer distance;
    private double mLatitude;
    private double mLongitude;
    private boolean isFavourite;
    private boolean isAware;

    public BusStop(String code, String roadName, String desc,
                   double latitude, double longitude) {
        mBusStopCode = code;
        mRoadName = roadName;
        mDescription = desc;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    private BusStop(Parcel parcel) {
        mBusStopCode = parcel.readString();
        mRoadName = parcel.readString();
        mDescription = parcel.readString();
        mLatitude = parcel.readDouble();
        mLongitude = parcel.readDouble();
        isFavourite = parcel.readInt() == 1;
        isAware = parcel.readInt() == 1;
    }

    public String getBusStopCode() {
        return mBusStopCode;
    }

    public String getRoadName() {
        return mRoadName;
    }

    public String getDescription() {
        return mDescription;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public String getBody() {
        return mDescription + " (" + mBusStopCode + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mBusStopCode);
        parcel.writeString(mRoadName);
        parcel.writeString(mDescription);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mLongitude);
        parcel.writeInt(isFavourite ? 1 : 0);
        parcel.writeInt(isAware ? 1 : 0);
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public boolean isAware() {
        return isAware;
    }

    public void setAware(boolean aware) {
        isAware = aware;
    }
}
