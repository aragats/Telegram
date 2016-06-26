/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.telegram.android.PostsStorage;
import org.telegram.utils.StringUtils;

import ru.aragats.wgo.dto.User;
import ru.aragats.wgo.rest.mock.UserServiceMock;

import java.io.File;
import java.util.UUID;

import ru.aragats.aracle.ApplicationLoader;

public class UserConfig {

    private static boolean appActivated;
    private static User currentUser;
    public static int lastLocalId = -210000;
    private final static Object sync = new Object();
    public static int lastUpdateVersion;

    public static int getNewMessageId() {
        int id = 0;
//        synchronized (sync) {
//            id = lastSendMessageId;
//            lastSendMessageId--;
//        }
        return id;
    }

    public static void saveConfig(boolean withFile) {
        saveConfig(withFile, null);
    }

    public static void saveConfig(boolean withFile, File oldFile) {
        synchronized (sync) {
            try {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("lastLocalId", lastLocalId);
                editor.putInt("lastUpdateVersion", lastUpdateVersion);

                if (currentUser != null) {
                    if (withFile) {
                        SerializedData data = new SerializedData();
//                        currentUser.serializeToStream(data);
                        String userString = Base64.encodeToString(data.toByteArray(), Base64.DEFAULT);
                        editor.putString("user", userString);
                        data.cleanup();
                    }
                } else {
                    editor.remove("user");
                }

                editor.commit();
                if (oldFile != null) {
                    oldFile.delete();
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    //TODO activated user.
    public static boolean isClientActivated() {
        synchronized (sync) {
            return currentUser != null;
        }
    }

    public static boolean isAppActivated() {
        return appActivated;
    }

    public static void activateApp() {
        appActivated = true;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("appActivated", appActivated);
        editor.commit();
    }

    public static String getClientUserId() {
        synchronized (sync) {
            return currentUser != null ? currentUser.getId() : null;
        }
    }

    public static User getCurrentUser() {
        synchronized (sync) {
            return currentUser;
        }
    }

    public static void setCurrentUser(User user) {
        synchronized (sync) {
            currentUser = user;
        }
    }

    public static void loadConfigAragats() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);

        String userId = preferences.getString("userId", "");
        if (StringUtils.isEmpty(userId)) {
            userId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("userId", userId);
            editor.commit();
        }
        currentUser = new User();
        currentUser.setId(userId);

        appActivated = preferences.getBoolean("appActivated", false);


    }

    public static void loadConfig() {
        synchronized (sync) {
            //TODO which file I read.
            final File configFile = new File(ApplicationLoader.applicationContext.getFilesDir(), "user.dat");
            if (configFile.exists()) {
                try {
                    SerializedData data = new SerializedData(configFile);
                    int ver = data.readInt32(false);
                    if (ver == 1) {
                        int constructor = data.readInt32(false);
//                        currentUser = TLRPC.TL_userSelf.TLdeserialize(data, constructor, false);
                        currentUser = UserServiceMock.getRandomUser();
                        PostsStorage.lastDateValue = data.readInt32(false);
                        PostsStorage.lastPtsValue = data.readInt32(false);
                        PostsStorage.lastSeqValue = data.readInt32(false);
                        lastLocalId = data.readInt32(false);
                        PostsStorage.lastQtsValue = data.readInt32(false);
                        PostsStorage.lastSecretVersion = data.readInt32(false);
                        int val = data.readInt32(false);
                        if (val == 1) {
                            PostsStorage.secretPBytes = data.readByteArray(false);
                        }
                        PostsStorage.secretG = data.readInt32(false);
                        Utilities.stageQueue.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                saveConfig(true, configFile);
                            }
                        });
                    } else if (ver == 2) {
                        int constructor = data.readInt32(false);
//                        currentUser = TLRPC.TL_userSelf.TLdeserialize(data, constructor, false);
                        currentUser = UserServiceMock.getRandomUser();

                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
                        lastLocalId = preferences.getInt("lastLocalId", -210000);
                    }
                    if (lastLocalId > -210000) {
                        lastLocalId = -210000;
                    }
                    data.cleanup();
                    Utilities.stageQueue.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            saveConfig(true, configFile);
                        }
                    });
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            } else {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userconfing", Context.MODE_PRIVATE);
                lastLocalId = preferences.getInt("lastLocalId", -210000);
                lastUpdateVersion = preferences.getInt("lastUpdateVersion", 511);
                String user = preferences.getString("user", null);
                if (user != null) {
                    byte[] userBytes = Base64.decode(user, Base64.DEFAULT);
                    if (userBytes != null) {
                        SerializedData data = new SerializedData(userBytes);
//                        currentUser = TLRPC.TL_userSelf.TLdeserialize(data, data.readInt32(false), false);
                        currentUser = UserServiceMock.getRandomUser();
                        data.cleanup();
                    }
                }
            }
        }
    }


    public static void clearConfig() {
        currentUser = null;
        lastUpdateVersion = BuildVars.BUILD_VERSION;
        saveConfig(true);
    }
}
