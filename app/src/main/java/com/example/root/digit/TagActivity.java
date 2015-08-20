package com.example.root.digit;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class TagActivity extends AppCompatActivity {

    private MyGLSurfaceView mGLView;
    private CameraView cameraView;
    private OverlayView arContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        HashMap tag  = (HashMap) bundle.getSerializable("tag");

        mGLView = new MyGLSurfaceView(this, tag);
        setContentView(mGLView);

        mGLView.setKeepScreenOn(true);
        // Now also create a view which contains the camera preview...
        cameraView = new CameraView(this);
        // ...and add it, wrapping the full screen size.
        addContentView( cameraView, new ViewGroup.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT ));
        arContent = new OverlayView(getApplicationContext(), cameraView);
        arContent.setLocation(Float.parseFloat(tag.get("latitude").toString()), Float.parseFloat(tag.get("longitude").toString()), Float.parseFloat(tag.get("altitude").toString()));
        addContentView(arContent, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Remember to resume the glSurface
     */
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
        arContent.onResume();
    }

    /**
     * Also pause the glSurface
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Making sure we do this so it doesn't keep attempting to get location data and eating up
        // memory attempting to get the bearing when we don't need it
        mGLView.onPause();
        arContent.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mGLView.setKeepScreenOn(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer mRenderer;

        public MyGLSurfaceView(Context context, HashMap tag){
            super(context);

            setEGLContextClientVersion(2);

            mRenderer = new MyGLRenderer(context, tag);
            // Set the Renderer for drawing on the GLSurfaceView
            // To see the camera preview, the OpenGL surface has to be created translucently.
            // See link above.
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setRenderer(mRenderer);
        }

    }
}
