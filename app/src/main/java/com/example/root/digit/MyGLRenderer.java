package com.example.root.digit;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import text.GLText;

/**
 * Created by nglofton on 8/13/15.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {
    public static float distance = 0;
    private String tag;
    private GLText glText;                             // A GLText Instance
    private Context context;                           // Context (from Activity)

    private int width = 100;                           // Updated to the Current Width + Height in onSurfaceChanged()
    private int height = 100;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MyGLRenderer(Context context, HashMap tag)  {
        super();
        this.context = context;
        this.tag = tag.get("comment").toString();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // Create the GLText
        glText = new GLText(context.getAssets());

        // Load the font from file (set size + padding), creates the texture
        // NOTE: after a successful call to this the font is ready for rendering!
        glText.load("Roboto-Regular.ttf", 14, 2, 2);  // Create Font (Height: 14 Pixels / X+Y Padding 2 Pixels)

        // enable texture + alpha blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

        GLES20.glClear(clearMask);
        if (OverlayView.x != null && OverlayView.y != null) {
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

            // Begin Text Rendering (Set Color WHITE)
            glText.begin(1.0f, 1.0f, 1.0f, 1.0f, mMVPMatrix);

            // Set X,Y based on current position in relation to tag position
            float myX = Float.parseFloat(OverlayView.x) - Float.parseFloat(OverlayView.dx);
            float myY = Float.parseFloat(OverlayView.dy) - Float.parseFloat(OverlayView.y);
            Log.d("distance", String.valueOf(distance));
            Log.d("size", String.valueOf(15 * ((100 - distance)/100)));

            // Place center of string as location(Assuming screen rotation is on)
            glText.drawC(this.tag, myX, myY, 0);

            // Scale text based on position
            glText.setScale(10);
            glText.end();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // Take into account device orientation
        if (width > height) {
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        } else {
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -1 / ratio, 1 / ratio, 1, 10);
        }

        // Save width and height
        this.width = width;                             // Save Current Width
        this.height = height;                           // Save Current Height

        int useForOrtho = Math.min(width, height);

        //TODO: Is this wrong?
        Matrix.orthoM(mViewMatrix, 0,
                -useForOrtho / 2,
                useForOrtho / 2,
                -useForOrtho / 2,
                useForOrtho / 2, 0.1f, 100f);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
