/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class FileLoader {


    public static final int MEDIA_DIR_IMAGE = 0;
    public static final int MEDIA_DIR_AUDIO = 1;
    public static final int MEDIA_DIR_VIDEO = 2;
    public static final int MEDIA_DIR_DOCUMENT = 3;
    public static final int MEDIA_DIR_CACHE = 4;

    private HashMap<Integer, File> mediaDirs = null;
    private volatile DispatchQueue fileLoaderQueue = new DispatchQueue("fileUploadQueue");


    private static volatile FileLoader Instance = null;

    public static FileLoader getInstance() {
        FileLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (FileLoader.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new FileLoader();
                }
            }
        }
        return localInstance;
    }

    public void setMediaDirs(HashMap<Integer, File> dirs) {
        mediaDirs = dirs;
    }

    public File getDirectory(int type) {
        File dir = mediaDirs.get(type);
        if (dir == null && type != MEDIA_DIR_CACHE) {
            dir = mediaDirs.get(MEDIA_DIR_CACHE);
        }
        try {
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            //don't promt
        }
        return dir;
    }


    public static File getPathToAttach(TLRPC.PhotoSize attach, boolean forceCache) {
        return getPathToAttach(attach, null, forceCache);
    }

    public static File getPathToAttach(TLRPC.PhotoSize attach, String ext, boolean forceCache) {
        File dir = null;
        if (forceCache) {
            dir = getInstance().getDirectory(MEDIA_DIR_CACHE);
        } else {
            if (attach.location == null || attach.location.key != null || attach.location.volume_id == Integer.MIN_VALUE && attach.location.local_id < 0) {
                dir = getInstance().getDirectory(MEDIA_DIR_CACHE);
            } else {
                dir = getInstance().getDirectory(MEDIA_DIR_IMAGE);
            }

        }
        if (dir == null) {
            return new File("");
        }
        return new File(dir, getAttachFileName(attach, ext));
    }

    public static String getAttachFileName(TLRPC.PhotoSize attach) {
        return getAttachFileName(attach, null);
    }

    public static String getAttachFileName(TLRPC.PhotoSize attach, String ext) {
        if (attach.location == null) {
            return "";
        }
        return attach.location.volume_id + "_" + attach.location.local_id + "." + (ext != null ? ext : "jpg");
    }

    public void deleteFiles(final ArrayList<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        fileLoaderQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (File file : files) {
                    if (file.exists()) {
                        try {
                            if (!file.delete()) {
                                file.deleteOnExit();
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
            }
        });
    }
}
