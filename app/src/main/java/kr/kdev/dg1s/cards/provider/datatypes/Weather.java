package kr.kdev.dg1s.cards.provider.datatypes;

import android.content.Context;

import java.util.Calendar;

import kr.kdev.dg1s.R;

public class Weather {

    public static final String INVALID_TEMP = "-999";
    final Context context;
    int ORDER_ON_DATABASE;

    int HOUR_OF_DAY = -1;
    String TEMPERATURE;
    CLOUD CLOUD_STATE = CLOUD.UNKNOWN;
    PRECIPITATION PRECIPITATION_STATE = PRECIPITATION.UNKNOWN;
    int TIME_SHIFT = 0;

    public Weather(Context incomingContext) {
        context = incomingContext;
    }

    public Weather(Context iContext, int order, int time, String temp, int cloudState, int precipitationState, int timeShift) {
        this.TIME_SHIFT = timeShift;
        context = iContext;
        this.ORDER_ON_DATABASE = order;
        this.HOUR_OF_DAY = time;
        this.TEMPERATURE = temp;
        setCloudState(cloudState);
        setPrecipitationState(precipitationState);
    }

    public String getReadableWeatherState() {
        if (this.PRECIPITATION_STATE == PRECIPITATION.NONE) {
            switch (this.CLOUD_STATE) {
                case CLEAR:
                    return context.getString(R.string.cloud_clear);
                case PARTLY_CLOUDY:
                    return context.getString(R.string.cloud_partly);
                case MOSTLY_CLOUDY:
                    return context.getString(R.string.cloud_mostly);
                case CLOUDY:
                    return context.getString(R.string.cloud_cloudy);
            }
        } else {
            switch (this.PRECIPITATION_STATE) {
                case RAIN:
                    return context.getString(R.string.precipitation_rain);
                case SLEET:
                    return context.getString(R.string.precipitation_sleet);
                case SNOW:
                    return context.getString(R.string.precipitation_snow);
            }
        }
        return context.getString(R.string.unknown);
    }

    public String getReadableCloudState() {
        switch (CLOUD_STATE) {
            case CLEAR:
                return context.getString(R.string.cloud_clear);
            case PARTLY_CLOUDY:
                return context.getString(R.string.cloud_partly);
            case MOSTLY_CLOUDY:
                return context.getString(R.string.cloud_mostly);
            case CLOUDY:
                return context.getString(R.string.cloud_cloudy);
            default:
                return context.getString(R.string.unknown);
        }
    }

    public String getReadablePrecipitationState() {
        switch (PRECIPITATION_STATE) {
            case NONE:
                return context.getString(R.string.precipitation_none);
            case RAIN:
                return context.getString(R.string.precipitation_rain);
            case SLEET:
                return context.getString(R.string.precipitation_sleet);
            case SNOW:
                return context.getString(R.string.precipitation_snow);
            default:
                return context.getString(R.string.unknown);
        }
    }

    boolean isDaytime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return (hour > 7) && (hour < 21);
    }

    public int getWeatherColor() {
        if (this.PRECIPITATION_STATE == PRECIPITATION.UNKNOWN || this.CLOUD_STATE == CLOUD.UNKNOWN) {
            return context.getResources().getColor(R.color.grey_600);
        }

        if (isDaytime()) {
            if (this.PRECIPITATION_STATE == PRECIPITATION.NONE) {
                return context.getResources().getColor(R.color.blue_A400);
            } else if (this.PRECIPITATION_STATE == PRECIPITATION.SNOW) {
                return context.getResources().getColor(R.color.blue_A200);
            } else {
                return context.getResources().getColor(R.color.blue_A700);
            }
        } else {
            if (this.PRECIPITATION_STATE == PRECIPITATION.NONE) {
                return context.getResources().getColor(R.color.grey_800);
            } else if (this.PRECIPITATION_STATE == PRECIPITATION.SNOW) {
                return context.getResources().getColor(R.color.blue_grey_700);
            } else {
                return context.getResources().getColor(R.color.blue_grey_800);
            }
        }
    }

    public int getSubWeatherColor() {
        if (this.PRECIPITATION_STATE == PRECIPITATION.UNKNOWN || this.CLOUD_STATE == CLOUD.UNKNOWN) {
            return context.getResources().getColor(R.color.grey_400);
        }

        if (isDaytime()) {
            if (this.PRECIPITATION_STATE == PRECIPITATION.NONE) {
                return context.getResources().getColor(R.color.blue_400);
            } else if (this.PRECIPITATION_STATE == PRECIPITATION.SNOW) {
                return context.getResources().getColor(R.color.blue_300);
            } else {
                return context.getResources().getColor(R.color.blue_A200);
            }
        } else {
            if (this.PRECIPITATION_STATE == PRECIPITATION.NONE) {
                return context.getResources().getColor(R.color.grey_600);
            } else if (this.PRECIPITATION_STATE == PRECIPITATION.SNOW) {
                return context.getResources().getColor(R.color.blue_grey_500);
            } else {
                return context.getResources().getColor(R.color.blue_grey_600);
            }
        }
    }

    public int getImageId() {
        if (isDaytime()) {
            switch (this.CLOUD_STATE) {

                case CLEAR:
                    return R.drawable.weather_daytime_clear;

                case PARTLY_CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_daytime_partly;
                        case RAIN:
                            return R.drawable.weather_daytime_rain;
                        case SLEET:
                            return R.drawable.weather_daytime_sleet;
                        case SNOW:
                            return R.drawable.weather_daytime_snow;
                    }

                case MOSTLY_CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_daytime_partly;
                        case RAIN:
                            return R.drawable.weather_daytime_rain;
                        case SLEET:
                            return R.drawable.weather_daytime_sleet;
                        case SNOW:
                            return R.drawable.weather_daytime_snow;
                    }

                case CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_cloudy_clear;
                        case RAIN:
                            return R.drawable.weather_cloudy_rain;
                        case SLEET:
                            return R.drawable.weather_cloudy_sleet;
                        case SNOW:
                            return R.drawable.weather_cloudy_snow;
                    }
            }

        } else {
            switch (this.CLOUD_STATE) {

                case CLEAR:
                    return R.drawable.weather_nighttime_clear;

                case PARTLY_CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_nighttime_partly;
                        case RAIN:
                            return R.drawable.weather_nighttime_rain;
                        case SLEET:
                            return R.drawable.weather_nighttime_sleet;
                        case SNOW:
                            return R.drawable.weather_nighttime_snow;
                    }

                case MOSTLY_CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_nighttime_partly;
                        case RAIN:
                            return R.drawable.weather_nighttime_rain;
                        case SLEET:
                            return R.drawable.weather_nighttime_sleet;
                        case SNOW:
                            return R.drawable.weather_nighttime_snow;
                    }

                case CLOUDY:
                    switch (this.PRECIPITATION_STATE) {
                        case NONE:
                            return R.drawable.weather_cloudy_clear;
                        case RAIN:
                            return R.drawable.weather_cloudy_rain;
                        case SLEET:
                            return R.drawable.weather_cloudy_sleet;
                        case SNOW:
                            return R.drawable.weather_cloudy_snow;
                    }
            }
        }
        return R.drawable.weather_unknown;
    }

    public int getTimeShift() {
        return TIME_SHIFT;
    }

    public void setTimeShift(int timeShift) {
        this.TIME_SHIFT = timeShift;
    }

    public int getTime() {
        return this.HOUR_OF_DAY;
    }

    public void setTime(int time) {
        this.HOUR_OF_DAY = time;
    }

    public String getTemperature() {
        return this.TEMPERATURE;
    }

    public void setTemperature(String temp) {
        this.TEMPERATURE = temp;
    }

    public int getCloudState() {
        return this.CLOUD_STATE.getStatusCode();
    }

    public void setCloudState(int cloudState) {
        switch (cloudState) {
            case 1:
                CLOUD_STATE = CLOUD.CLEAR;
                break;
            case 2:
                CLOUD_STATE = CLOUD.PARTLY_CLOUDY;
                break;
            case 3:
                CLOUD_STATE = CLOUD.MOSTLY_CLOUDY;
                break;
            case 4:
                CLOUD_STATE = CLOUD.CLOUDY;
                break;
            default:
                CLOUD_STATE = CLOUD.UNKNOWN;
                break;
        }
    }

    public int getPrecipitationState() {
        return this.PRECIPITATION_STATE.getStatusCode();
    }

    public void setPrecipitationState(int precipitationState) {
        switch (precipitationState) {
            case 0:
                PRECIPITATION_STATE = PRECIPITATION.NONE;
                break;
            case 1:
                PRECIPITATION_STATE = PRECIPITATION.RAIN;
                break;
            case 2:
                PRECIPITATION_STATE = PRECIPITATION.SLEET;
                break;
            case 3:
                PRECIPITATION_STATE = PRECIPITATION.SNOW;
                break;
            default:
                PRECIPITATION_STATE = PRECIPITATION.UNKNOWN;
                break;
        }
    }

    @Override
    public String toString() {
        return "INDEX          : " + this.ORDER_ON_DATABASE +
                "\nTIME          : " + this.getTime() +
                "\nTEMPERATURE   : " + this.getTemperature() +
                "\nCLOUD         : " + this.getReadableCloudState().replace("\n", " ") +
                "\nPRECIPITATION : " + this.getReadablePrecipitationState();
    }

    enum CLOUD {
        CLEAR(1), PARTLY_CLOUDY(2), MOSTLY_CLOUDY(3), CLOUDY(4), UNKNOWN(-1);

        final int statusCode;

        CLOUD(int code) {
            this.statusCode = code;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    enum PRECIPITATION {
        NONE(0), RAIN(1), SNOW(2), SLEET(3), UNKNOWN(-1);

        final int statusCode;

        PRECIPITATION(int code) {
            this.statusCode = code;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
