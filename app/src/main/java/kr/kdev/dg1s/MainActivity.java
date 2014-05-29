package kr.kdev.dg1s;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import kr.kdev.dg1s.utils.Adapters;
import kr.kdev.dg1s.utils.NotificationService;
import kr.kdev.dg1s.utils.Parsers;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends ActionBarActivity implements OnRefreshListener {

    Context context;

    SharedPreferences prefs;
    Parsers.WeatherParser weatherParser;
    String breakfast, lunch, dinner;
    private Source source;

    private PullToRefreshLayout pullToRefreshLayout;

    private TextView BreakfastTextView, LunchTextView, DinnerTextView;
    private Handler SikdanHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0) {
                setMeal();
                setAcademic();
                setTwaesa();
                Crouton.makeText(MainActivity.this, getString(R.string.sikdan_updated), Style.INFO).show();
            } else if (message.what == 1) {
                Crouton.makeText(MainActivity.this, getString(R.string.error_network_long), Style.INFO).show();
            }
            pullToRefreshLayout.setRefreshComplete();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = getApplicationContext();

        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);

        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.infoScrollView);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .setup(pullToRefreshLayout);

        ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.dark_gray));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        BreakfastTextView = (TextView) findViewById(R.id.mealDetails);
        LunchTextView = (TextView) findViewById(R.id.lunchtv);
        DinnerTextView = (TextView) findViewById(R.id.dinnertv);

        //식단표 보기 버튼
        Button menubtn1 = (Button) findViewById(R.id.menubtn1);
        menubtn1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent1 = new Intent(MainActivity.this, MealPlanner.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });

        //숙제 보기 버튼
        Button menubtn2 = (Button) findViewById(R.id.menubtn2);
        menubtn2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this, PostView.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });

        //학사일정 버튼
        Button menubtn4 = (Button) findViewById(R.id.menubtn4);
        menubtn4.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent4 = new Intent(MainActivity.this, AcademicCalendar.class);
                startActivity(intent4);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });

        RelativeLayout breakfastbtn = (RelativeLayout) findViewById(R.id.breakfastcontainer);
        RelativeLayout lunchbtn = (RelativeLayout) findViewById(R.id.lunchcontainer);
        RelativeLayout dinnerbtn = (RelativeLayout) findViewById(R.id.dinnercontainer);
        RelativeLayout academicbtn = (RelativeLayout) findViewById(R.id.academiccontainer);

        breakfastbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MealPlanner.class);
                startActivity(intent);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });
        lunchbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MealPlanner.class);
                startActivity(intent);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });
        dinnerbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MealPlanner.class);
                startActivity(intent);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });
        academicbtn.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AcademicCalendar.class);
                startActivity(intent);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
            }
        });

        prefs = getSharedPreferences("kr.kdev.dg1s", MODE_PRIVATE);
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
                Intent i2 = new Intent(MainActivity.this, Credits.class);
                startActivity(i2);
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
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
        chkFirstRun();
        updateAll();
    }

    private void updateAll() {
        weatherParser = new Parsers.WeatherParser();
        long now = System.currentTimeMillis();
        Date date = new Date(now);

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd");
        String strNow = sdfNow.format(date);

        if (chkNetStat()) {//네트워크가 통신가능한 상태라면
            Log.d("NetStat", "네트워크 상태 양호.");

            if (now - prefs.getLong("updatehour", 0) >= 10800000 || prefs.getString("updateDate", "").length() <= 0) {//처음 실행했거나 이전 실행시에서 3시간이 경과했다면
                new WeatherAsync().execute(null, null, null);//날씨 업데이트
                prefs.edit().putLong("updatehour", now).commit();//실행시간 업데이트
            }
            if (!prefs.getString("updateDate", "").equals(strNow) || prefs.getString("updateDate", "").length() <= 0)//처음 실행했거나 이전 실행시에서 하루가 경과했다면
            {
                UpdateThread thread = new UpdateThread();
                thread.start();//식단, 학사정보 업데이트

                prefs.edit().putString("updateDate", strNow).commit();//실행날짜 업데이트
            }
        } else {//네트워크가 통신가능하지 않다면
            Crouton.makeText(MainActivity.this, R.string.error_network_long, Style.ALERT).show();
            Log.d("NetStat", "네트워크 상태 불량!");
        }
        setWeather();
        setMeal();
        setDayInfo();
        setAcademic();
        setTwaesa();
    }

    private void setTwaesa() {
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
    }

    private void ManualUpdateAll() {
        if (chkNetStat()) {
            new WeatherAsync().execute(null, null, null);//날씨 업데이트
            UpdateThread thread = new UpdateThread();
            thread.start();//식단, 학사정보 업데이트
            Log.d("ManualUpdateAll", "강제 업데이트 성공.");
        } else {
            pullToRefreshLayout.setRefreshComplete();
            Crouton.makeText(MainActivity.this, getString(R.string.error_network_long), Style.ALERT).show();
            Log.d("ManualUpdateAll", "강제 업데이트 실패.");
        }

    }

    private void setAcademic() {
        TextView academicinfo = (TextView) findViewById(R.id.academicInfo);
        TextView academicdayinfo = (TextView) findViewById(R.id.academicDate);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.academiccontainer);

        if (prefs.getString("academicinfo", "").length() >= 2) {
            final Calendar c = Calendar.getInstance();

            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH) + 1;
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            academicinfo.setText(prefs.getString("academicinfo", ""));
            academicdayinfo.setText(mYear + "년 " + mMonth + "월 " + mDay + "일");

            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.GONE);
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

        Log.d("SetDayInfo", "요일정보 업데이트 완료.");
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


    private void updateSikdan() throws IOException {
        breakfast = "등록된 식단이 없습니다.";
        lunch = "등록된 식단이 없습니다.";
        dinner = "등록된 식단이 없습니다.";

        URL nURL = new URL("http://www.dg1s.hs.kr/user/carte/list.do");
        InputStream html = nURL.openStream();
        source = new Source(new InputStreamReader(html, "UTF-8"));
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
        prefs.edit().putString("breakfast", breakfast).commit();
        prefs.edit().putString("lunch", lunch).commit();
        prefs.edit().putString("lunch_original", lunch).commit();
        prefs.edit().putString("dinner", dinner).commit();
    }

    private void updateAcademic() throws IOException {
        String academicinfo;
        final Calendar c = Calendar.getInstance();

        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH) + 1;
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        URL nURL = new URL("http://old.dg1s.hs.kr/general/calendar/view_more.html?CalSeq=2&SelectYear=" + mYear + "&SelectMonth=" + mMonth + "&SelectDay=" + mDay);

        InputStream html = nURL.openStream();
        source = new Source(new InputStreamReader(html, "EUC-KR"));

        //테이블가져오기
        academicinfo = source.getFirstElementByClass("more_data").getContent().toString();
        academicinfo = academicinfo.replaceAll("<br>", "");
        prefs.edit().putString("academicinfo", academicinfo).commit();
        Log.d("AcademicParser", "내용: " + academicinfo);
        html.close();
    }

    private void setMeal() {
        ImageView sudagreen = (ImageView) findViewById(R.id.sudagreen);

        if (prefs.getString("lunch", "").contains("<수다날>")) {
            String dump = prefs.getString("lunch", "");
            dump = dump.replaceAll("<수다날>", "");
            prefs.edit().putString("lunch", dump).commit();
        } else if (prefs.getString("lunch", "").contains("<그린데이>")) {
            String dump = prefs.getString("lunch", "");
            dump = dump.replaceAll("<그린데이>", "");
            prefs.edit().putString("lunch", dump).commit();
        }


        if (prefs.getString("lunch_original", "").contains("<수다날>")) {
            sudagreen.setImageResource(R.drawable.sudanalback);
            sudagreen.setVisibility(View.VISIBLE);
        } else if (prefs.getString("lunch_original", "").contains("<그린데이>")) {
            sudagreen.setImageResource(R.drawable.greendayback);
            sudagreen.setVisibility(View.VISIBLE);
        } else {
            sudagreen.setVisibility(View.GONE);
        }

        BreakfastTextView.setText(prefs.getString("breakfast", ""));
        LunchTextView.setText(prefs.getString("lunch", ""));
        DinnerTextView.setText(prefs.getString("dinner", ""));

        findViewById(R.id.breakfastcontainer).setVisibility(View.VISIBLE);
        findViewById(R.id.lunchcontainer).setVisibility(View.VISIBLE);
        findViewById(R.id.dinnercontainer).setVisibility(View.VISIBLE);

        if (prefs.getString("breakfast", "").length() < 15) {
            findViewById(R.id.breakfastcontainer).setVisibility(View.GONE);
        }
        if (prefs.getString("lunch", "").length() < 15) {
            findViewById(R.id.lunchcontainer).setVisibility(View.GONE);
        }
        if (prefs.getString("dinner", "").length() < 15) {
            findViewById(R.id.dinnercontainer).setVisibility(View.GONE);
        }

        Log.d("SetSikdan", "식단 업데이트 완료.");
    }

    private void setWeather() {
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
    }

    private boolean chkNetStat() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        return ((mobile != null && mobile.isConnected()) || (wifi != null && wifi.isConnected())
                || (wimax != null && wimax.isConnected()));
    }

    @Override
    public void onRefreshStarted(View view) {
        ManualUpdateAll();
    }

    class UpdateThread extends Thread {
        public void run() {
            Message msg = new Message();
            try {
                updateSikdan();
                updateAcademic();
                msg.what = 0;
            } catch (IOException e) {
                e.printStackTrace();
                msg.what = 1;
            }
            SikdanHandler.sendMessage(msg);
        }
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