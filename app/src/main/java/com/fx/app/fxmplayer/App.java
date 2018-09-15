package com.fx.app.fxmplayer;

import android.app.Application;
import android.content.Intent;

import com.fx.app.service.AppService;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, AppService.class));
    }

    public static boolean isServiceReady = false;
}
