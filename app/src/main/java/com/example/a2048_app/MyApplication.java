package com.example.a2048_app;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Initialise le manager avec le contexte global
        MusicManager.getInstance().init(this);

        // 2. Abonne le MusicManager au cycle de vie de l'application
        ProcessLifecycleOwner.get().getLifecycle().addObserver(MusicManager.getInstance());
    }
}