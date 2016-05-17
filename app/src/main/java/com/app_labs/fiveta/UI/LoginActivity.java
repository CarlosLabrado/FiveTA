package com.app_labs.fiveta.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.Toast;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.User;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.LogUtil;
import com.app_labs.fiveta.util.Utils;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.auth.core.AuthProviderType;
import com.firebase.ui.auth.core.FirebaseLoginBaseActivity;
import com.firebase.ui.auth.core.FirebaseLoginError;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LoginActivity extends FirebaseLoginBaseActivity {
    @Bind(R.id.buttonLogin)
    Button mButtonLogin;

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Firebase mFirebaseRef;

    @Override
    protected void onStart() {
        super.onStart();
        // All providers are optional! Remove any you don't want.
//        setEnabledAuthProvider(AuthProviderType.FACEBOOK);
//        setEnabledAuthProvider(AuthProviderType.TWITTER);
//        setEnabledAuthProvider(AuthProviderType.GOOGLE);
        setEnabledAuthProvider(AuthProviderType.PASSWORD);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mFirebaseRef = new Firebase(Constants.FIREBASE_URL);

    }

    @Override
    protected Firebase getFirebaseRef() {
        return mFirebaseRef;
    }

    @Override
    protected void onFirebaseLoginProviderError(FirebaseLoginError firebaseLoginError) {
        dismissFirebaseLoginPrompt();
        Toast.makeText(this, "There is a connection error, please try again", Toast.LENGTH_SHORT).show();
        showFirebaseLoginPrompt();
    }

    @Override
    protected void onFirebaseLoginUserError(FirebaseLoginError firebaseLoginError) {
        dismissFirebaseLoginPrompt();
        Toast.makeText(this, "Non valid credentials, please try again", Toast.LENGTH_SHORT).show();
        showFirebaseLoginPrompt();
    }

    @Override
    public void onFirebaseLoggedIn(AuthData authData) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spe = sp.edit();
        String unprocessedEmail;

        unprocessedEmail = authData.getProviderData().get("email").toString().toLowerCase();

        final String encodedEmail = Utils.encodeEmail(unprocessedEmail);
        spe.putString(Constants.KEY_EMAIL, encodedEmail).apply();

        final String userName = (String) authData.getProviderData().get(Constants.PROVIDER_DATA_DISPLAY_NAME);

             /* If no user exists, make a user */
        final Firebase userLocation = new Firebase(Constants.FIREBASE_URL_USERS).child(encodedEmail);
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                            /* If nothing is there ...*/
                if (dataSnapshot.getValue() == null) {
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

                    User newUser = new User(userName, encodedEmail, timestampJoined, null);
                    userLocation.setValue(newUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                LogUtil.logD(TAG, getString(R.string.log_error_occurred) + firebaseError.getMessage());
            }
        });

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFirebaseLoggedOut() {
        // TODO: Handle logout
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @OnClick(R.id.buttonLogin)
    public void onClick() {
        showFirebaseLoginPrompt();
    }
}
