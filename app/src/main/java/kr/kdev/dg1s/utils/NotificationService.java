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
import android.widget.Toast;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.kdev.dg1s.R;

public class NotificationService extends Service {

    final String TAG = "Notifications";

    Context context;

    Intent breakfastIntent;
    Intent lunchIntent;
    Intent dinnerIntent;

    AlarmManager breakfastAlarm;
    AlarmManager lunchAlarm;
    AlarmManager dinnerAlarm;

    SharedPreferences preferences;
    NotificationManager manager;

    String current;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                pullSettings(current);
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

        Toast.makeText(this, "Congrats! MyService Created", Toast.LENGTH_LONG).show();

        //Create alarm manager
        breakfastAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        lunchAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        dinnerAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //Create pending intent & register it to your alarm notifier class
        breakfastIntent = new Intent(this, NotificationService.class);
        lunchIntent = new Intent(this, AlarmReceiver.class);
        dinnerIntent = new Intent(this, NotificationService.class);
        breakfastIntent.putExtra("time", "breakfast");
        lunchIntent.putExtra("time", "lunch");
        dinnerIntent.putExtra("time", "dinner");

        //set timer you want alarm to work
        //set that timer as a RTC Wakeup to alarm manager object
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        setAlarm(breakfastAlarm, breakfastIntent, calendar, 260500);

        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 27);
        calendar.set(Calendar.SECOND, 0);
        setAlarm(lunchAlarm, lunchIntent, calendar, 232700);

        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        setAlarm(dinnerAlarm, dinnerIntent, calendar, 181100);

        Log.d(TAG, "onCreate");
    }

    void setAlarm(AlarmManager manager, Intent intent, Calendar calendar, int id) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 19) {
            manager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }

    void displayNotifications(String title, String menu) {
        if (!(menu.equals("없음"))) {
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(menu)
                    .build();
            manager.notify(1, notification);
        }
    }

    void pullSettings(String time) {
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String strNow = sdfNow.format(date);
        if (!preferences.getString("updatedate", "").equals(strNow)) {
            UpdateThread thread = new UpdateThread();
            thread.start();//식단, 학사정보 업데이트
            preferences.edit().putString("updatedate", strNow).commit();//실행날짜 업데이트
        } else {
            if (time.equals("breakfast")) {
                displayNotifications(getString(R.string.breakfast), preferences.getString("breakfast", "없음"));
            } else if (time.equals("lunch")) {
                displayNotifications(getString(R.string.lunch), preferences.getString("lunch", "없음"));
            } else if (time.equals("dinner")) {
                displayNotifications(getString(R.string.dinner), preferences.getString("dinner", "없음"));
            }
        }
    }

    private void updateSikdan() throws IOException {
        String breakfast = "등록된 식단이 없습니다.";
        String lunch = "등록된 식단이 없습니다.";
        String dinner = "등록된 식단이 없습니다.";

        URL nURL = new URL("http://www.dg1s.hs.kr/user/carte/list.do");
        InputStream html = nURL.openStream();
        Source source = new Source(new InputStreamReader(html, "UTF-8"));
        source.fullSequentialParse();

        //테이블가져오기
        Element table = source.getFirstElementByClass("meals_today_list");
        int cnt = table.getAllElements(HTMLElementName.IMG).size();

        for (int i = 0; i < cnt; i++) {
            String panbyul = table.getAllElements(HTMLElementName.IMG).get(i).getAttributeValue("alt");
            if (panbyul != null) {
                if (panbyul.equals("조식")) {
                    breakfast = table.getAllElements(HTMLElementName.IMG).get(i).getParentElement().getContent().toString();
                    breakfast = breakfast.replaceAll("[^>]*/> ", "");
                    breakfast = breakfast.replaceAll("[①-⑮]", "");
                } else if (panbyul.equals("중식")) {
                    lunch = table.getAllElements(HTMLElementName.IMG).get(i).getParentElement().getContent().toString();
                    lunch = lunch.replaceAll("[^>]*/> ", "");
                    lunch = lunch.replaceAll("[①-⑮]", "");
                } else if (panbyul.equals("석식")) {
                    dinner = table.getAllElements(HTMLElementName.IMG).get(i).getParentElement().getContent().toString();
                    dinner = dinner.replaceAll("[^>]*/> ", "");
                    dinner = dinner.replaceAll("[①-⑮]", "");
                }
            }
        }
        html.close();
        preferences.edit().putString("breakfast", breakfast).commit();
        preferences.edit().putString("lunch", lunch).commit();
        preferences.edit().putString("dinner", dinner).commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Intent received", Toast.LENGTH_LONG).show();
        displayNotifications(getString(R.string.breakfast), preferences.getString("breakfast", "없음"));
        if (intent.getStringExtra("init").equals("true")) {
            return START_STICKY;
        }
        Toast.makeText(this, "Intent received " + intent.getStringExtra("time"), Toast.LENGTH_LONG).show();
        current = intent.getStringExtra("time");
        pullSettings(intent.getStringExtra("time"));
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "MyService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    class UpdateThread extends Thread {
        public void run() {
            Parsers.MealParser mealParser = new Parsers.MealParser();
            mealParser.parseMeal(context);
            Message msg = new Message();
            msg.what = 0;
            handler.sendMessage(msg);
        }
    }
}
