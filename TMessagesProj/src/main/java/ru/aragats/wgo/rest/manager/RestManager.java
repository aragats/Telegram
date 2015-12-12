package ru.aragats.wgo.rest.manager;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import ru.aragats.wgo.rest.client.RestClient;
import ru.aragats.wgo.rest.dto.Image;
import ru.aragats.wgo.rest.dto.Post;
import ru.aragats.wgo.rest.dto.PostRequest;
import ru.aragats.wgo.rest.dto.PostResponse;
import ru.aragats.wgo.rest.dto.VenuePostsRequest;

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


    public void findNearPosts(PostRequest request, Callback<PostResponse> callback) {
        findNearPostsCall(request).enqueue(callback);
    }

    private Call<PostResponse> findNearPostsCall(PostRequest request) {
        return restClient.getRestService().findNearPosts(request.getLng(), request.getLat(),
                request.getDistance(), request.getOffset(), request.getCount());
    }


    public void uploadImage(PostRequest request, Callback<List<Image>> callback) {
        uploadImage(request).enqueue(callback);
    }

    private Call<List<Image>> uploadImage(PostRequest request) {
//        File file = new File("/storage/emulated/0/download/1289.jpeg");
        File file = new File("/storage/emulated/0/Download/amerIstotia1.jpg");
//        File file = new File(request.getFilePath());


        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
//        MultipartBuilder multipartBuilder = new MultipartBuilder("95416089-b2fd-4eab-9a14-166bb9c5788b");
        MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.addFormDataPart("file", file.getName(), fileBody);
        multipartBuilder.addFormDataPart("description", "value");
        multipartBuilder.type(MultipartBuilder.FORM);
        RequestBody fileRequestBody = multipartBuilder.build();

        return restClient.getRestService().uploadImage(fileRequestBody);
    }


    // findPostsAtVenue
    public void findPostsAtVenue(VenuePostsRequest request, Callback<PostResponse> callback) {
        findPostsAtVenueCall(request).enqueue(callback);
    }

    private Call<PostResponse> findPostsAtVenueCall(VenuePostsRequest request) {
        return restClient.getRestService().findPostsAtVenue(request.getVenueId(), request.getOffset(), request.getCount());
    }

    // addPost
    public void addPost(Post post, Callback<String> callback) {
        addPostCall(post).enqueue(callback);
    }

    private Call<String> addPostCall(Post post) {
        return restClient.getRestService().addPost(post);
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


    public void addTest(PostRequest request, Callback<PostResponse> callback) {
        addTest(request).enqueue(callback);
    }

    public Call<PostResponse> addTest(PostRequest request) {
        return restClient.getRestService().addTest(new Post());
    }


}
