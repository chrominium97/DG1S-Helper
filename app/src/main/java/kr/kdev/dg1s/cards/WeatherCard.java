package kr.kdev.dg1s.cards;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.kdev.dg1s.R;
import kr.kdev.dg1s.cards.provider.WeatherProvider;
import kr.kdev.dg1s.cards.provider.datatypes.Weather;

public class WeatherCard implements WeatherProvider.WeatherProviderInterface {

    private final static String TAG = "WeatherCard";

    private final int targetDelay = 1000;
    private WeatherProvider provider;
    private LinearLayout header;
    private FrameLayout contents;
    private ImageView weatherIcon;
    private TextView weatherText;
    private LinearLayout subWeathers;
    private LinearLayout subWeather1;
    private ImageView subWeatherIcon1;
    private TextView subWeatherText1;
    private LinearLayout subWeather2;
    private ImageView subWeatherIcon2;
    private TextView subWeatherText2;
    private LinearLayout subWeather3;
    private ImageView subWeatherIcon3;
    private TextView subWeatherText3;
    private LinearLayout subWeather4;
    private ImageView subWeatherIcon4;
    private TextView subWeatherText4;
    private CardViewStatusNotifier statusNotifier;
    private long timeAtLastViewChange;

    private Context context;

    public WeatherCard(Context origin, ViewGroup viewParent, Activity activity) {

        try {
            statusNotifier = (CardViewStatusNotifier) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "needs to implement CardViewStatusNotifier");
        }

        context = origin;

        CardView weatherCard = (CardView) LayoutInflater.from(origin).inflate(R.layout.card_weather, viewParent, false);

        header = (LinearLayout) weatherCard.findViewById(R.id.header);
        contents = (FrameLayout) weatherCard.findViewById(R.id.contents);

        weatherIcon = (ImageView) header.findViewById(R.id.weather_icon);
        weatherText = (TextView) header.findViewById(R.id.weather_text);

        subWeathers = (LinearLayout) contents.findViewById(R.id.weathers_sub);
        subWeather1 = (LinearLayout) subWeathers.findViewById(R.id.weather_sub_1);
        subWeather2 = (LinearLayout) subWeathers.findViewById(R.id.weather_sub_2);
        subWeather3 = (LinearLayout) subWeathers.findViewById(R.id.weather_sub_3);
        subWeather4 = (LinearLayout) subWeathers.findViewById(R.id.weather_sub_4);
        subWeatherIcon1 = (ImageView) subWeather1.findViewById(R.id.weather_sub_1_icon);
        subWeatherIcon2 = (ImageView) subWeather2.findViewById(R.id.weather_sub_2_icon);
        subWeatherIcon3 = (ImageView) subWeather3.findViewById(R.id.weather_sub_3_icon);
        subWeatherIcon4 = (ImageView) subWeather4.findViewById(R.id.weather_sub_4_icon);
        subWeatherText1 = (TextView) subWeather1.findViewById(R.id.weather_sub_1_text);
        subWeatherText2 = (TextView) subWeather2.findViewById(R.id.weather_sub_2_text);
        subWeatherText3 = (TextView) subWeather3.findViewById(R.id.weather_sub_3_text);
        subWeatherText4 = (TextView) subWeather4.findViewById(R.id.weather_sub_4_text);

        provider = new WeatherProvider(origin, this);
        provider.query(false);

        hideWeathers();
        timeAtLastViewChange = System.currentTimeMillis() - targetDelay;

        viewParent.addView(weatherCard);
    }

    public void onWeatherReceived(boolean succeeded, Weather[] weathers) {
        try {
            showWeathers(weathers);
            if (succeeded) {
                statusNotifier.notifyCompletion(this, CardViewStatusNotifier.SUCCESS);
            } else {
                statusNotifier.notifyCompletion(this, CardViewStatusNotifier.FAILURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusNotifier.notifyCompletion(this, CardViewStatusNotifier.FAILURE);
        }
    }

    public void requestUpdate(boolean isForced) {
        hideWeathers();
        provider.query(isForced);
    }

    String formatTime(Weather weather) {
        int hour = weather.getTime();
        String output = String.valueOf(hour % 12) + " ";
        if (hour >= 12) {
            if (hour == 24) {
                output = output + "AM";
            } else {
                output = output + "PM";
            }
        } else {
            output = output + "AM";
        }
        return output;
    }

    long delay() {
        Log.d(TAG + "_AnimationDelay",
                String.valueOf((timeAtLastViewChange + targetDelay) - System.currentTimeMillis()) + " " +
                        "milliseconds delayed");
        return (timeAtLastViewChange + targetDelay) - System.currentTimeMillis();
    }

    void hideWeathers() {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new hideWeatherAction(), delay);
    }

    void showWeathers(Weather[] input) throws Exception {
        final Handler handler = new Handler();
        long delay = delay();
        if (delay < 0) {
            delay = 0;
        }
        handler.postDelayed(new showWeatherAction(input), delay);
    }

    private void addView(View view, LinearLayout viewParent) {
        if (view.getParent() == null) {
            viewParent.addView(view);
        }
    }

    private void removeView(View view, ViewGroup viewParent) {
        if (view.getParent() != null) {
            viewParent.removeView(view);
        }
    }

    class hideWeatherAction implements Runnable {
        @Override
        public void run() {
            removeView(weatherIcon, header);
            removeView(weatherText, header);

            removeView(subWeather1, subWeathers);
            removeView(subWeather2, subWeathers);
            removeView(subWeather3, subWeathers);
            removeView(subWeather4, subWeathers);

            timeAtLastViewChange = System.currentTimeMillis();
        }
    }

    class showWeatherAction implements Runnable {

        final Weather[] weathers;

        showWeatherAction(Weather[] input) {
            weathers = input;
        }

        @Override
        public void run() {

            header.setBackgroundColor(weathers[0].getWeatherColor());
            subWeathers.setBackgroundColor(weathers[0].getSubWeatherColor());

            weatherIcon.setImageResource(weathers[0].getImageId());
            weatherText.setText(weathers[0].getReadableWeatherState().replace("\n", " ") +
                    " (" + weathers[0].getTemperature() + "℃)");
            subWeatherIcon1.setImageResource(weathers[1].getImageId());
            subWeatherText1.setText(formatTime(weathers[1]) + "\n" +
                    weathers[1].getReadableWeatherState() + "\n(" +
                    weathers[1].getTemperature() + "℃)");
            subWeatherIcon2.setImageResource(weathers[2].getImageId());
            subWeatherText2.setText(formatTime(weathers[2]) + "\n" +
                    weathers[2].getReadableWeatherState() + "\n(" +
                    weathers[2].getTemperature() + "℃)");
            subWeatherIcon3.setImageResource(weathers[3].getImageId());
            subWeatherText3.setText(formatTime(weathers[3]) + "\n" +
                    weathers[3].getReadableWeatherState() + "\n(" +
                    weathers[3].getTemperature() + "℃)");
            subWeatherIcon4.setImageResource(weathers[4].getImageId());
            subWeatherText4.setText(formatTime(weathers[4]) + "\n" +
                    weathers[4].getReadableWeatherState() + "\n(" +
                    weathers[4].getTemperature() + "℃)");

            addView(weatherIcon, header);
            addView(weatherText, header);

            addView(subWeather1, subWeathers);
            addView(subWeather2, subWeathers);
            addView(subWeather3, subWeathers);
            addView(subWeather4, subWeathers);
            timeAtLastViewChange = System.currentTimeMillis();
        }
    }
}
