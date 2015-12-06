package ru.aragats.wgo.rest.service;

import retrofit.Call;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import ru.aragats.wgo.rest.dto.PostResponse;
import ru.aragats.wgo.rest.dto.Request;

/**
 * Created by aragats on 05/12/15.
 */
//http://stackoverflow.com/questions/29323095/retrofit-call-inside-asynctask
public interface WGOService {
    // sync
    @GET("/api/posts/all")
    Call<PostResponse> all();

    // async
//    @GET("/api/posts/all")
//    Call<PostResponse> allAsync(Callback<PostResponse> callback);
//    Call<PostResponse> allAsync(@Body Request request, Callback<PostResponse> callback);
}