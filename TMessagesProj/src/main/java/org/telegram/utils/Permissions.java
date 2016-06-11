package org.telegram.utils;

import android.Manifest;

/**
 * Created by aragats on 05/06/16.
 */
public class Permissions {

    public static boolean locationPermitted = false;
    public static boolean cameraPermitted = false;
    public static boolean storagePermitted = false;


    //    Permissions
//Runtime Permissions
//https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en
//https://www.captechconsulting.com/blogs/runtime-permissions-best-practices-and-how-to-gracefully-handle-permission-removal
    ///http://stackoverflow.com/questions/32083913/android-gps-requires-access-fine-location-error-even-though-my-manifest-file
    //Request Permission in Realtime
    public static final String[] LOCATION_PERMISSION_GROUP = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] CAMERA_PERMISSION_GROUP = {
            Manifest.permission.CAMERA
    };
    public static final String[] STORAGE_PERMISSION_GROUP = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final int LOCATION_REQUEST_CODE = 1;
    public static final int STORAGE_REQUEST = 2;
    public static final int CAMERA_REQUEST = 3;

}
