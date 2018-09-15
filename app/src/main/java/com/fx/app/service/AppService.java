package com.fx.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.fx.app.fxmplayer.App;
import com.fx.app.sqlite.DB;

public class AppService extends Service {



    public AppService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onCreate() {
        Log.i("AppService", "Creating Service...");
        DB.create(this, "DB", 1);

        Log.i("AppService", "Creating Service finish");
        App.isServiceReady = true;
    }


}
