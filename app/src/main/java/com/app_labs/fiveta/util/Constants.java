package com.app_labs.fiveta.util;


import com.app_labs.fiveta.BuildConfig;

/**
 * Constants class store most important strings and paths of the app
 */
public class Constants {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;


    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_WELLS = "wells";

    public static final String FIREBASE_URL_WELLS = FIREBASE_URL + "/" + FIREBASE_LOCATION_WELLS;
    public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;

    public static final String KEY_EMAIL = "EMAIL";

    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";

    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";


    public static final String EXTRA_USER = "extraUser";

    public static final String USER_FRIENDS = "friends";
    public static final String FIREBASE_BUCKET = "gs://five-1302.appspot.com";
    public static final String USER_FRIENDS_IMAGES = "userImages";
    public static final String PERSONAL_ETAS = "personal";
}
