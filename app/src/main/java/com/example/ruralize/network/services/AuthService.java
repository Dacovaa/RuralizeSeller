package com.example.ruralize.network.services;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import java.util.Map;

public interface AuthService {
    @GET("auth/{uid}")
    Call<ResponseBody> getProfile(@Path("uid") String uid);

    @PATCH("auth/update")
    Call<ResponseBody> updateProfile(@Body Map<String, Object> profileData);

    @PATCH("auth/updatePassword")
    Call<ResponseBody> updatePassword(@Body Map<String, Object> passwordData);
}
