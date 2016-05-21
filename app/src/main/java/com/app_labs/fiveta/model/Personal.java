package com.app_labs.fiveta.model;

import java.util.HashMap;

/**
 * Created by Vazh on 20/5/2016.
 */
public class Personal {

    private HashMap<String, Object> owner;
    private HashMap<String, Object> sharedWith;
    private String message;
    private Long eta;
    private Long plusEta;
    private HashMap<String, Object> timestampCreated;
    private boolean active;
    private boolean completed;

    public Personal(HashMap<String, Object> owner, HashMap<String, Object> sharedWith, String message, Long eta, Long plusEta, HashMap<String, Object> timestampCreated, boolean active, boolean completed) {
        this.owner = owner;
        this.sharedWith = sharedWith;
        this.message = message;
        this.eta = eta;
        this.plusEta = plusEta;
        this.timestampCreated = timestampCreated;
        this.active = active;
        this.completed = completed;
    }


    public HashMap<String, Object> getOwner() {
        return owner;
    }

    public void setOwner(HashMap<String, Object> owner) {
        this.owner = owner;
    }

    public HashMap<String, Object> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(HashMap<String, Object> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getEta() {
        return eta;
    }

    public void setEta(Long eta) {
        this.eta = eta;
    }

    public Long getPlusEta() {
        return plusEta;
    }

    public void setPlusEta(Long plusEta) {
        this.plusEta = plusEta;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(HashMap<String, Object> timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
