package ru.aragats.wgo.rest.service;

import com.squareup.okhttp.RequestBody;

import java.util.List;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.PostResponse;

/**
 * Created by aragats on 05/12/15.
 */
public interface RestService {


    @GET("/api/posts/find/near")
    Call<PostResponse> findNearPosts(@Query("lng") double longitude, @Query("lat") double latitude,
                                     @Query("distance") int distance, @Query("offset") String offset,
                                     @Query("count") int count);


    @GET("/api/posts/find/near/venue/{venueId}")
    Call<PostResponse> findPostsAtVenue(@Path(value = "venueId") String venueId,
                                        @Query("offset") String offset,
                                        @Query("count") int count);

    @POST("/api/posts/upload")
    Call<List<Image>> uploadImage(@Body RequestBody fileRequestBody);


    @POST("/api/posts/save")
    Call<String> savePost(@Body Post post);





    ////////////////////////////////////////////////////////////////////////////////////////////////

    @POST("/api/posts/addTest")
    Call<PostResponse> addTest(@Body Post post);


    //TODO both methods for uploading work.
//    @Multipart
//    @POST("/api/posts/upload")
//    Call<PostResponse> uploadTest(@Part("myfile\"; filename=\"image.png\" ") RequestBody file, @Part("description") String description);


}
