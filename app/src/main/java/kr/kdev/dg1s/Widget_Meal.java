package kr.kdev.dg1s;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import net.htmlparser.jericho.Source;

import java.io.IOException;

public class Widget_Meal extends AppWidgetProvider {

    Parser_Meal MealParser = new Parser_Meal();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        try {
            MealParser.parseMeal(context);
            updateSikdan(context, appWidgetManager, appWidgetIds);
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    private void updateSikdan(Context context, AppWidgetManager awm, int[] appids) throws IOException {
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

        Intent intent = new Intent(context, Activity_Main.class);
        PendingIntent pi = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.widget_container, pi);

        awm.updateAppWidget(appids, remoteViews);
    }
}
