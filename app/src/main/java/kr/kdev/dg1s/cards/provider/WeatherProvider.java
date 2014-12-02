package kr.kdev.dg1s.cards.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

import kr.kdev.dg1s.cards.provider.datatypes.Weather;

public class WeatherProvider {

    final static String TAG = "WeatherProvider";

    final static String URL = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=2714074500";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            weatherProviderInterface.onWeatherReceived(message.what == 0,
                    new WeatherDatabaseManager(context).getWeather());
        }
    };
    Context context;
    UpdateCenter center;
    WeatherProviderInterface weatherProviderInterface;

    public WeatherProvider(Context arg0, Object origin) {
        try {
            weatherProviderInterface = (WeatherProviderInterface) origin;
        } catch (ClassCastException e) {
            //TODO 설명 추가하기
            throw new ClassCastException();
        }

        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_WEATHER, context);
    }

    public void requestWeather(boolean forceUpdate) {
        Runnable refreshProcess = new RefreshProcess(forceUpdate || center.needsUpdate());
        new Thread(refreshProcess).start();
    }

    public interface WeatherProviderInterface {
        void onWeatherReceived(boolean succeeded, Weather weather);
    }

    enum XMLRecordType {
        DATA("data"), TIME("hour"), TIME_SHIFT("day"), TEMPERATURE("temp"), CLOUD("sky"),
        PRECIPIATION("pty"), NONE("");
        String TAG;

        XMLRecordType(String tag) {
            TAG = tag;
        }

        @Override
        public String toString() {
            return this.TAG;
        }

        public boolean equalsTag(Object object) {
            try {
                String input = (String) object;
                return this.toString().equals(input);
            } catch (ClassCastException e) {
                return super.equals(object);
            }
        }
    }

    class RefreshProcess implements Runnable {

        boolean shouldRefresh = false;

        RefreshProcess(boolean isOrder) {
            this.shouldRefresh = isOrder;
        }

        //Method for parsing meal information
        void refreshWeather() throws IOException, ArrayIndexOutOfBoundsException, XmlPullParserException {
            Log.i(TAG, "Requesting KMA for WEATHER UPDATES\nURL : " + URL);

            InputStream inputStream = new java.net.URL(URL).openStream();

            //Parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(inputStream, "UTF-8");

            WeatherDatabaseManager databaseManager = new WeatherDatabaseManager(context);
            databaseManager.initializeDatabase();

            int parserPosition = parser.getEventType();
            int count = 0;

            XMLRecordType type = XMLRecordType.NONE;
            String tag;
            Weather weather = new Weather(context);

            while (parserPosition != XmlPullParser.END_DOCUMENT && count <= 4) {

                switch (parserPosition) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals(XMLRecordType.DATA.toString())) {
                            weather = new Weather(context);
                        } else if (tag.equals(XMLRecordType.TIME.toString())) {
                            type = XMLRecordType.TIME;
                        } else if (tag.equals(XMLRecordType.TIME_SHIFT.toString())) {
                            type = XMLRecordType.TIME_SHIFT;
                        } else if (tag.equals(XMLRecordType.TEMPERATURE.toString())) {
                            type = XMLRecordType.TEMPERATURE;
                        } else if (tag.equals(XMLRecordType.CLOUD.toString())) {
                            type = XMLRecordType.CLOUD;
                        } else if (tag.equals(XMLRecordType.PRECIPIATION.toString())) {
                            type = XMLRecordType.PRECIPIATION;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        tag = parser.getText();
                        if (type.equalsTag(tag)) {
                            weather.setTime(Integer.parseInt(tag));
                        } else if (type.equals(XMLRecordType.TIME_SHIFT)) {
                            weather.setTimeShift(Integer.parseInt(tag));
                        } else if (type.equals(XMLRecordType.TIME)) {
                            weather.setTime(Integer.parseInt(tag));
                        } else if (type.equals(XMLRecordType.TEMPERATURE)) {
                            weather.setTemperature(Integer.parseInt(tag));
                        } else if (type.equals(XMLRecordType.CLOUD)) {
                            weather.setCloudState(Integer.parseInt(tag));
                        } else if (type.equals(XMLRecordType.PRECIPIATION)) {
                            weather.setCloudState(Integer.parseInt(tag));
                        }
                        type = XMLRecordType.NONE;
                        break;
                    case XmlPullParser.END_TAG:
                        if (XMLRecordType.DATA.equalsTag(parser.getName())) {
                            databaseManager.updateWeather(weather);
                            count++;
                        }
                        break;
                }
                parserPosition = parser.next();
            }
        }

        @Override
        public void run() {
            int succeeded = 0;
            if (shouldRefresh) {
                try {
                    refreshWeather();
                } catch (Exception e) {
                    succeeded = -1;
                }
            }
            handler.sendEmptyMessage(succeeded);
        }
    }

    private class WeatherDatabaseManager extends SQLiteOpenHelper {

        public static final int DB_VERSION = 1;
        private final String KEY_ID = "order";
        private final String KEY_TIME = "time";
        private final String KEY_TEMP = "temp";
        private final String KEY_CLOUD = "clouds";
        private final String KEY_PRECIPITATION = "precipitation";
        private final String KEY_TIMESHIFT = "isToday";
        private final String TABLE_NAME = "weatherTable";

        public WeatherDatabaseManager(Context context) {
            super(context, "weather.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            final String CREATE_MEAL_TABLE = "CREATE TABLE" + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                    + KEY_TEMP + " INTEGER," + KEY_CLOUD + " INTEGER,"
                    + KEY_PRECIPITATION + " INTEGER," + KEY_TIMESHIFT + " INTEGER" + ")";
            Log.d("SQL", "Querying database w/ command " + CREATE_MEAL_TABLE);
            database.execSQL(CREATE_MEAL_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int i, int i2) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(database);
        }

        public Weather getWeather() {
            return getWeather(0);
        }

        public Weather getWeather(int order) {
            SQLiteDatabase database = this.getReadableDatabase();
            Log.d(TAG, "Retrieved meal at index " + order);
            Cursor cursor = database.query(TABLE_NAME, new String[]
                            {KEY_ID, KEY_TIME, KEY_TEMP, KEY_CLOUD, KEY_PRECIPITATION, KEY_TIMESHIFT}, KEY_ID + " = ?",
                    new String[]{String.valueOf(order)}, null, null, null, null);
            if (cursor != null)
                cursor.moveToFirst();

            try {
                return new Weather(context, cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),
                        cursor.getInt(3), cursor.getInt(4),
                        cursor.getInt(5));
            } catch (NullPointerException e) {
                return new Weather(context);
            }
        }

        public long updateWeather(Weather weather) {
            if (weather == null || weather.getTemperature() == Weather.INVALID_TEMP)
                return 0;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_TIME, weather.getTime());
            values.put(KEY_TEMP, weather.getTemperature());
            values.put(KEY_CLOUD, weather.getCloudState());
            values.put(KEY_PRECIPITATION, weather.getPrecipitationState());
            values.put(KEY_TIMESHIFT, weather.getTimeShift());

            Log.v("SQL", "Recorded weather at hour " + weather.getTime() + "\n" + weather);

            return database.insert(TABLE_NAME, null, values);
        }

        public void initializeDatabase() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(TABLE_NAME, null, null);

            for (int i = 0; i < 4; i++) {
                ContentValues values = new ContentValues();
                values.put(KEY_TIME, "");
                values.put(KEY_TEMP, "");
                values.put(KEY_CLOUD, "");
                values.put(KEY_PRECIPITATION, "");
                database.insert(TABLE_NAME, null, values);
            }
        }
    }
}
