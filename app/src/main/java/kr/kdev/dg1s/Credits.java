package kr.kdev.dg1s;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Credits extends ActionBarActivity {

    private int i = 0;
    private boolean media_playing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credit);

        getSupportActionBar().setIcon(R.drawable.backbtn);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dark_gray)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.actionbar_credits);
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        ImageView Kdev = (ImageView) findViewById(R.id.kdev);
        Kdev.setOnTouchListener(new Button.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (v.getId()) {
                    case R.id.kdev:
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (!media_playing) i++;
                            if (i >= 5 && !media_playing) {
                                media_playing = true;
                                i = 0;
                                findViewById(R.id.creditback).setBackgroundColor(Color.rgb(255, 0, 0));
                                Toast.makeText(Credits.this, "보라타운이 돌아왔습니다 ㅎㅎ", Toast.LENGTH_SHORT).show();
                                Credits.this.getResources().openRawResource(R.raw.easter_egg);
                                MediaPlayer mp;
                                mp = MediaPlayer.create(Credits.this, R.raw.easter_egg);
                                mp.seekTo(0);
                                mp.start();
                                mp.setOnCompletionListener(new OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mp) {
                                        media_playing = false;
                                    }
                                });
                            }
                        }
                        break;
                }
                return false;
            }
        });

        LinearLayout Mailto = (LinearLayout) findViewById(R.id.mailto_container);
        Mailto.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + "chrominium97@gmail.com"));
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.appear_decelerate_ltr, R.anim.disappear_decelerate_ltr);
                return true;
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

}
