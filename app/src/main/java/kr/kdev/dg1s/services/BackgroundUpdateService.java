package kr.kdev.dg1s.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import kr.kdev.dg1s.cards.provider.MealProvider;
import kr.kdev.dg1s.cards.provider.PlanProvider;
import kr.kdev.dg1s.cards.provider.WeatherProvider;

public class BackgroundUpdateService extends BroadcastReceiver {

    private final static String TAG = "BackgroundUpdateService";

    // The application's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent received");

        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        new MealProvider(context, this).query(false);
        new PlanProvider(context, this).query(false);
        new WeatherProvider(context, this).query(false);
    }

    public void setAlarm(Context context) {

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context.getApplicationContext(), BackgroundUpdateService.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        try {
            alarmMgr.cancel(alarmIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
                3 * AlarmManager.INTERVAL_HOUR, alarmIntent);
    }
}
