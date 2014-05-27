package kr.kdev.dg1s.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.kdev.dg1s.R;

public class NotificationService extends Service {

    final String TAG = "Notifications";

    Context context;
    Calendar calendar;

    Intent breakfastIntent;
    Intent lunchIntent;
    Intent dinnerIntent;

    AlarmManager breakfastAlarm;
    AlarmManager lunchAlarm;
    AlarmManager dinnerAlarm;

    SharedPreferences preferences;
    NotificationManager manager;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                pullAndDisplaySettings();
            }
        }
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        preferences = context.getSharedPreferences("kr.kdev.dg1s", MODE_PRIVATE);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Create alarm manager
        breakfastAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        lunchAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        dinnerAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Create pending intent & register it to your alarm notifier class
        breakfastIntent = new Intent(this, NotificationService.class);
        lunchIntent = new Intent(this, NotificationService.class);
        dinnerIntent = new Intent(this, NotificationService.class);
        breakfastIntent.putExtra("time", "breakfast");
        lunchIntent.putExtra("time", "lunch");
        dinnerIntent.putExtra("time", "dinner");

        calendar = Calendar.getInstance();

        setAlarm(0, false);
        setAlarm(1, false);
        setAlarm(2, false);
        Log.d(TAG, "onCreate");
    }

    void setAlarm(int code, boolean nextDay) {
        switch (code) {
            case 0:
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                calendar.set(Calendar.MINUTE, 5);
                calendar.set(Calendar.SECOND, 0);
                setAlarmManager(breakfastAlarm, breakfastIntent, calendar, 260500, nextDay);
                break;
            case 1:
                calendar.set(Calendar.HOUR_OF_DAY, 12);
                calendar.set(Calendar.MINUTE, 27);
                calendar.set(Calendar.SECOND, 0);
                setAlarmManager(lunchAlarm, lunchIntent, calendar, 122700, nextDay);
                break;
            case 2:
                calendar.set(Calendar.HOUR_OF_DAY, 18);
                calendar.set(Calendar.MINUTE, 5);
                calendar.set(Calendar.SECOND, 0);
                setAlarmManager(dinnerAlarm, dinnerIntent, calendar, 180500, nextDay);
        }
    }

    void setAlarmManager(AlarmManager manager, Intent intent, Calendar calendar, int id, boolean nextDay) {
        PendingIntent pendingIntent = PendingIntent.getService(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long time = calendar.getTimeInMillis();
        if (nextDay) {
            time = time + 24 * 60 * 60 * 1000;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC, time, pendingIntent);
        } else {
            manager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    void displayNotifications(String title, String menu) {
        if (menu != null && menu.length() < 15) {
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(menu)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(menu))
                    .build();
            manager.notify(1, notification);
        }
    }

    void pullAndDisplaySettings() {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String strNow = sdfNow.format(date);
        if (!preferences.getString("updateDate", "").equals(strNow)) {
            UpdateThread thread = new UpdateThread();
            thread.start();//식단, 학사정보 업데이트
            preferences.edit().putString("updateDate", strNow).commit();//실행날짜 업데이트
            Log.d("Update", "Updating meals...");
        } else {
            if (calendar.get(Calendar.HOUR_OF_DAY) <= 7) {
                displayNotifications(getString(R.string.breakfast), preferences.getString("breakfast", null));
                setAlarm(0, true);
            } else if (calendar.get(Calendar.HOUR_OF_DAY) <= 11 ||
                    (calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) < 40)) {
                displayNotifications(getString(R.string.lunch), preferences.getString("lunch", null));
                setAlarm(1, true);
            } else if (calendar.get(Calendar.HOUR_OF_DAY) < 23) {
                displayNotifications(getString(R.string.dinner), preferences.getString("dinner", null));
                setAlarm(2, true);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getStringExtra("time") != null) {
            pullAndDisplaySettings();
        }
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);
    }

    class UpdateThread extends Thread {
        public void run() {
            Parsers.MealParser mealParser = new Parsers.MealParser();
            mealParser.parseMeal(context);

            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);
        }
    }
}
