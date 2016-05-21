package com.app_labs.fiveta.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Defines the data structure for User objects.
 */
public class User implements Parcelable {
    private String name;
    private String email;
    private String imageUrl;
    private HashMap<String, Object> timestampJoined;

    /**
     * Required public constructor
     */
    public User() {
    }

    /**
     * Use this constructor to create new User.
     * Takes user name, email and timestampJoined as params
     *
     * @param name
     * @param email
     * @param timestampJoined
     */
    public User(String name, String email, String imageUrl, HashMap<String, Object> timestampJoined) {
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.timestampJoined = timestampJoined;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }


    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        imageUrl = in.readString();
        timestampJoined = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(imageUrl);
        dest.writeValue(timestampJoined);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}