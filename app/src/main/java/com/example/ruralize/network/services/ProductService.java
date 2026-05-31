package com.example.ruralize.network.services;

import com.example.ruralize.Produto;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import java.util.List;

public interface ProductService {
    @GET("products/empresa/{uid}")
    Call<List<Produto>> getProducts(@Path("uid") String uid);

    @POST("products")
    Call<ResponseBody> createProduct(@Body Produto product);

    @PATCH("products/{empresaId}/{productId}")
    Call<ResponseBody> updateProduct(@Path("empresaId") String empresaId, @Path("productId") String productId, @Body Produto product);

    @DELETE("products/{uid}/{productId}")
    Call<ResponseBody> deleteProduct(@Path("uid") String uid, @Path("productId") String productId);

    @Multipart
    @POST("products/{uid}/{productId}/upload")
    Call<ResponseBody> uploadImage(@Path("uid") String uid, @Path("productId") String productId, @Part MultipartBody.Part file);
}
