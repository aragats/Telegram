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

import org.telegram.android.location.LocationManagerHelper;

import ru.aragats.wgo.ApplicationLoader;

import org.telegram.messenger.dto.Coordinates;
import org.telegram.messenger.dto.Post;
import org.telegram.messenger.dto.PostResponse;
import org.telegram.messenger.dto.User;
import org.telegram.messenger.dto.Venue;
import org.telegram.messenger.service.mock.PostServiceMock;
import org.telegram.messenger.service.mock.UserServiceMock;
import org.telegram.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//import org.telegram.messenger.TLRPC;

// TODO-aragats
//TODO Look at MessagesController methods. There are many good examples and best practice.
public class PostsController implements NotificationCenter.NotificationCenterDelegate {

    private Location currentLocation;
    public Venue currentVenue;

    public Post currentPost;


    public List<Post> posts = new ArrayList<>();
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

        postsMap.clear();
        posts.clear();

        loadingPosts = false;

    }


    public void addPost(Post post) {
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


    public void loadPosts(final int offset, final int count, boolean reload, boolean fromCache) {
        if (loadingPosts) {
            return;
        }
        loadingPosts = true;
        Location location = LocationManagerHelper.getInstance().getLastLocation();
        if (location == null) {
            loadingPosts = false;
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
            return;
        }
        currentLocation = location;
//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);

        //TODO here async  request
        PostResponse postResponse = PostServiceMock.getPosts("location", null, offset, count);
//        after getting response.
        processLoadedPosts(postResponse, offset, count, reload);

    }

    public Location getCurrentLocation() {
        return currentLocation;
    }


    public void processLoadedPosts(PostResponse postResponse, final int offset, final int count, boolean reload) {
        if (reload) {
            posts.clear();
            postsMap.clear();
        }
        posts.addAll(postResponse.getPosts());
        for (Post post : posts) {
            postsMap.putIfAbsent(post.getId(), post);
        }
        loadingPosts = false;
        //TODO notify Activity to run postsAdapter.notifyDataSetChanged();
        if (!postResponse.getPosts().isEmpty()) {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postsNeedReload);
        } else {
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.postRequestFinished);
        }


    }


    public void loadCurrentVenue(String loc) {
        Location location = LocationManagerHelper.getInstance().getLastLocation();
        if (location == null) {
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.undefinedLocation);
            return;
        }

        Venue venue = new Venue();
        Coordinates coordinates = new Coordinates();
        coordinates.setCoordinates(Arrays.asList(location.getLongitude(), location.getLatitude()));
        coordinates.setType("Point");
        venue.setCoordinates(coordinates);
        if (StringUtils.isEmpty(venue.getAddress())) {
            venue.setAddress(location.getLongitude() + ", " + location.getLatitude());
        }
//        this.currentVenue = VenueServiceMock.getRandomVenue();
        this.currentVenue = venue;
    }

    public Venue getCurrentVenue() {
        return currentVenue;
    }

    public void setCurrentVenue(Venue currentVenue) {
        this.currentVenue = currentVenue;
    }

    public Post getCurrentPost() {
        return currentPost;
    }

    public void setCurrentPost(Post currentPost) {
        this.currentPost = currentPost;
    }

    public boolean isLoadingPosts() {
        return loadingPosts;
    }

    public void setLoadingPosts(boolean loadingPosts) {
        this.loadingPosts = loadingPosts;
    }

    public User getUser(String id) {
        return UserServiceMock.getRandomUser();
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
}
