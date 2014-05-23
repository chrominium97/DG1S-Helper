package kr.kdev.dg1s;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class Activity_PostView extends ActionBarActivity {

    Parser_Homework homeworkParser;
    Adapter_Post adapter;
    ListView lv;
    private WebView wb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hwdnld);

        getSupportActionBar().setIcon(R.drawable.backbtn);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.homework)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.actionbar_homework);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        lv = (ListView) findViewById(R.id.lvs);
        wb = (WebView) findViewById(R.id.tpweb);

        homeworkParser = new Parser_Homework();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
                return true;
            case R.id.action_refresh:
                if (chkNetStat()) {
                    xmlRefresh();
                    new HWAsync().execute(null, null, null);
                } else {
                    Toast.makeText(getApplicationContext(), getText(R.string.error_network_long), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_homework_add:
                Intent i = new Intent(getApplicationContext(), Activity_PostAdd.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.appear_decelerate_rtl, R.anim.disappear_decelerate_rtl);
        }
        return false;
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

    private void xmlRefresh() {
        wb.loadUrl("http://junbread.woobi.co.kr/dbtoxml.php");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chkNetStat()) {
            xmlRefresh();
            new HWAsync().execute(null, null, null);
        } else {
            findViewById(R.id.lvs).setVisibility(View.GONE);
        }
    }

    private boolean chkNetStat() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo wimax = cm.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);

        if (wimax != null) {
            return (mobile.isConnected() || wifi.isConnected() || wimax.isConnected()) ? true : false;
        } else {
            return (mobile.isConnected() || wifi.isConnected()) ? true : false;
        }
    }

    public class HWAsync extends AsyncTask<String, String, ArrayList<Adapter_PostList>> {

        @Override
        protected ArrayList<Adapter_PostList> doInBackground(String... params) {
            return homeworkParser.parsePost();//파싱 정보를 ArrayList로 가지고 옵니다.
        }

        @Override
        protected void onPostExecute(ArrayList<Adapter_PostList> result) {
            adapter = new Adapter_Post(Activity_PostView.this, R.layout.postitem, result);
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            AnimationSet set = new AnimationSet(true);
            Animation rtl = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
            );
            rtl.setDuration(200);
            set.addAnimation(rtl);

            Animation alpha = new AlphaAnimation(0.0f, 1.0f);
            alpha.setDuration(200);
            set.addAnimation(alpha);

            LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
            lv.setLayoutAnimation(controller);
            Log.d("SetHW", "숙제 파싱 완료.");
        }
    }
}

