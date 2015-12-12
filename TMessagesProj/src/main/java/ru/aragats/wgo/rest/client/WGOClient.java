package ru.aragats.wgo.rest.client;

import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import ru.aragats.wgo.rest.dto.PostResponse;
import ru.aragats.wgo.rest.dto.Request;
import ru.aragats.wgo.rest.service.WGOService;

/**
 * Created by aragats on 05/12/15.
 */
public class WGOClient {

    public String method() {

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.100:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            WGOService service = retrofit.create(WGOService.class);

            Call<PostResponse> call = service.all();
            Response<PostResponse> reposResponse = null;

            reposResponse = call.execute();
            PostResponse postResponse = reposResponse.body();
            System.out.println("Error =" + postResponse.getError() + " Posts = " + postResponse.getPosts());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "ome str";

    }


    public String asyncMethod() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.100:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WGOService service = retrofit.create(WGOService.class);

        Call<PostResponse> call = service.all();

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Response<PostResponse> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    // tasks available
                    System.out.println("Error =" + response.body().getError() + " Posts = " + response.body().getPosts());

                } else {
                    // error response, no access to resource?
                }
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Failure");
                // something went completely south (like no internet connection)
//                                 Log.d("Error", t.getMessage());
            }
        });


        return "some str";

    }

}
