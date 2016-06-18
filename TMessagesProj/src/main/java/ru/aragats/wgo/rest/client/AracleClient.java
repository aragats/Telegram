package ru.aragats.wgo.rest.client;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.aragats.wgo.dto.PostResponse;
import ru.aragats.wgo.rest.service.AracleService;

/**
 * Created by aragats on 05/12/15.
 */
public class AracleClient {

    public String method() {

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.0.100:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            AracleService service = retrofit.create(AracleService.class);

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

        AracleService service = retrofit.create(AracleService.class);

        Call<PostResponse> call = service.all();

        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful()) {
                    // tasks available
                    System.out.println("Error =" + response.body().getError() + " Posts = " + response.body().getPosts());

                } else {
                    // error response, no access to resource?
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                System.out.println("Failure");
                // something went completely south (like no internet connection)
//                                 Log.d("Error", t.getMessage());
            }

        });


        return "some str";

    }

}
