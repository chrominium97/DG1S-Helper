package kr.kdev.dg1s;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class Settings extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                overridePendingTransition(R.anim.still, R.anim.disappear_decelerate_ttb);
        }
        return super.onKeyDown(keyCode, event);
    }
}
