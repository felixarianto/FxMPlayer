package com.fx.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
        }
        else {
            System.out.println("Not yet implemented >> " + intent.getAction());
        }
    }
}
