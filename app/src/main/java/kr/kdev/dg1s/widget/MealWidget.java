package kr.kdev.dg1s.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import kr.kdev.dg1s.MainActivity;
import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.MealProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Meal;

public class MealWidget extends AppWidgetProvider implements MealProvider.MealProviderInterface {

    MealProvider mealProvider;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initializeLayout(context, appWidgetManager, appWidgetIds);

        //mealProvider.query(false);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onMealReceived(boolean succeeded, Meal meal) {

    }

    private void initializeLayout(Context context, AppWidgetManager appWidgetManager, int[] appIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.sikdanwidget);
        PendingIntent intent = PendingIntent.getActivity(
                context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.contents, intent);

        appWidgetManager.updateAppWidget(appIds, remoteViews);
    }

}
