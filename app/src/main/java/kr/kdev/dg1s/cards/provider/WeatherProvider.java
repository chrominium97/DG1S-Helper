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

    final static String URL = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=2714074500";
    private final static String TAG = "WeatherProvider";
    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            weatherProviderInterface.onWeatherReceived(message.what == 0,
                    new WeatherDatabaseManager(context).getForecast());
        }
    };

    Context context;
    UpdateCenter center;
    WeatherProviderInterface weatherProviderInterface;

    public WeatherProvider(Context arg0, Object origin) {
        try {
            weatherProviderInterface = (WeatherProviderInterface) origin;
        } catch (ClassCastException e) {
            Log.e(TAG, "WeatherProviderInterface not cast");
        }

        context = arg0;
        center = new UpdateCenter(UpdateCenter.TYPE_WEATHER, context);
    }

    public void query(boolean forceUpdate) {
        Runnable refreshProcess = new RefreshProcess(forceUpdate || center.needsUpdate());
        new Thread(refreshProcess).start();
    }

    public interface WeatherProviderInterface {
        void onWeatherReceived(boolean succeeded, Weather[] weathers);
    }

    class RefreshProcess implements Runnable {

        private static final String TAG = "WeatherProvider@RefreshProcess";

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

            String tag = "";
            Weather weather = new Weather(context);

            while (parserPosition != XmlPullParser.END_DOCUMENT && count <= 4) {

                switch (parserPosition) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        if (tag.equals("data")) {
                            Log.d(TAG, "New weather object created");
                            weather = new Weather(context);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        String text = parser.getText();
                        if (tag.equals("hour")) {
                            weather.setTime(Integer.parseInt(text));
                        } else if (tag.equals("day")) {
                            weather.setTimeShift(Integer.parseInt(text));
                        } else if (tag.equals("temp")) {
                            weather.setTemperature(text);
                        } else if (tag.equals("sky")) {
                            weather.setCloudState(Integer.parseInt(text));
                        } else if (tag.equals("pty")) {
                            weather.setPrecipitationState(Integer.parseInt(text));
                        }
                        tag = "";
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("data")) {
                            Log.v(TAG, "Returned weather");
                            databaseManager.updateWeather(weather);
                            count++;
                        }
                        break;
                }
                parserPosition = parser.next();
            }
            Log.i(TAG, "Parsing finished");
        }

        @Override
        public void run() {
            int succeeded = 0;
            if (shouldRefresh) {
                try {
                    refreshWeather();
                    center.updateTime();
                } catch (Exception e) {
                    e.printStackTrace();
                    succeeded = -1;
                }
            }
            handler.sendEmptyMessage(succeeded);
        }
    }

    private class WeatherDatabaseManager extends SQLiteOpenHelper {

        public static final int DB_VERSION = 4;
        private static final String TAG = "WeatherDatabaseManager";
        private final String KEY_ID = "listOrder";
        private final String KEY_TIME = "time";
        private final String KEY_TEMP = "temp";
        private final String KEY_CLOUD = "clouds";
        private final String KEY_PRECIPITATION = "precipitation";
        private final String KEY_TIME_SHIFT = "timeShift";
        private final String TABLE_NAME = "weatherTable";

        public WeatherDatabaseManager(Context context) {
            super(context, "weather.db", null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            String CREATE_WEATHER_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                    + KEY_TEMP + " STRING," + KEY_CLOUD + " INTEGER,"
                    + KEY_PRECIPITATION + " INTEGER," + KEY_TIME_SHIFT + " INTEGER" + ")";
            Log.d(TAG, "Querying database w/ command " + CREATE_WEATHER_TABLE);
            database.execSQL(CREATE_WEATHER_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int i, int i2) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            onCreate(database);
            query(true);
        }

        public Weather[] getForecast() {
            return new Weather[]{getWeather(1), getWeather(2), getWeather(3), getWeather(4), getWeather(5)};
        }

        public Weather getWeather(int order) {
            SQLiteDatabase database = this.getReadableDatabase();
            Log.d(TAG, "Retrieved weather at index " + order);
            Cursor cursor = database.query(TABLE_NAME, new String[]
                            {KEY_ID, KEY_TIME, KEY_TEMP, KEY_CLOUD, KEY_PRECIPITATION, KEY_TIME_SHIFT}, KEY_ID + " = ?",
                    new String[]{String.valueOf(order)}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                try {
                    return new Weather(context, cursor.getInt(0), cursor.getInt(1), cursor.getString(2),
                            cursor.getInt(3), cursor.getInt(4),
                            cursor.getInt(5));
                } catch (NullPointerException e) {
                    return new Weather(context);
                }
            }
            return new Weather(context);
        }

        public long updateWeather(Weather weather) {
            if (weather == null || weather.getTemperature().equals(Weather.INVALID_TEMP))
                return 0;

            SQLiteDatabase database = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_TIME, weather.getTime());
            values.put(KEY_TEMP, weather.getTemperature());
            values.put(KEY_CLOUD, weather.getCloudState());
            values.put(KEY_PRECIPITATION, weather.getPrecipitationState());
            values.put(KEY_TIME_SHIFT, weather.getTimeShift());

            Log.v(TAG, "Recorded weather at hour " + weather.getTime() + "\n" + weather);

            return database.insert(TABLE_NAME, null, values);
        }

        public void initializeDatabase() {
            SQLiteDatabase database = this.getWritableDatabase();
            database.delete(TABLE_NAME, null, null);

        }
    }
}
