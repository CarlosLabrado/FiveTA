package com.app_labs.fiveta.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.events.GetTimePickedEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePersonalActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.fab_personal_favorite_add)
    FloatingActionButton mFabPersonalFavoriteAdd;
    @Bind(R.id.imageViewCreatePersonal)
    ImageView mImageViewCreatePersonal;
    @Bind(R.id.textViewCreatePersonalName)
    TextView mTextViewCreatePersonalName;
    @Bind(R.id.editTextPersonalMessage)
    EditText mEditTextPersonalMessage;
    @Bind(R.id.textViewCreatePersonalETA)
    TextView mTextViewCreatePersonalETA;
    @Bind(R.id.buttonCreatePersonalStart)
    Button mButtonCreatePersonalStart;
    @Bind(R.id.imageButtonMinutesUp)
    ImageButton mImageButtonMinutesUp;
    @Bind(R.id.imageButtonMinutesDown)
    ImageButton mImageButtonMinutesDown;

    public static Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_personal);

        ButterKnife.bind(this);

        mBus = new Bus();
        mBus.register(this);

        /** toolBar **/
        setUpToolBar();
    }

    /**
     * sets up the top bar
     */
    public void setUpToolBar() {
        setSupportActionBar(mToolbar);
        setActionBarTitle(getString(R.string.personal_new_eta_toolbar), null, true);
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

    @OnClick(R.id.fab_personal_favorite_add)
    public void onClick() {
        Toast.makeText(getApplicationContext(), "WHOA!", Toast.LENGTH_SHORT).show();
        mFabPersonalFavoriteAdd.setImageResource(R.drawable.ic_heart_yellow_24dp);
    }

    @Subscribe
    public void getTimePicked(GetTimePickedEvent event) {
        mTextViewCreatePersonalETA.setText(String.valueOf(event.getHourOfDay()) + ":" + String.valueOf(event.getMinute()));
    }

    /**
     * Handles the 3 main buttons on the screen.
     *
     * @param view
     */
    @OnClick({R.id.imageButtonMinutesUp, R.id.imageButtonMinutesDown, R.id.buttonCreatePersonalStart})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageButtonMinutesUp:
                if (mTextViewCreatePersonalETA.getText().equals("55")) {
                    mTextViewCreatePersonalETA.setText("60");
                    mImageButtonMinutesUp.setEnabled(false);
                } else {
                    int timeSet = Integer.parseInt(mTextViewCreatePersonalETA.getText().toString());
                    timeSet = timeSet + 5;
                    mTextViewCreatePersonalETA.setText(String.valueOf(timeSet));
                    mImageButtonMinutesDown.setEnabled(true);
                }
                break;
            case R.id.imageButtonMinutesDown:
                if (mTextViewCreatePersonalETA.getText().equals("5")) {
                    mTextViewCreatePersonalETA.setText("0");
                    mImageButtonMinutesDown.setEnabled(false);
                } else {
                    int timeSet = Integer.parseInt(mTextViewCreatePersonalETA.getText().toString());
                    timeSet = timeSet - 5;
                    mTextViewCreatePersonalETA.setText(String.valueOf(timeSet));
                    mImageButtonMinutesUp.setEnabled(true);
                }
                break;
            case R.id.buttonCreatePersonalStart:
                break;
        }
    }
}
