package com.example.root.digit;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class TagListFragment extends Fragment implements LocationListener {

    private static LruCache<String, Bitmap> mImgMemoryCache;
    private static RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<Map<String, ?>> mTags;
    private MyRecyclerViewAdapter mRecyclerViewAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static FloatingActionButton mActionButton;
    private static HashMap user;
    private RestApi API;
    private onListItemSelectedListener mListener;
    public static LocationManager mLocationManager;
    private static Location mLocation;
    private static ProgressBar progressBar;
    private static TextView empty_tags;

    private static final String ARG_OPTION = "argument_option";

    public TagListFragment() {}

    public static TagListFragment newInstance(HashMap userData) {
        TagListFragment fragment = new TagListFragment();
        user = userData;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (onListItemSelectedListener) activity;
        }
        catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    public interface onListItemSelectedListener {
        void onListItemSelected(HashMap tag);
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTags = new ArrayList<Map<String,?>>();

        API = new RestApi(getActivity().getApplicationContext(), (String) user.get("username"), (String) user.get("password"));
        if (mImgMemoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory());

            final int cacheSize = maxMemory / 8;

            mImgMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        ((MainActivity)getActivity()).setActionBarTitle(getActivity().getString(R.string.app_name));

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mLocation != null && mLocation.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        }
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            mLocation = location;
            mLocationManager.removeUpdates(this);
        }
    }

    // Required functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity)getActivity()).setActionBarTitle(getActivity().getString(R.string.app_name));

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_tag_list, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        empty_tags = (TextView) rootView.findViewById(R.id.empty_tags);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!mSwipeRefreshLayout.isRefreshing() || !progressBar.isShown()) {
                    mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    // Refresh items
                    TagAsync downloadJson = new TagAsync(mRecyclerViewAdapter, mSwipeRefreshLayout, true);
                    downloadJson.execute((Void) null);
                }
                else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        mActionButton = (FloatingActionButton) rootView.findViewById(R.id.add_tag);
        mActionButton.setOnClickListener(new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                showDialogGetResult();
            }
        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.tags);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerViewAdapter = new MyRecyclerViewAdapter(getActivity(), mTags, mImgMemoryCache);

        TagAsync downloadJson = new TagAsync(mRecyclerViewAdapter, mSwipeRefreshLayout, false);
        downloadJson.execute((Void) null);

        mRecyclerViewAdapter.setOnClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                HashMap tag =
                        (HashMap) mTags.get(position-1);
                mListener.onListItemSelected(tag);
            }

            @Override
            public void onItemDoubleClick(View view, int position) {
                HashMap tag = (HashMap) mTags.get(position-1);
                LikeTagAsync uploadJson =
                        new LikeTagAsync(
                                mRecyclerViewAdapter,
                                mSwipeRefreshLayout,
                                Integer.parseInt(tag.get("id").toString()),
                                position-1
                        );
                uploadJson.execute((Void) null);
                Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.shakeanim);
                view.startAnimation(animation);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // Go to user
                HashMap tag = (HashMap) mTags.get(position-1);
                HashMap otherUser = new HashMap();

                otherUser.put("id", tag.get("user_id").toString());
                otherUser.put("username", tag.get("username").toString());
                otherUser.put("email", tag.get("email").toString());

                getActivity().getSupportFragmentManager().beginTransaction().
                        setCustomAnimations(R.anim.abc_popup_enter, R.anim.abc_popup_exit).
                        replace(R.id.container, UserFragment.newInstance(user, otherUser)).addToBackStack(null).commit();
            }
        });

        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    int firstPos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstPos > 0) {
                        mSwipeRefreshLayout.setEnabled(false);
                    } else {
                        mSwipeRefreshLayout.setEnabled(true);
                        if (mRecyclerView.getScrollState() == 1) {
                            if (mSwipeRefreshLayout.isRefreshing()) {
                                mRecyclerView.stopScroll();
                            }
                        }
                    }

                } catch (Exception e) {
                }
            }
        });

        return rootView;
    }

    public void showDialogGetResult() {
        if (!TagFragment.checkGPS(getActivity())) {
            buildAlertMessageNoGps(getActivity());
            return;
        }

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (mLocation != null && mLocation.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
            // Do something with the recent location fix
            //  otherwise wait for the update below
        }
        else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 50, 0, this);
        }

        TagFragment dialog = new TagFragment();
        dialog.newInstance(getActivity().getApplicationContext());
        dialog.setTargetFragment(TagListFragment.this, 0);
        dialog.show(getFragmentManager(), "Create Tag: Add Tag to Location");
    }

    private class TagAsync extends AsyncTask<Void, Void, RestApi> {

        private final WeakReference<MyRecyclerViewAdapter> adapterReference;
        private final WeakReference<SwipeRefreshLayout> refreshReference;
        private final WeakReference<Boolean> refreshData;

        public TagAsync (MyRecyclerViewAdapter adapter, SwipeRefreshLayout layout, Boolean refresh) {
            adapterReference = new WeakReference<>(adapter);
            refreshReference = new WeakReference<>(layout);
            refreshData = new WeakReference<>(refresh);
        }

        @Override
        protected void onPreExecute() {
            final Boolean refresh = refreshData.get();
            if (refreshData != null && !refresh) {
                progressBar.setVisibility(View.VISIBLE);
            }

        }

        @Override
        protected RestApi doInBackground(Void... params) {
            while (mLocation == null) {
               // Wait for location because we need it to get out tags
            }
            API.error = null;
            API.getTags(0, String.valueOf(mLocation.getLatitude()),  String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getAltitude()));
            return API;
        }

        @Override
        protected void onPostExecute (RestApi tagData) {

            mTags.clear();

            if (tagData.getSize() == 0) {
                empty_tags.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            else {
                empty_tags.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < tagData.getSize(); i++) {
                mTags.add(tagData.getItem(i));
            }

            if (adapterReference != null) {
                final MyRecyclerViewAdapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
            final Boolean refresh = refreshData.get();
            if (refreshReference != null && refresh) {
                final SwipeRefreshLayout layout = refreshReference.get();
                if (layout != null) {
                    layout.setRefreshing(false);
                }
            }
            else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private class AddTagAsync extends AsyncTask<Void, Void, RestApi> {

        private final WeakReference<MyRecyclerViewAdapter> adapterReference;
        private final WeakReference<SwipeRefreshLayout> refreshReference;
        private final WeakReference<HashMap> tagData;


        public AddTagAsync (MyRecyclerViewAdapter adapter, SwipeRefreshLayout layout, HashMap data) {
            adapterReference = new WeakReference<>(adapter);
            refreshReference = new WeakReference<>(layout);
            tagData          = new WeakReference<>(data);
        }

        @Override
        protected RestApi doInBackground(Void... params) {
            HashMap tag = tagData.get();
            if (tag != null) {
                API.error = null;
                API.addTag(tag.get("title").toString(), tag.get("tag_data").toString(), tag.get("latitude").toString(), tag.get("longitude").toString(), tag.get("altitude").toString());

                if (API.error == null) {
                    API.getTags(0, String.valueOf(mLocation.getLatitude()),  String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getAltitude()));
                }
                return API;
            }
            return null;
        }

        @Override
        protected void onPostExecute (RestApi tagData) {

            mTags.clear();

            if (tagData.getSize() == 0) {
                empty_tags.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            else {
                empty_tags.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < tagData.getSize(); i++) {
                mTags.add(tagData.getItem(i));
            }

            if (adapterReference != null) {
                final MyRecyclerViewAdapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            if (refreshReference != null) {
                final SwipeRefreshLayout layout = refreshReference.get();
                if (layout != null) {
                    layout.setRefreshing(false);
                }
            }
        }
    }

    private class LikeTagAsync extends AsyncTask<Void, Void, RestApi> {

        private final WeakReference<MyRecyclerViewAdapter> adapterReference;
        private final WeakReference<SwipeRefreshLayout> refreshReference;
        private final WeakReference<Integer> tagId;
        private final WeakReference<Integer> position;


        public LikeTagAsync (MyRecyclerViewAdapter adapter, SwipeRefreshLayout layout, int tag_id, int pos) {
            adapterReference = new WeakReference<>(adapter);
            refreshReference = new WeakReference<>(layout);
            tagId            = new WeakReference<>(tag_id);
            position         = new WeakReference<>(pos);
        }

        @Override
        protected RestApi doInBackground(Void... params) {
            int tag_id = tagId.get();
            API.error = null;
            API.likeTag(tag_id);

            if (API.error == null) {
                API.getTags(0, String.valueOf(mLocation.getLatitude()),  String.valueOf(mLocation.getLongitude()), String.valueOf(mLocation.getAltitude()));
            }
            return API;
        }

        @Override
        protected void onPostExecute (RestApi tagData) {

            mTags.clear();

            if (tagData.getSize() == 0) {
                empty_tags.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
            else {
                empty_tags.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < tagData.getSize(); i++) {
                mTags.add(tagData.getItem(i));
            }

            if (adapterReference != null) {
                final MyRecyclerViewAdapter adapter = adapterReference.get();
                if (mRecyclerViewAdapter != null) {
                    mRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

            if (refreshReference != null) {
                final SwipeRefreshLayout layout = refreshReference.get();
                if (layout != null) {
                    layout.setRefreshing(false);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == 0) {
            String name = intent.getStringExtra(TagFragment.NAME_ARGS);
            String note = intent.getStringExtra(TagFragment.NOTE_ARGS);

            while (mLocation == null) {

            }

            if (name != null && note != null) {
                HashMap tag = new HashMap();
                tag.put("title", name);
                tag.put("tag_data", note);
                tag.put("latitude", mLocation.getLatitude());
                tag.put("longitude", mLocation.getLongitude());
                tag.put("altitude", mLocation.getAltitude());

                AddTagAsync upload = new AddTagAsync(mRecyclerViewAdapter, mSwipeRefreshLayout, tag);
                upload.execute((Void) null);
            }
        }
    }



    public void buildAlertMessageNoGps(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it? If not I'll close until you're ready.")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        getActivity().startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        getActivity().finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

        private static final float HIDE_THRESHOLD = 10;
        private static final float SHOW_THRESHOLD = 70;

        private int mToolbarOffset = 0;
        private boolean mControlsVisible = true;
        private int mToolbarHeight;

        public HidingScrollListener(Context context) {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
            {
                mToolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mControlsVisible) {
                    if (mToolbarOffset > HIDE_THRESHOLD) {
                        setInvisible();
                    } else {
                        setVisible();
                    }
                } else {
                    if ((mToolbarHeight - mToolbarOffset) > SHOW_THRESHOLD) {
                        setVisible();
                    } else {
                        setInvisible();
                    }
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            clipToolbarOffset();
            onMoved(mToolbarOffset);

            if((mToolbarOffset <mToolbarHeight && dy>0) || (mToolbarOffset >0 && dy<0)) {
                mToolbarOffset += dy;
            }

        }

        private void clipToolbarOffset() {
            if(mToolbarOffset > mToolbarHeight) {
                mToolbarOffset = mToolbarHeight;
            } else if(mToolbarOffset < 0) {
                mToolbarOffset = 0;
            }
        }

        private void setVisible() {
            if(mToolbarOffset > 0) {
                onShow();
                mToolbarOffset = 0;
            }
            mControlsVisible = true;
        }

        private void setInvisible() {
            if(mToolbarOffset < mToolbarHeight) {
                onHide();
                mToolbarOffset = mToolbarHeight;
            }
            mControlsVisible = false;
        }

        public abstract void onMoved(int distance);
        public abstract void onShow();
        public abstract void onHide();
    }
}
