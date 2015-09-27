/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.R;

import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;
import org.telegram.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaController implements NotificationCenter.NotificationCenterDelegate {

    private native int startRecord(String path);
    private native int writeFrame(ByteBuffer frame, int len);
    private native void stopRecord();
    private native int openOpusFile(String path);
    private native int seekOpusFile(float position);
    private native int isOpusFile(String path);
    private native void closeOpusFile();
    private native void readOpusFile(ByteBuffer buffer, int capacity, int[] args);
    private native long getTotalPcmDuration();

    public static int[] readArgs = new int[3];

    public interface FileDownloadProgressListener {
        void onFailedDownload(String fileName);
        void onSuccessDownload(String fileName);
        void onProgressDownload(String fileName, float progress);
        void onProgressUpload(String fileName, float progress, boolean isEncrypted);
        int getObserverTag();
    }


    private static final String[] projectionPhotos = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION
    };

    private static final String[] projectionVideo = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN
    };

    public static class AlbumEntry {
        public int bucketId;
        public String bucketName;
        public PhotoEntry coverPhoto;
        public ArrayList<PhotoEntry> photos = new ArrayList<>();
        public HashMap<Integer, PhotoEntry> photosByIds = new HashMap<>();
        public boolean isVideo;

        public AlbumEntry(int bucketId, String bucketName, PhotoEntry coverPhoto, boolean isVideo) {
            this.bucketId = bucketId;
            this.bucketName = bucketName;
            this.coverPhoto = coverPhoto;
            this.isVideo = isVideo;
        }

        public void addPhoto(PhotoEntry photoEntry) {
            photos.add(photoEntry);
            photosByIds.put(photoEntry.imageId, photoEntry);
        }
    }

    public static class PhotoEntry {
        public int bucketId;
        public int imageId;
        public long dateTaken;
        public String path;
        public int orientation;
        public String thumbPath;
        public String imagePath;
        public boolean isVideo;
        public CharSequence caption;

        public PhotoEntry(int bucketId, int imageId, long dateTaken, String path, int orientation, boolean isVideo) {
            this.bucketId = bucketId;
            this.imageId = imageId;
            this.dateTaken = dateTaken;
            this.path = path;
            this.orientation = orientation;
            this.isVideo = isVideo;
        }
    }

    public static class SearchImage {
        public int uid;
        public String id;
        public String imageUrl;
        public String thumbUrl;
        public String localUrl;
        public int width;
        public int height;
        public int size;
        public int type;
        public int date;
        public String thumbPath;
        public String imagePath;
        public CharSequence caption;
    }


    private HashMap<Long, Long> typingTimes = new HashMap<>();

    public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
    public int mobileDataDownloadMask = 0;
    public int wifiDownloadMask = 0;
    public int roamingDownloadMask = 0;
    private int lastCheckMask = 0;

    private boolean saveToGallery = true;

    private HashMap<String, ArrayList<WeakReference<FileDownloadProgressListener>>> loadingFileObservers = new HashMap<>();
    private HashMap<Integer, String> observersByTag = new HashMap<>();
    private boolean listenerInProgress = false;
    private HashMap<String, FileDownloadProgressListener> addLaterArray = new HashMap<>();
    private ArrayList<FileDownloadProgressListener> deleteLaterArray = new ArrayList<>();
    private int lastTag = 0;


    private ByteBuffer fileBuffer;


    private class InternalObserver extends ContentObserver {
        public InternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            processMediaObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        }
    }

    private class ExternalObserver extends ContentObserver {
        public ExternalObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            processMediaObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
    }

    private ExternalObserver externalObserver = null;
    private InternalObserver internalObserver = null;
    private int startObserverToken = 0;
    private StopMediaObserverRunnable stopMediaObserverRunnable = null;
    private final class StopMediaObserverRunnable implements Runnable {
        public int currentObserverToken = 0;

        @Override
        public void run() {
            if (currentObserverToken == startObserverToken) {
                try {
                    if (internalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(internalObserver);
                        internalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                try {
                    if (externalObserver != null) {
                        ApplicationLoader.applicationContext.getContentResolver().unregisterContentObserver(externalObserver);
                        externalObserver = null;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        }
    }
    private String[] mediaProjections = null;

    private static volatile MediaController Instance = null;
    public static MediaController getInstance() {
        MediaController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MediaController();
                }
            }
        }
        return localInstance;
    }

    public MediaController() {
        fileBuffer = ByteBuffer.allocateDirect(1920);

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
//        mobileDataDownloadMask = preferences.getInt("mobileDataDownloadMask", AUTODOWNLOAD_MASK_PHOTO | AUTODOWNLOAD_MASK_AUDIO);
//        wifiDownloadMask = preferences.getInt("wifiDownloadMask", AUTODOWNLOAD_MASK_PHOTO | AUTODOWNLOAD_MASK_AUDIO);
        mobileDataDownloadMask = preferences.getInt("mobileDataDownloadMask", AUTODOWNLOAD_MASK_PHOTO);
        wifiDownloadMask = preferences.getInt("wifiDownloadMask", AUTODOWNLOAD_MASK_PHOTO);
        roamingDownloadMask = preferences.getInt("roamingDownloadMask", 0);
        saveToGallery = preferences.getBoolean("save_gallery", false);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidFailedLoad);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileLoadProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.FileUploadProgressChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.messagesDeleted);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.removeAllMessagesFromDialog);

        BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAutodownloadSettings();
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        ApplicationLoader.applicationContext.registerReceiver(networkStateReceiver, filter);

        if (UserConfig.isClientActivated()) {
            checkAutodownloadSettings();
        }

        if (Build.VERSION.SDK_INT >= 16) {
            mediaProjections = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.TITLE,
                    MediaStore.Images.ImageColumns.WIDTH,
                    MediaStore.Images.ImageColumns.HEIGHT
            };
        } else {
            mediaProjections = new String[] {
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.TITLE
            };
        }
    }


    public void cleanup() {
        typingTimes.clear();
    }

    protected int getAutodownloadMask() {
        int mask = 0;
        if ((mobileDataDownloadMask & AUTODOWNLOAD_MASK_PHOTO) != 0 || (wifiDownloadMask & AUTODOWNLOAD_MASK_PHOTO) != 0 || (roamingDownloadMask & AUTODOWNLOAD_MASK_PHOTO) != 0) {
            mask |= AUTODOWNLOAD_MASK_PHOTO;
        }
        return mask;
    }

    public void checkAutodownloadSettings() {
        int currentMask = getCurrentDownloadMask();
        if (currentMask == lastCheckMask) {
            return;
        }
        lastCheckMask = currentMask;

        int mask = getAutodownloadMask();
//        if (mask == 0) {
//            MessagesStorage.getInstance().clearDownloadQueue(0);
//        } else {
//            if ((mask & AUTODOWNLOAD_MASK_PHOTO) == 0) {
//                MessagesStorage.getInstance().clearDownloadQueue(AUTODOWNLOAD_MASK_PHOTO);
//            }
//        }
    }

    public boolean canDownloadMedia(int type) {
        return (getCurrentDownloadMask() & type) != 0;
    }

    //TODO NEED
    private int getCurrentDownloadMask() {
        if (ConnectionsManager.isConnectedToWiFi()) {
            return wifiDownloadMask;
        } else if(ConnectionsManager.isRoaming()) {
            return roamingDownloadMask;
        } else {
            return mobileDataDownloadMask;
        }
    }


    private void checkDownloadFinished(String fileName, int state) {
//        DownloadObject downloadObject = downloadQueueKeys.get(fileName);
//        if (downloadObject != null) {
//            downloadQueueKeys.remove(fileName);
//            if (state == 0 || state == 2) {
//                MessagesStorage.getInstance().removeFromDownloadQueue(downloadObject.id, downloadObject.type, false /*state != 0*/);
//            }
//            if (downloadObject.type == AUTODOWNLOAD_MASK_PHOTO) {
//                photoDownloadQueue.remove(downloadObject);
//                if (photoDownloadQueue.isEmpty()) {
//                    newDownloadObjectsAvailable(AUTODOWNLOAD_MASK_PHOTO);
//                }
//            }
//        }
    }

    public void startMediaObserver() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return;
        }
        ApplicationLoader.applicationHandler.removeCallbacks(stopMediaObserverRunnable);
        startObserverToken++;
        try {
            if (internalObserver == null) {
                ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, externalObserver = new ExternalObserver());
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            if (externalObserver == null) {
                ApplicationLoader.applicationContext.getContentResolver().registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, false, internalObserver = new InternalObserver());
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public void stopMediaObserver() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return;
        }
        if (stopMediaObserverRunnable == null) {
            stopMediaObserverRunnable = new StopMediaObserverRunnable();
        }
        stopMediaObserverRunnable.currentObserverToken = startObserverToken;
        ApplicationLoader.applicationHandler.postDelayed(stopMediaObserverRunnable, 5000);
    }

    public void processMediaObserver(Uri uri) {
        try {
            Point size = AndroidUtilities.getRealScreenSize();

            Cursor cursor = ApplicationLoader.applicationContext.getContentResolver().query(uri, mediaProjections, null, null, "date_added DESC LIMIT 1");
            final ArrayList<Long> screenshotDates = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String val = "";
                    String data = cursor.getString(0);
                    String display_name = cursor.getString(1);
                    String album_name = cursor.getString(2);
                    long date = cursor.getLong(3);
                    String title = cursor.getString(4);
                    int photoW = 0;
                    int photoH = 0;
                    if (Build.VERSION.SDK_INT >= 16) {
                        photoW = cursor.getInt(5);
                        photoH = cursor.getInt(6);
                    }
                    if (data != null && data.toLowerCase().contains("screenshot") ||
                            display_name != null && display_name.toLowerCase().contains("screenshot") ||
                            album_name != null && album_name.toLowerCase().contains("screenshot") ||
                            title != null && title.toLowerCase().contains("screenshot")) {
                        try {
                            if (photoW == 0 || photoH == 0) {
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                bmOptions.inJustDecodeBounds = true;
                                BitmapFactory.decodeFile(data, bmOptions);
                                photoW = bmOptions.outWidth;
                                photoH = bmOptions.outHeight;
                            }
                            if (photoW <= 0 || photoH <= 0 || (photoW == size.x && photoH == size.y || photoH == size.x && photoW == size.y)) {
                                screenshotDates.add(date);
                            }
                        } catch (Exception e) {
                            screenshotDates.add(date);
                        }
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }


    public int generateObserverTag() {
        return lastTag++;
    }

    public void addLoadingFileObserver(String fileName, FileDownloadProgressListener observer) {
        if (listenerInProgress) {
            addLaterArray.put(fileName, observer);
            return;
        }
        removeLoadingFileObserver(observer);

        ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
        if (arrayList == null) {
            arrayList = new ArrayList<>();
            loadingFileObservers.put(fileName, arrayList);
        }
        arrayList.add(new WeakReference<>(observer));

        observersByTag.put(observer.getObserverTag(), fileName);
    }

    public void removeLoadingFileObserver(FileDownloadProgressListener observer) {
        if (listenerInProgress) {
            deleteLaterArray.add(observer);
            return;
        }
        String fileName = observersByTag.get(observer.getObserverTag());
        if (fileName != null) {
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (int a = 0; a < arrayList.size(); a++) {
                    WeakReference<FileDownloadProgressListener> reference = arrayList.get(a);
                    if (reference.get() == null || reference.get() == observer) {
                        arrayList.remove(a);
                        a--;
                    }
                }
                if (arrayList.isEmpty()) {
                    loadingFileObservers.remove(fileName);
                }
            }
            observersByTag.remove(observer.getObserverTag());
        }
    }

    private void processLaterArrays() {
        for (HashMap.Entry<String, FileDownloadProgressListener> listener : addLaterArray.entrySet()) {
            addLoadingFileObserver(listener.getKey(), listener.getValue());
        }
        addLaterArray.clear();
        for (FileDownloadProgressListener listener : deleteLaterArray) {
            removeLoadingFileObserver(listener);
        }
        deleteLaterArray.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.FileDidFailedLoad) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onFailedDownload(fileName);
                        observersByTag.remove(reference.get().getObserverTag());
                    }
                }
                loadingFileObservers.remove(fileName);
            }
            listenerInProgress = false;
            processLaterArrays();
            checkDownloadFinished(fileName, (Integer)args[1]);
        } else if (id == NotificationCenter.FileDidLoaded) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onSuccessDownload(fileName);
                        observersByTag.remove(reference.get().getObserverTag());
                    }
                }
                loadingFileObservers.remove(fileName);
            }
            listenerInProgress = false;
            processLaterArrays();
            checkDownloadFinished(fileName, 0);
        } else if (id == NotificationCenter.FileLoadProgressChanged) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                Float progress = (Float)args[1];
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onProgressDownload(fileName, progress);
                    }
                }
            }
            listenerInProgress = false;
            processLaterArrays();
        } else if (id == NotificationCenter.FileUploadProgressChanged) {
            listenerInProgress = true;
            String fileName = (String)args[0];
            ArrayList<WeakReference<FileDownloadProgressListener>> arrayList = loadingFileObservers.get(fileName);
            if (arrayList != null) {
                Float progress = (Float)args[1];
                Boolean enc = (Boolean)args[2];
                for (WeakReference<FileDownloadProgressListener> reference : arrayList) {
                    if (reference.get() != null) {
                        reference.get().onProgressUpload(fileName, progress, enc);
                    }
                }
            }
            listenerInProgress = false;
            processLaterArrays();
            try {
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }



    public static void saveFile(String fullPath, Context context, final int type, final String name) {
        if (fullPath == null) {
            return;
        }

        File file = null;
        if (fullPath != null && fullPath.length() != 0) {
            file = new File(fullPath);
            if (!file.exists()) {
                file = null;
            }
        }

        if (file == null) {
            return;
        }

        final File sourceFile = file;
        if (sourceFile.exists()) {
            ProgressDialog progressDialog = null;
            if (context != null) {
                try {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMax(100);
                    progressDialog.show();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }

            final ProgressDialog finalProgress = progressDialog;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File destFile = null;
                        if (type == 0) {
                            destFile = AndroidUtilities.generatePicturePath();
                        } else if (type == 1) {
                            destFile = AndroidUtilities.generateVideoPath();
                        } else if (type == 2) {
                            File f = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                            destFile = new File(f, name);
                        }

                        if(!destFile.exists()) {
                            destFile.createNewFile();
                        }
                        FileChannel source = null;
                        FileChannel destination = null;
                        boolean result = true;
                        long lastProgress = System.currentTimeMillis() - 500;
                        try {
                            source = new FileInputStream(sourceFile).getChannel();
                            destination = new FileOutputStream(destFile).getChannel();
                            long size = source.size();
                            for (long a = 0; a < size; a += 4096) {
                                destination.transferFrom(source, a, Math.min(4096, size - a));
                                if (finalProgress != null) {
                                    if (lastProgress <= System.currentTimeMillis() - 500) {
                                        lastProgress = System.currentTimeMillis();
                                        final int progress = (int) ((float) a / (float) size * 100);
                                        AndroidUtilities.runOnUIThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    finalProgress.setProgress(progress);
                                                } catch (Exception e) {
                                                    FileLog.e("tmessages", e);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                            result = false;
                        } finally {
                            if (source != null) {
                                source.close();
                            }
                            if (destination != null) {
                                destination.close();
                            }
                        }

                        if (result && (type == 0 || type == 1)) {
                            AndroidUtilities.addMediaToGallery(Uri.fromFile(destFile));
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    if (finalProgress != null) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    finalProgress.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }





    public static boolean isWebp(Uri uri) {
        ParcelFileDescriptor parcelFD = null;
        FileInputStream input = null;
        try {
            parcelFD = ApplicationLoader.applicationContext.getContentResolver().openFileDescriptor(uri, "r");
            input = new FileInputStream(parcelFD.getFileDescriptor());
            if (input.getChannel().size() > 12) {
                byte[] header = new byte[12];
                input.read(header, 0, 12);
                String str = new String(header);
                if (str != null) {
                    str = str.toLowerCase();
                    if (str.startsWith("riff") && str.endsWith("webp")){
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        } finally {
            try {
                if (parcelFD != null) {
                    parcelFD.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e2) {
                FileLog.e("tmessages", e2);
            }
        }
        return false;
    }




    public void toggleSaveToGallery() {
        saveToGallery = !saveToGallery;
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("save_gallery", saveToGallery);
        editor.commit();
        checkSaveToGalleryFiles();
    }

    public void checkSaveToGalleryFiles() {
        try {
            File telegramPath = new File(Environment.getExternalStorageDirectory(), Constants.WGO);
            File imagePath = new File(telegramPath, Constants.WGO_IMAGE);
            imagePath.mkdir();
            File videoPath = new File(telegramPath, Constants.WGO_VIDEO);
            videoPath.mkdir();

            if (saveToGallery) {
                if (imagePath.isDirectory()) {
                    new File(imagePath, ".nomedia").delete();
                }
                if (videoPath.isDirectory()) {
                    new File(videoPath, ".nomedia").delete();
                }
            } else {
                if (imagePath.isDirectory()) {
                    new File(imagePath, ".nomedia").createNewFile();
                }
                if (videoPath.isDirectory()) {
                    new File(videoPath, ".nomedia").createNewFile();
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public boolean canSaveToGallery() {
        return saveToGallery;
    }

    public static void loadGalleryPhotosAlbums(final int guid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<AlbumEntry> albumsSorted = new ArrayList<>();
                final ArrayList<AlbumEntry> videoAlbumsSorted = new ArrayList<>();
                HashMap<Integer, AlbumEntry> albums = new HashMap<>();
                AlbumEntry allPhotosAlbum = null;
                String cameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/" + "Camera/";
                Integer cameraAlbumId = null;
                Integer cameraAlbumVideoId = null;

                Cursor cursor = null;
                try {
                    cursor = MediaStore.Images.Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
                        int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                        int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                        int orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

                        while (cursor.moveToNext()) {
                            int imageId = cursor.getInt(imageIdColumn);
                            int bucketId = cursor.getInt(bucketIdColumn);
                            String bucketName = cursor.getString(bucketNameColumn);
                            String path = cursor.getString(dataColumn);
                            long dateTaken = cursor.getLong(dateColumn);
                            int orientation = cursor.getInt(orientationColumn);

                            if (path == null || path.length() == 0) {
                                continue;
                            }

                            PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, orientation, false);

                            if (allPhotosAlbum == null) {
                                allPhotosAlbum = new AlbumEntry(0, LocaleController.getString("AllPhotos", R.string.AllPhotos), photoEntry, false);
                                albumsSorted.add(0, allPhotosAlbum);
                            }
                            if (allPhotosAlbum != null) {
                                allPhotosAlbum.addPhoto(photoEntry);
                            }

                            AlbumEntry albumEntry = albums.get(bucketId);
                            if (albumEntry == null) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, false);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                    albumsSorted.add(0, albumEntry);
                                    cameraAlbumId = bucketId;
                                } else {
                                    albumsSorted.add(albumEntry);
                                }
                            }

                            albumEntry.addPhoto(photoEntry);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                } finally {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }

                try {
                    albums.clear();
                    allPhotosAlbum = null;
                    cursor = MediaStore.Images.Media.query(ApplicationLoader.applicationContext.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projectionVideo, "", null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
                    if (cursor != null) {
                        int imageIdColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                        int bucketIdColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID);
                        int bucketNameColumn = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
                        int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        int dateColumn = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN);

                        while (cursor.moveToNext()) {
                            int imageId = cursor.getInt(imageIdColumn);
                            int bucketId = cursor.getInt(bucketIdColumn);
                            String bucketName = cursor.getString(bucketNameColumn);
                            String path = cursor.getString(dataColumn);
                            long dateTaken = cursor.getLong(dateColumn);

                            if (path == null || path.length() == 0) {
                                continue;
                            }

                            PhotoEntry photoEntry = new PhotoEntry(bucketId, imageId, dateTaken, path, 0, true);

                            if (allPhotosAlbum == null) {
                                allPhotosAlbum = new AlbumEntry(0, LocaleController.getString("AllVideo", R.string.AllVideo), photoEntry, true);
                                videoAlbumsSorted.add(0, allPhotosAlbum);
                            }
                            if (allPhotosAlbum != null) {
                                allPhotosAlbum.addPhoto(photoEntry);
                            }

                            AlbumEntry albumEntry = albums.get(bucketId);
                            if (albumEntry == null) {
                                albumEntry = new AlbumEntry(bucketId, bucketName, photoEntry, true);
                                albums.put(bucketId, albumEntry);
                                if (cameraAlbumVideoId == null && cameraFolder != null && path != null && path.startsWith(cameraFolder)) {
                                    videoAlbumsSorted.add(0, albumEntry);
                                    cameraAlbumVideoId = bucketId;
                                } else {
                                    videoAlbumsSorted.add(albumEntry);
                                }
                            }

                            albumEntry.addPhoto(photoEntry);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                } finally {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }
                }

                final Integer cameraAlbumIdFinal = cameraAlbumId;
                final Integer cameraAlbumVideoIdFinal = cameraAlbumVideoId;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.albumsDidLoaded, guid, albumsSorted, cameraAlbumIdFinal, videoAlbumsSorted, cameraAlbumVideoIdFinal);
                    }
                });
            }
        }).start();
    }


}
