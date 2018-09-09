package com.fx.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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

        DB.create(this, "DB", 1);

    }
}
