package foundation.chill.model.forecast;

import java.util.List;

/**
 * Created by samsiu on 5/6/16.
 */
public class WeatherDetail {
    String summary;
    String icon;
    List<WeatherInfo> data;

    public String getSummary() {
        return summary;
    }

    public String getIcon() {
        return icon;
    }

    public List<WeatherInfo> getData() {
        return data;
    }
}
