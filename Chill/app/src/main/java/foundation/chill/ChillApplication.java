package foundation.chill;

import android.app.Application;

import com.adobe.creativesdk.aviary.IAviaryClientCredentials;
import com.adobe.creativesdk.foundation.AdobeCSDKFoundation;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by samsiu on 8/25/16.
 */
public class ChillApplication extends Application implements IAviaryClientCredentials {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "ldfJSw0ye1ZkilJVYVFDCInzV";
    private static final String TWITTER_SECRET = "AboDmjYTm1yZ8PK9jS2VW9cNjq9mtfoHswx5b1V4nmk8NGgXiJ";


    private static String creativeSDKClientId;
    private static String creativeSDKClientSecret;


    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

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
