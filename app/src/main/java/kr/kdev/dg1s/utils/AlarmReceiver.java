package kr.kdev.dg1s.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Broadcast", "Received");
        Toast.makeText(context, "sdf", Toast.LENGTH_LONG).show();
        Intent outputIntent = new Intent(context, NotificationService.class);
        outputIntent.putExtra("time", intent.getStringExtra("time"));
        context.startService(outputIntent);
    }
}
