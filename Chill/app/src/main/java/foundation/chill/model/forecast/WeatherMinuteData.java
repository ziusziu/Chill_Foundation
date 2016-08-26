package foundation.chill.model.forecast;

/**
 * Created by samsiu on 5/6/16.
 */
public class WeatherMinuteData {
    int time;
    double precipIntensity;
    double precipIntensityError;
    double precipProbability;
    String precipType;

    public int getTime() {
        return time;
    }

    public double getPrecipIntensity() {
        return precipIntensity;
    }

    public double getPrecipIntensityError() {
        return precipIntensityError;
    }

    public double getPrecipProbability() {
        return precipProbability;
    }

    public String getPrecipType() {
        return precipType;
    }
}
