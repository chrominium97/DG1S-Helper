package kr.kdev.dg1s;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.DatePicker;

import java.util.Calendar;

public class MealPlanner extends ActionBarActivity {

    static final int DATE_DIALOG_ID = 0;
    private int mYear;
    private int mMonth;
    private int mDay;
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                    setWebView();
                }
            };
    private WebView sdweb;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sikdan);

        getSupportActionBar().setIcon(R.drawable.backbtn);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.meal_planner)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.actionbar_meal);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        sdweb = (WebView) findViewById(R.id.SikdanWeb);
        sdweb.getSettings().setJavaScriptEnabled(true);
        sdweb.loadUrl("http://old.dg1s.hs.kr/general/food/skin3.html?print=ok");
        sdweb.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                findViewById(R.id.SikdanWeb).setVisibility(View.GONE);
            }

        });

        final Calendar c = Calendar.getInstance();

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        updateDisplay();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setWebView() {
        int Month = mMonth + 1;
        String url = "http://old.dg1s.hs.kr/general/food/skin3.html?print=ok&TodayDate=" + mYear + "-" + Month + "-" + mDay;
        sdweb.loadUrl(url);
    }

    private void updateDisplay() {
        String Month1 = String.format("%02d", mMonth + 1);
        String Day1 = String.format("%02d", mDay);

        getSupportActionBar().setTitle((new StringBuilder()
                .append(mYear).append("-")
                .append(Month1).append("-").append(Day1)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
                return true;
            case R.id.action_calendar:
                showDialog(DATE_DIALOG_ID);

        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meal, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }
}    

