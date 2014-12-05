package kr.kdev.dg1s.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import kr.kdev.dg1s.MainActivity;
import kr.kdev.dg1s.R;

public class MealWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateSikdan(context, appWidgetManager, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    private void updateSikdan(Context context, AppWidgetManager awm, int[] appids) {
        SharedPreferences prefs;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.sikdanwidget);

        prefs = context.getSharedPreferences("kr.kdev.dg1s", Context.MODE_PRIVATE);

        remoteViews.setTextViewText(R.id.w_breakfasttv, prefs.getString("breakfast", ""));
        remoteViews.setTextViewText(R.id.w_lunchtv, prefs.getString("lunch", ""));
        remoteViews.setTextViewText(R.id.w_dinnertv, prefs.getString("dinner", ""));

        if (prefs.getString("breakfast", "") == "")
            remoteViews.setViewVisibility(R.id.w_breakfastcontainer, View.GONE);
        if (prefs.getString("lunch", "") == "")
            remoteViews.setViewVisibility(R.id.w_lunchcontainer, View.GONE);
        if (prefs.getString("dinner", "") == "")
            remoteViews.setViewVisibility(R.id.w_dinnercontainer, View.GONE);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.widget_container, pi);

        awm.updateAppWidget(appids, remoteViews);
    }
}
