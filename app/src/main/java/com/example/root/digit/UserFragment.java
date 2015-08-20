package com.example.root.digit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class UserFragment extends Fragment {

    private static RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<Map<String, ?>> mTags;
    private MyRecyclerViewAdapter mRecyclerViewAdapter;
    private static HashMap viewUser;
    private static HashMap user;
    private Bitmap image;
    private String imageString;
    private RestApi API;
    private static ProgressBar progressBar;
    private static TextView empty_tags;

    private static final String ARG_OPTION = "argument_option";

    public UserFragment() {}

    public static UserFragment newInstance(HashMap api_user, HashMap view_user) {
        UserFragment fragment = new UserFragment();
        user = api_user;
        viewUser = (view_user != null) ? view_user : user;
        return fragment;
    }

    public static UserFragment newInstance(HashMap api_user) {
        return newInstance(api_user, null);
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.gc();
        mTags = new ArrayList<Map<String,?>>();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK)
        {
            Uri chosenImageUri = data.getData();

            try {
                Bitmap mBitmap = null;
                image = decodeUri((Context) getActivity().getApplicationContext(), chosenImageUri, 100);
                imageString = BitMapToString(image);
                ImageAsync uploadImage = new ImageAsync();
                uploadImage.execute((Void) null);
                return;
            }
            catch (IOException ex) {

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setActionBarTitle(viewUser.get("username").toString().toUpperCase());
        }
        else {
            ((UserActivity)getActivity()).setActionBarTitle(viewUser.get("username").toString().toUpperCase());
        }

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_user, container, false);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        empty_tags = (TextView) rootView.findViewById(R.id.empty_tags);
        ImageView profile_pic = (ImageView) rootView.findViewById(R.id.circleView);

        ImageGetAsync task = new ImageGetAsync(profile_pic);
        task.execute(new Integer[]{Integer.parseInt(viewUser.get("id").toString())});

        Log.d("View Username", viewUser.get("username").toString().trim());
        Log.d("Current Username", user.get("username").toString().trim());

        if (viewUser.get("username").toString().trim().equals(user.get("username").toString().trim())) {
            profile_pic.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                }
            });
        }

        TextView username = (TextView) rootView.findViewById(R.id.name);
        TextView email   = (TextView) rootView.findViewById(R.id.email);
        username.setText((String) viewUser.get("username"));
        email.setText((String) viewUser.get("email"));

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.tags);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerViewAdapter = new MyRecyclerViewAdapter(getActivity(), mTags, MyRecyclerViewAdapter.mImgMemoryCache, true);

        TagAsync downloadJson = new TagAsync(mRecyclerViewAdapter);
        downloadJson.execute((Void) null);

        mRecyclerViewAdapter.setOnClickListener(new MyRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO implement like system
                // Refresh items
                HashMap tag = (HashMap) mTags.get(position);
                popupTagDetails(tag);
            }

            @Override
            public void onItemDoubleClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {}
        });

        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        return rootView;
    }

    private class TagAsync extends AsyncTask<Void, Void, RestApi> {

        private final WeakReference<MyRecyclerViewAdapter> adapterReference;

        public TagAsync (MyRecyclerViewAdapter adapter) {
            adapterReference = new WeakReference<>(adapter);
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected RestApi doInBackground(Void... params) {
            API = new RestApi(getActivity().getApplicationContext(), (String) user.get("username"), (String) user.get("password"));
            API.error = null;
            API.getTags(Integer.parseInt(viewUser.get("id").toString()), null, null, null);
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
            progressBar.setVisibility(View.GONE);
        }
    }

    private class ImageAsync extends AsyncTask<Void, Void, Boolean> {

        public ImageAsync () {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            API = new RestApi(getActivity().getApplicationContext(), (String) user.get("username"), (String) user.get("password"));
            API.updateUser(Integer.parseInt(user.get("id").toString()), imageString);

            if (API.error != null) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute (final Boolean success) {

            if (success) {
                image = StringToBitMap(API.user.get("profile_pic").toString());
                ImageView profile_pic = (ImageView) getView().findViewById(R.id.circleView);
                profile_pic.setImageBitmap(image);
                MainActivity.getUserData(getActivity().getApplicationContext());
                Log.d("Profic Pic (New)", API.user.get("profile_pic").toString());
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "Profile Pic Upload Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String BitMapToString(Bitmap bitmap){
        String temp = null;
        try {
            getResizedBitmap(bitmap, 20);
            ByteArrayOutputStream baos=new  ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            bitmap.recycle();
            byte [] b=baos.toByteArray();
            baos.close();
            baos = null;
            temp= Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } catch (IOException ex) {
            temp = null;
        }
        return temp;
    }

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            encodeByte = null;
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    private void popupTagDetails(HashMap tag) {
        View view =  getActivity().getLayoutInflater().inflate(R.layout.dialog_tag_data, null);

        TextView tag_name = (TextView) view.findViewById(R.id.tag_title);
        TextView tag_text = (TextView) view.findViewById(R.id.tag_text);
        TextView tag_lon = (TextView) view.findViewById(R.id.tag_lon);
        TextView tag_lat = (TextView) view.findViewById(R.id.tag_lat);

        tag_name.setText(tag.get("tag").toString());
        tag_text.setText(tag.get("comment").toString());
        tag_lon.setText(tag.get("longitude").toString());
        tag_lat.setText(tag.get("latitude").toString());

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Tag Details");
        builder.setView(view);
        final AlertDialog alert = builder.create();
        alert.show();
    }

    protected class ImageGetAsync extends AsyncTask<Integer, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        public ImageGetAsync (ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(Integer... user_ids) {
            Bitmap image = null;
            HashMap imageData = null;
            RestApi API = new RestApi(getActivity(), (String) MainActivity.user.get("username"), (String) MainActivity.user.get("password"));
            for (int user_id : user_ids) {
                imageData = API.retrieveUserImage(user_id);
                if (imageData.get("profile_pic") != null) {
                    image = StringToBitMap(imageData.get("profile_pic").toString());
                    if (image != null) {
                        MyRecyclerViewAdapter.mImgMemoryCache.put(imageData.get("id").toString(), image);
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
            }
        }
    }
}
