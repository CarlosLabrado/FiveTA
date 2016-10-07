package com.app_labs.fiveta.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.ui.Friends.FriendsFragment;
import com.app_labs.fiveta.ui.favorites.FavoritesFragment;
import com.app_labs.fiveta.ui.group.GroupFragment;
import com.app_labs.fiveta.ui.personal.PersonalFragment;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.LogUtil;
import com.app_labs.fiveta.util.Utils;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Stack;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottonnavigation.BuildConfig;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class MainActivity extends AppCompatActivity implements BottomNavigation.OnMenuItemSelectionListener {

    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.bottomNavigation)
    BottomNavigation mBottomNavigation;
    @Bind(R.id.coordinatorLayout)
    View mView;

    private static Stack<Integer> mTabStack;

    @Bind(R.id.progressBarMain)
    ProgressBar mProgressBarMain;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;


    private User mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomNavigation.DEBUG = BuildConfig.DEBUG;

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference(Constants.FIREBASE_LOCATION_USERS);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(LoginActivity.createIntent(this));
            finish();
            return;
        }


        mValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mCurrentUser = dataSnapshot.getValue(User.class);
                    if (mCurrentUser.getImageUrl() == null || mCurrentUser.getImageUrl().isEmpty()) {
                        askForImage(Utils.encodeEmail(mCurrentUser.getEmail()));
                    }
                    mProgressBarMain.setVisibility(View.GONE);

                    displayView(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child(Utils.encodeEmail(currentUser.getEmail())).addListenerForSingleValueEvent(mValueEventListener);


        mTabStack = new Stack<>();

        /** toolBar **/
        setUpToolBar();

        initializeBottomNavigation(savedInstanceState);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String encodedEmail = sp.getString(Constants.KEY_EMAIL, null);


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
//            displayView(0);
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

        if (!isFinishing()) {
            mTabStack.push(position);
            switch (position) {
                case 0:
                    new PersonalFragment();
                    fragment = PersonalFragment.newInstance(mCurrentUser);
                    break;
                case 1:
                    fragment = new GroupFragment();
                    break;
                case 2:
                    fragment = new FavoritesFragment();
                    break;
                case 3:
                    fragment = new FriendsFragment();
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
                        .commitAllowingStateLoss(); // why? http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
                LogUtil.logD(TAG, "fragment added " + fragment.getTag());
            } else {
                // error in creating fragment
                LogUtil.logE(TAG, "Error in creating fragment");
            }
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

    public void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(LoginActivity.createIntent(MainActivity.this));
                            finish();
                        } else {
                            showSnackBar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        mDatabaseReference.removeEventListener(mValueEventListener);
    }

    @MainThread
    private void showSnackBar(@StringRes int errorMessageRes) {
        Snackbar.make(mView, errorMessageRes, Snackbar.LENGTH_LONG)
                .show();
    }

    private void askForImage(final String userKey) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.dialog_no_image))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.dialog_no_image_confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EasyImage.openChooserWithGallery(MainActivity.this, "Pick Image", 0);

                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }


        });
    }

    private void onPhotoReturned(File imageFile) {
        mProgressBarMain.setVisibility(View.VISIBLE);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReferenceFromUrl(Constants.FIREBASE_BUCKET);
        final String imageName = Utils.encodeEmail(mCurrentUser.getEmail()) + ".jpg";
        StorageReference imageRef = storageRef.child(Constants.USER_FRIENDS_IMAGES).child(imageName);

        Uri file = Uri.fromFile(imageFile);
        UploadTask uploadTask = imageRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }

        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                updateUserWithImagePath(imageName);
                mProgressBarMain.setVisibility(View.GONE);
            }
        });
    }

    private void updateUserWithImagePath(String imageName) {
        mDatabaseReference.child(Utils.encodeEmail(mCurrentUser.getEmail())).child(Constants.IMAGE_URL_FIELD_NAME).setValue(imageName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, MainActivity.class);
        return in;
    }
}
