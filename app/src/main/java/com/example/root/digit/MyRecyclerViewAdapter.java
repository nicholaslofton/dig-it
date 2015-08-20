package com.example.root.digit;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.root.digit.UserFragment.StringToBitMap;

/**
 * Created by nglofton on 6/14/15.
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private OnItemClickListener mItemClickListener;

    private List<Map<String, ?>> mDataset;
    private Context mContext;
    public static LruCache<String, Bitmap> mImgMemoryCache;
    private boolean userList = false;

    public MyRecyclerViewAdapter(Context myContext, List<Map<String, ?>> myDataset, LruCache myCache, boolean user) {
        mContext = myContext;
        mDataset = myDataset;
        mImgMemoryCache = myCache;
        userList = user;
    }

    public MyRecyclerViewAdapter(Context myContext, List<Map<String, ?>> myDataset, LruCache myCache) {
        this(myContext, myDataset, myCache, false);
    }

    @Override
    public MyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (userList) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagview, parent, false);
        }
        else {
            switch (viewType) {
                case 0:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagview_header, parent, false);
                    break;
                default:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tagview, parent, false);
                    break;
            }
        }
        Log.d("MyRecyclerViewAdapter", view.toString());
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (userList) {
            final Map<String, ?> tag = mDataset.get(position);
            holder.bindTagData(tag);
        }
        else if (!isPositionHeader(position)) {
            final Map<String, ?> tag = mDataset.get(position-1);
            holder.bindTagData(tag);
        }
    }

    //added a method that returns viewType for a given position
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return 0;
        }
        return 1;
    }

    //added a method to check if given position is a header
    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    public int getBasicItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override
    public int getItemCount() {
        return (userList) ? getBasicItemCount() : getBasicItemCount() + 1;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onItemDoubleClick(View view, int position);
    }

    public void setOnClickListener (final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView vIcon;
        public TextView vUser;
        public TextView vTag;
        public TextView vDigs;
        public RelativeLayout vData;
        public ProgressBar vLoading;
        private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
        private long lastPressTime;
        private boolean mHasDoubleClicked;


        public ViewHolder(final View view) {
            super(view);

            if (view.findViewById(R.id.tag_user_icon) == null) {
                return;
            }
            vIcon = (ImageView) view.findViewById(R.id.tag_user_icon);
            vUser = (TextView) view.findViewById(R.id.tag_username);
            vTag = (TextView) view.findViewById(R.id.tag_name);
            vDigs = (TextView) view.findViewById(R.id.dig_count);
            vData = (RelativeLayout) view.findViewById(R.id.data);
            vLoading = (ProgressBar) view.findViewById(R.id.loading);


            view.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemClick(view, getPosition());
                        }
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemDoubleClick(view, getPosition());
                        }
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        if (mItemClickListener != null) {
                            mItemClickListener.onItemLongClick(view, getPosition());
                        }
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d("TEST", "Raw event: " + event.getAction() + ", (" + event.getRawX() + ", " + event.getRawY() + ")");
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }

        public void bindTagData (Map<String, ?> tag) {
            int user_id = Integer.parseInt(tag.get("user_id").toString());
            vUser.setText(tag.get("username").toString());
            vTag.setText(tag.get("tag").toString());
            vData.setAlpha(1.0f);
            if (tag.get("dig_count") == null) {
                vDigs.setText("0 Digs");
            }
            else {
                int digs = 0;
                try {
                    digs = Integer.parseInt(tag.get("dig_count").toString());
                } catch (NumberFormatException e) {
                }

                vDigs.setText((digs == 1) ? digs + " Dig" : digs + " Digs");
            }
            final Bitmap bitmap = mImgMemoryCache.get(tag.get("user_id").toString());
            if (bitmap != null) {
                vLoading.setVisibility(View.GONE);
                vIcon.setImageBitmap(bitmap);
            }
            else {
                vIcon.setVisibility(View.GONE);
                vLoading.setVisibility(View.VISIBLE);
                ImageAsync task = new ImageAsync(vIcon, vLoading);
                task.execute(new Integer[]{user_id});
            }
            vIcon.setAlpha(0.75f);
        }
    }

    protected class ImageAsync extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarReference;

        public ImageAsync (ImageView imageView, ProgressBar progressBar) {
            imageViewReference = new WeakReference<>(imageView);
            progressBarReference = new WeakReference<>(progressBar);
        }

        @Override
        protected Bitmap doInBackground(Integer... user_ids) {
            Bitmap image = null;
            HashMap imageData = null;
            RestApi API = new RestApi(mContext, (String) MainActivity.user.get("username"), (String) MainActivity.user.get("password"));
            for (int user_id : user_ids) {
                imageData = API.retrieveUserImage(user_id);
                if (imageData.get("profile_pic") != null) {
                    image = StringToBitMap(imageData.get("profile_pic").toString());
                    if (image != null) {
                        mImgMemoryCache.put(imageData.get("id").toString(), image);
                    }
                }
            }
            return image;
        }

        @Override
        protected void onPostExecute (Bitmap bitmap) {
            if (imageViewReference != null) {

                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setImageResource(R.drawable.no_face_icon);
                    }
                }
                imageView.setVisibility(View.VISIBLE);
            }
            if (progressBarReference != null) {
                final ProgressBar progressBar = progressBarReference.get();
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}