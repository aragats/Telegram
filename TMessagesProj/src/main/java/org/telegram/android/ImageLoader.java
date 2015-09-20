/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.android;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import org.telegram.messenger.DispatchQueue;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.ApplicationLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class ImageLoader {

    private HashMap<String, Integer> bitmapUseCounts = new HashMap<>();
    private LruCache memCache;
    private HashMap<String, CacheImage> imageLoadingByUrl = new HashMap<>();
    private HashMap<String, CacheImage> imageLoadingByKeys = new HashMap<>();
    private HashMap<Integer, CacheImage> imageLoadingByTag = new HashMap<>();
    private HashMap<Integer, String> waitingForQualityThumbByTag = new HashMap<>();
    private LinkedList<HttpImageTask> httpTasks = new LinkedList<>();
    private DispatchQueue cacheOutQueue = new DispatchQueue("cacheOutQueue");
    private DispatchQueue cacheThumbOutQueue = new DispatchQueue("cacheThumbOutQueue");
    private DispatchQueue imageLoadQueue = new DispatchQueue("imageLoadQueue");
    private ConcurrentHashMap<String, Float> fileProgresses = new ConcurrentHashMap<>();
    private static byte[] bytes;
    private static byte[] bytesThumb;
    private static byte[] header = new byte[12];
    private static byte[] headerThumb = new byte[12];
    private int currentHttpTasksCount = 0;

    private LinkedList<HttpFileTask> httpFileLoadTasks = new LinkedList<>();
    private HashMap<String, HttpFileTask> httpFileLoadTasksByKeys = new HashMap<>();
    private HashMap<String, Runnable> retryHttpsTasks = new HashMap<>();
    private int currentHttpFileLoadTasksCount = 0;

    protected VMRuntimeHack runtimeHack = null;
    private String ignoreRemoval = null;

    private volatile long lastCacheOutTime = 0;
    private int lastImageNum = 0;
    private long lastProgressUpdateTime = 0;

    private File telegramPath = null;


    private class HttpFileTask extends AsyncTask<Void, Void, Boolean> {

        private String url;
        private File tempFile;
        private String ext;
        private RandomAccessFile fileOutputStream = null;
        private boolean canRetry = true;

        public HttpFileTask(String url, File tempFile, String ext) {
            this.url = url;
            this.tempFile = tempFile;
            this.ext = ext;
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;

            URLConnection httpConnection = null;
            try {
                URL downloadUrl = new URL(url);
                httpConnection = downloadUrl.openConnection();
                httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                httpConnection.addRequestProperty("Referer", "google.com");
                httpConnection.setConnectTimeout(5000);
                httpConnection.setReadTimeout(5000);
                if (httpConnection instanceof HttpURLConnection) {
                    HttpURLConnection httpURLConnection = (HttpURLConnection) httpConnection;
                    httpURLConnection.setInstanceFollowRedirects(true);
                    int status = httpURLConnection.getResponseCode();
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        String newUrl = httpURLConnection.getHeaderField("Location");
                        String cookies = httpURLConnection.getHeaderField("Set-Cookie");
                        downloadUrl = new URL(newUrl);
                        httpConnection = downloadUrl.openConnection();
                        httpConnection.setRequestProperty("Cookie", cookies);
                        httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                        httpConnection.addRequestProperty("Referer", "google.com");
                    }
                }
                httpConnection.connect();
                httpConnectionStream = httpConnection.getInputStream();

                fileOutputStream = new RandomAccessFile(tempFile, "rws");
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }

            try {
                if (httpConnection != null && httpConnection instanceof HttpURLConnection) {
                    int code = ((HttpURLConnection) httpConnection).getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_ACCEPTED && code != HttpURLConnection.HTTP_NOT_MODIFIED) {
                        canRetry = false;
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }

            if (httpConnectionStream != null) {
                try {
                    byte[] data = new byte[1024 * 4];
                    while (true) {
                        if (isCancelled()) {
                            break;
                        }
                        try {
                            int read = httpConnectionStream.read(data);
                            if (read > 0) {
                                fileOutputStream.write(data, 0, read);
                            } else if (read == -1) {
                                done = true;
                                break;
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                            break;
                        }
                    }
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
            }

            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }

            try {
                if (httpConnectionStream != null) {
                    httpConnectionStream.close();
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }

            return done;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            runHttpFileLoadTasks(this, result ? 2 : 1);
        }

        @Override
        protected void onCancelled() {
            runHttpFileLoadTasks(this, 2);
        }
    }

    private class HttpImageTask extends AsyncTask<Void, Void, Boolean> {

        private CacheImage cacheImage = null;
        private RandomAccessFile fileOutputStream = null;
        private int imageSize;
        private long lastProgressTime;
        private boolean canRetry = true;
        private URLConnection httpConnection = null;

        public HttpImageTask(CacheImage cacheImage, int size) {
            this.cacheImage = cacheImage;
            imageSize = size;
        }

        private void reportProgress(final float progress) {
            long currentTime = System.currentTimeMillis();
            if (progress == 1 || lastProgressTime == 0 || lastProgressTime < currentTime - 500) {
                lastProgressTime = currentTime;
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fileProgresses.put(cacheImage.url, progress);
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileLoadProgressChanged, cacheImage.url, progress);
                            }
                        });
                    }
                });
            }
        }

        protected Boolean doInBackground(Void... voids) {
            InputStream httpConnectionStream = null;
            boolean done = false;

            if (!isCancelled()) {
                try {
                    URL downloadUrl = new URL(cacheImage.httpUrl);
                    httpConnection = downloadUrl.openConnection();
                    httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
                    httpConnection.addRequestProperty("Referer", "google.com");
                    httpConnection.setConnectTimeout(5000);
                    httpConnection.setReadTimeout(5000);
                    if (httpConnection instanceof HttpURLConnection) {
                        ((HttpURLConnection) httpConnection).setInstanceFollowRedirects(true);
                    }
                    if (!isCancelled()) {
                        httpConnection.connect();
                        httpConnectionStream = httpConnection.getInputStream();
                        fileOutputStream = new RandomAccessFile(cacheImage.tempFilePath, "rws");
                    }
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
            }

            if (!isCancelled()) {
                try {
                    if (httpConnection != null && httpConnection instanceof HttpURLConnection) {
                        int code = ((HttpURLConnection) httpConnection).getResponseCode();
                        if (code != HttpURLConnection.HTTP_OK && code != HttpURLConnection.HTTP_ACCEPTED && code != HttpURLConnection.HTTP_NOT_MODIFIED) {
                            canRetry = false;
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }

                if (httpConnectionStream != null) {
                    try {
                        byte[] data = new byte[1024 * 2];
                        int totalLoaded = 0;
                        while (true) {
                            if (isCancelled()) {
                                break;
                            }
                            try {
                                int read = httpConnectionStream.read(data);
                                if (read > 0) {
                                    totalLoaded += read;
                                    fileOutputStream.write(data, 0, read);
                                    if (imageSize != 0) {
                                        reportProgress(totalLoaded / (float) imageSize);
                                    }
                                } else if (read == -1) {
                                    done = true;
                                    if (imageSize != 0) {
                                        reportProgress(1.0f);
                                    }
                                    break;
                                } else {
                                    break;
                                }
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                                break;
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }

            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }

            try {
                if (httpConnectionStream != null) {
                    httpConnectionStream.close();
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }

            if (done) {
                if (cacheImage.tempFilePath != null) {
                    if (!cacheImage.tempFilePath.renameTo(cacheImage.finalFilePath)) {
                        cacheImage.finalFilePath = cacheImage.tempFilePath;
                    }
                }
            }

            return done;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (result || !canRetry) {
                fileDidLoaded(cacheImage.url, cacheImage.finalFilePath, FileLoader.MEDIA_DIR_IMAGE);
            } else {
                httpFileLoadError(cacheImage.url);
            }
            Utilities.stageQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    fileProgresses.remove(cacheImage.url);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (result) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidLoaded, cacheImage.url);
                            } else {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, cacheImage.url, 2);
                            }
                        }
                    });
                }
            });
            imageLoadQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    runHttpTasks(true);
                }
            });
        }

        @Override
        protected void onCancelled() {
            imageLoadQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    runHttpTasks(true);
                }
            });
            Utilities.stageQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    fileProgresses.remove(cacheImage.url);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, cacheImage.url, 1);
                        }
                    });
                }
            });
        }
    }

    private class CacheOutTask implements Runnable {
        private Thread runningThread;
        private final Object sync = new Object();

        private CacheImage cacheImage;
        private boolean isCancelled;

        public CacheOutTask(CacheImage image) {
            cacheImage = image;
        }

        @Override
        public void run() {
            synchronized (sync) {
                runningThread = Thread.currentThread();
                Thread.interrupted();
                if (isCancelled) {
                    return;
                }
            }

            Long mediaId = null;
            boolean mediaIsVideo = false;
            Bitmap image = null;
            File cacheFileFinal = cacheImage.finalFilePath;
            boolean canDeleteFile = true;
            boolean useNativeWebpLoaded = false;

            if (Build.VERSION.SDK_INT < 18) {
                RandomAccessFile randomAccessFile = null;
                try {
                    randomAccessFile = new RandomAccessFile(cacheFileFinal, "r");
                    byte[] bytes;
                    if (cacheImage.thumb) {
                        bytes = headerThumb;
                    } else {
                        bytes = header;
                    }
                    randomAccessFile.readFully(bytes, 0, bytes.length);
                    String str = new String(bytes);
                    if (str != null) {
                        str = str.toLowerCase();
                        if (str.startsWith("riff") && str.endsWith("webp")) {
                            useNativeWebpLoaded = true;
                        }
                    }
                    randomAccessFile.close();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                } finally {
                    if (randomAccessFile != null) {
                        try {
                            randomAccessFile.close();
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }
            }

            if (cacheImage.thumb) {
                int blurType = 0;
                if (cacheImage.filter != null) {
                    if (cacheImage.filter.contains("b2")) {
                        blurType = 3;
                    } else if (cacheImage.filter.contains("b1")) {
                        blurType = 2;
                    } else if (cacheImage.filter.contains("b")) {
                        blurType = 1;
                    }
                }

                try {
                    lastCacheOutTime = System.currentTimeMillis();
                    synchronized (sync) {
                        if (isCancelled) {
                            return;
                        }
                    }

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 1;

                    if (!useNativeWebpLoaded && Build.VERSION.SDK_INT > 10 && Build.VERSION.SDK_INT < 21) {
                        opts.inPurgeable = true;
                    }

                    if (useNativeWebpLoaded) {
                        RandomAccessFile file = new RandomAccessFile(cacheFileFinal, "r");
                        ByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, cacheFileFinal.length());
                        image = Utilities.loadWebpImage(buffer, buffer.limit(), null);
                        file.close();
                    } else {
                        if (opts.inPurgeable) {
                            RandomAccessFile f = new RandomAccessFile(cacheFileFinal, "r");
                            int len = (int) f.length();
                            byte[] data = bytesThumb != null && bytesThumb.length >= len ? bytesThumb : null;
                            if (data == null) {
                                bytesThumb = data = new byte[len];
                            }
                            f.readFully(data, 0, len);
                            image = BitmapFactory.decodeByteArray(data, 0, len, opts);
                        } else {
                            FileInputStream is = new FileInputStream(cacheFileFinal);
                            image = BitmapFactory.decodeStream(is, null, opts);
                            is.close();
                        }
                    }

                    if (image == null) {
                        if (cacheFileFinal.length() == 0 || cacheImage.filter == null) {
                            cacheFileFinal.delete();
                        }
                    } else {
                        if (blurType == 1) {
                            Utilities.blurBitmap(image, 3, opts.inPurgeable ? 0 : 1);
                        } else if (blurType == 2) {
                            Utilities.blurBitmap(image, 1, opts.inPurgeable ? 0 : 1);
                        } else if (blurType == 3) {
                            Utilities.blurBitmap(image, 7, opts.inPurgeable ? 0 : 1);
                            Utilities.blurBitmap(image, 7, opts.inPurgeable ? 0 : 1);
                            Utilities.blurBitmap(image, 7, opts.inPurgeable ? 0 : 1);
                        }
                        if (blurType == 0 && opts.inPurgeable) {
                            Utilities.pinBitmap(image);
                        }
                        if (runtimeHack != null) {
                            runtimeHack.trackFree(image.getRowBytes() * image.getHeight());
                        }
                    }
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
            } else {
                try {
                    if (cacheImage.httpUrl != null) {
                        if (cacheImage.httpUrl.startsWith("thumb://")) {
                            int idx = cacheImage.httpUrl.indexOf(":", 8);
                            if (idx >= 0) {
                                mediaId = Long.parseLong(cacheImage.httpUrl.substring(8, idx));
                                mediaIsVideo = false;
                            }
                            canDeleteFile = false;
                        } else if (cacheImage.httpUrl.startsWith("vthumb://")) {
                            int idx = cacheImage.httpUrl.indexOf(":", 9);
                            if (idx >= 0) {
                                mediaId = Long.parseLong(cacheImage.httpUrl.substring(9, idx));
                                mediaIsVideo = true;
                            }
                            canDeleteFile = false;
                        } else if (!cacheImage.httpUrl.startsWith("http")) {
                            canDeleteFile = false;
                        }
                    }

                    int delay = 20;
                    if (runtimeHack != null) {
                        delay = 60;
                    }
                    if (mediaId != null) {
                        delay = 0;
                    }
                    if (delay != 0 && lastCacheOutTime != 0 && lastCacheOutTime > System.currentTimeMillis() - delay && Build.VERSION.SDK_INT < 21) {
                        Thread.sleep(delay);
                    }
                    lastCacheOutTime = System.currentTimeMillis();
                    synchronized (sync) {
                        if (isCancelled) {
                            return;
                        }
                    }

                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inSampleSize = 1;

                    float w_filter = 0;
                    float h_filter = 0;
                    boolean blur = false;
                    if (cacheImage.filter != null) {
                        String args[] = cacheImage.filter.split("_");
                        if (args.length >= 2) {
                            w_filter = Float.parseFloat(args[0]) * AndroidUtilities.density;
                            h_filter = Float.parseFloat(args[1]) * AndroidUtilities.density;
                        }
                        if (cacheImage.filter.contains("b")) {
                            blur = true;
                        }
                        if (w_filter != 0 && h_filter != 0) {
                            opts.inJustDecodeBounds = true;

                            if (mediaId != null) {
                                if (mediaIsVideo) {
                                    MediaStore.Video.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.getContentResolver(), mediaId, MediaStore.Video.Thumbnails.MINI_KIND, opts);
                                } else {
                                    MediaStore.Images.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.getContentResolver(), mediaId, MediaStore.Images.Thumbnails.MINI_KIND, opts);
                                }
                            } else {
                                FileInputStream is = new FileInputStream(cacheFileFinal);
                                image = BitmapFactory.decodeStream(is, null, opts);
                                is.close();
                            }

                            float photoW = opts.outWidth;
                            float photoH = opts.outHeight;
                            float scaleFactor = Math.max(photoW / w_filter, photoH / h_filter);
                            if (scaleFactor < 1) {
                                scaleFactor = 1;
                            }
                            opts.inJustDecodeBounds = false;
                            opts.inSampleSize = (int) scaleFactor;
                        }
                    }
                    synchronized (sync) {
                        if (isCancelled) {
                            return;
                        }
                    }

                    if (cacheImage.filter == null || blur || cacheImage.httpUrl != null) {
                        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    } else {
                        opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    }
                    if (!useNativeWebpLoaded && Build.VERSION.SDK_INT > 10 && Build.VERSION.SDK_INT < 21) {
                        opts.inPurgeable = true;
                    }

                    opts.inDither = false;
                    if (mediaId != null) {
                        if (mediaIsVideo) {
                            image = MediaStore.Video.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.getContentResolver(), mediaId, MediaStore.Video.Thumbnails.MINI_KIND, opts);
                        } else {
                            image = MediaStore.Images.Thumbnails.getThumbnail(ApplicationLoader.applicationContext.getContentResolver(), mediaId, MediaStore.Images.Thumbnails.MINI_KIND, opts);
                        }
                    }
                    if (image == null) {
                        if (useNativeWebpLoaded) {
                            RandomAccessFile file = new RandomAccessFile(cacheFileFinal, "r");
                            ByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, cacheFileFinal.length());
                            image = Utilities.loadWebpImage(buffer, buffer.limit(), null);
                            file.close();
                        } else {
                            if (opts.inPurgeable) {
                                RandomAccessFile f = new RandomAccessFile(cacheFileFinal, "r");
                                int len = (int) f.length();
                                byte[] data = bytes != null && bytes.length >= len ? bytes : null;
                                if (data == null) {
                                    bytes = data = new byte[len];
                                }
                                f.readFully(data, 0, len);
                                image = BitmapFactory.decodeByteArray(data, 0, len, opts);
                            } else {
                                FileInputStream is = new FileInputStream(cacheFileFinal);
                                image = BitmapFactory.decodeStream(is, null, opts);
                                is.close();
                            }
                        }
                    }
                    if (image == null) {
                        if (canDeleteFile && (cacheFileFinal.length() == 0 || cacheImage.filter == null)) {
                            cacheFileFinal.delete();
                        }
                    } else {
                        boolean blured = false;
                        if (cacheImage.filter != null) {
                            float bitmapW = image.getWidth();
                            float bitmapH = image.getHeight();
                            if (!opts.inPurgeable && w_filter != 0 && bitmapW != w_filter && bitmapW > w_filter + 20) {
                                float scaleFactor = bitmapW / w_filter;
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, (int) w_filter, (int) (bitmapH / scaleFactor), true);
                                if (image != scaledBitmap) {
                                    image.recycle();
                                    image = scaledBitmap;
                                }
                            }
                            if (image != null && blur && bitmapH < 100 && bitmapW < 100) {
                                Utilities.blurBitmap(image, 3, opts.inPurgeable ? 0 : 1);
                                blured = true;
                            }
                        }
                        if (!blured && opts.inPurgeable) {
                            Utilities.pinBitmap(image);
                        }
                        if (runtimeHack != null && image != null) {
                            runtimeHack.trackFree(image.getRowBytes() * image.getHeight());
                        }
                    }
                } catch (Throwable e) {
                    //don't promt
                }
            }
            Thread.interrupted();
            onPostExecute(image != null ? new BitmapDrawable(image) : null);
        }

        private void onPostExecute(final BitmapDrawable bitmapDrawable) {
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    BitmapDrawable toSet = null;
                    if (bitmapDrawable != null) {
                        toSet = memCache.get(cacheImage.key);
                        if (toSet == null) {
                            memCache.put(cacheImage.key, bitmapDrawable);
                            toSet = bitmapDrawable;
                        } else {
                            Bitmap image = bitmapDrawable.getBitmap();
                            if (runtimeHack != null) {
                                runtimeHack.trackAlloc(image.getRowBytes() * image.getHeight());
                            }
                            image.recycle();
                        }
                    }
                    final BitmapDrawable toSetFinal = toSet;
                    imageLoadQueue.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            cacheImage.setImageAndClear(toSetFinal);
                        }
                    });
                }
            });
        }

        public void cancel() {
            synchronized (sync) {
                try {
                    isCancelled = true;
                    if (runningThread != null) {
                        runningThread.interrupt();
                    }
                } catch (Exception e) {
                    //don't promt
                }
            }
        }
    }

    public class VMRuntimeHack {
        private Object runtime = null;
        private Method trackAllocation = null;
        private Method trackFree = null;

        public boolean trackAlloc(long size) {
            if (runtime == null) {
                return false;
            }
            try {
                Object res = trackAllocation.invoke(runtime, size);
                return (res instanceof Boolean) ? (Boolean) res : true;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean trackFree(long size) {
            if (runtime == null) {
                return false;
            }
            try {
                Object res = trackFree.invoke(runtime, size);
                return (res instanceof Boolean) ? (Boolean) res : true;
            } catch (Exception e) {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        public VMRuntimeHack() {
            try {
                Class cl = Class.forName("dalvik.system.VMRuntime");
                Method getRt = cl.getMethod("getRuntime", new Class[0]);
                Object[] objects = new Object[0];
                runtime = getRt.invoke(null, objects);
                trackAllocation = cl.getMethod("trackExternalAllocation", new Class[]{long.class});
                trackFree = cl.getMethod("trackExternalFree", new Class[]{long.class});
            } catch (Exception e) {
                FileLog.e("tmessages", e);
                runtime = null;
                trackAllocation = null;
                trackFree = null;
            }
        }
    }

    //TODO cache image
    private class CacheImage {
        protected String key;
        protected String url;
        protected String filter;
        protected String ext;

        protected File finalFilePath;
        protected File tempFilePath;
        protected boolean thumb;

        protected String httpUrl;
        protected HttpImageTask httpTask;
        protected CacheOutTask cacheTask;

        protected ArrayList<ImageReceiver> imageReceiverArray = new ArrayList<>();

        public void addImageReceiver(ImageReceiver imageReceiver) {
            boolean exist = false;
            for (ImageReceiver v : imageReceiverArray) {
                if (v == imageReceiver) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                imageReceiverArray.add(imageReceiver);
                imageLoadingByTag.put(imageReceiver.getTag(thumb), this);
            }
        }

        public void removeImageReceiver(ImageReceiver imageReceiver) {
            for (int a = 0; a < imageReceiverArray.size(); a++) {
                ImageReceiver obj = imageReceiverArray.get(a);
                if (obj == null || obj == imageReceiver) {
                    imageReceiverArray.remove(a);
                    if (obj != null) {
                        imageLoadingByTag.remove(obj.getTag(thumb));
                    }
                    a--;
                }
            }
            if (imageReceiverArray.size() == 0) {
                for (ImageReceiver receiver : imageReceiverArray) {
                    imageLoadingByTag.remove(receiver.getTag(thumb));
                }
                imageReceiverArray.clear();
                if (cacheTask != null) {
                    if (thumb) {
                        cacheThumbOutQueue.cancelRunnable(cacheTask);
                    } else {
                        cacheOutQueue.cancelRunnable(cacheTask);
                    }
                    cacheTask.cancel();
                    cacheTask = null;
                }
                if (httpTask != null) {
                    httpTasks.remove(httpTask);
                    httpTask.cancel(true);
                    httpTask = null;
                }
                if (url != null) {
                    imageLoadingByUrl.remove(url);
                }
                if (key != null) {
                    imageLoadingByKeys.remove(key);
                }
            }
        }

        public void setImageAndClear(final BitmapDrawable image) {
            if (image != null) {
                final ArrayList<ImageReceiver> finalImageReceiverArray = new ArrayList<>(imageReceiverArray);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        for (ImageReceiver imgView : finalImageReceiverArray) {
                            imgView.setImageBitmapByKey(image, key, thumb, false);
                        }
                    }
                });
            }
            for (ImageReceiver imageReceiver : imageReceiverArray) {
                imageLoadingByTag.remove(imageReceiver.getTag(thumb));
            }
            imageReceiverArray.clear();
            if (url != null) {
                imageLoadingByUrl.remove(url);
            }
            if (key != null) {
                imageLoadingByKeys.remove(key);
            }
        }
    }

    private static volatile ImageLoader Instance = null;

    public static ImageLoader getInstance() {
        ImageLoader localInstance = Instance;
        if (localInstance == null) {
            synchronized (ImageLoader.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ImageLoader();
                }
            }
        }
        return localInstance;
    }

    public ImageLoader() {
        int cacheSize = Math.min(15, ((ActivityManager) ApplicationLoader.applicationContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() / 7) * 1024 * 1024;

        if (Build.VERSION.SDK_INT < 11) {
            runtimeHack = new VMRuntimeHack();
            cacheSize = 1024 * 1024 * 3;
        }
        memCache = new LruCache(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable bitmap) {
                Bitmap b = bitmap.getBitmap();
                if (Build.VERSION.SDK_INT < 12) {
                    return b.getRowBytes() * b.getHeight();
                } else {
                    return b.getByteCount();
                }
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, final BitmapDrawable oldBitmap, BitmapDrawable newBitmap) {
                if (ignoreRemoval != null && key != null && ignoreRemoval.equals(key)) {
                    return;
                }
                final Integer count = bitmapUseCounts.get(key);
                if (count == null || count == 0) {
                    Bitmap b = oldBitmap.getBitmap();
                    if (runtimeHack != null) {
                        runtimeHack.trackAlloc(b.getRowBytes() * b.getHeight());
                    }
                    if (!b.isRecycled()) {
                        b.recycle();
                    }
                }
            }
        };

        FileLoader.getInstance().setDelegate(new FileLoader.FileLoaderDelegate() {
            @Override
            public void fileUploadProgressChanged(final String location, final float progress, final boolean isEncrypted) {
                fileProgresses.put(location, progress);
                long currentTime = System.currentTimeMillis();
                if (lastProgressUpdateTime == 0 || lastProgressUpdateTime < currentTime - 500) {
                    lastProgressUpdateTime = currentTime;

                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileUploadProgressChanged, location, progress, isEncrypted);
                        }
                    });
                }
            }

            @Override
            public void fileDidUploaded(final String location, final TLRPC.InputFile inputFile, final TLRPC.InputEncryptedFile inputEncryptedFile, final byte[] key, final byte[] iv) {
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidUpload, location, inputFile, inputEncryptedFile, key, iv);
                            }
                        });
                        fileProgresses.remove(location);
                    }
                });
            }

            @Override
            public void fileDidFailedUpload(final String location, final boolean isEncrypted) {
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailUpload, location, isEncrypted);
                            }
                        });
                        fileProgresses.remove(location);
                    }
                });
            }

            @Override
            public void fileDidLoaded(final String location, final File finalFile, final int type) {
                fileProgresses.remove(location);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (location != null) {
                            if (MediaController.getInstance().canSaveToGallery() && telegramPath != null && finalFile != null && finalFile.exists() && (location.endsWith(".mp4") || location.endsWith(".jpg"))) {
                                if (finalFile.toString().startsWith(telegramPath.toString())) {
                                    AndroidUtilities.addMediaToGallery(finalFile.toString());
                                }
                            }
                        }
                        ImageLoader.this.fileDidLoaded(location, finalFile, type);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidLoaded, location);
                    }
                });
            }

            @Override
            public void fileDidFailedLoad(final String location, final int canceled) {
                fileProgresses.remove(location);
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoader.this.fileDidFailedLoad(location, canceled);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileDidFailedLoad, location, canceled);
                    }
                });
            }

            @Override
            public void fileLoadProgressChanged(final String location, final float progress) {
                fileProgresses.put(location, progress);
                long currentTime = System.currentTimeMillis();
                if (lastProgressUpdateTime == 0 || lastProgressUpdateTime < currentTime - 500) {
                    lastProgressUpdateTime = currentTime;
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.FileLoadProgressChanged, location, progress);
                        }
                    });
                }
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                FileLog.e("tmessages", "file system changed");
                Runnable r = new Runnable() {
                    public void run() {
                        FileLoader.getInstance().setMediaDirs(createMediaPaths());
                    }
                };
                if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                    AndroidUtilities.runOnUIThread(r, 1000);
                } else {
                    r.run();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_NOFS);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_SHARED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        ApplicationLoader.applicationContext.registerReceiver(receiver, filter);

        FileLoader.getInstance().setMediaDirs(createMediaPaths());
    }

    private HashMap<Integer, File> createMediaPaths() {
        HashMap<Integer, File> mediaDirs = new HashMap<>();
        File cachePath = AndroidUtilities.getCacheDir();
        if (!cachePath.isDirectory()) {
            try {
                cachePath.mkdirs();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
        try {
            new File(cachePath, ".nomedia").createNewFile();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        mediaDirs.put(FileLoader.MEDIA_DIR_CACHE, cachePath);
        FileLog.e("tmessages", "cache path = " + cachePath);

        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                telegramPath = new File(Environment.getExternalStorageDirectory(), "Telegram");
                telegramPath.mkdirs();

                boolean canRename = false;

                try {
                    for (int a = 0; a < 5; a++) {
                        File srcFile = new File(cachePath, "temp.file");
                        srcFile.createNewFile();
                        File dstFile = new File(telegramPath, "temp.file");
                        canRename = srcFile.renameTo(dstFile);
                        srcFile.delete();
                        dstFile.delete();
                        if (canRename) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }

                if (canRename) {
                    if (telegramPath.isDirectory()) {
                        try {
                            File imagePath = new File(telegramPath, "Telegram Images");
                            imagePath.mkdir();
                            if (imagePath.isDirectory()) {
                                mediaDirs.put(FileLoader.MEDIA_DIR_IMAGE, imagePath);
                                FileLog.e("tmessages", "image path = " + imagePath);
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }

                        try {
                            File videoPath = new File(telegramPath, "Telegram Video");
                            videoPath.mkdir();
                            if (videoPath.isDirectory()) {
                                mediaDirs.put(FileLoader.MEDIA_DIR_VIDEO, videoPath);
                                FileLog.e("tmessages", "video path = " + videoPath);
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }

                        try {
                            File audioPath = new File(telegramPath, "Telegram Audio");
                            audioPath.mkdir();
                            if (audioPath.isDirectory()) {
                                new File(audioPath, ".nomedia").createNewFile();
                                mediaDirs.put(FileLoader.MEDIA_DIR_AUDIO, audioPath);
                                FileLog.e("tmessages", "audio path = " + audioPath);
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }

                        try {
                            File documentPath = new File(telegramPath, "Telegram Documents");
                            documentPath.mkdir();
                            if (documentPath.isDirectory()) {
                                new File(documentPath, ".nomedia").createNewFile();
                                mediaDirs.put(FileLoader.MEDIA_DIR_DOCUMENT, documentPath);
                                FileLog.e("tmessages", "documents path = " + documentPath);
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                } else {
                    FileLog.e("tmessages", "this Android can't rename files");
                }
            }
            MediaController.getInstance().checkSaveToGalleryFiles();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        return mediaDirs;
    }

    public Float getFileProgress(String location) {
        return fileProgresses.get(location);
    }

    private void performReplace(String oldKey, String newKey) {
        BitmapDrawable b = memCache.get(oldKey);
        if (b != null) {
            ignoreRemoval = oldKey;
            memCache.remove(oldKey);
            memCache.put(newKey, b);
            ignoreRemoval = null;
        }
        Integer val = bitmapUseCounts.get(oldKey);
        if (val != null) {
            bitmapUseCounts.put(newKey, val);
            bitmapUseCounts.remove(oldKey);
        }
    }

    public void incrementUseCount(String key) {
        Integer count = bitmapUseCounts.get(key);
        if (count == null) {
            bitmapUseCounts.put(key, 1);
        } else {
            bitmapUseCounts.put(key, count + 1);
        }
    }

    public boolean decrementUseCount(String key) {
        Integer count = bitmapUseCounts.get(key);
        if (count == null) {
            return true;
        }
        if (count == 1) {
            bitmapUseCounts.remove(key);
            return true;
        } else {
            bitmapUseCounts.put(key, count - 1);
        }
        return false;
    }

    public void removeImage(String key) {
        bitmapUseCounts.remove(key);
        memCache.remove(key);
    }

    public boolean isInCache(String key) {
        return memCache.get(key) != null;
    }

    public void clearMemory() {
        memCache.evictAll();
    }

    private void removeFromWaitingForThumb(Integer TAG) {
        String location = waitingForQualityThumbByTag.get(TAG);
        if (location != null) {
//            ThumbGenerateInfo info = waitingForQualityThumb.get(location);
//            if (info != null) {
//                info.count--;
//                if (info.count == 0) {
//                    waitingForQualityThumb.remove(location);
//                }
//            }
//            waitingForQualityThumbByTag.remove(TAG);
        }
    }

    public void cancelLoadingForImageReceiver(final ImageReceiver imageReceiver, final int type) {
        if (imageReceiver == null) {
            return;
        }
        imageLoadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                int start = 0;
                int count = 2;
                if (type == 1) {
                    count = 1;
                } else if (type == 2) {
                    start = 1;
                }
                for (int a = start; a < count; a++) {
                    Integer TAG = imageReceiver.getTag(a == 0);
                    if (a == 0) {
                        removeFromWaitingForThumb(TAG);
                    }
                    if (TAG != null) {
                        CacheImage ei = imageLoadingByTag.get(TAG);
                        if (ei != null) {
                            ei.removeImageReceiver(imageReceiver);
                        }
                    }
                }
            }
        });
    }

    public BitmapDrawable getImageFromMemory(String key) {
        return memCache.get(key);
    }

    public BitmapDrawable getImageFromMemory(TLObject fileLocation, String httpUrl, String filter) {
        if (fileLocation == null && httpUrl == null) {
            return null;
        }
        String key = null;
        if (httpUrl != null) {
            key = Utilities.MD5(httpUrl);
        } else {
            if (fileLocation instanceof TLRPC.FileLocation) {
                TLRPC.FileLocation location = (TLRPC.FileLocation) fileLocation;
                key = location.volume_id + "_" + location.local_id;
            } else if (fileLocation instanceof TLRPC.Document) {
                TLRPC.Document location = (TLRPC.Document) fileLocation;
                key = location.dc_id + "_" + location.id;
            }
        }
        if (filter != null) {
            key += "@" + filter;
        }
        return memCache.get(key);
    }

    public void replaceImageInCache(final String oldKey, final String newKey, final TLRPC.FileLocation newLocation) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> arr = memCache.getFilterKeys(oldKey);
                if (arr != null) {
                    for (String filter : arr) {
                        String oldK = oldKey + "@" + filter;
                        String newK = newKey + "@" + filter;
                        performReplace(oldK, newK);
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldK, newK, newLocation);
                    }
                } else {
                    performReplace(oldKey, newKey);
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.didReplacedPhotoInMemCache, oldKey, newKey, newLocation);
                }
            }
        });
    }

    public void putImageToCache(BitmapDrawable bitmap, String key) {
        memCache.put(key, bitmap);
    }


    private void createLoadOperationForImageReceiver(final ImageReceiver imageReceiver, final String key, final String url, final String ext, final String httpLocation, final String filter, final int size, final boolean cacheOnly, final int thumb) {
        if (imageReceiver == null || url == null || key == null) {
            return;
        }
        Integer TAG = imageReceiver.getTag(thumb != 0);
        if (TAG == null) {
            imageReceiver.setTag(TAG = lastImageNum, thumb != 0);
            lastImageNum++;
            if (lastImageNum == Integer.MAX_VALUE) {
                lastImageNum = 0;
            }
        }

        final Integer finalTag = TAG;
        final boolean finalIsNeedsQualityThumb = imageReceiver.isNeedsQualityThumb();
        final boolean shouldGenerateQualityThumb = imageReceiver.isShouldGenerateQualityThumb();
        imageLoadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                boolean added = false;
                if (thumb != 2) {
                    CacheImage alreadyLoadingUrl = imageLoadingByUrl.get(url);
                    CacheImage alreadyLoadingCache = imageLoadingByKeys.get(key);
                    CacheImage alreadyLoadingImage = imageLoadingByTag.get(finalTag);
                    if (alreadyLoadingImage != null) {
                        if (alreadyLoadingImage == alreadyLoadingUrl || alreadyLoadingImage == alreadyLoadingCache) {
                            added = true;
                        } else {
                            alreadyLoadingImage.removeImageReceiver(imageReceiver);
                        }
                    }

                    if (!added && alreadyLoadingCache != null) {
                        alreadyLoadingCache.addImageReceiver(imageReceiver);
                        added = true;
                    }
                    if (!added && alreadyLoadingUrl != null) {
                        alreadyLoadingUrl.addImageReceiver(imageReceiver);
                        added = true;
                    }
                }

                if (!added) {
                    boolean onlyCache = false;
                    boolean isQuality = false;
                    File cacheFile = null;

                    if (httpLocation != null) {
                        if (!httpLocation.startsWith("http")) {
                            onlyCache = true;
                            if (httpLocation.startsWith("thumb://")) {
                                int idx = httpLocation.indexOf(":", 8);
                                if (idx >= 0) {
                                    cacheFile = new File(httpLocation.substring(idx + 1));
                                }
                            } else if (httpLocation.startsWith("vthumb://")) {
                                int idx = httpLocation.indexOf(":", 9);
                                if (idx >= 0) {
                                    cacheFile = new File(httpLocation.substring(idx + 1));
                                }
                            } else {
                                cacheFile = new File(httpLocation);
                            }
                        }
                    } else if (thumb != 0) {
                        if (finalIsNeedsQualityThumb) {
                            cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), "q_" + url);
                            if (!cacheFile.exists()) {
                                cacheFile = null;
                            }
                        }
                    }

                    if (thumb != 2) {
                        if (cacheFile == null) {
                            if (cacheOnly || size == 0 || httpLocation != null) {
                                cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), url);
                            } else {
                                cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_IMAGE), url);
                            }
                        }

                        CacheImage img = new CacheImage();
                        img.thumb = thumb != 0;
                        img.key = key;
                        img.filter = filter;
                        img.httpUrl = httpLocation;
                        img.ext = ext;
                        img.addImageReceiver(imageReceiver);
                        if (onlyCache || cacheFile.exists()) {
                            img.finalFilePath = cacheFile;
                            img.cacheTask = new CacheOutTask(img);
                            imageLoadingByKeys.put(key, img);
                            if (thumb != 0) {
                                cacheThumbOutQueue.postRunnable(img.cacheTask);
                            } else {
                                cacheOutQueue.postRunnable(img.cacheTask);
                            }
                        } else {
                            img.url = url;
                            imageLoadingByUrl.put(url, img);
//                            if (httpLocation == null) {
//                                if (imageLocation instanceof TLRPC.FileLocation) {
//                                    TLRPC.FileLocation location = (TLRPC.FileLocation) imageLocation;
//                                    FileLoader.getInstance().loadFile(location, ext, size, size == 0 || location.key != null || cacheOnly);
//                                } else if (imageLocation instanceof TLRPC.Document) {
//                                    FileLoader.getInstance().loadFile((TLRPC.Document) imageLocation, true, true);
//                                }
//                            } else {
                                String file = Utilities.MD5(httpLocation);
                                File cacheDir = FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE);
                                img.tempFilePath = new File(cacheDir, file + "_temp.jpg");
                                img.finalFilePath = cacheFile;
                                img.httpTask = new HttpImageTask(img, size);
                                httpTasks.add(img.httpTask);
                                runHttpTasks(false);
//                            }
                        }
                    }
                }
            }
        });
    }

    public void loadImageForImageReceiver(ImageReceiver imageReceiver) {
        if (imageReceiver == null) {
            return;
        }

        String key = imageReceiver.getKey();
        if (key != null) {
            BitmapDrawable bitmapDrawable = memCache.get(key);
            if (bitmapDrawable != null) {
                cancelLoadingForImageReceiver(imageReceiver, 0);
                if (!imageReceiver.isForcePreview()) {
                    imageReceiver.setImageBitmapByKey(bitmapDrawable, key, false, true);
                    return;
                }
            }
        }
        boolean thumbSet = false;
        String thumbKey = imageReceiver.getThumbKey();
        if (thumbKey != null) {
            BitmapDrawable bitmapDrawable = memCache.get(thumbKey);
            if (bitmapDrawable != null) {
                imageReceiver.setImageBitmapByKey(bitmapDrawable, thumbKey, true, true);
                cancelLoadingForImageReceiver(imageReceiver, 1);
                thumbSet = true;
            }
        }

        String httpLocation = imageReceiver.getHttpImageLocation();

        boolean saveImageToCache = false;

        String url = null;
        String thumbUrl = null;
        key = null;
        thumbKey = null;
        String ext = imageReceiver.getExt();
        if (ext == null) {
            ext = "jpg";
        }
        if (httpLocation != null) {
            key = Utilities.MD5(httpLocation);
            url = key + "." + getHttpUrlExtension(httpLocation);
        }

        String filter = imageReceiver.getFilter();
        String thumbFilter = imageReceiver.getThumbFilter();
        if (key != null && filter != null) {
            key += "@" + filter;
        }
        if (thumbKey != null && thumbFilter != null) {
            thumbKey += "@" + thumbFilter;
        }

        if (httpLocation != null) {
            createLoadOperationForImageReceiver(imageReceiver, key, url, ext, httpLocation, filter, 0, true, 0);
        }
//        else {
//            createLoadOperationForImageReceiver(imageReceiver, thumbKey, thumbUrl, ext, null, thumbFilter, 0, true, thumbSet ? 2 : 1);
//            createLoadOperationForImageReceiver(imageReceiver, key, url, ext, imageLocation, null, filter, imageReceiver.getSize(), saveImageToCache || imageReceiver.getCacheOnly(), 0);
//        }
    }

    private void httpFileLoadError(final String location) {
        imageLoadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                CacheImage img = imageLoadingByUrl.get(location);
                if (img == null) {
                    return;
                }
                HttpImageTask oldTask = img.httpTask;
                img.httpTask = new HttpImageTask(oldTask.cacheImage, oldTask.imageSize);
                httpTasks.add(img.httpTask);
                runHttpTasks(false);
            }
        });
    }

    private void fileDidLoaded(final String location, final File finalFile, final int type) {
        imageLoadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                CacheImage img = imageLoadingByUrl.get(location);
                if (img == null) {
                    return;
                }
                imageLoadingByUrl.remove(location);
                CacheOutTask task = null;
                for (ImageReceiver imageReceiver : img.imageReceiverArray) {
                    CacheImage cacheImage = imageLoadingByKeys.get(img.key);
                    if (cacheImage == null) {
                        cacheImage = new CacheImage();
                        cacheImage.finalFilePath = finalFile;
                        cacheImage.key = img.key;
                        cacheImage.httpUrl = img.httpUrl;
                        cacheImage.thumb = img.thumb;
                        cacheImage.ext = img.ext;
                        cacheImage.cacheTask = task = new CacheOutTask(cacheImage);
                        cacheImage.filter = img.filter;
                        imageLoadingByKeys.put(cacheImage.key, cacheImage);
                    }
                    cacheImage.addImageReceiver(imageReceiver);
                }
                if (task != null) {
                    if (img.thumb) {
                        cacheThumbOutQueue.postRunnable(task);
                    } else {
                        cacheOutQueue.postRunnable(task);
                    }
                }
            }
        });
    }

    private void fileDidFailedLoad(final String location, int canceled) {
        if (canceled == 1) {
            return;
        }
        imageLoadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                CacheImage img = imageLoadingByUrl.get(location);
                if (img != null) {
                    img.setImageAndClear(null);
                }
            }
        });
    }

    private void runHttpTasks(boolean complete) {
        if (complete) {
            currentHttpTasksCount--;
        }
        while (currentHttpTasksCount < 1 && !httpTasks.isEmpty()) {
            HttpImageTask task = httpTasks.poll();
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);
            } else {
                task.execute(null, null, null);
            }
            currentHttpTasksCount++;
        }
    }

    public void loadHttpFile(String url, String extension) {
        if (url == null || url.length() == 0 || httpFileLoadTasksByKeys.containsKey(url)) {
            return;
        }
        String ext = extension;
        if (ext == null) {
            int idx = url.lastIndexOf(".");
            if (idx != -1) {
                ext = url.substring(idx + 1);
            }
            if (ext == null || ext.length() == 0 || ext.length() > 4) {
                ext = "jpg";
            }
        }
        File file = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), Utilities.MD5(url) + "_temp." + ext);
        file.delete();

        HttpFileTask task = new HttpFileTask(url, file, ext);
        httpFileLoadTasks.add(task);
        httpFileLoadTasksByKeys.put(url, task);
        runHttpFileLoadTasks(null, 0);
    }

    public void cancelLoadHttpFile(String url) {
        HttpFileTask task = httpFileLoadTasksByKeys.get(url);
        if (task != null) {
            task.cancel(true);
            httpFileLoadTasksByKeys.remove(url);
            httpFileLoadTasks.remove(task);
        }
        Runnable runnable = retryHttpsTasks.get(url);
        if (runnable != null) {
            AndroidUtilities.cancelRunOnUIThread(runnable);
        }
        runHttpFileLoadTasks(null, 0);
    }

    private void runHttpFileLoadTasks(final HttpFileTask oldTask, final int reason) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (oldTask != null) {
                    currentHttpFileLoadTasksCount--;
                }
                if (oldTask != null) {
                    if (reason == 1) {
                        if (oldTask.canRetry) {
                            final HttpFileTask newTask = new HttpFileTask(oldTask.url, oldTask.tempFile, oldTask.ext);
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    httpFileLoadTasks.add(newTask);
                                    runHttpFileLoadTasks(null, 0);
                                }
                            };
                            retryHttpsTasks.put(oldTask.url, runnable);
                            AndroidUtilities.runOnUIThread(runnable, 1000);
                        } else {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.httpFileDidFailedLoad, oldTask.url);
                        }
                    } else if (reason == 2) {
                        httpFileLoadTasksByKeys.remove(oldTask.url);
                        File file = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), Utilities.MD5(oldTask.url) + "." + oldTask.ext);
                        String result = oldTask.tempFile.renameTo(file) ? file.toString() : oldTask.tempFile.toString();
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.httpFileDidLoaded, oldTask.url, result);
                    }
                }
                while (currentHttpFileLoadTasksCount < 2 && !httpFileLoadTasks.isEmpty()) {
                    HttpFileTask task = httpFileLoadTasks.poll();
                    if (android.os.Build.VERSION.SDK_INT >= 11) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null, null, null);
                    } else {
                        task.execute(null, null, null);
                    }
                    currentHttpFileLoadTasksCount++;
                }
            }
        });
    }

    public static Bitmap loadBitmap(String path, Uri uri, float maxWidth, float maxHeight, boolean useMaxScale) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        FileDescriptor fileDescriptor = null;
        ParcelFileDescriptor parcelFD = null;

        if (path == null && uri != null && uri.getScheme() != null) {
            String imageFilePath = null;
            if (uri.getScheme().contains("file")) {
                path = uri.getPath();
            } else {
                try {
                    path = AndroidUtilities.getPath(uri);
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
            }
        }

        if (path != null) {
            BitmapFactory.decodeFile(path, bmOptions);
        } else if (uri != null) {
            boolean error = false;
            try {
                parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
                fileDescriptor = parcelFD.getFileDescriptor();
                BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
                try {
                    if (parcelFD != null) {
                        parcelFD.close();
                    }
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
                return null;
            }
        }
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        float scaleFactor = useMaxScale ? Math.max(photoW / maxWidth, photoH / maxHeight) : Math.min(photoW / maxWidth, photoH / maxHeight);
        if (scaleFactor < 1) {
            scaleFactor = 1;
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;

        String exifPath = null;
        if (path != null) {
            exifPath = path;
        } else if (uri != null) {
            exifPath = AndroidUtilities.getPath(uri);
        }

        Matrix matrix = null;

        if (exifPath != null) {
            ExifInterface exif;
            try {
                exif = new ExifInterface(exifPath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
        }

        Bitmap b = null;
        if (path != null) {
            try {
                b = BitmapFactory.decodeFile(path, bmOptions);
                if (b != null) {
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
                ImageLoader.getInstance().clearMemory();
                try {
                    if (b == null) {
                        b = BitmapFactory.decodeFile(path, bmOptions);
                    }
                    if (b != null) {
                        b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    }
                } catch (Throwable e2) {
                    FileLog.e("tmessages", e2);
                }
            }
        } else if (uri != null) {
            try {
                b = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
                if (b != null) {
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            } finally {
                try {
                    parcelFD.close();
                } catch (Throwable e) {
                    FileLog.e("tmessages", e);
                }
            }
        }

        return b;
    }

    public static void fillPhotoSizeWithBytes(TLRPC.PhotoSize photoSize) {
        if (photoSize == null || photoSize.bytes != null) {
            return;
        }
        File file = FileLoader.getPathToAttach(photoSize, true);
        try {
            RandomAccessFile f = new RandomAccessFile(file, "r");
            int len = (int) f.length();
            if (len < 20000) {
                photoSize.bytes = new byte[(int) f.length()];
                f.readFully(photoSize.bytes, 0, photoSize.bytes.length);
            }
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }
    }

    private static TLRPC.PhotoSize scaleAndSaveImageInternal(Bitmap bitmap, int w, int h, float photoW, float photoH, float scaleFactor, int quality, boolean cache, boolean scaleAnyway) throws Exception {
        Bitmap scaledBitmap;
        if (scaleFactor > 1 || scaleAnyway) {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
        } else {
            scaledBitmap = bitmap;
        }

        TLRPC.TL_fileLocation location = new TLRPC.TL_fileLocation();
        location.volume_id = Integer.MIN_VALUE;
        location.dc_id = Integer.MIN_VALUE;
        location.local_id = UserConfig.lastLocalId;
        UserConfig.lastLocalId--;
        TLRPC.PhotoSize size = new TLRPC.TL_photoSize();
        size.location = location;
        size.w = scaledBitmap.getWidth();
        size.h = scaledBitmap.getHeight();
        if (size.w <= 100 && size.h <= 100) {
            size.type = "s";
        } else if (size.w <= 320 && size.h <= 320) {
            size.type = "m";
        } else if (size.w <= 800 && size.h <= 800) {
            size.type = "x";
        } else if (size.w <= 1280 && size.h <= 1280) {
            size.type = "y";
        } else {
            size.type = "w";
        }

        String fileName = location.volume_id + "_" + location.local_id + ".jpg";
        final File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName);
        FileOutputStream stream = new FileOutputStream(cacheFile);
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        if (cache) {
            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream2);
            size.bytes = stream2.toByteArray();
            size.size = size.bytes.length;
            stream2.close();
        } else {
            size.size = (int) stream.getChannel().size();
        }
        stream.close();
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle();
        }

        return size;
    }

    public static TLRPC.PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache) {
        return scaleAndSaveImage(bitmap, maxWidth, maxHeight, quality, cache, 0, 0);
    }

    public static TLRPC.PhotoSize scaleAndSaveImage(Bitmap bitmap, float maxWidth, float maxHeight, int quality, boolean cache, int minWidth, int minHeight) {
        if (bitmap == null) {
            return null;
        }
        float photoW = bitmap.getWidth();
        float photoH = bitmap.getHeight();
        if (photoW == 0 || photoH == 0) {
            return null;
        }
        boolean scaleAnyway = false;
        float scaleFactor = Math.max(photoW / maxWidth, photoH / maxHeight);
        if (minWidth != 0 && minHeight != 0 && (photoW < minWidth || photoH < minHeight)) {
            if (photoW < minWidth && photoH > minHeight) {
                scaleFactor = photoW / minWidth;
            } else if (photoW > minWidth && photoH < minHeight) {
                scaleFactor = photoH / minHeight;
            } else {
                scaleFactor = Math.max(photoW / minWidth, photoH / minHeight);
            }
            scaleAnyway = true;
        }
        int w = (int) (photoW / scaleFactor);
        int h = (int) (photoH / scaleFactor);
        if (h == 0 || w == 0) {
            return null;
        }

        try {
            return scaleAndSaveImageInternal(bitmap, w, h, photoW, photoH, scaleFactor, quality, cache, scaleAnyway);
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
            ImageLoader.getInstance().clearMemory();
            System.gc();
            try {
                return scaleAndSaveImageInternal(bitmap, w, h, photoW, photoH, scaleFactor, quality, cache, scaleAnyway);
            } catch (Throwable e2) {
                FileLog.e("tmessages", e2);
                return null;
            }
        }
    }

    public static String getHttpUrlExtension(String url) {
        String ext = null;
        int idx = url.lastIndexOf(".");
        if (idx != -1) {
            ext = url.substring(idx + 1);
        }
        if (ext == null || ext.length() == 0 || ext.length() > 4) {
            ext = "jpg";
        }
        return ext;
    }

    public static void saveMessageThumbs(TLRPC.Message message) {
        TLRPC.PhotoSize photoSize = null;
        if (message.media instanceof TLRPC.TL_messageMediaPhoto) {
            for (TLRPC.PhotoSize size : message.media.photo.sizes) {
                if (size instanceof TLRPC.TL_photoCachedSize) {
                    photoSize = size;
                    break;
                }
            }
        } else if (message.media instanceof TLRPC.TL_messageMediaVideo) {
            if (message.media.video.thumb instanceof TLRPC.TL_photoCachedSize) {
                photoSize = message.media.video.thumb;
            }
        } else if (message.media instanceof TLRPC.TL_messageMediaDocument) {
            if (message.media.document.thumb instanceof TLRPC.TL_photoCachedSize) {
                photoSize = message.media.document.thumb;
            }
        } else if (message.media instanceof TLRPC.TL_messageMediaWebPage) {
            if (message.media.webpage.photo != null) {
                for (TLRPC.PhotoSize size : message.media.webpage.photo.sizes) {
                    if (size instanceof TLRPC.TL_photoCachedSize) {
                        photoSize = size;
                        break;
                    }
                }
            }
        }
        if (photoSize != null && photoSize.bytes != null && photoSize.bytes.length != 0) {
            if (photoSize.location instanceof TLRPC.TL_fileLocationUnavailable) {
                photoSize.location = new TLRPC.TL_fileLocation();
                photoSize.location.volume_id = Integer.MIN_VALUE;
                photoSize.location.dc_id = Integer.MIN_VALUE;
                photoSize.location.local_id = UserConfig.lastLocalId;
                UserConfig.lastLocalId--;
            }
            File file = FileLoader.getPathToAttach(photoSize, true);
            if (!file.exists()) {
                try {
                    RandomAccessFile writeFile = new RandomAccessFile(file, "rws");
                    writeFile.write(photoSize.bytes);
                    writeFile.close();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
            TLRPC.TL_photoSize newPhotoSize = new TLRPC.TL_photoSize();
            newPhotoSize.w = photoSize.w;
            newPhotoSize.h = photoSize.h;
            newPhotoSize.location = photoSize.location;
            newPhotoSize.size = photoSize.size;
            newPhotoSize.type = photoSize.type;

            if (message.media instanceof TLRPC.TL_messageMediaPhoto) {
                for (int a = 0; a < message.media.photo.sizes.size(); a++) {
                    if (message.media.photo.sizes.get(a) instanceof TLRPC.TL_photoCachedSize) {
                        message.media.photo.sizes.set(a, newPhotoSize);
                        break;
                    }
                }
            } else if (message.media instanceof TLRPC.TL_messageMediaVideo) {
                message.media.video.thumb = newPhotoSize;
            } else if (message.media instanceof TLRPC.TL_messageMediaDocument) {
                message.media.document.thumb = newPhotoSize;
            } else if (message.media instanceof TLRPC.TL_messageMediaWebPage) {
                for (int a = 0; a < message.media.webpage.photo.sizes.size(); a++) {
                    if (message.media.webpage.photo.sizes.get(a) instanceof TLRPC.TL_photoCachedSize) {
                        message.media.webpage.photo.sizes.set(a, newPhotoSize);
                        break;
                    }
                }
            }
        }
    }

}
