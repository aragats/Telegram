package ru.aragats.wgo.rest.service;

import retrofit.Call;
import retrofit.http.GET;
import ru.aragats.wgo.dto.PostResponse;
import ru.aragats.wgo.dto.Request;

/**
 * Created by aragats on 05/12/15.
 */
public interface WGOService {
    // sync
    @GET("/api/posts/all")
    Call<PostResponse> all();

    // async
//    @GET("/api/posts/all")
//    Call<PostResponse> allAsync(Callback<PostResponse> callback);
//    Call<PostResponse> allAsync(@Body Request request, Callback<PostResponse> callback);
}