package com.example.root.digit;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestApi {

    List tagList;
    HashMap user;
    MyUtility restConn;
    String error;
    Context mContext;

    public List getTagList() {
        return tagList;
    }

    public int getSize(){
        return tagList.size();
    }
    public HashMap getItem(int i){
        if (i >=0 && i < tagList.size()){
            return (HashMap) tagList.get(i);
        } else
            return null;
    }

    public RestApi(Context context, String username, String password) {
        user = new HashMap();
        tagList= new ArrayList<>();
        restConn = new MyUtility();
        error = null;
        restConn.userName = username;
        restConn.password = password;
        mContext = context;
    }


    public void retrieveUser(int userId) {

        error = null;
        String url = mContext.getString(R.string.rest_url)+"/";

        if (userId != 0) {
            // Just go to the main url to get the current user info
            url += mContext.getString(R.string.user_endpoint)+"/"+userId;
        }

        user = new HashMap();

        Log.d("Username", restConn.userName);
        Log.d("Password", restConn.password);


        String userData = restConn.downloadJSON(url);

        if (userData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "User Not Found!";
            return;
        }

        Log.d("MyDebugMsg", "USERDATA: " + userData);


        if (userData.contains("Invalid credentials")) {
            error = userData;
            return;
        }

        try {
            JSONObject userJsonObj = new JSONObject(userData);
            if (userJsonObj != null) {
                String cleanImage = (userJsonObj.isNull("profile_picture")) ? null : (String) userJsonObj.get("profile_picture");
                if (cleanImage != null) {
                    cleanImage = cleanImage.replace("\\/", "/");
                    cleanImage = cleanImage.replace("\\n", "\n");
                }

                user.put("id", (userJsonObj.isNull("id")) ? null : Integer.parseInt((String) userJsonObj.get("id")));
                user.put("username", restConn.userName);
                user.put("password", restConn.password);
                user.put("email", (userJsonObj.isNull("email")) ? null : (String) userJsonObj.get("email"));
                user.put("profile_pic", cleanImage);
                user.put("created", (userJsonObj.isNull("created_at")) ? null : (String) userJsonObj.get("created_at"));
            }
        } catch (JSONException ex) {
            error = "User Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in retrieveUser");
            ex.printStackTrace();
        }

        String filename = ".userdata";
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(new JSONObject(user).toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap retrieveUserImage(int userId) {

        error = null;
        String url = mContext.getString(R.string.rest_url)+"/" + mContext.getString(R.string.user_endpoint)+"/"+userId+"/image";

        String userData = restConn.downloadJSON(url);

        if (userData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "User Not Found!";
            return null;
        }

        Log.d("MyDebugMsg", "USERDATA: " + userData);


        if (userData.contains("Invalid credentials")) {
            error = userData;
            return null;
        }

        HashMap image = new HashMap();

        try {
            JSONObject imageObj = new JSONObject(userData);
            if (imageObj != null) {
                String cleanImage = (imageObj.isNull("profile_picture")) ? null : (String) imageObj.get("profile_picture");
                if (cleanImage != null) {
                    cleanImage = cleanImage.replace("\\/", "/");
                    cleanImage = cleanImage.replace("\\n", "\n");
                }

                image.put("id", (imageObj.isNull("id")) ? null : Integer.parseInt((String) imageObj.get("id")));
                image.put("username", (imageObj.isNull("username")) ? null : (String) imageObj.get("username"));
                image.put("profile_pic", cleanImage);

                return image;
            }
        } catch (JSONException ex) {
            error = "User Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in retrieveUser");
            ex.printStackTrace();
        }
        return  null;
    }

    public void registerUser(String email) {

        String url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.user_endpoint);
        error = null;
        user = new HashMap();

        try {
            restConn.jsonParams.put("email", email);
        } catch (JSONException je) {
            error = "Email could not be parsed.";
            return;
        }

        String userData = restConn.uploadJSON(url, "POST");

        if (userData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "Could Not Register User!";
            return;
        }

        if (userData.contains("Invalid credentials") || userData.contains("already registered") || userData.contains("Could not")) {
            error = userData;
            return;
        }

        try {
            JSONObject userJsonObj = new JSONObject(userData);
            if (userJsonObj != null) {
                String cleanImage = (userJsonObj.isNull("profile_picture")) ? null : (String) userJsonObj.get("profile_picture");
                if (cleanImage != null) {
                    cleanImage = cleanImage.replace("\\/", "/");
                    cleanImage = cleanImage.replace("\\n", "\n");
                }

                user.put("id", (userJsonObj.isNull("id")) ? null : Integer.parseInt((String)userJsonObj.get("id")));
                user.put("username", restConn.userName);
                user.put("password", restConn.password);
                user.put("email", (userJsonObj.isNull("email")) ? null : (String) userJsonObj.get("email"));
                user.put("profile_pic", cleanImage);
                user.put("created", (userJsonObj.isNull("created_at")) ? null : (String) userJsonObj.get("created_at"));
            }
        } catch (JSONException ex) {
            error = "User Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in registerUser");
            ex.printStackTrace();
        }
    }

    public void updateUser(int userId, String image) {

        String url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.user_endpoint)+"/"+userId;
        error = null;
        user = new HashMap();

        try {
            restConn.jsonParams.put("profile_picture", image);
        } catch (JSONException je) {
            error = "Profile picture could not be parsed.";
            return;
        }

        String userData = restConn.uploadJSON(url, "PUT");

        if (userData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "User Not Found!";
            return;
        }

        if (userData.contains("Invalid credentials")) {
            error = userData;
            return;
        }

        try {
            JSONObject userJsonObj = new JSONObject(userData);
            if (userJsonObj != null) {
                String cleanImage = (userJsonObj.isNull("profile_picture")) ? null : (String) userJsonObj.get("profile_picture");
                if (cleanImage != null) {
                    cleanImage = cleanImage.replace("\\/", "/");
                    cleanImage = cleanImage.replace("\\n", "\n");
                }

                user.put("id", (userJsonObj.isNull("id")) ? null : Integer.parseInt((String)userJsonObj.get("id")));
                user.put("username", restConn.userName);
                user.put("password", restConn.password);
                user.put("email", (userJsonObj.isNull("email")) ? null : (String) userJsonObj.get("email"));
                user.put("profile_pic", cleanImage);
                user.put("created", (userJsonObj.isNull("created_at")) ? null : (String) userJsonObj.get("created_at"));
            }
        } catch (JSONException ex) {
            error = "User Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in updateUser");
            ex.printStackTrace();
        }

        String filename = ".userdata";
        FileOutputStream outputStream;

        try {
            outputStream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(new JSONObject(user).toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTags(int userId, String lat, String lon, String alt) {

        String url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.user_endpoint)+"/"+userId+"/"+mContext.getString(R.string.tags_endpoint);
        error = null;
        if (userId == 0) {
            // Just go to the main url to get the current user info
            url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.tags_endpoint)+"/"+lat+"/"+lon;
        }

        tagList.clear();

        String tagsArray = restConn.downloadJSON(url);

        if (tagsArray == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "Tags Not Found!";
            return;
        }

        Log.d("MyDebugMsg", "Tags: " + tagsArray);


        if (tagsArray.contains("Invalid credentials")) {
            error = tagsArray;
            return;
        }


        try {
            JSONArray tagsJsonArray = new JSONArray(tagsArray);
            for (int i = 0; i < tagsJsonArray.length(); i++) {
                JSONObject tagJsonObj = (JSONObject) tagsJsonArray.get(i);
                if (tagJsonObj != null) {
                    HashMap tag = new HashMap();
                    tag.put("id", (tagJsonObj.isNull("tag_id")) ? null : Integer.parseInt(tagJsonObj.get("tag_id").toString()));
                    tag.put("tag", (tagJsonObj.isNull("tag")) ? null : (String) tagJsonObj.get("tag"));
                    tag.put("comment", (tagJsonObj.isNull("comment")) ? null : (String) tagJsonObj.get("comment"));
                    tag.put("latitude", (tagJsonObj.isNull("latitude")) ? null : (String) tagJsonObj.get("latitude"));
                    tag.put("longitude", (tagJsonObj.isNull("longitude")) ? null : (String) tagJsonObj.get("longitude"));
                    tag.put("altitude", (tagJsonObj.isNull("altitude")) ? null : (String) tagJsonObj.get("altitude"));
                    tag.put("user_id", (tagJsonObj.isNull("id")) ? null : Integer.parseInt(tagJsonObj.get("id").toString()));
                    tag.put("username", (tagJsonObj.isNull("username")) ? null :  tagJsonObj.get("username").toString().trim());
                    tag.put("email", (tagJsonObj.isNull("email")) ? null : tagJsonObj.get("email").toString().trim());
                    tag.put("dig_count", (tagJsonObj.isNull("dig_count")) ? null : (String) tagJsonObj.get("dig_count"));
                    tagList.add(i, tag);
                }
            }
        } catch (JSONException ex) {
            error = "Tag Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in retrieveUser");
            ex.printStackTrace();
        }
    }

    public void addTag(String name, String data, String lat, String lon, String alt) {

        String url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.tags_endpoint);
        error = null;

        try {
            restConn.jsonParams.put("tag", name);
            restConn.jsonParams.put("comment", data);
            restConn.jsonParams.put("latitude", lat);
            restConn.jsonParams.put("longitude", lon);
            restConn.jsonParams.put("altitude", alt);
        } catch (JSONException je) {
            error = "Profile picture could not be parsed.";
            return;
        }

        String tagData = restConn.uploadJSON(url, "POST");

        if (tagData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "User Not Found!";
            return;
        }

        if (tagData.contains("Invalid credentials")) {
            error = tagData;
            return;
        }

        try {
            JSONObject tagJsonObj = new JSONObject(tagData);
            if (tagJsonObj != null) {
                return;
            }
        } catch (JSONException ex) {
            error = "User Data Corrupted!";
            Log.d("MyDebugMsg", "JSONException in updateUser");
            ex.printStackTrace();
        }
    }

    public void likeTag(int tagId) {

        String url = mContext.getString(R.string.rest_url)+"/"+mContext.getString(R.string.tags_endpoint)+"/"+tagId+"/like";
        error = null;

        String tagData = restConn.uploadJSON(url, "PUT");

        Log.d("TagData", tagData.toString());
        if (tagData == null){
            Log.d("MyDebugMsg", "Having trouble loading URL: "+url);
            error = "User Not Found!";
            return;
        }

        if (tagData.contains("Invalid credentials")) {
            error = tagData;
            return;
        }

        if (tagData != null) {
            return;
        }
    }

    public void deleteItem(int position) {
        tagList.remove(position);
    }

    public void duplicateItem (int position) {
        HashMap clone = (HashMap) ((HashMap) tagList.get(position)).clone();
        tagList.add(position + 1, clone);
    }
}