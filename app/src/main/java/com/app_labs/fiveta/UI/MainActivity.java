package com.app_labs.fiveta.ui;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.widget.Toast;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.LogUtil;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

import java.util.Stack;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottonnavigation.BuildConfig;

public class MainActivity extends FirebaseLoginBaseActivity implements BottomNavigation.OnMenuItemSelectionListener {

    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.bottomNavigation)
    BottomNavigation mBottomNavigation;

    private static Stack<Integer> mTabStack;

    private Firebase mFirebaseRef;
    private ValueEventListener mUserRefListener;

    private Firebase mUserRef;

    private User mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomNavigation.DEBUG = BuildConfig.DEBUG;

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

        mTabStack = new Stack<>();

        /** toolBar **/
        setUpToolBar();

        initializeBottomNavigation(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String encodedEmail = sp.getString(Constants.KEY_EMAIL, null);
        mUserRef = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        /**
         * Add ValueEventListeners to Firebase references
         * to control get data and control behavior and visibility of elements
         */
        mUserRefListener = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mCurrentUser = snapshot.getValue(User.class);

                /**
                 * Set the activity title to current user name if user is not null
                 */
                if (mCurrentUser != null) {
                    /* Assumes that the first word in the user's name is the user's first name. */
                    try {
//                        String firstName = mCurrentUser.getName().split("\\s+")[0];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                LogUtil.logE(TAG,
                        getString(R.string.log_error_the_read_failed) +
                                firebaseError.getMessage());
            }
        });


    }

    /**
     * sets up the top bar
     */
    public void setUpToolBar() {
        setSupportActionBar(toolbar);
        setActionBarTitle(getResources().getString(R.string.app_name), null, false);
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

    public BottomNavigation getBottomNavigation() {
        return mBottomNavigation;
    }

    protected void initializeBottomNavigation(final Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            mBottomNavigation.setOnMenuItemClickListener(this);
            mBottomNavigation.setDefaultSelectedIndex(0);
            displayView(0);
        }
    }

    @Override
    public void onMenuItemSelect(final int itemId, int position) {
        LogUtil.logI(TAG, "onMenuItemSelect(" + itemId + ", " + position + ")");
        displayView(position);
    }

    @Override
    public void onMenuItemReselect(@IdRes int itemId, int position) {
        LogUtil.logI(TAG, "onMenuItemReselect(" + itemId + ", " + position + ")");

    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        mTabStack.push(position);
        switch (position) {
            case 0:
                fragment = new PersonalFragment();
                break;
            case 1:
                fragment = new GroupFragment();
                break;
            case 2:
                fragment = new FavoritesFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.explode));
                fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(android.R.transition.fade));
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, fragment)
                    .commit();
            LogUtil.logD(TAG, "fragment added " + fragment.getTag());
        } else {
            // error in creating fragment
            LogUtil.logE(TAG, "Error in creating fragment");
        }
    }

    /**
     * If we press back we kill the fragment and the last fragment shows on the container, so we
     * also have to show the tab that that container had.
     * So we use a tab stack.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            if (!mTabStack.empty()) {
                mTabStack.pop();
                if (!mTabStack.empty()) {
                    mBottomNavigation.setSelectedIndex(mTabStack.peek(), true);
                } else {
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserRef.removeEventListener(mUserRefListener);
    }

    @Override
    protected Firebase getFirebaseRef() {
        return mFirebaseRef;
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseLoginError) {
        dismissFirebaseLoginPrompt();
        Toast.makeText(this, "There is a connection error, please try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseLoginError) {
        dismissFirebaseLoginPrompt();
        Toast.makeText(this, "Non valid credentials, please try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        Toast.makeText(this, "Hi ", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFirebaseLoggedOut() {
        finish();
        // TODO: Handle logout
    }
}
