package org.telegram.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by aragats on 15/05/15.
 */
//#607d8b - color of launcher icon
public class Constants {
    public static final String APP_FOLDER = "Aracle";
    public static final String APP_IMAGE_FOLDER = APP_FOLDER + " Images";
    public static final String APP_VIDEO_FOLDER = APP_FOLDER + " Videos";
    public static final String APP_AUDIO_FOLDER = APP_FOLDER + " Audios";
    public static final String APP_DOCUMENT_FOLDER = APP_FOLDER + " Documents";
    public static final String AppFaqUrl = "AppFaqUrl";
    public static final String App_FAQ = "APP_FAQ";


    public static final String PREF_NEW_POST_TEXT = "NEW_POST_TEXT";
    public static final String PREF_NEW_POST_PHOTO = "NEW_POST_PHOTO";


    public static int POST_COUNT = 20;

    public static final int MAX_TEXT_LENGTH = 140;
    //https://new.vk.com/dev/photos.search Radius of search in meters (works very approximately). Available values: 10, 100, 800, 6000, 50000.
    public static final int RADIUS = 800; // meters 8000 for VK
    public static final int RADIUS_4SQUARE = 1000;//
    //    public static final int RADIUS_BROWSER = 100_000;
//    public static final int RADIUS_CHECKIN = 800;
    public static final String FOURSQUARE_BROWSER = "browse"; // return small and only in the radius
    public static final String FOURSQUARE_CHECKIN = "checkin"; // return more results a lot of user created places.
    public static final double MAX_DISTANCE_DEGREE = 0.003; // +200 meters.
    //https://en.wikipedia.org/wiki/Decimal_degrees


    public static final String RESTRICTED_AREA = "restricted_area";
    public static final String RADIUS_ARG = "radius";
    public static final String SEARCH_PLACES_ENABLE_ARG = "search_places_enable";


    // sizes from VK
    public static final int PHOTO_WIDTH_2560 = 2560;
    public static final int PHOTO_HEIGHT_1920 = 1920;
    public static final int PHOTO_WIDTH_1280 = 1280;
    public static final int PHOTO_WIDTH_807 = 807;
    public static final int PHOTO_WIDTH_604 = 604;


    public static final int PHOTO_HEIGHT_1600 = 1600;


    //    https://colorlib.com/wp/size-of-the-instagram-picture/  1080x1350  1080x566 1080x1080 - instagram
    // width: 800 or 1080 or 1280 or 1920 or 2560
    // height: 1000 or 1350 or 1600 or 2400 or 3200s
    public static final int PHOTO_WIDTH_MAX = PHOTO_WIDTH_1280;
    public static final int PHOTO_HEIGHT_MAX = PHOTO_HEIGHT_1600;

    public static final int PHOTO_WIDTH_MIN = PHOTO_WIDTH_604;
    public static final int PHOTO_HEIGHT_MIN = PHOTO_WIDTH_604;

    //    http://photo.stackexchange.com/questions/30243/what-quality-to-choose-when-converting-to-jpg 40-60, 70-80, or 90-100
    public static final int PHOTO_QUALITY = 70; // 70 - was recommendation.


    public static final String EXTENSION_JPG = ".jpg";
    public static final String EXTENSION_JPEG = ".jpeg";
    public static final String EXTENSION_PNG = ".png";

    public static long PROGRESS_DIALOG_TIMEOUT = 500;

    public static double VK_API_VERSION = 5.52;


    public static String LOCAL_POSTS_FILENAME = "rtree";

    public static long TIME_DIFFERENCE = 1 * 24 * 60 * 60 * 1000; // 1 Day.

    public static String POINT = "Point";

    public static long MAX_DATE_SHIFT = 7 * 24 * 60 * 60 * 1000;

    public static final int MAX_RESULTS = 10;


    public static final int IMAGE_2_MB = 2 * 1024 * 1024; // 1 MB
    public static final int IMAGE_1_MB = 1024 * 1024; // 1 MB

    public static final String SIMPLE_DATE_FORMAT_PATTERN = "yyyy:MM:dd hh:mm:ss";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(SIMPLE_DATE_FORMAT_PATTERN, Locale.getDefault());


    // TODO cancelled or cancelled ??? Check it.
    public static final String CANCELLED_HTTP_EXCEPTION_MSG = "Canceled";
    public static final String SOCKET_CLOSED_HTTP_EXCEPTION_MSG = "Socket closed";
}