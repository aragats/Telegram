/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.android;

import ru.aragats.wgo.ApplicationLoader;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationCenter {


    private static int totalEvents = 1;

    public static final int switchToVKNewsFeedMode = totalEvents++;
    public static final int switchToVKPhotoMode = totalEvents++;
    public static final int switchToOfflineMode = totalEvents++;
    public static final int switchToOnlineMode = totalEvents++;
    public static final int stopRefreshingView = totalEvents++;
    public static final int venuesNeedReload = totalEvents++;
    public static final int postsNeedReload = totalEvents++;
    public static final int postRequestFinished = totalEvents++;
    public static final int undefinedLocation = totalEvents++;
    public static final int locationServiceDisabled = totalEvents++;
    public static final int newPostSaved = totalEvents++;
    public static final int postsRefresh = totalEvents++;
    public static final int offlinePostsLoaded = totalEvents++;

    public static final int savePostError = totalEvents++;
    //    public static final int loadPostsError = totalEvents++;
    public static final int invalidPost = totalEvents++;

    public static final int locationPermissionGranted = totalEvents++;
    public static final int storagePermissionGranted = totalEvents++;
    public static final int cameraPermissionGranted = totalEvents++;


    public static final int didReceivedNewPosts = totalEvents++; //TODO
    public static final int updateInterfaces = totalEvents++; //TODO
    public static final int closeChats = totalEvents++; //TODO important. force selfClose other activities when open some particular.
    public static final int hideEmojiKeyboard = totalEvents++;
    public static final int screenStateChanged = totalEvents++;
    public static final int didReplacedPhotoInMemCache = totalEvents++;
    public static final int closeOtherAppActivities = totalEvents++;
    public static final int didUpdatedConnectionState = totalEvents++; // TODO
    public static final int emojiDidLoaded = totalEvents++;
    public static final int FileLoadProgressChanged = totalEvents++; //TODO
    public static final int FileDidLoaded = totalEvents++;
    public static final int FileDidFailedLoad = totalEvents++;
    public static final int albumsDidLoaded = totalEvents++; //TODO
    public static final int cameraAlbumDidLoaded = totalEvents++; //TODO


    public static final int httpFileDidLoaded = totalEvents++; // TODO need receiver
    public static final int httpFileDidFailedLoad = totalEvents++; // TODO need receiver

    private HashMap<Integer, ArrayList<Object>> observers = new HashMap<>();
    private HashMap<Integer, Object> removeAfterBroadcast = new HashMap<>();
    private HashMap<Integer, Object> addAfterBroadcast = new HashMap<>();
    private ArrayList<DelayedPost> delayedPosts = new ArrayList<>(10);

    private int broadcasting = 0;
    private boolean animationInProgress;

    public interface NotificationCenterDelegate {
        void didReceivedNotification(int id, Object... args);
    }

    private class DelayedPost {

        private DelayedPost(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        private int id;
        private Object[] args;
    }

    private static volatile NotificationCenter Instance = null;

    public static NotificationCenter getInstance() {
        NotificationCenter localInstance = Instance;
        if (localInstance == null) {
            synchronized (NotificationCenter.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new NotificationCenter();
                }
            }
        }
        return localInstance;
    }

    public void setAnimationInProgress(boolean value) {
        animationInProgress = value;
        if (!animationInProgress && !delayedPosts.isEmpty()) {
            for (DelayedPost delayedPost : delayedPosts) {
                postNotificationNameInternal(delayedPost.id, true, delayedPost.args);
            }
            delayedPosts.clear();
        }
    }

    public void postNotificationName(int id, Object... args) {
        boolean allowDuringAnimation = false;
        if (id == closeChats) {
            allowDuringAnimation = true;
        }
        postNotificationNameInternal(id, allowDuringAnimation, args);
    }

    public void postNotificationNameInternal(int id, boolean allowDuringAnimation, Object... args) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("postNotificationName allowed only from MAIN thread");
            }
        }
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPost delayedPost = new DelayedPost(id, args);
            delayedPosts.add(delayedPost);
            if (BuildVars.DEBUG_VERSION) {
                FileLog.e("tmessages", "delay post notification " + id + " with args count = " + args.length);
            }
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            for (Object obj : objects) {
                ((NotificationCenterDelegate) obj).didReceivedNotification(id, args);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            if (!removeAfterBroadcast.isEmpty()) {
                for (HashMap.Entry<Integer, Object> entry : removeAfterBroadcast.entrySet()) {
                    removeObserver(entry.getValue(), entry.getKey());
                }
                removeAfterBroadcast.clear();
            }
            if (!addAfterBroadcast.isEmpty()) {
                for (HashMap.Entry<Integer, Object> entry : addAfterBroadcast.entrySet()) {
                    addObserver(entry.getValue(), entry.getKey());
                }
                addAfterBroadcast.clear();
            }
        }
    }

    public void addObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("addObserver allowed only from MAIN thread");
            }
        }
        if (broadcasting != 0) {
            addAfterBroadcast.put(id, observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects == null) {
            observers.put(id, (objects = new ArrayList<>()));
        }
        if (objects.contains(observer)) {
            return;
        }
        objects.add(observer);
    }

    public void removeObserver(Object observer, int id) {
        if (BuildVars.DEBUG_VERSION) {
            if (Thread.currentThread() != ApplicationLoader.applicationHandler.getLooper().getThread()) {
                throw new RuntimeException("removeObserver allowed only from MAIN thread");
            }
        }
        if (broadcasting != 0) {
            removeAfterBroadcast.put(id, observer);
            return;
        }
        ArrayList<Object> objects = observers.get(id);
        if (objects != null) {
            objects.remove(observer);
            if (objects.size() == 0) {
                observers.remove(id);
            }
        }
    }
}
