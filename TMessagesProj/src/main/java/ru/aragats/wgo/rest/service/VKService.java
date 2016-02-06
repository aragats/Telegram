package ru.aragats.wgo.rest.service;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;
import ru.aragats.wgo.dto.vk.VKPhotoResponse;

/**
 * Created by aragats on 05/12/15.
 */
public interface VKService {


    @GET("/method/photos.search")
    Call<VKPhotoResponse> findNearPhotos(@Query("long") double longitude, @Query("lat") double latitude,
                                         @Query("radius") int radius, @Query("offset") int offset, @Query("count") int count,
                                         @Query("v") double version);


}
