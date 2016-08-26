package foundation.chill;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by samsiu on 8/25/16.
 */
public class ChillApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }
}
