package foundation.chill.provider;

import foundation.chill.model.forecast.Weather;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by samsiu on 8/25/16.
 */
public class ForecastService {

    public static final String API_URL = "https://api.forecast.io/forecast/";

    public interface ForecastRx {
        @GET("{api_key}/{latlng}")
        Observable<Weather> getWeather(@Path("api_key") String key,
                                       @Path("latlng") String term
        );
    }

    public static ForecastRx createRx() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ForecastService.ForecastRx.class);
    }

}
