package com.example.root.digit;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    public static Camera camera;
    private Context mContext;
    public static float verticalFOV;
    public static float horizontalFOV;
    public static float mRotation;


    public CameraView( Context context ) {
        super( context );
        // We're implementing the Callback interface and want to get notified
        // about certain surface events.
        getHolder().addCallback( this );
        // We're changing the surface to a PUSH surface, meaning we're receiving
        // all buffer data from another component - the camera, in this case.
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mContext = context;
    }

    public void surfaceCreated( SurfaceHolder holder ) {
        // Once the surface is created, simply open a handle to the camera hardware.
        camera = Camera.open();
        Camera.Parameters p = camera.getParameters();
        verticalFOV = p.getVerticalViewAngle();
        horizontalFOV = p.getHorizontalViewAngle();
    }

    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        // This method is called when the surface changes, e.g. when it's size is set.
        // We use the opportunity to initialize the camera preview display dimensions.

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        Activity activity = (Activity) mContext;

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        camera.setDisplayOrientation((info.orientation - degrees + 360) % 360);


        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> sizes = p.getSupportedPreviewSizes();

        verticalFOV = p.getVerticalViewAngle();
        horizontalFOV = p.getHorizontalViewAngle();
        mRotation = (float) degrees;

        Camera.Size cs = sizes.get(0);
        p.setPreviewSize(cs.width, cs.height);
        camera.setParameters(p);

        // We also assign the preview display to this surface...
        try {
            camera.setPreviewDisplay(holder);
        } catch( IOException e ) {
            e.printStackTrace();
        }
        // ...and start previewing. From now on, the camera keeps pushing preview
        // images to the surface.
        camera.startPreview();
    }

    public void surfaceDestroyed( SurfaceHolder holder ) {
        // Once the surface gets destroyed, we stop the preview mode and release
        // the whole camera since we no longer need it.
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}