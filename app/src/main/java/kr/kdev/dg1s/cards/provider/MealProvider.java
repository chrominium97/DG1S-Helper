package kr.kdev.dg1s.cards.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.kdev.dg1s.cards.provider.datatypes.Meal;

public class MealProvider {

    final static String TAG = "MealProvider";

    private final static String domain = "http://hes.dge.go.kr/sts_sci_md00_001.do";
    private final static String ARG_1 = "schulCode=";
    private final static String VALUE_1 = "D100001936&";
    private final static String ARG_2 = "insttNm="; // 대구일과학고등학교
    private final static String VALUE_2 = "%EB%8C%80%EA%B5%AC%EC%9D%BC%EA%B3%BC%ED%95%99%EA%B3%A0%EB%93%B1%ED%95%99%EA%B5%90&"; // 대구일과학고등학교
    private final static String ARG_3 = "schulCrseScCode=";
    private final static String VALUE_3 = "4&";
    private final static String ARG_4 = "schulKndScCode=";
    private final static String VALUE_4 = "04&";
    private final static String ARG_5 = "schYm=";
    private final static String ARG_6 = "searchButton=";
    private final static String VALUE_6 = "";
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            mealTransferInterface.onMealReceived(message.what == 0,
                    new MealDatabaseManager(context).getMeal());
        }
    };
    private final String VALUE_5;
    Context context;
    UpdateCenter center;
    MealProviderInterface mealTransferInterface;

    public MealProvider(Context arg0, Object origin) {
        try {
            mealTransferInterface = (MealProviderInterface) origin;
        } catch (ClassCastException e) {
            Log.e(TAG, "MealProviderInterface not cast");
        }

        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_MEAL, context);
        VALUE_5 = new SimpleDateFormat("yyyy.MM").format(new Date(System.currentTimeMillis())) + "&";
    }

    public void requestMeal(boolean forceUpdate) {
        Runnable refreshProcess = new RefreshProcess(forceUpdate || center.needsUpdate());
        new Thread(refreshProcess).start();
    }

    public interface MealProviderInterface {
        void onMealReceived(boolean succeeded, Meal meal);
    }

    class RefreshProcess implements Runnable {

        boolean shouldRefresh = false;

        RefreshProcess(boolean isOrder) {
            this.shouldRefresh = isOrder;
        }

        // Method for parsing meal information
        void refreshMeals() throws IOException, ArrayIndexOutOfBoundsException {
            String URL = domain + "?" + ARG_1 + VALUE_1 + ARG_2 + VALUE_2 + ARG_3 + VALUE_3 + ARG_4 + VALUE_4 + ARG_5 + VALUE_5 + ARG_6 + VALUE_6;
            Log.i(TAG, "Requesting NEIS for MEAL UPDATES\nURL : " + URL);
            Source source = new Source(new InputStreamReader(new URL(URL).openStream(), "UTF-8"));

            MealDatabaseManager databaseManager = new MealDatabaseManager(context);
            databaseManager.initializeDatabase();

            Element table = source.getFirstElementByClass("tbl_type3");

            List<Element> rows = table.getFirstElement("tbody").getAllElements("tr");
            for (Element row : rows) {
                for (Element mealInfo : row.getAllElements("td")) {

                    int status = -1;
                    Meal meal = new Meal(-1, new ArrayList<String>(),
                            new ArrayList<String>(), new ArrayList<String>());

                    for (String text : mealInfo.getFirstElement("div").getContent().toString().split("<br />")) {

                        if (status == -1) { // Date recording
                            try {
                                meal.setDate(Integer.parseInt(text));
                                status = 0;
                            } catch (NumberFormatException e) {
                                meal.setDate(-1);
                                status = -2;
                            }
                        } else if (status == -2) {
                            break;
                        } else {
                            text = text.replaceAll("[①-⑮]", "\0").replaceAll("\\(.*?\\)", "").replaceAll("[0-9]", "");
                            if (text.equalsIgnoreCase("[조식]")) {
                                status = 1;
                            } else if (text.equalsIgnoreCase("[중식]")) {
                                status = 2;
                            } else if (text.equalsIgnoreCase("[석식]")) {
                                status = 3;
                            } else {
                                switch (status) {
                                    case 1:
                                        meal.addBreakfast(text);
                                        break;
                                    case 2:
                                        meal.addLunch(text);
                                        break;
                                    case 3:
                                        meal.addDinner(text);
                                        break;
                                }
                            }
                        }
                    }
                    databaseManager.updateMeal(meal);
                }
            }
        }

        @Override
        public void run() {
            int succeeded = 0;
            if (shouldRefresh) {
                try {
                    refreshMeals();
                    center.updateTime();
                } catch (IOException e) {
                    succeeded = -1;
                }
            }
            handler.sendEmptyMessage(succeeded);
        }
    }

    private class MealDatabaseManager extends SQLiteOpenHelper {

        public static final int DB_VERSION = 2;
        final String TAG = "MealDatabaseManager";
        private final String KEY_ID = "date";
        private final String KEY_BREAKFAST = "meal_breakfast";
        private final String KEY_LUNCH = "meal_lunch";
        private final String KEY_DINNER = "meal_dinner";
        private final String TABLE_NAME = "mealTable";

        public MealDatabaseManager(Context context) {
            super(context, "meals.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            String CREATE_MEAL_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BREAKFAST + " TEXT,"
                    + KEY_LUNCH + " TEXT," + KEY_DINNER + " TEXT" + ")";
            Log.d(TAG + "@SQL", "Querying database w/ command " + CREATE_MEAL_TABLE);
            database.execSQL(CREATE_MEAL_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(database);
            requestMeal(true);
        }

        public Meal getMeal() {
            return getMeal(Integer.parseInt(new SimpleDateFormat("dd").format(new Date())));
        }

        public Meal getMeal(int date) {
            SQLiteDatabase database = this.getReadableDatabase();
            Log.d(TAG, "Retrieved meal at day " + date);
            Cursor cursor = database.query(TABLE_NAME, new String[]
                            {KEY_ID, KEY_BREAKFAST, KEY_LUNCH, KEY_DINNER}, KEY_ID + " = ?",
                    new String[]{String.valueOf(date)}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();

                try {
                    return new Meal(cursor.getInt(0),
                            cursor.getString(1), cursor.getString(2), cursor.getString(3));
                } catch (NullPointerException e) {
                    return new Meal();
                } catch (CursorIndexOutOfBoundsException e) {
                    onUpgrade(database, 0, 0);
                    return new Meal();
                }
            }
            return new Meal();
        }

        public void updateMeal(Meal meal) {
            if (meal.getDate() == -1)
                return;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_BREAKFAST, meal.getBreakfast().toString());
            values.put(KEY_LUNCH, meal.getLunch().toString());
            values.put(KEY_DINNER, meal.getDinner().toString());

            Log.v(TAG + "@SQL", "Recorded meal at day " + meal.getDate());

            database.update(TABLE_NAME, values, KEY_ID + "=" + String.valueOf(meal.getDate()), null);
        }

        public void initializeDatabase() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(TABLE_NAME, null, null);

            for (int i = 1; i < 32; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_BREAKFAST, "");
                values.put(KEY_LUNCH, "");
                values.put(KEY_DINNER, "");
                database.insert(TABLE_NAME, null, values);
            }
        }
    }

}