package foundation.chill.model.forecast;

/**
 * Created by samsiu on 5/6/16.
 */
public class Weather {
    double latitude;
    double longitude;
    String timezone;
    int offset;
    WeatherInfo currently;
    WeatherMinute minutely;
    WeatherDetail hourly;
    WeatherDetailDaily daily;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public int getOffset() {
        return offset;
    }

    public WeatherInfo getCurrently() {
        return currently;
    }

    public WeatherMinute getMinutely() {
        return minutely;
    }

    public WeatherDetail getHourly() {
        return hourly;
    }

    public WeatherDetailDaily getDaily() {
        return daily;
    }
}
