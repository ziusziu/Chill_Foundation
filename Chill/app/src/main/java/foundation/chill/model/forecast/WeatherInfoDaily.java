package foundation.chill.model.forecast;

/**
 * Created by samsiu on 5/11/16.
 */
public class WeatherInfoDaily {

    int time;
    String summary;
    String icon;
    double sunriseTime;
    double sunsetTime;
    double moonPhase;
    double precipIntensity;
    double precipIntensityMax;
    double precipProbability;
    double temperatureMin;
    int temperatureMinTime;
    double temperatureMax;
    int temperatureMaxTime;
    double apparentTemperatureMin;
    int apparentTemperatureMinTime;
    double apparentTemperatureMax;
    int apparentTemperatureMaxTime;
    double dewPoint;
    double humidity;
    double windSpeed;
    int windBearing;
    double visibility;
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

    public double getSunriseTime() {
        return sunriseTime;
    }

    public double getSunsetTime() {
        return sunsetTime;
    }

    public double getMoonPhase() {
        return moonPhase;
    }

    public double getPrecipIntensity() {
        return precipIntensity;
    }

    public double getPrecipIntensityMax() {
        return precipIntensityMax;
    }

    public double getPrecipProbability() {
        return precipProbability;
    }

    public double getTemperatureMin() {
        return temperatureMin;
    }

    public int getTemperatureMinTime() {
        return temperatureMinTime;
    }

    public double getTemperatureMax() {
        return temperatureMax;
    }

    public int getTemperatureMaxTime() {
        return temperatureMaxTime;
    }

    public double getApparentTemperatureMin() {
        return apparentTemperatureMin;
    }

    public int getApparentTemperatureMinTime() {
        return apparentTemperatureMinTime;
    }

    public double getApparentTemperatureMax() {
        return apparentTemperatureMax;
    }

    public int getApparentTemperatureMaxTime() {
        return apparentTemperatureMaxTime;
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

    public double getPressure() {
        return pressure;
    }

    public double getOzone() {
        return ozone;
    }
}
