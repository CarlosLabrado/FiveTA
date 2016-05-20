package com.app_labs.fiveta.events;

import com.app_labs.fiveta.model.User;

/**
 * Created by Vazh on 20/5/2016.
 */
public class SelectedFriendFromDialogEvent {
    User mUser;

    public SelectedFriendFromDialogEvent(User model) {
        mUser = model;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }
}
