package com.app_labs.fiveta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.model.Personal;
import com.app_labs.fiveta.util.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class PersonalCountDownService extends IntentService {
    public static final String ACTION_START_COUNTDOWN = "com.app_labs.fiveta.service.action.ACTION_START_COUNTDOWN";

    public static final String EXTRA_PARAM_PERSONAL_ETA_KEY = "com.app_labs.fiveta.service.extra.EXTRA_PARAM_PERSONAL_ETA_KEY";

    private String mPersonalETAKey;
    private DatabaseReference mRef;
    private int mInterval = 5000; // 1 second
    private long mETA;
    private long mPlusEta;
    private Timer mTimer;
    private Personal mPersonalETAObject;
    private boolean isTimerStarted = false;

    public PersonalCountDownService() {
        super("PersonalCountDownService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCountDown(Context context, String personalETAKey) {
        Intent intent = new Intent(context, PersonalCountDownService.class);
        intent.setAction(ACTION_START_COUNTDOWN);
        intent.putExtra(EXTRA_PARAM_PERSONAL_ETA_KEY, personalETAKey);
//        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {

//        startForeground();
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_COUNTDOWN.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM_PERSONAL_ETA_KEY);
                handleActionCountDown(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCountDown(String personalEtaKey) {
//        throw new UnsupportedOperationException("Not yet implemented");
        try {
            mRef = FirebaseDatabase.getInstance().getReference();
            mTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPersonalETAKey = personalEtaKey;
        DatabaseReference personalEtaDatabaseRef = mRef.child(Constants.PERSONAL_ETAS).child(mPersonalETAKey);
        personalEtaDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    try {
                        mPersonalETAObject = dataSnapshot.getValue(Personal.class);
                        mETA = mPersonalETAObject.getEta();
                        mPlusEta = mPersonalETAObject.getPlusEta();
                        if (!isTimerStarted) {
                            startTimer();
                        }
                        if (mETA <= 0 && mPlusEta <= 0) {
                            setCompletedTrue();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                String data = databaseError.getDetails();

            }
        });
        startTimer();

    }

    private void setCompletedTrue() {
        DatabaseReference personalEtaDatabaseRef = mRef.child(Constants.PERSONAL_ETAS).child(mPersonalETAKey);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("completed", true);
        hashMap.put("eta", 0);
        hashMap.put("plusEta", 0);
        hashMap.put("color", getApplicationContext().getResources().getColor(R.color.grey_600));
        personalEtaDatabaseRef.updateChildren(hashMap);
    }

    private void startTimer() {
        isTimerStarted = true;
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                removeSecondsFromETA();
                Log.e("Timer", "Calls");
            }
        }, mInterval, mInterval);
    }

    private void removeSecondsFromETA() {
        DatabaseReference personalEtaDatabaseRef = mRef.child(Constants.PERSONAL_ETAS).child(mPersonalETAKey);

        long newETA = mETA - mInterval;
        if (newETA <= 0) {
            mPlusEta = mPlusEta - mInterval;
            if (mPlusEta <= 0) {
                setCompletedTrue();
                mTimer.cancel();
            } else {
                DatabaseReference databaseReference = personalEtaDatabaseRef.child(Constants.ETA_FIELD_NAME);
                databaseReference.setValue(0);

                DatabaseReference databaseReferencePlus = personalEtaDatabaseRef.child(Constants.PLUS_ETA_FIELD_NAME);
                databaseReferencePlus.setValue(mPlusEta);
            }
        } else {
            DatabaseReference databaseReference = personalEtaDatabaseRef.child(Constants.ETA_FIELD_NAME);
            databaseReference.setValue(newETA);
        }

    }
}
