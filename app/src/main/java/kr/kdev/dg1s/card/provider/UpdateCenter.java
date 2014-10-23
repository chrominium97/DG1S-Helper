package kr.kdev.dg1s.card.provider;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateCenter {

    public final static String PACKAGE_NAME = "kr.kdev.dg1s";

    public final static int TYPE_MEAL = 0;
    public final static int TYPE_PLAN = 1;
    public final static int TYPE_WEATHER = 2;

    private final static int INTERVAL_MONTHLY = 10;
    private final static int INTERVAL_DAILY = 11;
    private final static int INTERVAL_3HOURS = 12;
    private final static int INTERVAL_HOURLY = 13;

    private final static String KEY_MEAL = "update_stat_meal";
    private final static String KEY_PLAN = "update_stat_plan";
    private final static String KEY_WEATHER = "update_stat_weather";

    private String preferencesAccessKey;

    private int updateType;

    private SharedPreferences preferences;

    public UpdateCenter(int type, Context context) {
        switch (type) {
            case TYPE_MEAL:
                preferencesAccessKey = KEY_MEAL;
                updateType = INTERVAL_MONTHLY;
                break;
            case TYPE_PLAN:
                preferencesAccessKey = KEY_PLAN;
                updateType = INTERVAL_MONTHLY;
                break;
            case TYPE_WEATHER:
                preferencesAccessKey = KEY_WEATHER;
                updateType = INTERVAL_3HOURS;
        }
        preferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
    }

    public boolean needsUpdate() {
        switch (updateType) {
            case INTERVAL_MONTHLY:
                return preferences.getInt(preferencesAccessKey, 197001) <
                        Integer.parseInt(new SimpleDateFormat("yyyyMM").format(new Date(System.currentTimeMillis())));
            case INTERVAL_3HOURS:
                return true;
            default:
                return true;
        }
    }

    public void updateTime() {
        switch (updateType) {
            case INTERVAL_MONTHLY:
                preferences.edit().putInt(preferencesAccessKey,
                        Integer.parseInt(new SimpleDateFormat("yyyyMM").format(new Date(System.currentTimeMillis())))).commit();
                break;
        }
    }
}
