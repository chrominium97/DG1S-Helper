package kr.kdev.dg1s.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private final BackgroundUpdateService service = new BackgroundUpdateService();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            service.setAlarm(context);
        }
    }

}
