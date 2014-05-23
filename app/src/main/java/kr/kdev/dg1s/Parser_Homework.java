package kr.kdev.dg1s;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Parser_Homework {
    ArrayList<Adapter_PostList> parsePost;
    String TAG = "HWParser";

    Parser_Homework() {
        parsePost = new ArrayList<Adapter_PostList>();
    }

    ArrayList<Adapter_PostList> parsePost() {
        try {
            parsePost.clear();
            String url = "http://junbread.woobi.co.kr/hwlist.xml";
            URL targetURL = new URL(url);

            InputStream is = targetURL.openStream();

            //HomeworkParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser XMLparser = factory.newPullParser();

            XMLparser.setInput(is, "UTF-8");

            String tag;
            Adapter_PostList postData = null;
            int parserEvent = XMLparser.getEventType();
            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.END_TAG:
                        tag = XMLparser.getName();
                        if (tag.compareTo("item") == 0) {
                            parsePost.add(postData);
                            Log.d(TAG, "-----");
                        }
                        break;
                    case XmlPullParser.START_TAG:
                        tag = XMLparser.getName();
                        if (tag.compareTo("item") == 0) {
                            postData = new Adapter_PostList();
                        } else if (postData != null && tag.compareTo("grade") == 0) {
                            postData.gradeNum = XMLparser.getAttributeValue(null, "data");
                            Log.d(TAG, "학년: " + postData.gradeNum);
                        } else if (postData != null && tag.compareTo("clss") == 0) {
                            postData.classNum = XMLparser.getAttributeValue(null, "data");
                            Log.d(TAG, "반: " + postData.classNum);
                        } else if (postData != null && tag.compareTo("subject") == 0) {
                            postData.subject = XMLparser.getAttributeValue(null, "data");
                            Log.d(TAG, "과목: " + postData.subject);
                        } else if (postData != null && tag.compareTo("dscp") == 0) {
                            postData.info = XMLparser.getAttributeValue(null, "data");
                            Log.d(TAG, "설명: " + postData.info);
                        } else if (postData != null && tag.compareTo("due") == 0) {
                            postData.due = XMLparser.getAttributeValue(null, "data");
                            Log.d(TAG, "기한: " + postData.due);
                        }
                        break;
                }
                parserEvent = XMLparser.next();
            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return parsePost;
    }
}