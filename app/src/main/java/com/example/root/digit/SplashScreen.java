package com.example.root.digit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;

/**
 * Created by nglofton on 8/8/15.
 */

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;
    private static Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        i = null;

        File file = new File(getApplicationContext().getFilesDir(), ".userdata");
        if(file.exists()) {
            MainActivity.logged_in = true;
            MainActivity.getUserData(getApplicationContext());
            MainActivity.logged_in = false;
            i = new Intent(SplashScreen.this, MainActivity.class);
        }
        else {
            i = new Intent(SplashScreen.this, LoginActivity.class);
        }

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

}
