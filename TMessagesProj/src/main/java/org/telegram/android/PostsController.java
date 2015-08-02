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

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.dto.Post;
import org.telegram.messenger.dto.PostResponse;
import org.telegram.messenger.dto.Venue;
import org.telegram.messenger.service.mock.PostServiceMock;
import org.telegram.messenger.service.mock.VenueServiceMock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

//import org.telegram.messenger.TLRPC;

// TODO-aragats
//TODO Look at MessagesController methods. There are many good examples and best practice.
public class PostsController implements NotificationCenter.NotificationCenterDelegate {

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
        MessagesStorage.getInstance();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailUpload);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
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
        NotificationsController.getInstance().cleanup();

        postsMap.clear();
        posts.clear();

        loadingPosts = false;

    }


    public void deletePost(final String did, int offset, final boolean onlyHistory) {
        // TODO Delete Post
    }


    public void loadPosts(final int offset, final int count, boolean reload, boolean fromCache) {
        if (loadingPosts) {
            return;
        }
        loadingPosts = true;
//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);

        //TODO here async  request
        PostResponse postResponse = PostServiceMock.getPosts("location", null, offset, count);
//        after getting response.
        processLoadedPosts(postResponse, offset, count, reload);

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


    public void loadCurrentVenue(String location) {
        this.currentVenue = VenueServiceMock.getRandomVenue();
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
}
