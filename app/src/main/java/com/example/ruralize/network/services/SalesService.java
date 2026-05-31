package com.example.ruralize.network.services;

import com.example.ruralize.ResumoVendas;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SalesService {
    @GET("orders/totalVendas/{uid}")
    Call<ResumoVendas> getSalesSummary(@Path("uid") String uid);
}
