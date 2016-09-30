package com.abellimz.sgbusbuzz.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;

/**
 * Created by Abel on 9/19/2016.
 */

public class TimeApiClient {

    private static String TIME_API_BASE_URL = "http://www.timeapi.org/";

    public static Call<ResponseBody> getTimeApiCall() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TIME_API_BASE_URL)
                .build();
        return retrofit.create(TimeApiService.class).getTimeNow();
    }

    private interface TimeApiService {
        @GET("utc/now")
        Call<ResponseBody> getTimeNow();
    }
}
