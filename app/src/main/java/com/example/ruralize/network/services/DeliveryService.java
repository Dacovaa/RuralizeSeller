package com.example.ruralize.network.services;

import com.example.ruralize.Entrega;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import java.util.List;
import java.util.Map;

public interface DeliveryService {
    @GET("deliveries/{uid}")
    Call<List<Entrega>> getDeliveries(@Path("uid") String uid);

    @PATCH("deliveries/{uid}/{deliveryId}")
    Call<ResponseBody> updateDeliveryStatus(@Path("uid") String uid, @Path("deliveryId") String deliveryId, @Body Map<String, Object> statusData);
}
