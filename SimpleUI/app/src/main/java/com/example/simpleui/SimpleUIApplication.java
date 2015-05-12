package com.example.simpleui;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;

/**
 * Created by ggm on 5/12/15.
 */
public class SimpleUIApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "fgKEQGJ5j5hQRbC3Mwytop2zyR70MWyoSYUlpM9S", "cpaz9soGN8tfBG9VpeqjGBn4Oe3xza7DNepqIbbO");

    }
}
