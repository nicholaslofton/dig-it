package com.example.root.digit;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by nglofton on 6/22/2015.
 */
public class MyUtility {

    public JSONObject jsonParams = new JSONObject();

    public String userName = null;
    public String password = null;


    // Download an image from online
    public Bitmap downloadImage(String url) {
        Bitmap bitmap = null;

        InputStream stream = getHttpConnection(url);
        if(stream!=null) {
            bitmap = BitmapFactory.decodeStream(stream);
            try {
                stream.close();
            }catch (IOException e1) {
                Log.d("MyDebugMsg", "IOException in downloadImage()");
                e1.printStackTrace();
            }
        }

        return bitmap;
    }

    // Download a Json file from online
    public String downloadJSON(String url) {
        String json= null, line;

        InputStream stream = getHttpConnection(url);

        if (stream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder out = new StringBuilder();
            try {
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                reader.close();
                json = out.toString();
            } catch (IOException ex) {
                Log.d("MyDebugMsg", "IOException in downloadJSON()");
                ex.printStackTrace();
            }
        }
        line = null;
        stream = null;
        return json;
    }

    // Upload Json data to server
    public String uploadJSON(String url, String method) {
        String line;
        try {
            URL urlObj = new URL(url);
            HttpURLConnection httpConnection = (HttpURLConnection) urlObj.openConnection();
            httpConnection.setDoOutput(true);
            httpConnection.setRequestMethod(method);
            httpConnection.setRequestProperty("Content-Type", "application/json");
            String encoded = Base64.encodeToString((userName + ":" + password).getBytes("UTF-8"), Base64.NO_WRAP);
            httpConnection.setRequestProperty("Authorization", "Basic "+encoded);
            httpConnection.connect();
            // Send the JSON data
            OutputStreamWriter out = new  OutputStreamWriter(httpConnection.getOutputStream());
            out.write(jsonParams.toString());
            out.close();
            Log.d("JSON", jsonParams.toString());
            StringBuilder returnData = new StringBuilder();
            // Read the response, if needed
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    returnData.append(line);
                }
                reader.close();
                return returnData.toString();
            }
            else {
                InputStream stream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                while ((line = reader.readLine()) != null) {
                    Log.d("Uploading Finished", line); // for debugging purpose
                    return line.toString();
                }
                reader.close();
            }
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", "Unknown Host exception in uploadJSON()");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in uploadJSON()");
            ex.printStackTrace();
        }
        return null;
    }

    // Makes HttpURLConnection and returns InputStream
    public InputStream getHttpConnection(String urlString) {
        InputStream stream = null;
        try {
            URL url = new URL(urlString);
            Log.d("Testing", url.toString());
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            String encoded = Base64.encodeToString((userName + ":" + password).getBytes("UTF-8"), Base64.NO_WRAP);
            httpConnection.setRequestProperty("Authorization", "Basic "+encoded);
            httpConnection.connect();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = httpConnection.getInputStream();
            }
        }  catch (UnknownHostException e1) {
            Log.d("MyDebugMsg", "UnknownHostexception in getHttpConnection()");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.d("MyDebugMsg", "Exception in getHttpConnection()");
            ex.printStackTrace();
        }
        return stream;
    }
}
