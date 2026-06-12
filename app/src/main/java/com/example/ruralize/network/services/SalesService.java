package com.example.ruralize.network.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SalesService {
    @GET("orders/totalVendas/{uid}")
    Call<ResponseBody> getSalesSummary(@Path("uid") String uid);
}
