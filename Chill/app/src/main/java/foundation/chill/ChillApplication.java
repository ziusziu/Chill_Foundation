package foundation.chill;

import android.app.Application;

import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;

import timber.log.Timber;

/**
 * Created by samsiu on 8/25/16.
 */
public class ChillApplication extends Application implements IAviaryClientCredentials {

    private static String creativeSDKClientId;
    private static String creativeSDKClientSecret;


    @Override
    public void onCreate() {
        super.onCreate();

        creativeSDKClientId = getResources().getString(R.string.adobe_clientID);
        creativeSDKClientSecret = getResources().getString(R.string.adobe_secret);


        AdobeCSDKFoundation.initializeCSDKFoundation(getApplicationContext());

        if(BuildConfig.DEBUG){
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    public String getClientID() {
        return creativeSDKClientId;
    }

    @Override
    public String getClientSecret() {
        return creativeSDKClientSecret;
    }

    @Override
    public String getBillingKey() {
        return "";
    }
}
