package com.app_labs.fiveta.model;

import java.util.HashMap;

/**
 * Defines the data structure for User objects.
 */
public class User {
    private String name;
    private String email;
    private HashMap<String, Object> timestampJoined;
    private HashMap<String, Object> assignedWells;

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
    public User(String name, String email, HashMap<String, Object> timestampJoined, HashMap<String, Object> assignedWells) {
        this.name = name;
        this.email = email;
        this.timestampJoined = timestampJoined;
        this.assignedWells = assignedWells;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }


    public HashMap<String, Object> getAssignedWells() {
        return assignedWells;
    }
}