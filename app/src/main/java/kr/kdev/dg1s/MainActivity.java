package kr.kdev.dg1s;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import kr.kdev.dg1s.cards.CardViewStatusNotifier;
import kr.kdev.dg1s.cards.MealCard;
import kr.kdev.dg1s.cards.PlanCard;
import kr.kdev.dg1s.cards.WeatherCard;
import kr.kdev.dg1s.cards.provider.UpdateCenter;
import kr.kdev.dg1s.services.BackgroundUpdateService;

public class MainActivity extends ActionBarActivity implements CardViewStatusNotifier {

    private static final String TAG = "MainActivity";

    private Context context;
    private MealCard mealCard;
    private PlanCard planCard;
    private WeatherCard weatherCard;

    private int queue = 0;
    private SwipeRefreshLayout pullToRefreshLayout;

    public void notifyCompletion(Object origin, int status) {
        final String localTAG = TAG + ".CardQueue";

        if (queue > 0) { queue--;}

        if (status == FAILURE) {
            final String cardName = origin.getClass().getName();
            Log.e(localTAG, "Error occurred while updating card(s)");
            Log.e(localTAG, "ORIGIN : " + cardName);
        }

        if (queue == 0) {
            pullToRefreshLayout.setRefreshing(false);
        }

        Log.i(localTAG, queue + " left in queue");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        checkFirstRun();

        BackgroundUpdateService service = new BackgroundUpdateService();
        service.setAlarm(context);

        setLayouts();

        Log.d(TAG, "Initialized");

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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, Settings.class));
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.still);
                break;
            case R.id.action_credits:
                startActivity(new Intent(MainActivity.this, Credits.class));
                overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.still);
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setIsDismissed() {

    }

    private void startManualUpdate() {
        if (hasInternetConnection()) {
            queue = 3;
            weatherCard.requestUpdate(true);
            mealCard.requestUpdate(true);
            planCard.requestUpdate(true);
        } else {
            pullToRefreshLayout.setRefreshing(false);
        }

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
            center.changeAccessType(UpdateCenter.TYPE_SYSTEM_UPDATE_STAT);
            center.updateTime();
            return;
        }

        center.changeAccessType(UpdateCenter.TYPE_SYSTEM_UPDATE_STAT);
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