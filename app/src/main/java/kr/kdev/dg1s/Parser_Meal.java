package kr.kdev.dg1s;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Parser_Meal  {
    ArrayList<Adapter_Meal> parseMeal;

    SharedPreferences prefs;

    private Source source;

    ArrayList<Adapter_Meal> parseMeal(Context context) {
        UpdateThread updateThread = new UpdateThread();
        prefs = context.getSharedPreferences("kr.kdev.dg1s", Context.MODE_PRIVATE);
        updateThread.start();
        return parseMeal;
    }

    class UpdateThread extends Thread {
        public void run() {
            try {
                updateMeal();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMeal() throws IOException {
        try {
            parseMeal.clear();
            InputStream inputStream = new URL("http://www.dg1s.hs.kr/user/carte/list.do").openStream();
            source = new Source(new InputStreamReader(inputStream, "UTF-8"));
            source.fullSequentialParse();

            //테이블가져오기
            Element table = source.getFirstElementByClass("meals_today_list");
            int cnt = table.getAllElements(HTMLElementName.IMG).size();

            for (int i = 0; i < cnt; i++) {
                String panbyul = table.getAllElements(HTMLElementName.IMG).get(i).getAttributeValue("alt");
                Log.d("MealParser", panbyul);
                if (panbyul != null) {
                    if (panbyul.equals("조식")) {
                        Log.d("MealPrefs",table.getAllElements(HTMLElementName.IMG).get(i)
                                .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                .replaceAll("[①-⑮]", ""));
                        prefs.edit().putString("breakfast", table.getAllElements(HTMLElementName.IMG).get(i)
                                .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                .replaceAll("[①-⑮]", "")).commit();
                    } else if (panbyul.equals("중식")) {
                        prefs.edit().putString("lunch", table.getAllElements(HTMLElementName.IMG).get(i)
                                .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                .replaceAll("[①-⑮]", "")).commit();
                    } else if (panbyul.equals("석식")) {
                        prefs.edit().putString("dinner", table.getAllElements(HTMLElementName.IMG).get(i)
                                .getParentElement().getContent().toString().replaceAll("[^>]*/> ", "")
                                .replaceAll("[①-⑮]", "")).commit();
                    }
                }
            }
            inputStream.close();
        }  catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("TAG", "NPE");
        }
        return;
    }
}
