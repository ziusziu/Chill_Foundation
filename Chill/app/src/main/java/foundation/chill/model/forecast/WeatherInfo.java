package foundation.chill.model.forecast;

/**
 * Created by samsiu on 5/6/16.
 */
public class WeatherInfo {
    int time;
    String summary;
    String icon;
    int nearestStormDistance;
    double precipIntensity;
    double percipIntensityError;
    double precipProbability;
    String precipType;
    double temperature;
    double apparentTemperature;
    double dewPoint;
    double humidity;
    double windSpeed;
    int windBearing;
    double visibility;
    double cloudCover;
    double pressure;
    double ozone;

    public int getTime() {
        return time;
    }

    public String getSummary() {
        return summary;
    }

    public String getIcon() {
        return icon;
    }

    public int getNearestStormDistance() {
        return nearestStormDistance;
    }

    public double getPrecipIntensity() {
        return precipIntensity;
    }

    public double getPercipIntensityError() {
        return percipIntensityError;
    }

    public double getPrecipProbability() {
        return precipProbability;
    }

    public String getPrecipType() {
        return precipType;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getApparentTemperature() {
        return apparentTemperature;
    }

    public double getDewPoint() {
        return dewPoint;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public int getWindBearing() {
        return windBearing;
    }

    public double getVisibility() {
        return visibility;
    }

    public double getCloudCover() {
        return cloudCover;
    }

    public double getPressure() {
        return pressure;
    }

    public double getOzone() {
        return ozone;
    }
}
