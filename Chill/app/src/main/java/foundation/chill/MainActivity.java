package foundation.chill;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import foundation.chill.model.forecast.Weather;
import foundation.chill.provider.ForecastService;
import foundation.chill.utilities.CheckInternetConnection;
import foundation.chill.utilities.Constants;
import foundation.chill.provider.FetchAddressIntentService;
import foundation.chill.utilities.UtilityFunction;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private static Location lastLocation;
    private AddressResultReceiver resultReceiver;

    private static String latitude;
    private static String longitude;

    FloatingActionButton shareFAB;
    Button color1Button, color2Button, color3Button, color4Button;
    TextView snowFallTextView, temperatureTextView, elevationTextView, locationTextView,
            locationDetailTextView, locationHyphenTextView;

    String addressOutput;

    ImageView photoImage;
    Uri imageUri;
    Uri editedImageUri;

    Toolbar toolbar;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initActionBar();
        initColorButtons();

        setImageViewClickListener();
        setFabClickListenter();

        setGoogleApiClient();
        checkLocationPermissions();
        getReceiverAddress();


        callForecastApi();
    }

    private void initActionBar(){
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chill");
    }



    private void setImageViewClickListener(){

        Bitmap bitmap = ((BitmapDrawable)photoImage.getDrawable()).getBitmap();
        Uri imageViewUri = getImageUri(getApplicationContext(), bitmap);

        photoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageEditorIntent = new AdobeImageIntent.Builder(getApplicationContext()).setData(imageViewUri).build();
                startActivityForResult(imageEditorIntent, Constants.GET_EDIT_PICTURE);
                //verifyStoragePermissions(MainActivity.this);
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void verifyStoragePermissions(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, Constants.PERMISSIONS_STORAGE, Constants.REQUEST_EXTERNAL_STORAGE);
        }else{
            imageUri = UtilityFunction.takePhoto(activity);
        }
    }


    private void setFabClickListenter(){



        shareFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap(takeScreenshot(view));
                File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imagePath1 = new File(pix, "screenshot.jpg");
                Uri imagePath1Uri = Uri.fromFile(imagePath1);
                UtilityFunction.sendTweet(MainActivity.this, "Hello", imagePath1Uri);
                //UtilityFunction.postTumblr(MainActivity.this, "Hello", editedImageUri);
                //UtilityFunction.postInstagram(MainActivity.this, "Hello", editedImageUri);
                //UtilityFunction.postSnapChat(MainActivity.this, "Hello", editedImageUri);
                //UtilityFunction.postPinterest(MainActivity.this, "Hello", editedImageUri);

            }
        });
    }

    public Bitmap takeScreenshot(View view) {
        View rootView = view.getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache(true);
        Bitmap b1 = Bitmap.createBitmap(rootView.getDrawingCache(true));
        rootView.setDrawingCacheEnabled(false); // clear drawing cache
        return b1;
    }

    public void saveBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagePath1 = new File(pix, "screenshot.jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath1);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }


    protected void callForecastApi(){

        ForecastService.ForecastRx forecast = ForecastService.createRx();

        latitude = "-73.723975";
        longitude = "-66.215334";

        Timber.d("PRINT LATITUDE AND LONGITUDE " + latitude + " " + longitude);
        if(latitude.equals("0.0") & longitude.equals("0.0")){
            latitude = "Nothing";
            longitude = "Nothing";
        }
        String latLong = latitude+","+longitude;

        String forecastApiKey = getResources().getString(R.string.forecast_api);

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

                        snowFallTextView.setText(weather.getCurrently().getPrecipType() + " " + String.valueOf(weather.getCurrently().getPrecipIntensity()));
                        temperatureTextView.setText(String.valueOf(weather.getCurrently().getApparentTemperature()));
                        elevationTextView.setText("ELEVATION");
                        locationTextView.setText(weather.getHourly().getSummary().toString());
                        locationDetailTextView.setText(addressOutput);

                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            switch(requestCode){
                case Constants.TAKE_PICTURE:
                    Intent imageEditorIntent = new AdobeImageIntent.Builder(this).setData(imageUri).build();
                    startActivityForResult(imageEditorIntent, Constants.GET_EDIT_PICTURE);
                    break;
                case Constants.GET_EDIT_PICTURE:
                    editedImageUri = data.getData();
                    photoImage.setImageURI(editedImageUri);
                    photoImage.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;
                default: break;
            }
        }

    }






// ------- Get Location ---------//

    private void setGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void checkLocationPermissions() {
        //Ask for permission if we don't have it
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    Constants.PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            // Permissions Granted
            Log.d(TAG, "Permissions Granted");
            getLatLongCoordinates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSION_ACCESS_COARSE_LOCATION:
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
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }





// ----- Initialization Stuff -----////


    private void initializeViews() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
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
        photoImage = (ImageView) findViewById(R.id.photo_imageView);
        shareFAB = (FloatingActionButton) findViewById(R.id.fab);
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




// -------- CHECK INTERNET CONNECTION -----//

    @Override
    public void onConnected(Bundle connectionHint) {
        if(CheckInternetConnection.isNetworkAvailable(MainActivity.this)){
            Log.d(TAG, "Network Available");
            getLatLongCoordinates();
        }else{
            Log.d(TAG, "Internet Not Connected");
        }

        if(lastLocation != null){
            Log.d(TAG, "lastlocation not null");
            startFetchAddressIntentService();
        }else{
            Log.d(TAG, "lastlocation null");
        }
    }

    protected void startFetchAddressIntentService() {
        Intent fetchAddressIntent = new Intent(this, FetchAddressIntentService.class);
        fetchAddressIntent.putExtra(Constants.RECEIVER, resultReceiver);
        fetchAddressIntent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(fetchAddressIntent);
    }

    private void getReceiverAddress(){
        resultReceiver = new AddressResultReceiver(new Handler());
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
            addressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.d(TAG, "ADDRESS: " + addressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "ADDRESS FOUND: "+getString(R.string.address_found));
            }

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_camera:
                Log.d(TAG, "Camera Clicked");
                verifyStoragePermissions(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
