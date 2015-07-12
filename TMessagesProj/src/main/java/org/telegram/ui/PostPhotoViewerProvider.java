package org.telegram.ui;

import android.graphics.Bitmap;

import org.telegram.android.MessageObject;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.object.PostObject;

/**
 * Created by aragats on 05/07/15.
 */
//TODO-aragats. This is copy form PhotoViewerProvvider from PhotoViewer.
public interface PostPhotoViewerProvider {

    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(PostObject postObject);

    Bitmap getThumbForPhoto(PostObject postObject, int index);

//    void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index);

    public void willSwitchFromPhoto(PostObject postObject);


    void willHidePhotoViewer();

    boolean isPhotoChecked(int index);

    void setPhotoChecked(int index);

    void cancelButtonPressed();

    void sendButtonPressed(int index);

    int getSelectedCount();

    void updatePhotoAtIndex(int index);


    public static interface MyPhotoViewerProvider {
        public void willSwitchFromPhoto(PostObject postObject);
        public void willHidePhotoViewer();
        public boolean isPhotoChecked(int index);
        public void setPhotoChecked(int index);
        public void cancelButtonPressed();
        public void sendButtonPressed(int index);
        public int getSelectedCount();
    }
}