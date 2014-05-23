package kr.kdev.dg1s;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class Activity_PostAdd extends ActionBarActivity {

    String sbjt, desc;
    int[] due = new int[3];
    long grde;
    long clss;
    Spinner gdspn, csspn;

    @Override
    public void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.hwupld);

        getSupportActionBar().setIcon(R.drawable.backbtn);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.homework)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.actionbar_homework);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        SharedPreferences pd = PreferenceManager.getDefaultSharedPreferences(this);
        gdspn = (Spinner) findViewById(R.id.gdspinner);
        csspn = (Spinner) findViewById(R.id.csspinner);
        if (!(pd.getString("gradechk", "").length() <= 0 || pd.getString("classchk", "").length() <= 0)) {
            int gdefault = Integer.parseInt(pd.getString("gradechk", ""));
            int cdefault = Integer.parseInt(pd.getString("classchk", ""));
            gdspn.setSelection(gdefault - 1);
            csspn.setSelection(cdefault - 1);
        }
        gdspn.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                long str = parent.getItemIdAtPosition(position);
                grde = str + 1;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        csspn.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                long str = parent.getItemIdAtPosition(position);
                clss = str + 1;
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
                return true;
            case R.id.action_homework_add_confirm:
                if (!((EditText) (findViewById(R.id.sbjttext))).getText().toString().equals("") && !((EditText) (findViewById(R.id.desctext))).getText().toString().equals("")) {
                    sbjt = ((EditText) (findViewById(R.id.sbjttext))).getText().toString();
                    desc = ((EditText) (findViewById(R.id.desctext))).getText().toString();
                    due[0] = ((DatePicker) (findViewById(R.id.duedate))).getDayOfMonth();
                    due[1] = ((DatePicker) (findViewById(R.id.duedate))).getMonth() + 1;
                    due[2] = ((DatePicker) (findViewById(R.id.duedate))).getYear();
                    PostData();
                    Intent i = new Intent(Activity_PostAdd.this, Activity_PostView.class);
                    startActivity(i);
                    finish();
                    overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
                } else {
                    Toast toast = Toast.makeText(Activity_PostAdd.this, "내용을 입력해주세요!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent i = new Intent(Activity_PostAdd.this, Activity_PostView.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void PostData() {
        WebView web = (WebView) findViewById(R.id.webviewjongbum);
        web.loadUrl("http://junbread.woobi.co.kr/hwinsert.php?grade=" + grde + "&clss=" + clss + "&dscp=" + desc + "&subject=" + sbjt + "&due=" + due[2] + "-" + due[1] + "-" + due[0]);
    }

}
