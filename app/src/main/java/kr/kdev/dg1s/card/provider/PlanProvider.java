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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.kdev.dg1s.R;

public class PlanProvider {

    final static String TAG = "PlanProvider";

    final static String domain = "http://hes.dge.go.kr/sts_sci_sf01_001.do";
    final static String ARG_1 = "schulCode=";
    final static String VALUE_1 = "D100001936&";
    final static String ARG_2 = "insttNm="; // 대구일과학고등학교
    final static String VALUE_2 = "%EB%8C%80%EA%B5%AC%EC%9D%BC%EA%B3%BC%ED%95%99%EA%B3%A0%EB%93%B1%ED%95%99%EA%B5%90&"; // 대구일과학고등학교
    final static String ARG_3 = "schulCrseScCode=";
    final static String VALUE_3 = "4&";
    final static String ARG_4 = "schulKndScCode=";
    final static String VALUE_4 = "04&";
    final static String ARG_5 = "ay=";
    final String VALUE_5;
    final static String ARG_6 = "mm=";
    final String VALUE_6;

    Context context;

    UpdateCenter center;

    public class Plan {

        int DATE;
        String CONTENT;

        public Plan() {

        }

        public Plan(int id, String breakfast) {
            this.DATE = id;
            this.CONTENT = breakfast;
        }

        public Plan(String breakfast) {
            this.CONTENT = breakfast;
        }

        @Override
        public String toString() {
            return "DAY : " + String.valueOf(DATE) + "\n" +
                    "CONTENT : " + CONTENT.replaceAll("\n", " & ") + "\n";
        }

        // getting ID
        public int getDate() {
            return this.DATE;
        }

        // setting ID
        public void setDate(int id) {
            this.DATE = id;
        }

        public String getPlans() {
            return this.CONTENT;
        }

        public void setPlans(String content) {
            this.CONTENT = content;
        }

        public void appendPlans(String content) {
            if (CONTENT.equalsIgnoreCase(context.getString(R.string.no_meal))) {
                this.CONTENT = content;
            } else {
                this.CONTENT = CONTENT + "\n" + content;
            }
        }
    }

    public PlanProvider(Context arg0) throws IOException, ArrayIndexOutOfBoundsException {
        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_MEAL, context);

        VALUE_5 = new SimpleDateFormat("yyyy").format(new Date(System.currentTimeMillis())) + "&";
        VALUE_6 = new SimpleDateFormat("MM").format(new Date(System.currentTimeMillis()));

        Log.d(TAG, "Checking if update is needed...");
        if (center.needsUpdate()) {
            Log.e(TAG, "PLAN UPDATE NEEDED");
            refreshPlans();
            center.updateTime();
        }
    }

    public void forceRefresh() throws IOException, ArrayIndexOutOfBoundsException {
        refreshPlans();
        center.updateTime();
    }

    public Plan getPlan() {
        PlanDatabaseHandler handler = new PlanDatabaseHandler(context);
        return handler.getPlan(Integer.parseInt(new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis()))));
    }

    public ArrayList<Integer> getSummary(int grade) {
        PlanDatabaseHandler handler = new PlanDatabaseHandler(context);
        return handler.getSummary(grade);
    }

    // Method for parsing meal information
    void refreshPlans() throws IOException, ArrayIndexOutOfBoundsException {
        String URL = domain + "?" + ARG_1 + VALUE_1 + ARG_2 + VALUE_2 + ARG_3 + VALUE_3 + ARG_4 + VALUE_4 + ARG_5 + VALUE_5 + ARG_6 + VALUE_6;
        Log.e(TAG, "\nRequesting NEIS for PLAN UPDATES - URL is " + URL);
        Source source = new Source(new InputStreamReader(new URL(URL).openStream(), "UTF-8"));

        PlanDatabaseHandler handler = new PlanDatabaseHandler(context);

        List<Element> table = source.getAllElementsByClass("tbl_type3");
        Log.v(TAG, table.toString());

        Element planTable = table.get(0);

        Element planSummaryTable = table.get(1);

        handler.initializeDatabase();

        List<Element> plansByWeek = planTable.getFirstElement("tbody").getAllElements("tr");
        for (Element row : plansByWeek) {
            for (Element planInfo : row.getAllElements("td")) {

                Plan plan = new Plan(-1, context.getString(R.string.error_no_schedule));

                List<Element> plansByDate = planInfo.getFirstElement("div").getChildElements();
                for (Element element : plansByDate) {
                    if (element.getName().equals("em")) {
                        try {
                            plan.setDate(Integer.parseInt(element.getContent().toString()));
                        } catch (NumberFormatException e) {
                            plan.setDate(-1);
                        }
                    } else if (element.getName().equals("a")) {
                        plan.setPlans(element.getFirstElement("strong").getContent().toString());
                    }
                }

                Log.d(TAG, "\nPLAN\n" + plan.toString());
                handler.updatePlans(plan);
            }
        }

        List<Element> planSummary = planSummaryTable.getFirstElement("tbody").getAllElements("tr");
        for (Element row : planSummary) {
            List<Element> entries = row.getAllElements();
            int grade = 0;
            String gradeText = entries.get(0).getContent().toString();
            if (gradeText.equals("1학년")) {
                grade = 1;
            } else if (gradeText.equals("2학년")) {
                grade = 2;
            } else if (gradeText.equals("3학년")) {
                grade = 3;
            }
            int total = Integer.valueOf(entries.get(2).getContent().toString().replace("일", ""));
            int event = Integer.valueOf(entries.get(4).getContent().toString().replace("일", ""));
            int study = Integer.valueOf(entries.get(6).getContent().toString().replace("일", ""));
            handler.updateSummary(grade, total, event, study);
        }

    }

    private class PlanDatabaseHandler extends SQLiteOpenHelper {

        public static final int DB_VERSION = 1;
        private static final String KEY_DATE = "date";
        private static final String KEY_PLANS = "plans";

        private static final String KEY_GRADE = "grade";
        private static final String KEY_DAYS_TOTAL = "totalDays";
        private static final String KEY_DAYS_STUDY = "studyingDays";
        private static final String KEY_DAYS_EVENT = "eventDays";
        private static final String PLAN_TABLE_NAME = "planTable"; // Table의 이름
        private static final String SUMMARY_TABLE_NAME = "summaryTable"; // Table의 이름

        public PlanDatabaseHandler(Context context) {
            super(context, "plans.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            final String CREATE_PLAN_TABLE = "CREATE TABLE " + PLAN_TABLE_NAME + "(" +
                    KEY_DATE + " INTEGER PRIMARY KEY," + KEY_PLANS + " TEXT" + ")";
            final String CREATE_SUMMARY_TABLE = "CREATE TABLE " + SUMMARY_TABLE_NAME + "(" +
                    KEY_GRADE + " INTEGER PRIMARY KEY," + KEY_DAYS_TOTAL + " INTEGER," +
                    KEY_DAYS_EVENT + " INTEGER," + KEY_DAYS_STUDY + " INTEGER" + ")";
            database.execSQL(CREATE_PLAN_TABLE);
            database.execSQL(CREATE_SUMMARY_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL("DROP TABLE IF EXISTS " + PLAN_TABLE_NAME);
            database.execSQL("DROP TABLE IF EXISTS " + SUMMARY_TABLE_NAME);
            onCreate(database);
        }

        public Plan getPlan(int date) {
            SQLiteDatabase database = this.getReadableDatabase();

            Cursor cursor = database.query(PLAN_TABLE_NAME, new String[]
                            {KEY_DATE, KEY_PLANS}, KEY_DATE + " = ?",
                    new String[]{String.valueOf(date)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            try {
                return new Plan(Integer.parseInt(cursor.getString(0)),
                        cursor.getString(1));
            } catch (NullPointerException e) {
                String noPlan = context.getString(R.string.error_no_schedule);
                return new Plan(noPlan);
            }
        }

        public int updateSummary(int grade, int total, int event, int study) {
            if (grade == 0)
                return grade;

            SQLiteDatabase database = this.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_DAYS_TOTAL, total);
            values.put(KEY_DAYS_EVENT, event);
            values.put(KEY_DAYS_STUDY, study);

            return database.update(SUMMARY_TABLE_NAME, values, KEY_GRADE + "="
                    + String.valueOf(grade), null);
        }

        public ArrayList<Integer> getSummary(int grade) {
            SQLiteDatabase database = this.getReadableDatabase();

            Cursor cursor = database.query(SUMMARY_TABLE_NAME, new String[]
                            {KEY_DAYS_TOTAL, KEY_DAYS_EVENT, KEY_DAYS_STUDY}, KEY_GRADE + " = ?",
                    new String[]{String.valueOf(grade)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            try {
                ArrayList<Integer> summary = new ArrayList<Integer>();
                summary.add(cursor.getInt(0));
                summary.add(cursor.getInt(1));
                summary.add(cursor.getInt(2));
                return summary;
            } catch (NullPointerException e) {
                ArrayList<Integer> summary = new ArrayList<Integer>();
                for (int i = 0; i < 3; i++) {
                    summary.add(0);
                }
                return summary;
            }
        }

        public int updatePlans(Plan meal) {
            if (meal.getDate() == -1)
                return 0;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_PLANS, meal.getPlans());

            Log.d("SQL", "\nRecorded DAY " + String.valueOf(meal.getDate() + "\n") + meal);

            return database.update(PLAN_TABLE_NAME, values, KEY_DATE + "="
                    + String.valueOf(meal.getDate()), null);
        }

        public void initializeDatabase() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(PLAN_TABLE_NAME, null, null);
            database.delete(SUMMARY_TABLE_NAME, null, null);

            String noMeal = context.getString(R.string.error_no_schedule);

            for (int i = 1; i < 32; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_PLANS, noMeal);
                database.insert(PLAN_TABLE_NAME, null, values);
            }

            for (int i = 1; i < 4; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_DAYS_TOTAL, 0);
                values.put(KEY_DAYS_EVENT, 0);
                values.put(KEY_DAYS_STUDY, 0);
                database.insert(SUMMARY_TABLE_NAME, null, values);
            }
        }
    }


}
