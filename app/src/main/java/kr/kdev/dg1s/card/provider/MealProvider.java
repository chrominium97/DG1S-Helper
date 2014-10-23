package kr.kdev.dg1s.card.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.kdev.dg1s.R;

public class MealProvider {

    final static String TAG = "MealProvider";

    final static String domain = "http://hes.dge.go.kr/sts_sci_md00_001.do";
    final static String ARG_1 = "schulCode=";
    final static String VALUE_1 = "D100001936&";
    final static String ARG_2 = "insttNm="; // 대구일과학고등학교
    final static String VALUE_2 = "%EB%8C%80%EA%B5%AC%EC%9D%BC%EA%B3%BC%ED%95%99%EA%B3%A0%EB%93%B1%ED%95%99%EA%B5%90&"; // 대구일과학고등학교
    final static String ARG_3 = "schulCrseScCode=";
    final static String VALUE_3 = "4&";
    final static String ARG_4 = "schulKndScCode=";
    final static String VALUE_4 = "04&";
    final static String ARG_5 = "schYm=";
    final String VALUE_5;
    final static String ARG_6 = "searchButton=";
    final static String VALUE_6 = "";

    Context context;

    UpdateCenter center;

    public class Meal {

        int DATE;
        String BREAKFAST;
        String LUNCH;
        String DINNER;

        public Meal() {

        }

        public Meal(int id, String breakfast, String lunch, String dinner) {
            this.DATE = id;
            this.BREAKFAST = breakfast;
            this.LUNCH = lunch;
            this.DINNER = dinner;
        }

        public Meal(String breakfast, String lunch, String dinner) {
            this.BREAKFAST = breakfast;
            this.LUNCH = lunch;
            this.DINNER = dinner;
        }

        @Override
        public String toString() {
            return "DAY : " + String.valueOf(DATE) + "\n" +
                    "BREAKFAST : " + BREAKFAST.replaceAll("\n", " & ") + "\n" +
                    "LUNCH : " + LUNCH.replaceAll("\n", " & ") + "\n" +
                    "DINNER : " + DINNER.replaceAll("\n", " & ") + "\n";
        }

        // getting ID
        public int getDate() {
            return this.DATE;
        }

        // setting ID
        public void setDate(int id) {
            this.DATE = id;
        }

        public String getBreakfast() {
            return this.BREAKFAST;
        }

        public void setBreakfast(String content) {
            this.BREAKFAST = content;
        }

        public void appendBreakfast(String content) {
            if (BREAKFAST.equalsIgnoreCase(context.getString(R.string.no_meal))) {
                this.BREAKFAST = content;
            } else {
                this.BREAKFAST = BREAKFAST + "\n" + content;
            }
        }

        public String getLunch() {
            return this.LUNCH;
        }

        public void setLunch(String content) {
            this.LUNCH = content;
        }

        public void appendLunch(String content) {
            if (LUNCH.equalsIgnoreCase(context.getString(R.string.no_meal))) {
                this.LUNCH = content;
            } else {
                this.LUNCH = LUNCH + "\n" + content;
            }
        }

        public String getDinner() {
            return this.DINNER;
        }

        public void setDinner(String content) {
            this.DINNER = content;
        }

        public void appendDinner(String content) {
            if (DINNER.equalsIgnoreCase(context.getString(R.string.no_meal))) {
                this.DINNER = content;
            } else {
                this.DINNER = DINNER + "\n" + content;
            }
        }
    }

    public MealProvider(Context arg0) throws IOException, ArrayIndexOutOfBoundsException {
        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_MEAL, context);

        VALUE_5 = new SimpleDateFormat("yyyy.MM").format(new Date()) + "&";

        Log.d(TAG, "Checking if update is needed...");
        if (center.needsUpdate()) {
            Log.e(TAG, "MEAL UPDATE NEEDED");
            refreshMeals();
            center.updateTime();
        }
    }

    public void forceRefresh() throws IOException, ArrayIndexOutOfBoundsException {
        refreshMeals();
        center.updateTime();
    }

    public Meal getMeal() {
        MealDatabaseHandler handler = new MealDatabaseHandler(context);
        return handler.getMeal(Integer.parseInt(new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis()))));
    }

    // Method for parsing meal information
    void refreshMeals() throws IOException, ArrayIndexOutOfBoundsException {
        String URL = domain + "?" + ARG_1 + VALUE_1 + ARG_2 + VALUE_2 + ARG_3 + VALUE_3 + ARG_4 + VALUE_4 + ARG_5 + VALUE_5 + ARG_6 + VALUE_6;
        Log.e(TAG, "\nRequesting NEIS for MEAL UPDATES - URL is " + URL);
        Source source = new Source(new InputStreamReader(new URL(URL).openStream(), "UTF-8"));

        MealDatabaseHandler handler = new MealDatabaseHandler(context);

        Element table = source.getFirstElementByClass("tbl_type3");
        Log.v(TAG, table.toString());

        handler.deleteAll();

        List<Element> rows = table.getFirstElement("tbody").getAllElements("tr");
        for (Element row : rows) {
            for (Element mealInfo : row.getAllElements("td")) {

                int status = -1;
                Meal meal = new Meal(-1, context.getString(R.string.no_meal),
                        context.getString(R.string.no_meal), context.getString(R.string.no_meal));

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
                        text = text.replaceAll("[①-⑮]", "").replaceAll("\\(.*?\\)", "").replaceAll("[0-9]", "");
                        if (text.equalsIgnoreCase("[조식]")) {
                            status = 1;
                        } else if (text.equalsIgnoreCase("[중식]")) {
                            status = 2;
                        } else if (text.equalsIgnoreCase("[석식]")) {
                            status = 3;
                        } else {
                            switch (status) {
                                case 1:
                                    meal.appendBreakfast(text);
                                    break;
                                case 2:
                                    meal.appendLunch(text);
                                    break;
                                case 3:
                                    meal.appendDinner(text);
                                    break;
                            }
                        }
                    }
                }
                Log.d(TAG, "\nUPDATED MEAL\n" + meal.toString());
                handler.updateMeal(meal);
            }
        }
    }

    private class MealDatabaseHandler extends SQLiteOpenHelper {

        public static final int DB_VERSION = 1;
        private final String KEY_ID = "date";
        private final String KEY_BREAKFAST = "meal_breakfast";
        private final String KEY_LUNCH = "meal_lunch";
        private final String KEY_DINNER = "meal_dinner";
        private String TABLE_NAME = "mealTable"; // Table의 이름

        public MealDatabaseHandler(Context context) {
            super(context, "meal.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            String CREATE_MEAL_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BREAKFAST + " TEXT,"
                    + KEY_LUNCH + " TEXT," + KEY_DINNER + " TEXT" + ")";
            database.execSQL(CREATE_MEAL_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(database);
        }

        public Meal getMeal(int date) {
            SQLiteDatabase database = this.getReadableDatabase();

            Log.d("SQL", KEY_ID + " " + KEY_BREAKFAST + " " + KEY_LUNCH + " " + KEY_DINNER + "@" + date);

            Cursor cursor = database.query(TABLE_NAME, new String[]
                            {KEY_ID, KEY_BREAKFAST, KEY_LUNCH, KEY_DINNER}, KEY_ID + " = ?",
                    new String[]{String.valueOf(date)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            try {
                return new Meal(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1), cursor.getString(2), cursor.getString(3));
            } catch (NullPointerException e) {
                String noMeal = context.getString(R.string.no_meal);
                return new Meal(noMeal, noMeal, noMeal);
            }
        }

        public int updateMeal(Meal meal) {
            if (meal.getDate() == -1)
                return 0;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_BREAKFAST, meal.getBreakfast());
            values.put(KEY_LUNCH, meal.getLunch());
            values.put(KEY_DINNER, meal.getDinner());

            Log.d("SQL", "\nRecorded DAY " + String.valueOf(meal.getDate() + "\n") + meal);

            return database.update(TABLE_NAME, values, KEY_ID + "=" + String.valueOf(meal.getDate()),
                    null);
        }

        public void deleteAll() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(TABLE_NAME, null, null);

            String noMeal = context.getString(R.string.no_meal);

            for (int i = 1; i < 32; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_BREAKFAST, noMeal);
                values.put(KEY_LUNCH, noMeal);
                values.put(KEY_DINNER, noMeal);
                database.insert(TABLE_NAME, null, values);
            }
        }
    }


}
