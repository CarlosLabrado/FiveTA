package com.app_labs.fiveta.jobs;

import android.os.Handler;
import android.util.Log;

import com.app_labs.fiveta.model.Personal;
import com.app_labs.fiveta.util.Constants;
import com.app_labs.fiveta.util.LogUtil;
import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: change to a service :(
 * This job takes care of the count down for the personal timer
 */
public class PersonalETACountdownJob extends Job {

    private String mPersonalETAKey;
    private DatabaseReference mRef;
    private Handler mHandler;
    private int mInterval = 5000; // 5 seconds
    private long mETA;
    private long mPlusEta;
    private Timer mTimer;
    private Personal mPersonalETAObject;
    private boolean isTimerStarted = false;


    public PersonalETACountdownJob(String personalETAKey, Long eta) {
        super(new Params(1000).requireNetwork().groupBy("personal"));
        mPersonalETAKey = personalETAKey;
        mETA = eta;
    }


    @Override
    public void onAdded() {
        try {
            mRef = FirebaseDatabase.getInstance().getReference();
            mTimer = new Timer();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRun() throws Throwable {

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
        personalEtaDatabaseRef.child("completed").setValue(true);
    }

    private void startTimer() {
        isTimerStarted = true;
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                removeFiveSecondsFromETA();
                Log.e("Timer", "Calls");
            }
        }, mInterval, mInterval);
    }

    private void removeFiveSecondsFromETA() {
        DatabaseReference personalEtaDatabaseRef = mRef.child(Constants.PERSONAL_ETAS).child(mPersonalETAKey);

        long newETA = mETA - mInterval;
        if (newETA <= 0) {
            mPlusEta = mPlusEta - mInterval;
            if (mPlusEta <= 0) {
                mTimer.cancel();
            } else {
                DatabaseReference databaseReference = personalEtaDatabaseRef.child(Constants.ETA_FIELD_NAME);
                databaseReference.setValue(newETA);

                DatabaseReference databaseReferencePlus = personalEtaDatabaseRef.child(Constants.PLUS_ETA_FIELD_NAME);
                databaseReferencePlus.setValue(mPlusEta);
            }
        } else {
            DatabaseReference databaseReference = personalEtaDatabaseRef.child(Constants.ETA_FIELD_NAME);
            databaseReference.setValue(newETA);
        }


    }

    @Override
    protected void onCancel(int cancelReason) {
        LogUtil.logE("ERROR", String.valueOf(cancelReason));

    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }
}
