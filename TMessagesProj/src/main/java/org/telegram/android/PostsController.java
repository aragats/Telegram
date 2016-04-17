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
import org.telegram.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.comparator.PostDateComparator;
import ru.aragats.wgo.comparator.PostDistanceComparator;
import ru.aragats.wgo.converter.vk.VKPhotoResponseToPostListConverter;
import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.FileUploadRequest;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;
import ru.aragats.wgo.dto.PostRequest;
import ru.aragats.wgo.dto.PostResponse;
import ru.aragats.wgo.dto.User;
import ru.aragats.wgo.dto.Venue;
import ru.aragats.wgo.dto.vk.VKPhotoResponse;
import ru.aragats.wgo.rest.manager.RestManager;
import ru.aragats.wgo.rest.mock.PostServiceMock;
import ru.aragats.wgo.rest.mock.UserServiceMock;

//import org.telegram.messenger.TLRPC;

// TODO-aragats
//TODO Look at MessagesController methods. There are many good examples and best practice.
public class PostsController implements NotificationCenter.NotificationCenterDelegate {

    private VKPhotoResponseToPostListConverter vkPhotoResponseConverter = new VKPhotoResponseToPostListConverter();
    private Venue lastVenue;

    private List<Post> posts = new ArrayList<>();
    private int nextOffset;
    public ConcurrentHashMap<String, Post> postsMap = new ConcurrentHashMap<>(100, 1.0f, 2);

    private boolean loadingPosts = false;


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
                post.setImages(response.body()); // TODO chek whether images are not empty
                savePost(post);
            }

            @Override
            public void onFailure(Call<List<Image>> call, Throwable t) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.savePostError);
            }
        });
    }


    private void savePost(final Post post) {
        RestManager.getInstance().savePost(post, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                post.setId(response.body());
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.newPostSaved);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
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


    public void loadPosts(final String idOffset, final int offset, final int count, final boolean reload, final boolean offlineMode) {
        if (loadingPosts || offlineMode && MediaController.getInstance().getRTree() == null) {
            return;
        }
        //TODO rethink this.
//        if (offlineMode && MediaController.getRTree() == null) {
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
        if (offlineMode) {
            nextOffset = 0;
            loadLocalPosts(postRequest, reload);
        } else {
//            loadPostFromServer(postRequest, reload);
            loadVKPhotos(postRequest, reload);
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
        RestManager.getInstance().findNearPosts(postRequest, new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                //        after getting response.
                processLoadedPosts(response.body(), reload);
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
//                loadingPosts = false; // TODO false or true ??? if continue then true otherwise false. False if finish. true if goes to VK
//                NotificationCenter.getInstance().postNotificationName(NotificationCenter.loadPostsError);

                loadVKPhotos(postRequest, reload);
            }

        });
    }


    private void loadVKPhotos(final PostRequest postRequest, final boolean reload) {
        loadingPosts = true;
        RestManager.getInstance().findNearVKPhotos(postRequest, new Callback<VKPhotoResponse>() {
            @Override
            public void onResponse(Call<VKPhotoResponse> call, Response<VKPhotoResponse> response) {
                //        after getting response.
                PostResponse postResponse = new PostResponse();
                postResponse.setPosts(vkPhotoResponseConverter.convert(response.body() != null ?
                        response.body().getResponse() : null));
                if (postResponse.getPosts() == null) {
                    postResponse.setPosts(new ArrayList<Post>());
                }

                postResponse.setSource("VK");
                nextOffset = postRequest.getOffset() + postRequest.getCount();
                processLoadedPosts(postResponse, reload);
            }

            @Override
            public void onFailure(Call<VKPhotoResponse> call, Throwable t) {
                loadingPosts = false;
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.loadPostsError);
            }
        });
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

    public Post createPost(String dir, String photo, double latitude, double longitude, Date date) {
        Post post = new Post();
        post.setId(photo);
        Coordinates coordinates = new Coordinates();
        coordinates.setType("Point");
        coordinates.setCoordinates(Arrays.asList(longitude, latitude));
        post.setCoordinates(coordinates);
        post.setText("");
        post.setCreatedDate(date.getTime());
        Venue venue = new Venue();
        venue.setCoordinates(coordinates);
        venue.setName("Local");
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
