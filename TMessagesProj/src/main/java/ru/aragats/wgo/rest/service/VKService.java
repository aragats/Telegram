package ru.aragats.wgo.rest.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.aragats.wgo.dto.vk.newsfeed.VKNewsFeedResponse;
import ru.aragats.wgo.dto.vk.photos.VKPhotoResponse;

/**
 * Created by aragats on 05/12/15.
 */
public interface VKService {


    @GET("/method/photos.search")
    Call<VKPhotoResponse> findNearPhotos(@Query("long") double longitude, @Query("lat") double latitude,
                                         @Query("radius") int radius, @Query("offset") int offset, @Query("count") int count,
                                         @Query("v") double version);

    @GET("/method/newsfeed.search")
    Call<VKNewsFeedResponse> findNearNewsFeed(@Query("q") String query, @Query("longitude") double longitude, @Query("latitude") double latitude,
                                              @Query("start_from") String startFrom, @Query("count") int count,
                                              @Query("v") double version);

}
