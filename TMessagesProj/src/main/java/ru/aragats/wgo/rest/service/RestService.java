package ru.aragats.wgo.rest.service;

import com.squareup.okhttp.RequestBody;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Query;
import ru.aragats.wgo.rest.dto.Post;
import ru.aragats.wgo.rest.dto.PostResponse;

/**
 * Created by aragats on 05/12/15.
 */
public interface RestService {
    @GET("/api/posts/find")
    Call<PostResponse> find(@Query("sort") String sort);

    @POST("/api/posts/addTest")
    Call<PostResponse> addTest(@Body Post post);


    //TODO both methods for uploading work.
//    @Multipart
//    @POST("/api/posts/upload")
//    Call<PostResponse> uploadTest(@Part("myfile\"; filename=\"image.png\" ") RequestBody file, @Part("description") String description);

    @POST("/api/posts/upload")
    Call<PostResponse> uploadTest(@Body RequestBody myfile);



}
