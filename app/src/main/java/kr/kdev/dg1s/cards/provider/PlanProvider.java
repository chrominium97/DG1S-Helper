package kr.kdev.dg1s.cards.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
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

import kr.kdev.dg1s.cards.provider.datatypes.Plan;

public class PlanProvider {

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
    final static String ARG_6 = "mm=";
    private final static String TAG = "PlanProvider";
    final String VALUE_5;
    final String VALUE_6;
    Context context;
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            PlanDatabaseManager planDatabaseManager = new PlanDatabaseManager(context);
            planProviderInterface.onPlanReceived(message.what == 0,
                    planDatabaseManager.getPlans(), planDatabaseManager.getSummary(
                            Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("grade", "-1"))));
        }
    };
    UpdateCenter center;
    PlanProviderInterface planProviderInterface;

    public PlanProvider(Context arg0, Object origin) {
        try {
            planProviderInterface = (PlanProviderInterface) origin;
        } catch (ClassCastException e) {
            Log.e(TAG, "PlanProviderInterface not cast");
        }

        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_PLAN, context);
        VALUE_5 = new SimpleDateFormat("yyyy").format(new Date(System.currentTimeMillis())) + "&";
        VALUE_6 = new SimpleDateFormat("MM").format(new Date(System.currentTimeMillis()));

        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        query(false);
                    }
                }
        );
    }

    public void query(boolean forceUpdate) {
        Runnable refreshProcess = new RefreshThread(forceUpdate || center.needsUpdate());
        new Thread(refreshProcess).start();
    }

    public interface PlanProviderInterface {
        void onPlanReceived(boolean succeeded, Plan plan, ArrayList<Integer> summary);
    }

    class RefreshThread implements Runnable {

        boolean shouldRefresh = false;

        RefreshThread(boolean order) {
            this.shouldRefresh = order;
        }

        // Method for parsing meal information
        void refreshPlans() throws IOException, ArrayIndexOutOfBoundsException {
            String URL = domain + "?" + ARG_1 + VALUE_1 + ARG_2 + VALUE_2 + ARG_3 + VALUE_3 + ARG_4 + VALUE_4 + ARG_5 + VALUE_5 + ARG_6 + VALUE_6;
            Log.i(TAG, "Requesting NEIS for PLAN UPDATES\nURL : " + URL);
            Source source = new Source(new InputStreamReader(new URL(URL).openStream(), "UTF-8"));

            PlanDatabaseManager databaseManager = new PlanDatabaseManager(context);

            List<Element> table = source.getAllElementsByClass("tbl_type3");

            Element planTable = table.get(0);
            Element planSummaryTable = table.get(1);

            databaseManager.initializeDatabase();

            List<Element> plansByWeek = planTable.getFirstElement("tbody").getAllElements("tr");
            for (Element row : plansByWeek) {
                for (Element planInfo : row.getAllElements("td")) {

                    Plan plan = new Plan(-1, "");

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

                    databaseManager.updatePlans(plan);
                }
            }

            Log.d(TAG, "GENERATING SUMMARY");
            List<Element> planSummary = planSummaryTable.getFirstElement("tbody").getAllElements("tr");
            for (Element row : planSummary) {

                int grade = 0;
                String gradeText = row.getFirstElement("th").getContent().toString();
                if (gradeText.equals("1학년")) {
                    grade = 1;
                } else if (gradeText.equals("2학년")) {
                    grade = 2;
                } else if (gradeText.equals("3학년")) {
                    grade = 3;
                }

                List<Element> entries = row.getAllElements("td");
                int total = -1;
                int event = -1;
                int study = -1;
                try {
                    total = Integer.valueOf(entries.get(1).getContent().toString().replace("일", ""));
                    event = Integer.valueOf(entries.get(3).getContent().toString().replace("일", ""));
                    study = Integer.valueOf(entries.get(5).getContent().toString().replace("일", ""));
                } catch (NumberFormatException e) {
                    Log.e(TAG, row.toString());
                    throw new NumberFormatException();
                }
                databaseManager.updateSummary(grade, total, event, study);
            }

        }

        @Override
        public void run() {
            int succeeded = 0;
            if (shouldRefresh) {
                try {
                    refreshPlans();
                    center.updateTime();
                } catch (IOException e) {
                    succeeded = -1;
                }
            }
            handler.sendEmptyMessage(succeeded);
        }
    }

    private class PlanDatabaseManager extends SQLiteOpenHelper {

        public static final int DB_VERSION = 1;
        private static final String TAG = "PlanDatabaseManager";
        private static final String KEY_DATE = "date";
        private static final String KEY_PLANS = "plans";
        private static final String KEY_GRADE = "grade";
        private static final String KEY_DAYS_TOTAL = "totalDays";
        private static final String KEY_DAYS_STUDY = "studyingDays";
        private static final String KEY_DAYS_EVENT = "eventDays";
        private static final String PLAN_TABLE_NAME = "planTable";
        private static final String SUMMARY_TABLE_NAME = "summaryTable";

        public PlanDatabaseManager(Context context) {
            super(context, "plans.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            final String CREATE_PLAN_TABLE = "CREATE TABLE " + PLAN_TABLE_NAME + "(" +
                    KEY_DATE + " INTEGER PRIMARY KEY," + KEY_PLANS + " TEXT" + ")";
            final String CREATE_SUMMARY_TABLE = "CREATE TABLE " + SUMMARY_TABLE_NAME + "(" +
                    KEY_GRADE + " INTEGER PRIMARY KEY," + KEY_DAYS_TOTAL + " INTEGER," +
                    KEY_DAYS_EVENT + " INTEGER," + KEY_DAYS_STUDY + " INTEGER" + ")";
            Log.d(TAG, "Querying database w/ command " + CREATE_PLAN_TABLE);
            database.execSQL(CREATE_PLAN_TABLE);
            Log.d(TAG, "Querying database w/ command " + CREATE_SUMMARY_TABLE);
            database.execSQL(CREATE_SUMMARY_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            database.execSQL("DROP TABLE IF EXISTS " + PLAN_TABLE_NAME);
            database.execSQL("DROP TABLE IF EXISTS " + SUMMARY_TABLE_NAME);

            onCreate(database);
        }

        public Plan getPlans() {
            return getPlans(Integer.parseInt(new SimpleDateFormat("dd").format(new Date())));
        }

        public Plan getPlans(int date) {
            SQLiteDatabase database = this.getReadableDatabase();

            Cursor cursor = database.query(PLAN_TABLE_NAME, new String[]
                            {KEY_DATE, KEY_PLANS}, KEY_DATE + " = ?",
                    new String[]{String.valueOf(date)}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                try {
                    return new Plan(Integer.parseInt(cursor.getString(0)),
                            cursor.getString(1));
                } catch (NullPointerException e) {
                    return new Plan();
                }
            }
            return new Plan();
        }

        public int updatePlans(Plan plan) {
            if (plan.getDate() == -1)
                return 0;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_PLANS, plan.getPlans());

            Log.v(TAG, "Recorded plan at day " + plan.getDate());

            return database.update(PLAN_TABLE_NAME, values, KEY_DATE + "="
                    + String.valueOf(plan.getDate()), null);
        }

        public ArrayList<Integer> getSummary() {
            return getSummary(-1);
        }

        public ArrayList<Integer> getSummary(int grade) {
            if (grade == -1) {
                ArrayList<Integer> summary = new ArrayList<Integer>();
                for (int i = 0; i < 3; i++) {
                    summary.add(-1);
                }
                return summary;
            }

            SQLiteDatabase database = this.getReadableDatabase();

            Cursor cursor = database.query(SUMMARY_TABLE_NAME, new String[]
                            {KEY_DAYS_TOTAL, KEY_DAYS_EVENT, KEY_DAYS_STUDY}, KEY_GRADE + " = ?",
                    new String[]{String.valueOf(grade)}, null, null, null, null);
            if (cursor != null) {
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
                        summary.add(-1);
                    }
                    return summary;
                }
            }
            ArrayList<Integer> summary = new ArrayList<Integer>();
            for (int i = 0; i < 3; i++) {
                summary.add(-1);
            }
            return summary;
        }

        public int updateSummary(int grade, int total, int event, int study) {
            if (grade == 0)
                return grade;

            SQLiteDatabase database = this.getReadableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_DAYS_TOTAL, total);
            values.put(KEY_DAYS_EVENT, event);
            values.put(KEY_DAYS_STUDY, study);

            Log.d(TAG, "Recorded summary");

            return database.update(SUMMARY_TABLE_NAME, values, KEY_GRADE + "="
                    + String.valueOf(grade), null);
        }

        public void initializeDatabase() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(PLAN_TABLE_NAME, null, null);
            database.delete(SUMMARY_TABLE_NAME, null, null);

            for (int i = 1; i < 32; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_PLANS, "");
                database.insert(PLAN_TABLE_NAME, null, values);
            }

            for (int i = 1; i < 4; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_DAYS_TOTAL, -1);
                values.put(KEY_DAYS_EVENT, -1);
                values.put(KEY_DAYS_STUDY, -1);
                database.insert(SUMMARY_TABLE_NAME, null, values);
            }
        }
    }


}
