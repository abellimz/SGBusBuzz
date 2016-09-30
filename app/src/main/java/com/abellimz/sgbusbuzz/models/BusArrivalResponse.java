
package com.abellimz.sgbusbuzz.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BusArrivalResponse {

    @SerializedName("odata.metadata")
    private String odataMetadata;
    @SerializedName("BusStopID")
    private String busStopID;
    @SerializedName("Services")
    private List<Service> services = new ArrayList<Service>();

    /**
     * @return The odataMetadata
     */
    public String getOdataMetadata() {
        return odataMetadata;
    }

    /**
     * @param odataMetadata The odata.metadata
     */
    public void setOdataMetadata(String odataMetadata) {
        this.odataMetadata = odataMetadata;
    }

    /**
     * @return The busStopID
     */
    public String getBusStopID() {
        return busStopID;
    }

    /**
     * @param busStopID The BusStopID
     */
    public void setBusStopID(String busStopID) {
        this.busStopID = busStopID;
    }

    /**
     * @return The services
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * @param services The Services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }

}
