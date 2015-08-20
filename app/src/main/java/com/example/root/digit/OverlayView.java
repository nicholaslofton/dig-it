package com.example.root.digit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;


/**
 * Created by nglofton on 8/13/15.
 */
public class OverlayView extends View implements SensorEventListener, LocationListener {

    public static final String DEBUG_TAG = "OverlayView Log";

    private final Context context;
    private Handler handler;

    // Mount Washington, NH: 44.27179, -71.3039, 6288 ft (highest peak
    public final static Location tagLocation = new Location("manual");

    protected LocationManager locationManager = null;
    protected SensorManager sensors = null;

    private Location lastLocation;
    private float[] lastAccelerometer;
    private float[] lastCompass;

    private float verticalFOV;
    private float horizontalFOV;

    private boolean isAccelAvailable;
    private boolean isCompassAvailable;
    private boolean isGyroAvailable;
    private Sensor accelSensor;
    private Sensor compassSensor;
    private Sensor gyroSensor;
    private CameraView mCameraView;

    private TextPaint contentPaint;

    private float[] mCameraRotation = new float[9];
    private float[] mOrientation = new float[3];
    private static float ALPHA = 0.2f;


    public static String x = null;
    public static String y = null;
    public static String angle = null;
    public static String dx = null;
    public static String dy = null;
    public static String dz = null;

    public OverlayView(Context context, CameraView cameraView) {
        super(context);
        this.context = context;
        this.handler = new Handler();
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        sensors = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        startSensors();
        startGPS();

        // get some camera parameters
        mCameraView = cameraView;
        verticalFOV = mCameraView.verticalFOV;
        horizontalFOV = mCameraView.horizontalFOV;

        // paint for text
        contentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Paint.Align.LEFT);
        contentPaint.setTextSize(50);
        contentPaint.setColor(Color.RED);
    }

    protected void startSensors() {
        isAccelAvailable = sensors.registerListener(this, accelSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isCompassAvailable = sensors.registerListener(this, compassSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        isGyroAvailable = sensors.registerListener(this, gyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void startGPS() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "Best provider: " + best);

        locationManager.requestLocationUpdates(best, 0, 0, this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.d(DEBUG_TAG, "onDraw");
        super.onDraw(canvas);

        float curBearingToTag = 0.0f;
        float altDiff         = 0.0f;

        if (lastLocation != null) {

            curBearingToTag = lastLocation.bearingTo(tagLocation);
            altDiff         = (float) (tagLocation.getAltitude() - lastLocation.getAltitude());
            MyGLRenderer.distance = lastLocation.distanceTo(tagLocation);

        }

        // compute rotation matrix
        float rotation[] = new float[9];
        float identity[] = new float[9];
        if (lastAccelerometer != null && lastCompass != null) {
            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);
            if (gotRotation) {
                float cameraRotation[] = new float[9];
                // remap such that the camera is pointing straight down the Y axis
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X,
                        SensorManager.AXIS_Z, cameraRotation);

                // orientation vector
                float orientation[] = new float[3];
                SensorManager.getOrientation(cameraRotation, orientation);

                float cdx;
                float cdy;
                float tdy;

                // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
                cdx = (float) ((canvas.getWidth() / mCameraView.horizontalFOV) * (Math.toDegrees(orientation[0]) - curBearingToTag));
                cdy = (float) ((canvas.getHeight() / mCameraView.verticalFOV) * Math.toDegrees(orientation[1]));

                x  = String.valueOf(Math.toDegrees(orientation[0]) - curBearingToTag);
                y  = String.valueOf(Math.toDegrees(orientation[1]));
                angle = String.valueOf(Math.toDegrees(orientation[2]));

                dx = String.valueOf(cdx);
                dy = String.valueOf(cdy);

                Log.d("Tag Location Altitude", String.valueOf(tagLocation.getAltitude()));
            }
        }
        canvas.save();
        canvas.translate(15.0f, 15.0f);
        StaticLayout textBox = new StaticLayout("Look around for the tag data", contentPaint,
                800, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
        textBox.draw(canvas);
        canvas.restore();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.d(DEBUG_TAG, "onAccuracyChanged");

    }

    public void onSensorChanged(SensorEvent event) {
        // Log.d(DEBUG_TAG, "onSensorChanged");
        // We have to run this through a low-pass filter as the results of the requestLocation are
        // very noisy. Without the low-pass filter, our text will be very jumpy.
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lastAccelerometer = lowPass(event.values.clone(), lastAccelerometer);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lastCompass = lowPass(event.values.clone(), lastCompass);
        }

        this.invalidate();
    }

    public void onLocationChanged(Location location) {
        // store it off for use when we need it
        lastLocation = location;
    }

    public void onProviderDisabled(String provider) {
        // ...
    }

    public void onProviderEnabled(String provider) {
        // ...
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // ...
    }

    public static void setLocation (double latitude, double longitude, double altitude) {
        tagLocation.setLatitude(latitude);
        tagLocation.setLongitude(longitude);
        tagLocation.setAltitude(altitude);
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    // this is not an override
    public void onPause() {
        locationManager.removeUpdates(this);
        sensors.unregisterListener(this);
    }

    // this is not an override
    public void onResume() {
        startSensors();
        startGPS();
    }
}
