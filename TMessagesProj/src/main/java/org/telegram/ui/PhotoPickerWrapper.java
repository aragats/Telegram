/*
 * This is the source code of Telegram for Android v. 2.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui;

import org.telegram.android.MediaController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class PhotoPickerWrapper implements NotificationCenter.NotificationCenterDelegate {

    public interface PhotoPickerWrapperActivityDelegate {
        void didSelectPhotos(ArrayList<String> photos, ArrayList<String> captions, ArrayList<MediaController.SearchImage> webPhotos);

        boolean didSelectVideo(String path);

        void didBackButtonPressed();

        void startPhotoSelectActivity();
    }

    protected int classGuid = 0;

    private ArrayList<MediaController.AlbumEntry> albumsSorted = null;
    private HashMap<Integer, MediaController.PhotoEntry> selectedPhotos = new HashMap<>();
    private boolean loading = false;

    private boolean sendPressed = false;
    private boolean singlePhoto = false;

    private PhotoPickerWrapperActivityDelegate delegate;

    private BaseFragment baseFragment;


    public PhotoPickerWrapper(BaseFragment baseFragment, boolean singlePhoto) {
        this.classGuid = ConnectionsManager.getInstance().generateClassGuid();
        this.singlePhoto = singlePhoto;
        this.baseFragment = baseFragment;
        onFragmentCreate();
    }

    public boolean onFragmentCreate() {
        loading = true;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.cameraAlbumDidLoaded);
        return true;
    }

    public void onFragmentDestroy() {
        selectedPhotos = new HashMap<>();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.cameraAlbumDidLoaded);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.cameraAlbumDidLoaded) {
            int guid = (Integer) args[0];
            if (classGuid == guid) {
                albumsSorted = (ArrayList<MediaController.AlbumEntry>) args[1];
                loading = false;
                if (!albumsSorted.isEmpty()) {
                    openPhotoPicker(albumsSorted.get(0), 0);
                }
            }
        }
    }

    public void setDelegate(PhotoPickerWrapperActivityDelegate delegate) {
        this.delegate = delegate;
    }

    public void openPhotoPicker() {
        clearStates();
        MediaController.loadCameraPhotoAlbum(classGuid);
    }

    private void clearStates() {
        sendPressed = false;
        if (selectedPhotos != null) {
            selectedPhotos.clear();
        }
    }

    private void sendSelectedPhotos() {
        if (selectedPhotos.isEmpty() || delegate == null || sendPressed) {
            return;
        }
        sendPressed = true;
        ArrayList<String> photos = new ArrayList<>();
        ArrayList<String> captions = new ArrayList<>();
        for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
            MediaController.PhotoEntry photoEntry = entry.getValue();
            if (photoEntry.imagePath != null) {
                photos.add(photoEntry.imagePath);
                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
            } else if (photoEntry.path != null) {
                photos.add(photoEntry.path);
                captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
            }
        }
        ArrayList<MediaController.SearchImage> webPhotos = new ArrayList<>();


        delegate.didSelectPhotos(photos, captions, webPhotos);
    }


    private void openPhotoPicker(MediaController.AlbumEntry albumEntry, int type) {
        ArrayList<MediaController.SearchImage> recentImages = new ArrayList<>();
        HashMap<String, MediaController.SearchImage> selectedWebPhotos = new HashMap<>();

        PhotoPickerActivity fragment = new PhotoPickerActivity(type, albumEntry, selectedPhotos, selectedWebPhotos, recentImages, singlePhoto);
        fragment.setDelegate(new PhotoPickerActivity.PhotoPickerActivityDelegate() {
            @Override
            public void selectedPhotosChanged() {

            }

            @Override
            public void actionButtonPressed(boolean canceled) {
//                removeSelfFromStack();
                if (!canceled) {
                    sendSelectedPhotos();
                }
            }

            @Override
            public boolean didSelectVideo(String path) {
//                removeSelfFromStack();
                return delegate.didSelectVideo(path);
            }

            @Override
            public void backButtonPressed() {
//                removeSelfFromStack();
            }
        });

        baseFragment.presentFragment(fragment);

    }


}
