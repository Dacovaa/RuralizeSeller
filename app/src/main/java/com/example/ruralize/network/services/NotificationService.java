package com.example.ruralize.network.services;

import com.example.ruralize.Notificacao;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import java.util.List;

public interface NotificationService {
    @GET("notifications/{uid}")
    Call<List<Notificacao>> getNotifications(@Path("uid") String uid);

    @PATCH("notifications/{uid}/{notificationId}/read")
    Call<ResponseBody> markRead(@Path("uid") String uid, @Path("notificationId") String notificationId);
}
