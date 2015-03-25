package kr.kdev.dg1s.cards.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateCenter {

    public final static String PACKAGE_NAME = "kr.kdev.dg1s";

    public final static int TYPE_SYSTEM_UPDATE_STAT = 0xF0;
    public static final int TYPE_SYSTEM_FIRST_RUN = 0xF1;

    public final static int TYPE_MEAL = 0x00;
    public final static int TYPE_PLAN = 0x01;
    public final static int TYPE_WEATHER = 0x02;

    private final static int INTERVAL_IRREGULAR_DATE = 0x10;
    private final static int INTERVAL_MONTHLY = 0x11;
    private final static int INTERVAL_DAILY = 0x12;
    private final static int INTERVAL_3HOURS = 0x13;
    private final static int INTERVAL_HOURLY = 0x14;
    private final static int INTERVAL_MANUAL = 0x1E;
    private final static int INTERVAL_ONCE = 0x1F;

    private final static String KEY_MEAL = "update_stat_meal";
    private final static String KEY_PLAN = "update_stat_plan";
    private final static String KEY_WEATHER = "update_stat_weather";

    private final static String KEY_SYSTEM_VERSION_CHECK = "installed_version";
    private final static String KEY_SYSTEM_FIRST_RUN = "first_run";
    private final SharedPreferences preferences;
    private final Context context;
    private String preferencesAccessKey;
    private int updateType;

    public UpdateCenter(int type, Context context) {
        changeAccessType(type);
        preferences = context.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        this.context = context;
    }

    public void changeAccessType(int type) {
        switch (type) {
            case TYPE_SYSTEM_UPDATE_STAT:
                preferencesAccessKey = KEY_SYSTEM_VERSION_CHECK;
                updateType = INTERVAL_MANUAL;
                break;
            case TYPE_SYSTEM_FIRST_RUN:
                preferencesAccessKey = KEY_SYSTEM_FIRST_RUN;
                updateType = INTERVAL_ONCE;
                break;
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
    }

    public boolean needsUpdate() {
        switch (updateType) {
            case INTERVAL_IRREGULAR_DATE:
                return preferences.getInt(preferencesAccessKey, 19700101) <
                        Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis())));
            case INTERVAL_MONTHLY:
                return preferences.getInt(preferencesAccessKey, 197001) <
                        Integer.parseInt(new SimpleDateFormat("yyyyMM").format(new Date(System.currentTimeMillis())));
            case INTERVAL_DAILY:
                // TODO 매서드 구축
            case INTERVAL_3HOURS:
                return System.currentTimeMillis() - preferences.getLong(preferencesAccessKey, 0) >= (3 * 60 * 60 * 1000);
            case INTERVAL_HOURLY:
                // TODO 매서드 구축
            case INTERVAL_ONCE:
                return preferences.getBoolean(preferencesAccessKey, true);
            case INTERVAL_MANUAL:
                try {
                    return preferences.getInt(preferencesAccessKey, 0) <
                            context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    return true;
                }
            default:
                return true;
        }
    }

    public void updateTime() {
        switch (updateType) {
            case INTERVAL_IRREGULAR_DATE:
                preferences.edit().putInt(preferencesAccessKey,
                        Integer.parseInt(new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis())))).apply();
            case INTERVAL_MONTHLY:
                preferences.edit().putInt(preferencesAccessKey,
                        Integer.parseInt(new SimpleDateFormat("yyyyMM").format(new Date(System.currentTimeMillis())))).apply();
                break;
            case INTERVAL_DAILY:
                // TODO 매서드 구축
            case INTERVAL_3HOURS:
                preferences.edit().putLong(preferencesAccessKey, System.currentTimeMillis()).apply();
                break;
            case INTERVAL_HOURLY:
                // TODO 매서드 구축
            case INTERVAL_ONCE:
                preferences.edit().putBoolean(preferencesAccessKey, false).apply();
                break;
            case INTERVAL_MANUAL:
                if (preferencesAccessKey.equals(KEY_SYSTEM_VERSION_CHECK)) {
                    try {
                        preferences.edit().putInt(preferencesAccessKey,
                                context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode).apply();
                    } catch (PackageManager.NameNotFoundException e) {
                        preferences.edit().putInt(preferencesAccessKey, 0).apply();
                    }
                    break;
                }
        }
    }
}
