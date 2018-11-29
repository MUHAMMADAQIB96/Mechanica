package com.example.fyp.mechanica.helpers;

import android.app.Application;

import io.paperdb.Paper;

/**
 * Created by irfan on 11/28/18.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Paper.init(this);
    }
}
