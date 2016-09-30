package com.abellimz.sgbusbuzz.network;

import com.abellimz.sgbusbuzz.models.BusArrivalResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.QueryMap;

/**
 * Created by Abel on 9/16/2016.
 */

public class LtaDataApiClient {
    private static final String USER_ID = "ecf41e62-d342-4aa3-ae2e-b463fe93ef6f";
    private static final String ACCOUNT_KEY = "k4YQ6hloTmizoxUCGcW7Jw==";

    private static final String API_BASE_URL = "http://datamall2.mytransport.sg/ltaodataservice/";
    private static final String BUS_ARRIVAL_URL_DIR = "BusArrival";
    private static final String BUS_STOP_ID_KEY = "BusStopID";
    private static final String SST_KEY = "SST";
    private static final String shouldReturnSST = "True";

    public static Call<BusArrivalResponse> buildBusArrivalCall(String busStopID) {
        Map<String, String> params = new HashMap<>();
        params.put(SST_KEY, shouldReturnSST);
        params.put(BUS_STOP_ID_KEY, busStopID);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BusArrivalService service = retrofit.create(BusArrivalService.class);
        return service.getBusArrivals(params);
    }

    private interface BusArrivalService {
        @Headers({
                "AccountKey: " + ACCOUNT_KEY,
                "UniqueUserID: " + USER_ID
        })
        @GET(BUS_ARRIVAL_URL_DIR)
        Call<BusArrivalResponse> getBusArrivals(
                @QueryMap Map<String, String> params);
    }

}
