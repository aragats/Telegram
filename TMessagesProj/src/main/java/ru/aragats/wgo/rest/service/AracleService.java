package ru.aragats.wgo.rest.service;

import retrofit2.Call;
import retrofit2.http.GET;
import ru.aragats.wgo.dto.PostResponse;

/**
 * Created by aragats on 05/12/15.
 */
public interface AracleService {
    // sync
    @GET("/api/posts/all")
    Call<PostResponse> all();

    // async
//    @GET("/api/posts/all")
//    Call<PostResponse> allAsync(Callback<PostResponse> callback);
//    Call<PostResponse> allAsync(@Body Request request, Callback<PostResponse> callback);
}