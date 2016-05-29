package ru.aragats.wgo.rest.manager;

import org.telegram.utils.Constants;
import org.telegram.utils.StringUtils;

import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import ru.aragats.wgo.dto.FileUploadRequest;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.KeyValue;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.PostRequest;
import ru.aragats.wgo.dto.PostResponse;
import ru.aragats.wgo.dto.VenuePostsRequest;
import ru.aragats.wgo.dto.vk.newsfeed.VKNewsFeedResponse;
import ru.aragats.wgo.dto.vk.photos.VKPhotoResponse;
import ru.aragats.wgo.rest.client.RestClient;
import ru.aragats.wgo.rest.client.VKRestClient;

/**
 * Created by aragats on 05/12/15.
 */
public class RestManager {
    private static volatile RestManager Instance = null;
    private static volatile RestClient restClient;
    private static volatile VKRestClient vkRestClient;

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
        vkRestClient = VKRestClient.getInstance();
    }


    // TODO tem try it.
    public Call<PostResponse> findNearPosts(PostRequest request, Callback<PostResponse> callback) {
        Call<PostResponse> call = findNearPostsCall(request);
        call.enqueue(callback);
        return call;
    }

    private Call<PostResponse> findNearPostsCall(PostRequest request) {
        return restClient.getRestService().findNearPosts(request.getLongitude(), request.getLatitude(),
                request.getDistance(), request.getIdOffset(), request.getCount());
    }


    public Call<List<Image>> uploadImage(FileUploadRequest request, Callback<List<Image>> callback) {
        Call<List<Image>> call = uploadImage(request);
        call.enqueue(callback);
        return call;
    }

    private Call<List<Image>> uploadImage(FileUploadRequest request) {
        // TODO Validate parameters. After saving could be that parameters will be cleaned.
        // TODO  java.lang.NullPointerException: Attempt to invoke virtual method 'char[] java.lang.String.toCharArray()' on a null object reference
        File file = new File(request.getFilePath());
        // was
//        RequestBody fileBody = RequestBody.create(MediaType.parse(request.getContentType()), file);
////        MultipartBuilder multipartBuilder = new MultipartBuilder("95416089-b2fd-4eab-9a14-166bb9c5788b");
//        MultipartBuilder multipartBuilder = new MultipartBuilder();
//        multipartBuilder.addFormDataPart("file", file.getName(), fileBody);
//        multipartBuilder.addFormDataPart("description", "value");
//        multipartBuilder.type(MultipartBuilder.FORM);
//        RequestBody fileRequestBody = multipartBuilder.build();
        //was

        //new 2.0.2 TODO test Retrofit 2.0.2
        RequestBody fileBody = RequestBody.create(MediaType.parse(request.getContentType()), file);
//        MultipartBuilder multipartBuilder = new MultipartBuilder("95416089-b2fd-4eab-9a14-166bb9c5788b");
        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        multipartBuilder.addFormDataPart("file", file.getName(), fileBody);
        multipartBuilder.addFormDataPart("description", "value");
        multipartBuilder.setType(MultipartBody.FORM);
        RequestBody fileRequestBody = multipartBuilder.build();

        return restClient.getRestService().uploadImage(fileRequestBody);
    }


    // findPostsAtVenue
    public Call<PostResponse> findPostsAtVenue(VenuePostsRequest request, Callback<PostResponse> callback) {
        Call<PostResponse> call = findPostsAtVenueCall(request);
        call.enqueue(callback);
        return call;
    }

    private Call<PostResponse> findPostsAtVenueCall(VenuePostsRequest request) {
        return restClient.getRestService().findPostsAtVenue(request.getVenueId(), request.getIdOffset(), request.getCount());
    }

    // savePost
    public Call<KeyValue> savePost(Post post, Callback<KeyValue> callback) {
        Call<KeyValue> call = savePostCall(post);
        call.enqueue(callback);
        return call;
    }

    private Call<KeyValue> savePostCall(Post post) {
        return restClient.getRestService().savePost(post);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    //
    // // BackUP
//    private Call<List<Image>> uploadImage(PostRequest request) {
////        File file = new File("/storage/emulated/0/download/1289.jpeg");
//        File file = new File("/storage/emulated/0/Download/amerIstotia1.jpg");
////        File file = new File(request.getFilePath());
//
//
//        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
////        MultipartBuilder multipartBuilder = new MultipartBuilder("95416089-b2fd-4eab-9a14-166bb9c5788b");
//        MultipartBuilder multipartBuilder = new MultipartBuilder();
//        multipartBuilder.addFormDataPart("file", file.getName(), fileBody);
//        multipartBuilder.addFormDataPart("description", "value");
//        multipartBuilder.type(MultipartBuilder.FORM);
//        RequestBody fileRequestBody = multipartBuilder.build();
//
//        return restClient.getRestService().uploadImage(fileRequestBody);
//    }
//
//
    public void addTest(PostRequest request, Callback<PostResponse> callback) {
        addTest(request).enqueue(callback);
    }

    public Call<PostResponse> addTest(PostRequest request) {
        return restClient.getRestService().addTest(new Post());
    }


//    http://192.168.0.100:8080/api/posts/find/near?lng=11.22&lat=23.15&distance=1000&offset=sad&count=20
//    http://192.168.0.100:8080/api/posts/find/near?lng=13.0116908&lat=52.3898987&distance=1000&count=20.0&offset=sds


    public Call<VKPhotoResponse> findNearVKPhotos(PostRequest request, Callback<VKPhotoResponse> callback) {
        Call<VKPhotoResponse> call = findNearVKPhotos(request);
        call.enqueue(callback);
        return call;
    }

    private Call<VKPhotoResponse> findNearVKPhotos(PostRequest request) {
        return vkRestClient.getRestService().findNearPhotos(request.getLongitude(), request.getLatitude(),
                request.getDistance(), request.getOffset(), request.getCount(), Constants.VK_API_VERSION);
    }


    public Call<VKNewsFeedResponse> findNearVKNewsFeed(PostRequest request, Callback<VKNewsFeedResponse> callback) {
        Call<VKNewsFeedResponse> call = findNearVKPNewsFeed(request);
        call.enqueue(callback);
        return call;
    }

    private Call<VKNewsFeedResponse> findNearVKPNewsFeed(PostRequest request) {
        if (StringUtils.isEmpty(request.getIdOffset())) {
            return vkRestClient.getRestService().findNearNewsFeedWithoutStartFrom(" ", request.getLongitude(), request.getLatitude(), request.getCount(), Constants.VK_API_VERSION);
        } else {
            return vkRestClient.getRestService().findNearNewsFeed(" ", request.getLongitude(), request.getLatitude(), request.getIdOffset(), request.getCount(), Constants.VK_API_VERSION);
        }
    }
}
