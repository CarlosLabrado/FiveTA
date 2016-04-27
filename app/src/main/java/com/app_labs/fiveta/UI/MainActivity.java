package com.app_labs.fiveta.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;

import com.app_labs.fiveta.R;
import com.app_labs.fiveta.util.LogUtil;

import java.util.Stack;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;
import it.sephiroth.android.library.bottonnavigation.BuildConfig;

public class MainActivity extends AppCompatActivity implements BottomNavigation.OnMenuItemSelectionListener {

    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.bottomNavigation)
    BottomNavigation mBottomNavigation;

    private static Stack<Integer> mTabStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomNavigation.DEBUG = BuildConfig.DEBUG;

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTabStack = new Stack<>();

        /** toolBar **/
        setUpToolBar();

        initializeBottomNavigation(savedInstanceState);

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
}
