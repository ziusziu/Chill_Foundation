package foundation.chill;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import foundation.chill.model.forecast.Weather;
import foundation.chill.provider.ForecastService;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 22;

    private GoogleApiClient googleApiClient;
    private static Location lastLocation;
    private AddressResultReceiver resultReceiver;

    private static String latitude;
    private static String longitude;

    Button color1Button;
    Button color2Button;
    Button color3Button;
    Button color4Button;
    TextView snowFallTextView;
    TextView temperatureTextView;
    TextView elevationTextView;
    TextView locationTextView;
    TextView locationDetailTextView;
    TextView locationHyphenTextView;
    SeekBar contrastSeekBar;
    ImageView photoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initColorButtons();

        initSeekBar();

        setGoogleApiClient();
        checkPermissions();

        resultReceiver = new AddressResultReceiver(new Handler());

        ForecastService.ForecastRx forecast = ForecastService.createRx();
        callForecastApi(forecast);

    }

    protected void callForecastApi(ForecastService.ForecastRx forecast){

        latitude = "-73.723975";
        longitude = "-66.215334";

        Timber.d("PRINT LATITUDE AND LONGITUDE " + latitude + " " + longitude);
        if(latitude.equals("0.0") & longitude.equals("0.0")){
            latitude = "Nothing";
            longitude = "Nothing";
        }
        String latLong = latitude+","+longitude;

        String forecastApiKey = "";

        Observable<Weather> observable = forecast.getWeather(forecastApiKey, latLong);
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Weather>(){

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e.getMessage());
                    }

                    @Override
                    public void onNext(Weather weather) {
                        Timber.d("TimeZone: " + weather.getTimezone());
                        locationTextView.setText(weather.getHourly().getSummary().toString());
                    }
                });


    }


    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

    private void initializeViews() {
        color1Button = (Button) findViewById(R.id.color1_button);
        color2Button = (Button) findViewById(R.id.color2_button);
        color3Button = (Button) findViewById(R.id.color3_button);
        color4Button = (Button) findViewById(R.id.color4_button);
        snowFallTextView = (TextView) findViewById(R.id.snowfall_textView);
        temperatureTextView = (TextView) findViewById(R.id.temperature_textView);
        elevationTextView = (TextView) findViewById(R.id.elevation_textView);
        locationTextView = (TextView) findViewById(R.id.location1_textView);
        locationDetailTextView = (TextView) findViewById(R.id.location2_textView);
        locationHyphenTextView = (TextView) findViewById(R.id.locationHyphen_textView);
        contrastSeekBar = (SeekBar) findViewById(R.id.contrast_seekBar);
        photoImage = (ImageView) findViewById(R.id.photo_imageView);
    }

    private void initColorButtons() {
        setButtonOnClickListener(color1Button);
        setButtonOnClickListener(color2Button);
        setButtonOnClickListener(color3Button);
        setButtonOnClickListener(color4Button);
    }

    private void setButtonOnClickListener(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.color1_button:
                        Log.d(TAG, "Button 1 Clicked");
                        int colorGrey = ContextCompat.getColor(MainActivity.this, R.color.colorGrey);
                        setTextColor(colorGrey);
                        break;
                    case R.id.color2_button:
                        Log.d(TAG, "Button 2 Clicked");
                        int colorNeon = ContextCompat.getColor(MainActivity.this, R.color.colorNeon);
                        setTextColor(colorNeon);
                        break;
                    case R.id.color3_button:
                        Log.d(TAG, "Button 3 Clicked");
                        int colorWhite = ContextCompat.getColor(MainActivity.this, R.color.colorWhite);
                        setTextColor(colorWhite);
                        break;
                    case R.id.color4_button:
                        Log.d(TAG, "Button 4 Clicked");
                        int colorBlack = ContextCompat.getColor(MainActivity.this, R.color.colorBlack);
                        setTextColor(colorBlack);
                        break;
                    default:
                        Log.d(TAG, "NOTHING CLICKED");
                        break;
                }
            }
        });
    }

    private void setTextColor(int color) {
        snowFallTextView.setTextColor(color);
        temperatureTextView.setTextColor(color);
        elevationTextView.setTextColor(color);
        locationTextView.setTextColor(color);
        locationHyphenTextView.setTextColor(color);
        locationDetailTextView.setTextColor(color);
    }


    private void initSeekBar() {

        contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                photoImage.setImageBitmap(changeBitmapContrastBrightness(BitmapFactory.decodeResource(getResources(), R.drawable.android_arms), (float) progress / 100f, 1));
                //textView.setText("Contrast: "+(float) progress / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        contrastSeekBar.setMax(200);
        contrastSeekBar.setProgress(100);
        contrastSeekBar.incrementProgressBy(1);
    }

    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }


    private void setGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void checkPermissions() {
        //Ask for permission if we don't have it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            // Permissions Granted
            Log.d(TAG, "Permissions Granted");
            getLatLongCoordinates();
        }
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        if(CheckInternetConnection.isNetworkAvailable(MainActivity.this)){
            Log.d(TAG, "Network Available");
            getLatLongCoordinates();
        }else{
            Log.d(TAG, "Internet Not Connected");
        }

        if(lastLocation != null){
            startIntentService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (permissions.length < 0){
                    return;
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permissions Granted");
                    getLatLongCoordinates();
                } else {
                    Toast.makeText(this, "Need device location.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getLatLongCoordinates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.d(TAG, "Latitude: "+String.valueOf(lastLocation.getLatitude()));
                Log.d(TAG, "Longitude: "+String.valueOf(lastLocation.getLongitude()));
            }
            else {
                Log.d(TAG, "LastLocation not null");
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.d(TAG, "ADDRESS: " + addressOutput);


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "ADDRESS FOUND: "+getString(R.string.address_found));
            }

        }
    }
}
