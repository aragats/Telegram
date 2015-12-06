package ru.aragats.wgo.rest.manager;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.File;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.aragats.wgo.rest.client.RestClient;
import ru.aragats.wgo.rest.dto.Post;
import ru.aragats.wgo.rest.dto.PostRequest;
import ru.aragats.wgo.rest.dto.PostResponse;

/**
 * Created by aragats on 05/12/15.
 */
public class RestManager {
    private static volatile RestManager Instance = null;
    private static volatile RestClient restClient;

    public static RestManager getInstance() {
        RestManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (RestManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new RestManager();
                }
            }
        }
        return localInstance;
    }


    private RestManager() {
        restClient = RestClient.getInstance();
    }


    public void findPosts(PostRequest request, Callback<PostResponse> callback) {
        findPostsCall(request).enqueue(callback);
    }

    public Call<PostResponse> findPostsCall(PostRequest request) {
        return restClient.getRestService().find("sd");
    }


    public void addTest(PostRequest request, Callback<PostResponse> callback) {
        addTest(request).enqueue(callback);
    }

    public Call<PostResponse> addTest(PostRequest request) {
        return restClient.getRestService().addTest(new Post(1, "test"));
    }


    public void uploadTest(PostRequest request, Callback<PostResponse> callback) {
        uploadTest(request).enqueue(callback);
    }

    public Call<PostResponse> uploadTest(PostRequest request) {
//        File file = new File("/storage/emulated/0/download/1289.jpeg");
        File file = new File("/storage/emulated/0/Download/amerIstotia1.jpg");
//        File file = new File(request.getFilePath());


        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
//        MultipartBuilder multipartBuilder = new MultipartBuilder("95416089-b2fd-4eab-9a14-166bb9c5788b");
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.addFormDataPart("myfile", file.getName(), fileBody);
        multipartBuilder.addFormDataPart("description", "value");
        multipartBuilder.type(MultipartBuilder.FORM);
        RequestBody fileRequestBody = multipartBuilder.build();

        return restClient.getRestService().uploadTest(fileRequestBody);
    }


//    public Call<PostResponse> uploadTest1(PostRequest request) {
//        File file = new File("/storage/emulated/0/Download/amerIstotia1.jpg");
////        File file = new File(request.getFilePath());
////
////        // please check you mime type, i'm uploading only images
//        RequestBody requestBody =
//                RequestBody.create(MediaType.parse("image/jpeg"), file);
//
//
//        return restClient.getRestService().uploadTest(requestBody, "key-value");
//    }

}
