
package com.abellimz.sgbusbuzz.models;

import com.google.gson.annotations.SerializedName;

public class Bus {

    private String minsToArrival;
    @SerializedName("EstimatedArrival")
    private String estimatedArrival;
    @SerializedName("Latitude")
    private String latitude;
    @SerializedName("Longitude")
    private String longitude;
    @SerializedName("VisitNumber")
    private String visitNumber;
    @SerializedName("Load")
    private String load;
    @SerializedName("Feature")
    private String feature;

    /**
     * @return The estimatedArrival
     */
    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    /**
     * @param estimatedArrival The EstimatedArrival
     */
    public void setEstimatedArrival(String estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    /**
     * @return The latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * @param latitude The Latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * @return The longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * @param longitude The Longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * @return The visitNumber
     */
    public String getVisitNumber() {
        return visitNumber;
    }

    /**
     * @param visitNumber The VisitNumber
     */
    public void setVisitNumber(String visitNumber) {
        this.visitNumber = visitNumber;
    }

    /**
     * @return The load
     */
    public String getLoad() {
        return load;
    }

    /**
     * @param load The Load
     */
    public void setLoad(String load) {
        this.load = load;
    }

    /**
     * @return The feature
     */
    public String getFeature() {
        return feature;
    }

    /**
     * @param feature The Feature
     */
    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getMinsToArrival() {
        return minsToArrival;
    }

    public void setMinsToArrival(String minsToArrival) {
        this.minsToArrival = minsToArrival;
    }
}
