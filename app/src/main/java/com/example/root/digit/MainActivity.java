package com.example.root.digit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.example.root.digit.LoginActivity.md5;

public class MainActivity extends AppCompatActivity implements TagListFragment.onListItemSelectedListener {

    public static Toolbar mToolbar;
    public static RelativeLayout mDrawer;
    public static RecyclerView mDrawerList;
    public static DrawerLayout mDrawerLayout;
    public static ActionBarDrawerToggle mDrawerToggle;
    public static MyDrawerRecyclerViewAdapter mDrawerRecyclerViewAdapter;
    public static HashMap user;
    private static UserLoginTask mAuthTask;
    private static RestApi API;
    public static boolean logged_in = false;
    public static AlertDialog alert = null;

    private final int ABOUT_ME_FRAGMENT = 1;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (!(currentFragment  instanceof AboutMeFragment)
                && !(currentFragment instanceof UserFragment)
                && !(currentFragment instanceof TagListFragment)) {
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (alert != null) {
            alert.hide();
        }
        if (!TagFragment.checkGPS(this)) {
            buildAlertMessageNoGps(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateDrawer(savedInstanceState);
    }

    protected void onCreateDrawer(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        System.gc();

        getUserData(getApplicationContext());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Setup the RecylcerView
        mDrawer = (RelativeLayout) findViewById(R.id.drawer);
        mDrawerList = (RecyclerView) findViewById(R.id.drawer_list);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mDrawerRecyclerViewAdapter = new MyDrawerRecyclerViewAdapter(this, (new DrawerData_New(user)).getDrawerList());
        mDrawerRecyclerViewAdapter.setOnClickListener(new MyDrawerRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion) {
                selectItem(postion);
            }
        });

        mDrawerList.setAdapter(mDrawerRecyclerViewAdapter);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackgroundColor(Color.MAGENTA);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer) {

            public void onDrawerClosed(View view) {
                logged_in = true;
                getUserData(getApplicationContext());
                logged_in = false;
                mDrawerRecyclerViewAdapter = new MyDrawerRecyclerViewAdapter(getApplicationContext(), (new DrawerData_New(user)).getDrawerList());
                mDrawerRecyclerViewAdapter.setOnClickListener(new MyDrawerRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int postion) {
                        selectItem(postion);
                    }
                });
                mDrawerList.setAdapter(mDrawerRecyclerViewAdapter);
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View view) {
                logged_in = true;
                getUserData(getApplicationContext());
                logged_in = false;
                mDrawerRecyclerViewAdapter = new MyDrawerRecyclerViewAdapter(getApplicationContext(), (new DrawerData_New(user)).getDrawerList());
                mDrawerRecyclerViewAdapter.setOnClickListener(new MyDrawerRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int postion) {
                        selectItem(postion);
                    }
                });
                mDrawerList.setAdapter(mDrawerRecyclerViewAdapter);
                super.onDrawerOpened(view);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(ABOUT_ME_FRAGMENT);
            mDrawerRecyclerViewAdapter.mCurrentPosition = ABOUT_ME_FRAGMENT;
            mDrawerRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) TagListFragment.mActionButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        Toast.makeText(getApplicationContext(), String.valueOf(TagListFragment.mActionButton.getHeight() + fabBottomMargin), Toast.LENGTH_LONG).show();
        TagListFragment.mActionButton.animate().translationY(TagListFragment.mActionButton.getHeight() + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void showViews(boolean user_fragment) {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        if (!user_fragment) {
            TagListFragment.mActionButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        }
    }

    public void setTranslationY (int distance) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) TagListFragment.mActionButton.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;

        mToolbar.setTranslationY(-distance);
        TagListFragment.mActionButton.animate().translationY(distance + fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();;
    }

    public void showViews() {
        showViews(false);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mDrawerLayout != null && isDrawerOpen())
            showGlobalContextActionBar();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        boolean closeDrawer = true;
        switch(position) {
            case 0:
                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(R.anim.abc_popup_enter, R.anim.abc_popup_exit).
                        replace(R.id.container, UserFragment.newInstance(user)).addToBackStack(null).commit();
                break;
            case 1:

                if (alert != null) {
                    alert.hide();
                }
                if (!TagFragment.checkGPS(this)) {
                    buildAlertMessageNoGps(this);
                }

                getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(R.anim.abc_popup_enter, R.anim.abc_popup_exit).
                        replace(R.id.container, TagListFragment.newInstance(user)).addToBackStack(null).commit();
                break;
            case 3:
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new AboutMeFragment()).addToBackStack(null).commit();
                break;
            case 4:
                File file = new File(getApplicationContext().getFilesDir(), ".userdata");
                file.delete();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                break;
            default:
                closeDrawer = false;
                break;
        }
        if (closeDrawer) {
            mDrawerLayout.closeDrawer(mDrawer);
        }
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(findViewById(R.id.drawer));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onListItemSelected(HashMap tag) {

        if (!TagFragment.checkGPS(this)) {
            buildAlertMessageNoGps(this);
            return;
        }

        Intent i = new Intent(MainActivity.this, TagActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("tag", tag);
        i.putExtras(bundle);
        startActivity(i);
    }

    public static void getUserData(Context context) {
        try {
            FileInputStream fis = context.openFileInput(".userdata");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            sb.toString();
            user = new HashMap();
            String s = sb.toString();
            s = s.substring(1, s.length()-1);
            String[] pairs = s.split(",");
            for (int i=0;i<pairs.length;i++) {
                String pair = pairs[i];
                String[] keyValue = pair.split(":");
                boolean isImage = (keyValue[0].contains("profile_pic")) ? true : false;
                String key = keyValue[0];
                key = key.replace("\"", "");

                String val;
                try {
                    val = String.valueOf(Integer.parseInt(keyValue[1]));
                } catch (NumberFormatException e) {
                    val = keyValue[1];
                    val = val.replace("\"","");
                    if (isImage && val != null) {
                        val = val.replace("\\/", "/");
                        val = val.replace("\\n", "\n");
                    }
                }
                user.put(key, (val.contains("null") && val.length() == 4) ? null : val);
            }

            if (logged_in) {
                mAuthTask = new UserLoginTask(user.get("username").toString(), user.get("password").toString(), context);
                mAuthTask.execute((Void) null);
            }

        }
        catch (IOException ex) {

        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public static class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserName;
        private final String mPassword;
        private final Context mContext;

        UserLoginTask(String username, String password, Context context) {
            mUserName = username;
            mPassword = password;
            mContext  = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            API = new RestApi(mContext, mUserName, mPassword);
            API.retrieveUser(0);

            if (API.error != null) {
                return false;
            }

            user = API.user;
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

    }

    public void setActionBarTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    public void buildAlertMessageNoGps(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it? If not I'll close until you're ready.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        if (alert != null) {
                            alert.hide();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        finish();
                    }
                });
        alert = builder.create();
        alert.show();
    }
}
