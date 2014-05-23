package kr.kdev.dg1s;

import android.content.res.Resources;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Parser_Weather {
    ArrayList<Adapter_Weather> parseWeather;
    private String TAG = "WeatherParser";

    Parser_Weather() {
        parseWeather = new ArrayList<Adapter_Weather>();
    }

    ArrayList<Adapter_Weather> parseWeather() {
        try {
            parseWeather.clear();
            InputStream inputStream = new URL("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=2714074500").openStream();

            //Parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            parser.setInput(inputStream, "UTF-8");

            String tag;
            Adapter_Weather wa = null;
            int parserEvent = parser.getEventType();
            int count = 0;
            boolean weather = false;
            boolean time = false;
            boolean temp = false;
            while (parserEvent != XmlPullParser.END_DOCUMENT && count <= 4) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();

                        if (tag.equals("data")) {
                            wa = new Adapter_Weather();
                        } else if (tag.equals("wfKor")) {
                            weather = true;
                        } else if (tag.equals("hour")) {
                            time = true;
                        } else if (tag.equals("temp")) {
                            temp = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        tag = parser.getName();
                        if (weather) {
                            wa.mWeather = parser.getText().toString();
                            weather = false;
                            Log.d(TAG, "Weather: " + wa.mWeather);
                        } else if (time) {
                            wa.mTime = parser.getText().toString();
                            time = false;
                            Log.d(TAG, "Time: " + wa.mTime);
                        } else if (temp) {
                            wa.mTemp = parser.getText().toString();
                            temp = false;
                            Log.d(TAG, "Temp: " + wa.mTemp);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tag = parser.getName();

                        if (tag.equals("data")) {
                            parseWeather.add(wa);
                            count++;
                            Log.d(TAG, "-----");
                        }
                        break;
                }
                parserEvent = parser.next();
            }
            inputStream.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("TAG", "NPE");
        }

        return parseWeather;
    }
}