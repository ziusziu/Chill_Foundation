package foundation.chill.model.forecast;

import java.util.List;

/**
 * Created by samsiu on 5/6/16.
 */
public class WeatherMinute {
    String summary;
    String icon;
    List<WeatherMinuteData> data;

    public String getSummary() {
        return summary;
    }

    public String getIcon() {
        return icon;
    }

    public List<WeatherMinuteData> getData() {
        return data;
    }
}
