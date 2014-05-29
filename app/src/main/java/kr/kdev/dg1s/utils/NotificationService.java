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

    final static String TAG = "Notifications";

    Context context;
    Calendar calendar;

    Intent breakfastIntent;
    Intent lunchIntent;
    Intent dinnerIntent;

    AlarmManager breakfastAlarm;
    AlarmManager lunchAlarm;
    AlarmManager dinnerAlarm;

    SharedPreferences preferences;
    NotificationManager notificationManager;

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
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

        refreshAlarm();

        Log.d(TAG, "onCreate");
    }

    void refreshAlarm() {
        calendar = Calendar.getInstance();

        cancelAlarmManager(breakfastAlarm, breakfastIntent, R.id.breakfastAlarmId);
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setAlarmManager(breakfastAlarm, breakfastIntent, calendar, R.id.breakfastAlarmId,
                (calendar.get(Calendar.HOUR_OF_DAY) < 6 ||
                        (calendar.get(Calendar.HOUR_OF_DAY) == 6 && calendar.get(Calendar.MINUTE) < 5))
        );

        cancelAlarmManager(lunchAlarm, lunchIntent, R.id.lunchAlarmId);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 25);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setAlarmManager(lunchAlarm, lunchIntent, calendar, R.id.breakfastAlarmId,
                (calendar.get(Calendar.HOUR_OF_DAY) < 12 ||
                        (calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) < 25))
        );

        cancelAlarmManager(dinnerAlarm, dinnerIntent, R.id.dinnerAlarmId);
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setAlarmManager(dinnerAlarm, dinnerIntent, calendar, R.id.dinnerAlarmId,
                (calendar.get(Calendar.HOUR_OF_DAY) < 18 ||
                        (calendar.get(Calendar.HOUR_OF_DAY) == 18 && calendar.get(Calendar.MINUTE) < 5))
        );
    }

    void cancelAlarmManager(AlarmManager manager, Intent intent, int id) {
        PendingIntent pendingIntent = PendingIntent.getService(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.cancel(pendingIntent);
    }

    void setAlarmManager(AlarmManager manager, Intent intent, Calendar calendar, int id, boolean isToday) {
        PendingIntent pendingIntent = PendingIntent.getService(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long time = calendar.getTimeInMillis();
        if (!isToday) {
            time = time + 24 * 60 * 60 * 1000;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC, time, pendingIntent);
        } else {
            manager.set(AlarmManager.RTC, time, pendingIntent);
        }
    }

    void displayNotification(String title, String menu) {
        if (menu != null && menu.length() < 15) {
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(menu)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(menu))
                    .build();
            notificationManager.notify(1, notification);
        }
    }

    void pullAndDisplaySettings() {
        Log.d("PDS", "start");
        String strNow = new SimpleDateFormat("yyyy/MM/dd").format(new Date(System.currentTimeMillis()));
        if (!preferences.getString("updateDate", "").equals(strNow)) {
            UpdateThread thread = new UpdateThread();
            thread.start();
            preferences.edit().putString("updateDate", strNow).commit();//실행날짜 업데이트
            Log.d("Update", "Updating meals...");
        } else {
            if (calendar.get(Calendar.HOUR_OF_DAY) == 6 && calendar.get(Calendar.MINUTE) <= 5) {
                displayNotification(getString(R.string.breakfast), preferences.getString("breakfast", null));
            } else if ((calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) < 40)) {
                displayNotification(getString(R.string.lunch), preferences.getString("lunch", null));
            } else if (calendar.get(Calendar.HOUR_OF_DAY) < 19) {
                displayNotification(getString(R.string.dinner), preferences.getString("dinner", null));
            }
        }
        refreshAlarm();
        Log.d("PDS", "fin");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Message received");
        if (intent.getStringExtra("time") != null) {
            pullAndDisplaySettings();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        refreshAlarm();
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
