package org.telegram.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import ru.aragats.aracle.ApplicationLoader;

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


    public static boolean checkStoragePermission(Activity activity) {
        if (!Permissions.storagePermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.STORAGE_PERMISSION_GROUP, Permissions.STORAGE_REQUEST);
            return false;
        }
        storagePermitted = true;
        return true;
    }


    public static boolean checkLocationPermission(Activity activity) {
        //TODO check both permissions
        if (!Permissions.locationPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.LOCATION_PERMISSION_GROUP, Permissions.LOCATION_REQUEST_CODE);
            return false;
        }
        locationPermitted = true;
        return true;
    }

    public static boolean checkCameraPermission(Activity activity) {
        if (!Permissions.cameraPermitted && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(Permissions.CAMERA_PERMISSION_GROUP, Permissions.CAMERA_REQUEST);
            return false;
        }
        cameraPermitted = true;
        return true;
    }


}
