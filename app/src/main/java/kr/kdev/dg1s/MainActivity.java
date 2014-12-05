package kr.kdev.dg1s;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.Calendar;

import kr.kdev.dg1s.cards.CardViewStatusNotifier;
import kr.kdev.dg1s.cards.MealCard;
import kr.kdev.dg1s.cards.PlanCard;
import kr.kdev.dg1s.cards.WeatherCard;
import kr.kdev.dg1s.cards.provider.UpdateCenter;
import kr.kdev.dg1s.services.BackgroundUpdateService;
import kr.kdev.dg1s.utils.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends ActionBarActivity implements CardViewStatusNotifier {

    private Context context;
    private MealCard mealCard;
    private PlanCard planCard;
    private WeatherCard weatherCard;

    private int queue = 0;
    private SwipeRefreshLayout pullToRefreshLayout;

    public void notifyCompletion(Object origin, int status) {
        if (queue > 0) {
            queue--;
        }

        if (status == FAILURE) {
            Log.e("MainActivity_CardQueue", "Error occurred while updating card(s)");
            try {
                MealCard card = (MealCard) origin;
                Log.e("MainActivity_CardQueue", "Error originated from MealCard");
            } catch (ClassCastException e) {
                try {
                    PlanCard card = (PlanCard) origin;
                    Log.e("MainActivity_CardQueue", "Error originated from PlanCard");
                } catch (ClassCastException f) {
                    try {
                        WeatherCard card = (WeatherCard) origin;
                        Log.e("MainActivity_CardQueue", "Error originated from WeatherCard");
                    } catch (ClassCastException g) {
                        Log.e("MainActivity_CardQueue", "Error originated from unknown source");
                    }
                }
            }
        } else {
            try {
                MealCard card = (MealCard) origin;
                //if (card.isDismissalDay()) {
                // TODO 퇴사
                //}
            } catch (ClassCastException e) {

            }
        }

        if (queue == 0) {
            pullToRefreshLayout.setRefreshing(false);
        }

        Log.i("MainActivity_CardQueue", queue + " left in queue");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        checkFirstRun();

        BackgroundUpdateService service = new BackgroundUpdateService();
        service.setAlarm(context);

        setLayouts();

        //ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(R.color.dark_gray));
        //getSupportActionBar().setBackgroundDrawable(colorDrawable);

    }

    void setLayouts() {
        setContentView(R.layout.main);

        pullToRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.infoScrollView);
        pullToRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        pullToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startManualUpdate();
            }
        });

        ViewGroup group = (ViewGroup) findViewById(R.id.container);
        queue = queue + 3;
        weatherCard = new WeatherCard(context, group, MainActivity.this);
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
                String mealBackupDBPath = "meals.db";

                String planDBPath = "/data/" + "kr.kdev.dg1s" + "/databases/" + "plans.db";
                String planBackupDBPath = "plans.db";

                String weatherDBPath = "/data/" + "kr.kdev.dg1s" + "/databases/" + "weather.db";
                String weatherBackupDBPath = "weather.db";

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
                    destination.close();

                    currentDB = new File(internalDirectory, weatherDBPath);
                    backupDB = new File(externalDirectory, weatherBackupDBPath);
                    source = new FileInputStream(currentDB).getChannel();
                    destination = new FileOutputStream(backupDB).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    source.close();
                    destination.close();

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
    }

    private void setIsDismissed() {
        // TODO 퇴사판별 알고리즘 만들기

    }

    private void startManualUpdate() {
        if (hasInternetConnection()) {
            queue = 3;
            weatherCard.requestUpdate(true);
            mealCard.requestUpdate(true);
            planCard.requestUpdate(true);
            Log.i("ManualUpdate", "강제 업데이트 성공.");
        } else {
            pullToRefreshLayout.setRefreshing(false);
            Log.i("ManualUpdate", "네크워크 접근 불가");
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

    private void checkFirstRun() {
        UpdateCenter center = new UpdateCenter(UpdateCenter.TYPE_SYSTEM_FIRST_RUN, context);

        if (center.needsUpdate()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setMessage(getResources().getString(R.string.welcome));
            alert.show();
            center.updateTime();
            center.setAccessMode(UpdateCenter.TYPE_SYSTEM_UPDATE_STAT);
            center.updateTime();
            return;
        }

        center.setAccessMode(UpdateCenter.TYPE_SYSTEM_UPDATE_STAT);
        if (center.needsUpdate()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setMessage(getString(R.string.update_info) + "\n" + getString(R.string.update_description));
            alert.show();
            center.updateTime();
        }
    }

    private boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wiMax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        return ((mobile != null && mobile.isConnected()) || (wifi != null && wifi.isConnected())
                || (wiMax != null && wiMax.isConnected()));
    }
}