package ru.aragats.wgo.rest.client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.aragats.wgo.rest.service.VKService;

/**
 * Created by aragats on 05/12/15.
 */
public class VKRestClient {
    private static final String BASE_URL = "https://api.vk.com/";
    //    private static final String BASE_URL = "http://aragatss-macbook-pro.local:8080/";
    private VKService restService;


    private static volatile VKRestClient Instance = null;

    public static VKRestClient getInstance() {
        VKRestClient localInstance = Instance;
        if (localInstance == null) {
            synchronized (VKRestClient.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new VKRestClient();
                }
            }
        }
        return localInstance;
    }


    private VKRestClient() {
//        Gson gson = new GsonBuilder()
//                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
//                .create();
//        restAdapter.setConverter(new GsonConverter(gson))
        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        restService = restAdapter.create(VKService.class);
    }

    public VKService getRestService() {
        return restService;
    }


}
