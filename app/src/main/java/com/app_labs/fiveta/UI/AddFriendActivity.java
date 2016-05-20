package com.app_labs.fiveta.ui;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.util.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddFriendActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.friendLists)
    RecyclerView mFriendListsRecyclerView;
    @Bind(R.id.addFriendRootView)
    CoordinatorLayout mRootView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private Query mUsersRef;

    private LinearLayoutManager mManager;
    private FirebaseRecyclerAdapter<User, UserHolder> mRecyclerViewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        ButterKnife.bind(this);
        /** toolBar **/
        setUpToolBar();

        mAuth = FirebaseAuth.getInstance();

        mRef = FirebaseDatabase.getInstance().getReference();
        mUsersRef = mRef.child("users");

        populateRecyclerAdapter();
    }

    private void populateRecyclerAdapter() {
        mManager = new LinearLayoutManager(this);
        mFriendListsRecyclerView.setLayoutManager(mManager);

        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<User, UserHolder>(
                User.class, R.layout.item_users, UserHolder.class, mUsersRef) {

            @Override
            protected void populateViewHolder(UserHolder viewHolder, final User user, int position) {
                viewHolder.setName(user.getName());
                viewHolder.setEmail(user.getEmail());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String selectedUserKey = Utils.encodeEmail(user.getEmail());
                        createFriendRelationship(user);

                    }
                });

            }
        };

        mFriendListsRecyclerView.setAdapter(mRecyclerViewAdapter);
    }

    /**
     * Firebase Magic, adds the encoded email as a key in friends
     *
     * @param selectedUser selected user
     */
    private void createFriendRelationship(User selectedUser) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        DatabaseReference friendsReference = FirebaseDatabase.getInstance().getReference("friends");

        if (currentUser != null) {
            String currentLoggedUser = Utils.encodeEmail(currentUser.getEmail());
            String selectedUserId = Utils.encodeEmail(selectedUser.getEmail());
            if (selectedUserId.equalsIgnoreCase(currentLoggedUser)) {
                showSnackBar(R.string.error_cant_friend_yourself);
            } else {
                friendsReference.child(currentLoggedUser).child(selectedUserId).setValue(selectedUser);
                //originalUserReference.child(Constants.FIREBASE_LOCATION_USERS).child(currentLoggedUser).child("friends").child(selectedUserKey).setValue(true);
                showSnackBar(R.string.snack_friend_added);
                finish();
            }
        }
    }

    /**
     * sets up the top bar
     */
    public void setUpToolBar() {
        setSupportActionBar(mToolbar);
        setActionBarTitle(getString(R.string.friends_add_toolbar), null, true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            // enabling action bar app icon and behaving it as toggle button
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    /**
     * Gets called from the fragments onResume and its because only the first doesn't have the up
     * button on the actionBar
     *
     * @param title          The title to show on the ActionBar
     * @param subtitle       The subtitle to show on the ActionBar
     * @param showNavigateUp if true, shows the up button
     */
    public void setActionBarTitle(String title, String subtitle, boolean showNavigateUp) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            if (subtitle != null) {
                getSupportActionBar().setSubtitle(subtitle);
            } else {
                getSupportActionBar().setSubtitle(null);
            }
            if (showNavigateUp) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        View mView;

        public UserHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.textViewUsersName);
            field.setText(name);
        }

        public void setEmail(String text) {
            TextView field = (TextView) mView.findViewById(R.id.textViewUsersEmail);
            field.setText(text);
        }
    }

    @MainThread
    private void showSnackBar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG)
                .show();
    }
}
