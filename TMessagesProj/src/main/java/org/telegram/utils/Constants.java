package org.telegram.utils;

/**
 * Created by aragats on 15/05/15.
 */
public class Constants {
    public static final String WGO = "WGO";
    public static final String WGO_IMAGE = "WGO Images";
    public static final String WGO_VIDEO = "WGO Videos";
    public static final String WGO_AUDIO = "WGO Audios";
    public static final String WGO_DOCUMENT = "WGO Documents";
    public static final String WGOFaqUrl = "WGOFaqUrl";
    public static final String WGO_FAQ = "WGOFAQ";


    public static final String PREF_NEW_POST_TEXT = "NEW_POST_TEXT";
    public static final String PREF_NEW_POST_PHOTO = "NEW_POST_PHOTO";


    public static int POST_COUNT = 20;

    public static final int RADIUS = 800; // meters 8000 for VK
    public static final double MAX_DISTANCE_DEGREE = 0.01;


    public static final String RESTRICTED_AREA = "restricted_area";
    public static final String RADIUS_ARG = "radius";
    public static final String SEARCH_PLACES_ENABLE_ARG = "search_places_enable";


    // sizes from VK
    public static final int PHOTO_WIDTH_2560 = 2560;
    public static final int PHOTO_WIDTH_1280 = 1280;
    public static final int PHOTO_WIDTH_807 = 807;
    public static final int PHOTO_WIDTH_604 = 604;


    public static final int PHOTO_WIDTH_MAX = PHOTO_WIDTH_2560;

    public static final int PHOTO_WIDTH_MIN = PHOTO_WIDTH_604;
    public static final int PHOTO_HEIGHT_MIN = PHOTO_WIDTH_604;

    public static final int PHOTO_QUALITY = 70;


    public static final String EXTENSION_JPG = ".jpg";
    public static final String EXTENSION_JPEG = ".jpeg";
    public static final String EXTENSION_PNG = ".png";

    public static long PROGRESS_DIALOG_TIMEOUT = 500;


    public static String LOCAL_POSTS_FILENAME = "rtree";

    public static long TIME_DIFFERENCE = 1 * 24 * 60 * 60 * 1000; // 1 Day.

    public static String POINT = "Point";
}