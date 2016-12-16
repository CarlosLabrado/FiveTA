package com.app_labs.fiveta.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.events.GetTimePickedEvent;
import com.app_labs.fiveta.ui.custom.TimePickerFragment;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateGroupActivity extends AppCompatActivity implements OnMapReadyCallback {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab_group_favorite_add)
    FloatingActionButton mFabGroupFavoriteAdd;
    @BindView(R.id.editTextGroupTitle)
    EditText mEditTextGroupTitle;
    @BindView(R.id.fab_group_create_contact_add)
    FloatingActionButton mFabGroupCreateContactAdd;
    @BindView(R.id.buttonCreateGroupStart)
    Button mButtonCreateGroupStart;
    @BindView(R.id.textViewCreateGroupTime)
    TextView mTextViewCreateGroupTime;

    int PLACE_PICKER_REQUEST = 1;
    public static Bus mBus;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);

        mBus = new Bus();
        mBus.register(this);

        /** toolBar **/
        setUpToolBar();

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLite);
        mapFragment.getMapAsync(this);
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

    @OnClick({R.id.fab_group_favorite_add, R.id.fab_group_create_contact_add, R.id.buttonCreateGroupStart})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_group_favorite_add:
                break;
            case R.id.fab_group_create_contact_add:
                break;
            case R.id.buttonCreateGroupStart:
                inflateMapPicker();
                break;
        }
    }

    @OnClick(R.id.textViewCreateGroupTime)
    public void onClick() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(fragmentManager, "Time picker");
    }

    @Subscribe
    public void getTimePicked(GetTimePickedEvent event) {
        mTextViewCreateGroupTime.setText(String.valueOf(event.getHourOfDay()) + ":" + String.valueOf(event.getMinute()));
    }

    private void inflateMapPicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                updateLiteMap(place);
            }
        }
    }

    private void updateLiteMap(Place place) {
        mMap.addMarker(new MarkerOptions()
                .position(place.getLatLng())
                .title((String) place.getAddress()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
