package kr.kdev.dg1s;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import kr.kdev.dg1s.cards.CardViewStatusNotifier;
import kr.kdev.dg1s.cards.MealCard;
import kr.kdev.dg1s.cards.PlanCard;
import kr.kdev.dg1s.utils.Adapters;
import kr.kdev.dg1s.utils.Parsers;
import kr.kdev.dg1s.utils.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends ActionBarActivity implements CardViewStatusNotifier {

    Context context;
    MealCard mealCard;
    PlanCard planCard;
    SharedPreferences prefs;
    Parsers.WeatherParser weatherParser;
    private int queue = 0;
    private SwipeRefreshLayout pullToRefreshLayout;

    public void notifyCompletion(int status) {
        if (queue == 0) {
            pullToRefreshLayout.setRefreshing(false);
            if (status == FAILURE) {
                Crouton.makeText(MainActivity.this, getString(R.string.update_finished_with_errors), Style.ALERT);
            } else {
                Crouton.makeText(MainActivity.this, getString(R.string.update_finished_successfully), Style.ALERT);
            }

        }

        if (queue > 0) {
            queue--;
        }

        if (status == FAILURE) {
            Log.e("MainActivity_CardQueue", "Error occurred while updating card(s)");
        }
        Log.i("MainActivity_CardQueue", queue + " left in queue");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("kr.kdev.dg1s", MODE_PRIVATE);

        chkFirstRun();

        context = getApplicationContext();

        // TODO 알림 서비스 다시 구축
        //Intent intent = new Intent(context, NotificationService.class);
        //context.startService(intent);

        setLayouts();

        //ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.dark_gray));
        //getSupportActionBar().setBackgroundDrawable(colorDrawable);

    }

    void setLayouts() {
        setContentView(R.layout.main);

        pullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.infoScrollView);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startManualUpdate();
            }
        });

        ViewGroup group = (ViewGroup) findViewById(R.id.container);
        mealCard = new MealCard(context, group, MainActivity.this);
        planCard = new PlanCard(context, group, MainActivity.this);

        final FloatingActionsMenu fab = (FloatingActionsMenu) findViewById(R.id.FAB);
        ScrollView scrollView = (ScrollView) pullToRefreshLayout.findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                fab.collapse();
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i1 = new Intent(MainActivity.this, Settings.class);
                startActivity(i1);
                overridePendingTransition(R.anim.appear_decelerate_btt, R.anim.still);
                break;
            case R.id.action_credits:
                /**
                 Intent i2 = new Intent(MainActivity.this, Credits.class);
                 startActivity(i2);
                 overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
                 */
                File externalDirectory = Environment.getExternalStorageDirectory();
                File internalDirectory = Environment.getDataDirectory();
                FileChannel source;
                FileChannel destination;
                String mealDBPath = "/data/" + "kr.kdev.dg1s" + "/databases/" + "meal.db";
                String mealBackupDBPath = "meal.db";

                String planDBPath = "/data/" + "kr.kdev.dg1s" + "/databases/" + "plans.db";
                String planBackupDBPath = "plans.db";
                File currentDB;
                File backupDB;

                try {
                    currentDB = new File(internalDirectory, mealDBPath);
                    backupDB = new File(externalDirectory, mealBackupDBPath);
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();

                    currentDB = new File(internalDirectory, planDBPath);
                    backupDB = new File(externalDirectory, planBackupDBPath);
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
            case KeyEvent.KEYCODE_MENU:
                openOptionsMenu();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAll();
    }

    private void updateAll() {
        weatherParser = new Parsers.WeatherParser();
        long now = System.currentTimeMillis();

        if (hasInternetConnection()) {//네트워크가 통신가능한 상태라면
            Log.i("NetStat", "네트워크 상태 양호.");

            if (now - prefs.getLong("updatehour", 0) >= 10800000 || prefs.getString("updateDate", "").length() <= 0) {//처음 실행했거나 이전 실행시에서 3시간이 경과했다면
                new WeatherAsync().execute(null, null, null);//날씨 업데이트
                prefs.edit().putLong("updatehour", now).commit();//실행시간 업데이트
            }
        } else {//네트워크가 통신가능하지 않다면
            Crouton.makeText(MainActivity.this, R.string.error_network_long, Style.ALERT).show();
            Log.i("NetStat", "네트워크 상태 불량!");
        }
        setWeather();
        setDayInfo();
        setTwaesa();
    }


    private void setTwaesa() {
        // TODO : 퇴사판별 알고리즘 만들기

        /**
         TextView gobustime = (TextView) findViewById(R.id.busTime);
         RelativeLayout container = (RelativeLayout) findViewById(R.id.gobustime);
         Calendar cal = Calendar.getInstance();

         int day_of_week = cal.get(Calendar.DAY_OF_WEEK);

         if (prefs.getString("academicinfo", "").contains("퇴사")) {
         container.setVisibility(View.VISIBLE);
         if (day_of_week == 6) {
         gobustime.setText("5:40 PM");
         } else if (day_of_week == 7) {
         gobustime.setText("1:20 PM");
         } else {
         gobustime.setText("5:40 PM");
         }
         } else {
         container.setVisibility(View.GONE);
         }
         */
    }

    private void startManualUpdate() {
        if (hasInternetConnection()) {
            new WeatherAsync().execute(null, null, null);//날씨 업데이트
            mealCard.forceUpdate();
            planCard.forceUpdate();
            Log.i("ManualUpdate", "강제 업데이트 성공.");
        } else {
            pullToRefreshLayout.setRefreshing(false);
            Crouton.makeText(MainActivity.this, getString(R.string.error_network_long), Style.ALERT).show();
            Log.i("ManualUpdate", "강제 업데이트 실패.");
        }

    }

    private void setDayInfo() {
        TextView dayinfo = (TextView) findViewById(R.id.dayinfo);
        TextView dayinfo_text = null;
        boolean vertical = true;

        if (findViewById(R.id.dayinfotext) == null) {
            vertical = false;
        } else {
            dayinfo_text = (TextView) findViewById(R.id.dayinfotext);
        }
        Calendar cal = Calendar.getInstance();

        int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
        if (day_of_week == 1) {
            dayinfo.setText(getString(R.string.sunday));
            if (vertical) dayinfo_text.setText(getString(R.string.sunday_t));
        } else if (day_of_week == 2) {
            dayinfo.setText(getString(R.string.monday));
            if (vertical) dayinfo_text.setText(getString(R.string.monday_t));
        } else if (day_of_week == 3) {
            dayinfo.setText(getString(R.string.tuesday));
            if (vertical) dayinfo_text.setText(getString(R.string.tuesday_t));
        } else if (day_of_week == 4) {
            dayinfo.setText(getString(R.string.wednesday));
            if (vertical) dayinfo_text.setText(getString(R.string.wednesday_t));
        } else if (day_of_week == 5) {
            dayinfo.setText(getString(R.string.thursday));
            if (vertical) dayinfo_text.setText(getString(R.string.thursday_t));
        } else if (day_of_week == 6) {
            dayinfo.setText(getString(R.string.friday));
            if (vertical) dayinfo_text.setText(getString(R.string.friday_t));

        } else if (day_of_week == 7) {
            dayinfo.setText(getString(R.string.saturday));
            if (vertical) dayinfo_text.setText(getString(R.string.saturday_t));
        }

        Log.i("MainActivity_DayInfo", "요일정보 업데이트 완료.");
    }

    private void chkFirstRun() {
        if (prefs.getBoolean("firstrun", true)) {
            //앱 최초 설치시에 호출
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setMessage(getResources().getString(R.string.welcome));
            alert.show();
            prefs.edit().putBoolean("firstrun", false).commit();
            try {
                if (prefs.getInt("updaterun", 0) != getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    prefs.edit().putInt("updaterun", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        } else
            try {
                if (prefs.getInt("updaterun", 0) != getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    //업데이트 후 호출
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage(getString(R.string.update_info) + "\n" + getString(R.string.update_description));
                    alert.show();
                    prefs.edit().putInt("updaterun", getPackageManager().getPackageInfo(getPackageName(), 0).versionCode).commit();
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
    }

    private void setWeather() {
        /**
         ImageView tIcon[] = new ImageView[5];
         TextView tWeather[] = new TextView[5];
         TextView tTime[] = new TextView[5];
         TextView tTemp[] = new TextView[5];

         tIcon[0] = (ImageView) findViewById(R.id.t1_icon);
         tIcon[1] = (ImageView) findViewById(R.id.t2_icon);
         tIcon[2] = (ImageView) findViewById(R.id.t3_icon);
         tIcon[3] = (ImageView) findViewById(R.id.t4_icon);
         tIcon[4] = (ImageView) findViewById(R.id.t5_icon);

         tWeather[0] = (TextView) findViewById(R.id.t1_weather);
         tWeather[1] = (TextView) findViewById(R.id.t2_weather);
         tWeather[2] = (TextView) findViewById(R.id.t3_weather);
         tWeather[3] = (TextView) findViewById(R.id.t4_weather);
         tWeather[4] = (TextView) findViewById(R.id.t5_weather);

         tTime[0] = (TextView) findViewById(R.id.t1_time);
         tTime[1] = (TextView) findViewById(R.id.t2_time);
         tTime[2] = (TextView) findViewById(R.id.t3_time);
         tTime[3] = (TextView) findViewById(R.id.t4_time);
         tTime[4] = (TextView) findViewById(R.id.t5_time);

         tTemp[0] = (TextView) findViewById(R.id.t1_temp);
         tTemp[1] = (TextView) findViewById(R.id.t2_temp);
         tTemp[2] = (TextView) findViewById(R.id.t3_temp);
         tTemp[3] = (TextView) findViewById(R.id.t4_temp);
         tTemp[4] = (TextView) findViewById(R.id.t5_temp);

         String sunny = getString(R.string.sunny);
         String cloudy1 = getString(R.string.cloudy1);
         String cloudy2 = getString(R.string.cloudy2);
         String cloudy3 = getString(R.string.cloudy3);
         String rainy = getString(R.string.rainy);
         String rainsnow = getString(R.string.rainsnow);
         String snowy = getString(R.string.snowy);


         for (int i = 0; i < 5; i++) {
         if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("맑음")) {
         tIcon[i].setImageResource(R.drawable.weather_sunny);
         tWeather[i].setText(sunny);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("구름 조금")) {
         tIcon[i].setImageResource(R.drawable.weather_cloudy1);
         tWeather[i].setText(cloudy1);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("구름 많음")) {
         tIcon[i].setImageResource(R.drawable.weather_cloudy2);
         tWeather[i].setText(cloudy2);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("흐림")) {
         tIcon[i].setImageResource(R.drawable.weather_cloudy3);
         tWeather[i].setText(cloudy3);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("비")) {
         tIcon[i].setImageResource(R.drawable.weather_rainy);
         tWeather[i].setText(rainy);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("눈/비")) {
         tIcon[i].setImageResource(R.drawable.weather_rainsnow);
         tWeather[i].setText(rainsnow);
         } else if (prefs.getString("weather" + i, "") != null && prefs.getString("weather" + i, "").equals("눈")) {
         tIcon[i].setImageResource(R.drawable.weather_snowy);
         tWeather[i].setText(snowy);
         }
         if (prefs.getString("time" + i, "") != null)
         tTime[i].setText(prefs.getString("time" + i, "") + ":00");
         if (prefs.getString("temp" + i, "") != null)
         tTemp[i].setText(prefs.getString("temp" + i, "") + "ºC");
         }

         Log.d("SetWeather", "날씨 업데이트 완료.");
         */
    }

    private boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        return ((mobile != null && mobile.isConnected()) || (wifi != null && wifi.isConnected())
                || (wimax != null && wimax.isConnected()));
    }

    public class WeatherAsync extends AsyncTask<String, String, ArrayList<Adapters.WeatherAdapter>> {

        @Override
        protected ArrayList<Adapters.WeatherAdapter> doInBackground(String... params) {
            return weatherParser.parseWeather();
        }

        @Override
        protected void onPostExecute(ArrayList<Adapters.WeatherAdapter> result) {
            for (int i = 0; i < result.size(); i++) {
                Adapters.WeatherAdapter wa = result.get(i);
                prefs.edit().putString("weather" + i, wa.weather).commit();
                prefs.edit().putString("time" + i, wa.time).commit();
                prefs.edit().putString("temp" + i, wa.temperature).commit();
                setWeather();
            }
        }

    }
}