package foundation.chill.model.forecast;

import java.util.List;

/**
 * Created by samsiu on 5/11/16.
 */
public class WeatherDetailDaily {

    String summary;
    String icon;
    List<WeatherInfoDaily> data;

    public String getSummary() {
        return summary;
    }

    public String getIcon() {
        return icon;
    }

    public List<WeatherInfoDaily> getData() {
        return data;
    }

}
