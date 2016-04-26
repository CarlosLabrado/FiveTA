package com.app_labs.fiveta.UI;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.util.LogUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

    @Bind(R.id.toolbar)
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /** toolBar **/
        setUpToolBar();
    }

    /**
     * sets up the top bar
     */
    public void setUpToolBar() {
        setSupportActionBar(toolbar);
        setActionBarTitle("Replace me!", null, false);
        // if (getSupportActionBar() != null) {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        //  }
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
}
