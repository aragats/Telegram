package ru.aragats.wgo.rest.client;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import ru.aragats.wgo.rest.service.RestService;

/**
 * Created by aragats on 05/12/15.
 */
public class RestClient {
    private static final String BASE_URL = "http://192.168.0.100:8080/";
    private RestService restService;


    private static volatile RestClient Instance = null;

    public static RestClient getInstance() {
        RestClient localInstance = Instance;
        if (localInstance == null) {
            synchronized (RestClient.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new RestClient();
                }
            }
        }
        return localInstance;
    }


    private RestClient() {
//        Gson gson = new GsonBuilder()
//                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
//                .create();
//        restAdapter.setConverter(new GsonConverter(gson))
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restService = restAdapter.create(RestService.class);
    }

    public RestService getRestService() {
        return restService;
    }



}
