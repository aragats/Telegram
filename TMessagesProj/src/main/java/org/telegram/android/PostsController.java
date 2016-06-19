/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;

import org.telegram.android.location.LocationManagerHelper;
import org.telegram.utils.CollectionUtils;
import org.telegram.utils.Constants;
import org.telegram.utils.PostMode;
import org.telegram.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.aragats.aracle.ApplicationLoader;
import ru.aragats.wgo.comparator.PostDateComparator;
import ru.aragats.wgo.comparator.PostDistanceComparator;
import ru.aragats.wgo.converter.vk.newsfeed.ProfileToPostAssigner;
import ru.aragats.wgo.converter.vk.newsfeed.VKNewsFeedResponseToPostListConverter;
import ru.aragats.wgo.converter.vk.photos.VKPhotoResponseToPostListConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.FileUploadRequest;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.KeyValue;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.PostRequest;
import ru.aragats.wgo.dto.PostResponse;
import ru.aragats.wgo.dto.User;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.newsfeed.NewsFeedResponse;
import ru.aragats.wgo.dto.vk.newsfeed.VKNewsFeedResponse;
import ru.aragats.wgo.dto.vk.photos.VKPhotoResponse;
import ru.aragats.wgo.rest.manager.RestManager;
import ru.aragats.wgo.rest.mock.PostServiceMock;
import ru.aragats.wgo.rest.mock.UserServiceMock;

//import org.telegram.messenger.TLRPC;

// TODO-aragats
//TODO Look at MessagesController methods. There are many good examples and best practice.
public class PostsController implements NotificationCenter.NotificationCenterDelegate {

    private VKPhotoResponseToPostListConverter vkPhotoResponseConverter = new VKPhotoResponseToPostListConverter();
    private VKNewsFeedResponseToPostListConverter vkNewsFeedResponseToPostListConverter = new VKNewsFeedResponseToPostListConverter();
    private Venue lastVenue;

    private List<Post> posts = new ArrayList<>();
    private int nextOffset;
    private String nextFrom;
    public ConcurrentHashMap<String, Post> postsMap = new ConcurrentHashMap<>(100, 1.0f, 2);

    private boolean loadingPosts = false;

    private ProfileToPostAssigner profileToPostAssigner = new ProfileToPostAssigner();

    public int fontSize = AndroidUtilities.dp(16);


    public static final int UPDATE_MASK_NAME = 1;
    public static final int UPDATE_MASK_AVATAR = 2;
    public static final int UPDATE_MASK_STATUS = 4;
    public static final int UPDATE_MASK_CHAT_AVATAR = 8;
    public static final int UPDATE_MASK_CHAT_NAME = 16;
    public static final int UPDATE_MASK_CHAT_MEMBERS = 32;
    public static final int UPDATE_MASK_USER_PRINT = 64;
    public static final int UPDATE_MASK_USER_PHONE = 128;
    public static final int UPDATE_MASK_READ_DIALOG_MESSAGE = 256;
    public static final int UPDATE_MASK_SELECT_DIALOG = 512;
    public static final int UPDATE_MASK_PHONE = 1024;
    public static final int UPDATE_MASK_NEW_MESSAGE = 2048;
    public static final int UPDATE_MASK_SEND_STATE = 4096;
    public static final int UPDATE_MASK_ALL = UPDATE_MASK_AVATAR | UPDATE_MASK_STATUS | UPDATE_MASK_NAME | UPDATE_MASK_CHAT_AVATAR | UPDATE_MASK_CHAT_NAME | UPDATE_MASK_CHAT_MEMBERS | UPDATE_MASK_USER_PRINT | UPDATE_MASK_USER_PHONE | UPDATE_MASK_READ_DIALOG_MESSAGE | UPDATE_MASK_PHONE;


    /**
     * The set of all call currently being processed by this PostController. A Call
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    //TODO think about making CallQueue like it is in RequestQueue.
    private final Set<Call<?>> currentCalls = new HashSet<Call<?>>();


    private static volatile PostsController Instance = null;

    public static PostsController getInstance() {
        PostsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (PostsController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new PostsController();
                }
            }
        }
        return localInstance;
    }

    public PostsController() {
        ImageLoader.getInstance();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);

        preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);

        fontSize = preferences.getInt("fons_size", AndroidUtilities.isTablet() ? 18 : 16);
        String disabledFeaturesString = preferences.getString("disabledFeatures", null);

    }

    private void addCall(Call<?> call) {
        currentCalls.add(call);
    }

    //finish call
    private void removeCall(Call<?> call) {
        currentCalls.remove(call);
    }

    public void cancelAllCalls() {
        for (Call<?> call : currentCalls) {
            call.cancel();
        }
        loadingPosts = false;
    }

    private void cancelCall(Call<?> call) {
        boolean result = currentCalls.remove(call);
        if (result) {
            call.cancel();
        }
    }

    public void updateConfig() {

    }


    @Override
    public void didReceivedNotification(int id, Object... args) {
        //TODO perform some action when receive observing notification
    }

    public void cleanUp() {
        MediaController.getInstance().cleanup();

        posts.clear();

        loadingPosts = false;

    }


    public void addPost(final Post post) {


        MediaController.getInstance().saveBitmap(post.getImage());


        RestManager.getInstance().uploadImage(new FileUploadRequest(post.getImage().getUrl(), post.getImage().getType()), new Callback<List<Image>>() {
            @Override
            public void onResponse(Call<List<Image>> call, Response<List<Image>> response) {
                MediaController.getInstance().deleteFile(post.getImage().getUrl());
                if (response.body() == null) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.savePostError);
                    return;
                }
                post.setImages(response.body()); // TODO check whether images are not empty
                savePost(post);
            }

            @Override
            public void onFailure(Call<List<Image>> call, Throwable t) {
                MediaController.getInstance().deleteFile(post.getImage().getUrl());
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.savePostError);
            }
        });
    }


    private void savePost(final Post post) {
        RestManager.getInstance().savePost(post, new Callback<KeyValue>() {
            @Override
            public void onResponse(Call<KeyValue> call, Response<KeyValue> response) {
                KeyValue keyValue = response.body();
                if (keyValue == null || StringUtils.isEmpty(keyValue.getKey())) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.savePostError);
                    return;
                }
                post.setId(response.body().getKey());
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.newPostSaved);
            }

            @Override
            public void onFailure(Call<KeyValue> call, Throwable t) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.savePostError);
            }

        });
    }


    public void addPostMock(Post post) {

        //TODO temp test
//        RestManager.getInstance().uploadTest(new PostRequest(post.getImage().getUrl()), new Callback<ru.aragats.wgo.dto.PostResponse>() {
//            @Override
//            public void onResponse(Response<ru.aragats.wgo.dto.PostResponse> response, Retrofit retrofit) {
//                System.out.println(response);
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                System.out.println(t);
//            }
//        });

        PostServiceMock.addPost(post);
        //TODO mock loading
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.newPostSaved);
//                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.newPostSaved);
            }
        }, 2000);

//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.newPostSaved);


    }

    public void deletePost(final String did, int offset, final boolean onlyHistory) {
        // TODO Delete Post
    }


    public void loadPosts(final String idOffset, final String nextFromOffset, final int offset, final int count, final boolean reload, final PostMode mode) {
        if (loadingPosts || mode == null || (mode == PostMode.LOCAL && MediaController.getInstance().getRTree() == null)) {
            return;
        }
        //TODO rethink this.
//        if (mode && MediaController.getRTree() == null) {
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.stopRefreshingView);
//            return;
//        }
        loadingPosts = true;
        if (offset == 0) {
            nextOffset = offset;
        }
        Location location = LocationManagerHelper.getInstance().getLocation4TimeLine();
        if (location == null) {
            loadingPosts = false; // TODO
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
            return;
        }
        final PostRequest postRequest = new PostRequest();
        postRequest.setLatitude(location.getLatitude());
        postRequest.setLongitude(location.getLongitude());
        postRequest.setCount(count);
        postRequest.setIdOffset(idOffset);
        postRequest.setOffset(offset);
        postRequest.setDistance(Constants.RADIUS);
        if (!StringUtils.isEmpty(nextFromOffset)) {
            postRequest.setIdOffset(nextFromOffset);
        }
        switch (mode) {
            case LOCAL:
                nextOffset = 0;
                loadLocalPosts(postRequest, reload);
                break;
            case SERVER:
                loadPostFromServer(postRequest, reload);
                break;
            case VK_NEWS_FEED:
                loadVKNewsFeed(postRequest, reload);

                break;
            case VK_PHOTO:
                loadVKPhotos(postRequest, reload);
                break;
        }

    }

    private void loadLocalPosts(final PostRequest postRequest, final boolean reload) {
        List<Post> results = new ArrayList<>();
        RTree<Post, Geometry> rTree = MediaController.getInstance().getRTree();
        if (rTree == null) {
            return;
        }
        List<Entry<Post, Geometry>> entries = rTree.search(
                Geometries.point(postRequest.getLongitude(), postRequest.getLatitude()), Constants.MAX_DISTANCE_DEGREE)
                .toList().toBlocking().single();

//        Collections.sort(entries, new Comparator<Entry<Post, Geometry>>() {
//            @Override
//            public int compare(Entry<Post, Geometry> lhs, Entry<Post, Geometry> rhs) {
//                return rhs.value().;
//            }
//        });


        Location userLocation = LocationManagerHelper.getInstance().getLastSavedLocation();

        //TODO duplicate in PostCell. But not for case when load data from server.
        for (Entry<Post, Geometry> entry : entries) {
            Post post = entry.value();
            post.setLocal(true); // local post
            if (userLocation != null && post.getPostCoordinates() != null) {
                Coordinates coordinates = post.getPostCoordinates();
                Location location = new Location("network");
                location.setLongitude(coordinates.getCoordinates().get(0));
                location.setLatitude(coordinates.getCoordinates().get(1));
                float distance = location.distanceTo(userLocation);
                post.setDistance(distance);
            }
            results.add(post);
        }
        Collections.sort(results, new PostDistanceComparator());

        int start = postRequest.getOffset();
        int end = postRequest.getOffset() + postRequest.getCount();
        if (end > results.size()) {
            end = results.size();
        }
        if (!results.isEmpty()) {
            results = results.subList(start, end);
        }

        PostResponse postResponse = new PostResponse();
        postResponse.setPosts(results);
        processLoadedPosts(postResponse, reload);

    }


    private void loadPostFromServer(final PostRequest postRequest, final boolean reload) {
        nextOffset = 0;
        final Call<PostResponse> call = RestManager.getInstance().findNearPosts(postRequest, new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                removeCall(call);
                //        after getting response.
                processLoadedPosts(response.body(), reload);

            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                removeCall(call);
//                loadingPosts = false; // TODO false or true ??? if continue then true otherwise false. False if finish. true if goes to VK
//                boolean withError = true;
//                if (t != null && t.getMessage().equals("Cancelled")) {
//                    withError = false;
//                }
//                NotificationCenter.getInstance().postNotificationName(NotificationCenter.postRequestFinished, withError);

                loadVKPhotos(postRequest, reload);
            }

        });
        addCall(call);
    }


    //    Load original string
//    ResponseBody response
//    str = new String(response.body().bytes());
    private void loadVKNewsFeed(final PostRequest postRequest, final boolean reload) {
        loadingPosts = true;
        postRequest.setCount(postRequest.getCount() * 2);
        //round lat and longitude
        postRequest.setLatitude(Math.round(postRequest.getLatitude() * 10000000000.0) / 10000000000.0);
        postRequest.setLongitude(Math.round(postRequest.getLongitude() * 10000000000.0) / 10000000000.0);
        postRequest.setExtended(1);
        final Call<VKNewsFeedResponse> call = RestManager.getInstance().findNearVKNewsFeed(postRequest, new Callback<VKNewsFeedResponse>() {
            @Override
            public void onResponse(Call<VKNewsFeedResponse> call, Response<VKNewsFeedResponse> response) {
                removeCall(call);
                //        after getting response.
                VKNewsFeedResponse vkNewsFeedResponse = response.body();
                List<Post> posts = vkNewsFeedResponseToPostListConverter.convert(vkNewsFeedResponse != null ?
                        vkNewsFeedResponse.getResponse() : null);
                posts = filterVKPostsByLikes(posts);
                if (!CollectionUtils.isEmpty(posts)
                        && (vkNewsFeedResponse != null ? vkNewsFeedResponse.getResponse() : null) != null
                        && !CollectionUtils.isEmpty(vkNewsFeedResponse.getResponse().getProfiles())) {
                    profileToPostAssigner.assign(posts, vkNewsFeedResponse.getResponse().getProfiles());

                }

                PostResponse postResponse = new PostResponse();
                postResponse.setPosts(posts);

                if (postResponse.getPosts() == null) {
                    postResponse.setPosts(new ArrayList<Post>());
                }
                if (vkNewsFeedResponse != null) {
                    NewsFeedResponse newsFeedResponse = vkNewsFeedResponse.getResponse();
                    if (newsFeedResponse != null) {
                        String nextFrom = newsFeedResponse.getNextFrom();
                        postResponse.setNextFrom(nextFrom);
                    }
                }

                postResponse.setSource("VK");
                nextFrom = postResponse.getNextFrom();
                processLoadedPosts(postResponse, reload);
            }

            @Override
            public void onFailure(Call<VKNewsFeedResponse> call, Throwable t) {
//                call.request().url() GEt URL of request.
                removeCall(call);
                loadingPosts = false;
                boolean withError = true;
                if (t != null && t.getMessage().equals("Canceled")) {
                    withError = false;
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.postRequestFinished, withError);
            }
        });
        addCall(call);


        // it works and it forces onFailure java.io.IOException: Canceled
//        call.cancel(); //
    }


    private void loadVKPhotos(final PostRequest postRequest, final boolean reload) {
        loadingPosts = true;
        postRequest.setCount(postRequest.getCount() * 2);
        final Call<VKPhotoResponse> call = RestManager.getInstance().findNearVKPhotos(postRequest, new Callback<VKPhotoResponse>() {
            @Override
            public void onResponse(Call<VKPhotoResponse> call, Response<VKPhotoResponse> response) {
                removeCall(call);
                //        after getting response.
                List<Post> posts = vkPhotoResponseConverter.convert(response.body() != null ?
                        response.body().getResponse() : null);
//                posts = filterVKPosts(posts);
                PostResponse postResponse = new PostResponse();
                postResponse.setPosts(posts);

                if (postResponse.getPosts() == null) {
                    postResponse.setPosts(new ArrayList<Post>());
                }

                postResponse.setSource("VK");
                nextOffset = postRequest.getOffset() + postRequest.getCount();
                processLoadedPosts(postResponse, reload);
            }

            @Override
            public void onFailure(Call<VKPhotoResponse> call, Throwable t) {
                removeCall(call);
                loadingPosts = false;
                boolean withError = true;
                if (t != null && t.getMessage().equals("Canceled")) {
                    withError = false;
                }
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.postRequestFinished, withError);
            }
        });
        addCall(call);


        // it works and it forces onFailure java.io.IOException: Canceled
//        call.cancel(); //
    }

    private List<Post> filterVKPosts(List<Post> posts) {
        if (posts == null) {
            return null;
        }
        List<Post> result = new ArrayList<>();
        for (Post post : posts) {
            if (!StringUtils.isEmpty(post.getText())) {
                result.add(post);
            }
        }
        return result;
    }

    private List<Post> filterVKPostsByLikes(List<Post> posts) {
        if (posts == null) {
            return null;
        }
        List<Post> result = new ArrayList<>();
        for (Post post : posts) {
            if (post.getLikes() >= 10) {
                result.add(post);
            }
        }
        return result;
    }


    public void loadPostsMock(final int offset, final int count, boolean reload) {
        if (loadingPosts) {
            return;
        }
        loadingPosts = true;
        Location location = LocationManagerHelper.getInstance().getLocation4TimeLine();
        if (location == null) {
            loadingPosts = false;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
            return;
        }
        //TODO here async  request
        PostResponse postResponse = PostServiceMock.getPosts("location", null, offset, count);
//        after getting response.
        processLoadedPosts(postResponse, reload);

    }

    public void processLoadedPosts(PostResponse postResponse, boolean reload) {
        if (postResponse.getPosts() == null) {
            postResponse.setPosts(new ArrayList<Post>());
        }
        if (reload) {
            posts.clear();
            postsMap.clear();
        }
        posts.addAll(postResponse.getPosts());
        //TODO this exclude duplicated post from VK. because VK do not return specified count of posts.
        for (Post post : postResponse.getPosts()) {
            postsMap.putIfAbsent(post.getId(), post);
        }
        //TODO rethink ti. not the best solution for VK. VK do not return specified number of posts !! !
        if (postResponse.getSource() != null && postResponse.getSource().equals("VK")) {
            posts.clear();
            posts.addAll(postsMap.values());
            Collections.sort(posts, new PostDateComparator());
        }
        loadingPosts = false;

        //TODO recursive
//        if ((posts.size() < 3 || (posts.size() > 2 && postResponse.getPosts().isEmpty())) && !StringUtils.isEmpty(postResponse.getSource()) && postResponse.getSource().equals("VK")) {
//            loadPosts(null, nextOffset, Constants.POST_COUNT, false, false);
//        }

        //TODO notify Activity to run postsAdapter.notifyDataSetChanged();
        if (!postResponse.getPosts().isEmpty() || reload) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsNeedReload, reload);
        } else {
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsNeedReload);  //TODO hide progress view does not work !!!
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postRequestFinished);
        }


    }


    public void loadCurrentVenue(String loc) {
        Location location = LocationManagerHelper.getInstance().getLocation4TimeLine();
        if (location == null) {
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
        }
    }

    public boolean isLoadingPosts() {
        return loadingPosts;
    }

    public User getUser(String id) {
        return UserServiceMock.getDefaultUser();
    }

    public void setUser(User user) {
//        return UserServiceMock.getRandomUser();
    }

//    public TLRPC.User getUser(Integer id) {
//        return users.get(id);
//    }
//
//    public TLRPC.User getUser(String username) {
//        if (username == null || username.length() == 0) {
//            return null;
//        }
//        return usersByUsernames.get(username.toLowerCase());
//    }


    public Venue getLastVenue() {
        return lastVenue;
    }

    public void setLastVenue(Venue lastVenue) {
        this.lastVenue = lastVenue;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public int getOffset() {
        if (nextOffset > 0) {
            return nextOffset;
        }
        return posts.size();
    }

    public String getOffsetId() {
        String offset = null;
        if (!CollectionUtils.isEmpty(posts)) {
            offset = posts.get(posts.size() - 1).getId(); // TODO When empty list. java.lang.ArrayIndexOutOfBoundsException: length=12; index=-1
        }
        return offset;
    }

    public String getNextFrom() {
        return nextFrom;
    }

    public Post createPost(String dir, String photo, double latitude, double longitude, Date date, boolean local) {
        Post post = new Post();
        post.setLocal(local);
        post.setId(photo);
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Point");
        coordinates.setCoordinates(Arrays.asList(longitude, latitude));
        post.setCoordinates(coordinates);
        post.setText("");
        post.setCreatedDate(date.getTime());
        Venue venue = new Venue();
        venue.setCoordinates(coordinates);
        venue.setName("");
        venue.setAddress("");
        post.setVenue(venue);

        File file = new File(dir, photo);
        String photoUrl = dir + File.separator + photo;
        Image image = new Image();
        image.setUrl(photoUrl);
        image.setSize(file.length());

//        BitmapFactory.Options options = new BitmapFactory.Options();
        // TODO THIS Do not allow decode the file.
//            options.inJustDecodeBounds = true;

//Returns null, sizes are in the options variable
//        Bitmap bitmap = BitmapFactory.decodeFile(photoUrl, options);
//        int width = options.outWidth;
//        int height = options.outHeight;
//If you want, the MIME type will also be decoded (if possible)
//        String type = options.outMimeType;
//            String type = getMimeType(photoUrl


        image.setWidth(AndroidUtilities.getPhotoSize());
        image.setHeight(AndroidUtilities.getPhotoSize());
        post.setImages(Arrays.asList(image, image));
        return post;


    }
}
