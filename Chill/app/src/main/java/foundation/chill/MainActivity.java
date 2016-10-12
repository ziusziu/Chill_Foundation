package foundation.chill;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.adobe.creativesdk.aviary.AdobeImageIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import foundation.chill.model.DistanceUnit;
import foundation.chill.model.forecast.Weather;
import foundation.chill.provider.ForecastService;
import foundation.chill.utilities.AltimatePrefs;
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
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        ,LocationListener, SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private static Location lastLocation;
    private AddressResultReceiver resultReceiver;

    private static String latitude;
    private static String longitude;

    FloatingActionButton shareFAB;
    Button color1Button, color2Button, color3Button, color4Button;
    TextView snowFallTextView, temperatureTextView, elevationTextView, weatherSummaryTextView,
            locationTextView;
    LinearLayout logoImagesLayout;

    Animation bounceRightToLeftAnimation;

    String addressOutput;

    ImageView photoImageView;
    Uri imageUri;
    Uri editedImageUri;

    Toolbar toolbar;
    ActionBar actionBar;

    PrintHelper photoPrinter;

    String screenshotId = "";
    String pictureId = "";

    private DistanceUnit distanceUnit;
    private double myBasePressure;
    private double basePressureCoefficient = 0.000986923;
    double altitude_ft_zero;

    /** Sensor objects */
    private SensorManager sensorManager;
    private Sensor pressure;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setGoogleApiClient();
        googleApiClient.connect();

        initSensorManager();
        initPrinterHepler();

        initializeViews();
        initActionBar();
        initColorButtons();

        setImageViewClickListener();
        setFabClickListenter();

        getReceiverAddress();
        initAnimations();
    }



    //================================================================================
    // Image Methods
    //================================================================================

    /**
     *
     * Set long click listener on ImageView.
     * Long click will
     * 1) Convert image to bitmap
     * 2) Store image to Content Provider URI
     * 3) Start intent to Adobe Image Editor
     *
     */
    private void setImageViewClickListener(){
        photoImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Bitmap bitmap = ((BitmapDrawable) photoImageView.getDrawable()).getBitmap();
                editedImageUri = getImageUri(getApplicationContext(), bitmap);

                Intent imageEditorIntent = new AdobeImageIntent
                                                .Builder(getApplicationContext())
                                                .setData(editedImageUri)
                                                .build();
                startActivityForResult(imageEditorIntent, Constants.GET_EDIT_PICTURE);
                return false;
            }
        });
    }

    /**  CAN GO TO UTILITIES
     *
     * Takes Bitmap, stores and returns a Content Provider Uri
     * Will delete the previously stored image in Content URI before returning new URI
     *
     * @param context
     * @param photoImage
     * @return
     *
     */
    public Uri getImageUri(Context context, Bitmap photoImage) {

        // Check for previous URI and delete
        if(!screenshotId.equals("")){
            int rowsDeleted = getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media._ID + "=?",
                    new String[]{screenshotId}
            );
        }


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        photoImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                                            photoImage,
                                                            "Photo",
                                                            null);
        Uri imageUri = Uri.parse(path);
        screenshotId = imageUri.getLastPathSegment();
        return imageUri;
    }

    /** CAN GO TO UTILITIES
     *
     * Check permissions for writing to external storage
     * Continue to take photo if permissions granted
     *
     * @param activity
     *
     */
    public void verifyStoragePermissions(Activity activity){
        int permission = ActivityCompat
                        .checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                                                Constants.PERMISSIONS_STORAGE,
                                                Constants.REQUEST_EXTERNAL_STORAGE);
        }else{
            // Permission Granted, Take Photo
            imageUri = UtilityFunction.takePhoto(activity);
        }
    }

    /** CAN GO TO UTILITIES
     *
     * Take the image from view id and return a bitmap
     *
     * @param view
     * @return
     *
     */
    public Bitmap takeScreenshot(View view) {
        View rootView = view.getRootView().findViewById(R.id.photo_container);
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache(true);
        Bitmap b1 = Bitmap.createBitmap(rootView.getDrawingCache(true));
        rootView.setDrawingCacheEnabled(false); // clear drawing cache
        return b1;
    }

    /** CAN GO TO UTILITIES, MAKE IMAGE NAME A GLOBAL VARIABLE
     *
     * Saves image to DIRECTORY_PICTURES with name screenshot.jpg
     *
     * @param bitmap
     *
     */
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


    /** CAN COMBINE WIT saveBitmap(Bitmap bitmap) to return URI
     *
     * Gets the uri of saved image
     *
     * @return
     *
     */
    public Uri getScreenshotFileUri(){
        File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagePath1 = new File(pix, "screenshot.jpg");
        return Uri.fromFile(imagePath1);
    }


    /**
     *
     * setFab to take screenshot and send image to Instagram
     *
     */
    private void setFabClickListenter(){
        shareFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap(takeScreenshot(view));
                Uri imagePathFileUri = getScreenshotFileUri();

                //UtilityFunction.sendTweet(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postTumblr(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                UtilityFunction.postInstagram(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postSnapChat(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postPinterest(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);

            }
        });
    }

    /**
     *
     * Move item in provided to Content Provider
     * 1) Deletes previous item in Content Provider from app
     * 2) Returns new Content Provider URI after item inserted
     *
     * @param uri
     * @return
     */
    private Uri moveToContentProvider(Uri uri){
        if(!pictureId.equals("")){
            // Delete previous image stored in URI
            Log.d(TAG, "URI: pictureId " + pictureId);
            int rowsDeleted = getContentResolver().delete(
                                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                    MediaStore.Images.Media._ID + "=?",
                                                    new String[]{pictureId}
                                                    );
            Log.d(TAG, "URI: pictureId rows deleted" + rowsDeleted);
        }
        try{
            Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), uri.getPath(), null, null));
            // Update previous URI Id
            pictureId = newUri.getLastPathSegment();
            return newUri;
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return uri;
    }

    /** COMBINE 3) and 4)
     *
     * Handle results from different requests
     * 1) Take Photo and Adobe Edit Picture
     * 2) Loading image
     * 3) Handle GPS NOT CONNECTED, okay clicked
     * 4) Handle GSP NOT CONNECTED, cancel clicked
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Timber.d("-----onActivityResult START ------- RESULT CODE: " + resultCode);
        Timber.d("-----onActivityResult START ------- REQUEST CODE: " + requestCode);
        if(resultCode == RESULT_OK){
            switch(requestCode){
                case Constants.TAKE_PICTURE:
                    Intent imageEditorIntent = new AdobeImageIntent.Builder(this).setData(imageUri).build();
                    startActivityForResult(imageEditorIntent, Constants.GET_EDIT_PICTURE);
                    break;
                case Constants.GET_EDIT_PICTURE:
                    editedImageUri = data.getData();
                    Log.d(TAG, "Picture Take " + editedImageUri);


                    editedImageUri = moveToContentProvider(editedImageUri);
//                    String id = editedImageUri.getLastPathSegment();
//                    Log.d(TAG, "URI: Picture Take moved to content provider " + editedImageUri);
//                    Timber.d("URI: Picture Take Mediastore " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    Timber.d("URI: Printer ID " + id);
//
//                    ContentValues test_values = new ContentValues();
//                    String newPath = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "41070").toString();
//                    Timber.d("URI: Printer new Path " + newPath);
//                    test_values.put(MediaStore.Images.Media.DATA, newPath);
//
//                    String sIMAGE_ID = editedImageUri.toString();
//                    Timber.d("URI: Printer old Path " + sIMAGE_ID);
//                    Timber.d("URI: Printer MediaColumns " + MediaStore.MediaColumns.DATA);
//
//
//                    int res = getContentResolver().update(editedImageUri, test_values, null, null);//, MediaStore.MediaColumns.DATA + "='" + sIMAGE_ID + "'", null);
//
//                    Timber.d("URI: Printer update " + res);
                    photoImageView.setImageURI(editedImageUri);
                    photoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;
                default: break;
            }

            if (requestCode == Constants.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                photoImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            }

            if(requestCode == Constants.RESULT_GPS_NOT_CONNECT && resultCode == RESULT_OK){
                Timber.d("resolution code 1000");
                checkLocationPermissions();
            }

            if(resultCode == RESULT_CANCELED && requestCode == Constants.RESULT_GPS_NOT_CONNECT){
                Timber.d("Result Cancelled, GPS Not connected");
                logoImagesLayout.setVisibility(View.VISIBLE);
                // loadAnimations();
            }
        }
        Timber.d("-----onActivityResult END -------");
    }




    //================================================================================
    // Weather API
    //================================================================================

    /**
     *
     * Use RxJava to makeRequest to API
     * 1) Update text with response
     * 2) Load Text Animations
     *
     */
    protected void callForecastApi(){

        ForecastService.ForecastRx forecast = ForecastService.createRx();

        Timber.d("PRINT LATITUDE AND LONGITUDE " + latitude + " " + longitude);
        if(latitude.equals("0.0") & longitude.equals("0.0")){
            latitude = "Nothing";
            longitude = "Nothing";
        }
        String latLong = latitude+","+longitude;

        Timber.d("------ API LOCATION START------");
        Timber.d("FORECASTAPI: Latitude " + latitude);
        Timber.d("FORECASTAPI: Longitude " + longitude);
        Timber.d("------ API LOCATION END------");

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
                        Timber.d("----- UPDATE TEXTVIEW START -------");


                        double currentTemp = weather.getCurrently().getTemperature();
                        String precipTypeCurrent = weather.getCurrently().getPrecipType();
                        String weatherSummary = weather.getHourly().getSummary();

                        if(precipTypeCurrent == null){
                            Timber.d(" --- NO PRECIPITATION----");
                            snowFallTextView.setText("");
                            snowFallTextView.setVisibility(View.GONE);
                        }else{
                            String precipTypeCurrentCapFirst = precipTypeCurrent.substring(0,1).toUpperCase() + precipTypeCurrent.substring(1);
                            double precipIntensity = weather.getCurrently().getPrecipIntensity();
                            snowFallTextView.setText(precipTypeCurrentCapFirst + ": " + String.valueOf(precipIntensity));
                        }

                        temperatureTextView.setText(String.valueOf(currentTemp)+"\u00B0"+"C");
                        elevationTextView.setText("ELEVATION");
                        locationTextView.setText(addressOutput);
                        weatherSummaryTextView.setText(weatherSummary);

                        //loadAnimations();
                        Timber.d("---- UPDATE TEXTVIEW END --------");
                    }
                });
        loadAnimations();
    }




    //================================================================================
    // Permissions Methods
    //================================================================================

    /**
     *
     * Check permissions to access location,
     * 1) Ask for location permissions if none
     * 2) Get phone's lat, long if permisisons granted
     *
     */
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
            Timber.d("------- checkLocationPermissions Permission Granted START-------");
            // Permissions Granted
            Log.d(TAG, "Permissions Granted");
            getLatLongCoordinates();
            Timber.d("------- checkLocationPermissions Permission Granted END-------");
        }
    }


    /** SEEMS REDUNDANT, MIGHT BE ABLE TO REMOVE OR COMBINE
     *
     * Handle requests to checking permission
     * 1) Get Lat Long if permission given
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

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

    /** PERMISSIONS AND API CONNECTION CODE VERY CONVOLUTED, NEED TO SIMPLIFY
     *
     * Check for permissions
     * 1) Get Lat and Long from phone
     *
     */
    private void getLatLongCoordinates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Timber.d("------- getLatLongCoordinates    lastlocation != null   START------");
                latitude = String.valueOf(lastLocation.getLatitude());
                longitude = String.valueOf(lastLocation.getLongitude());
                Log.d(TAG, "Latitude: "+String.valueOf(lastLocation.getLatitude()));
                Log.d(TAG, "Longitude: "+String.valueOf(lastLocation.getLongitude()));
                startFetchAddressIntentService();
                callForecastApi();
//                SharedPreferences pref = getApplicationContext().getSharedPreferences("lastLocation", MODE_PRIVATE);
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putString("lastLat", String.valueOf(lastLocation.getLatitude()));
//                editor.putString("lastLong", String.valueOf(lastLocation.getLongitude()));
//                editor.commit();
                Timber.d("------- getLatLongCoordinates    lastlocation != null   END------");
            }
            else {
                Timber.d("------- getLatLongCoordinates    lastlocation == null   START------");
                // ---- Crashes, error is googleapiclient not connected --- ////
                if(googleApiClient.isConnected()){
                    Log.d(TAG, "GOOGLE API CONNECTED");
                    locationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                            .setFastestInterval(1 * 1000);

                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                }else{

                    Log.d(TAG, "GOOGLE API NOT CONNECTED");
//                    SharedPreferences pref = getSharedPreferences("lastLocation", MODE_PRIVATE);
//                    String Lat = pref.getString("lastLat", "29.764822");
//                    String Long = pref.getString("lastLong", "-95.372206");
//                    Log.d(TAG, "Pref Lat: " + Lat);
//                    Log.d(TAG, "Pref Long: " + Long);
//                    lastLocation.setLatitude(Double.valueOf(Lat));
//                    lastLocation.setLongitude(Double.valueOf(Long));
                }
                Timber.d("------- getLatLongCoordinates    lastlocation == null   END------");
            }

        }
    }



    //================================================================================
    // Google Maps API & Sensor Connection Methods
    //================================================================================

    /**
     *
     * Set Google API Client if there is none
     *
     */
    private void setGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    /**
     *
     * Connect the google api client
     * Google api client does not have to be built prior to connect
     *
     */
    @Override
    protected void onStart() {
        Timber.d("----- OnStart -----");
        googleApiClient.connect();
        requestLocationStatus();
        super.onStart();
    }

    /**
     *
     * Request location of phone immediately
     *
     */
    private void requestLocationStatus(){
        Timber.d("------- RequestLocationStatus START ----------");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        Timber.d("------ Location Status Success START--------");
                        checkLocationPermissions();
                        Timber.d("------ Location Status Success END--------");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Timber.d("------ Location Resolution Required START--------");
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, Constants.RESULT_GPS_NOT_CONNECT);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        Timber.d("------ Location Resolution Required END--------");
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Timber.d("------ Location Settings Change Unavailable START--------");
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        Timber.d("------ Location Settings Change Unavailable END--------");
                        break;
                }
            }
        });
        Timber.d("------- RequestLocationStatus END ----------");
    }


    /**
     *
     * When location changes
     * 1) Use new lat long to get address
     * 2) Make forecast api call with new lat long
     *
     * @param location
     *
     */
    @Override
    public void onLocationChanged(Location location) {
        if(googleApiClient.isConnected()){
            Log.d(TAG, "lastLocation latitdue"+location.getLatitude());
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Timber.d("-------- Location Changed START -------");
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                lastLocation.setLatitude(location.getLatitude());
                lastLocation.setLongitude(location.getLongitude());
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                startFetchAddressIntentService();
                callForecastApi();
                Timber.d("-------- Location Changed END -------");
            }
        }
    }


    /**
     *
     * Register sensor listener on resume
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(MainActivity.this, pressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     *
     * Disconnect google api client on stop
     *
     */
    @Override
    protected void onStop() {
        Timber.d(" ------- onStop -----");
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     *
     * Unregister sensor on pause
     *
     */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(MainActivity.this);
    }


    //================================================================================
    // Initialization Methods
    //================================================================================

    /**
     *
     * Initialize views for
     * 1) Toolbar
     * 2) Buttons
     * 3) TextViews
     * 4) Layouts
     *
     */
    private void initializeViews() {
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        color1Button = (Button) findViewById(R.id.color1_button);
        color2Button = (Button) findViewById(R.id.color2_button);
        color3Button = (Button) findViewById(R.id.color3_button);
        color4Button = (Button) findViewById(R.id.color4_button);
        snowFallTextView = (TextView) findViewById(R.id.snowfall_textView);
        temperatureTextView = (TextView) findViewById(R.id.temperature_textView);
        elevationTextView = (TextView) findViewById(R.id.elevation_textView);
        weatherSummaryTextView = (TextView) findViewById(R.id.weatherSummary_textView);
        locationTextView = (TextView) findViewById(R.id.location_textView);
        photoImageView = (ImageView) findViewById(R.id.photo_imageView);
        shareFAB = (FloatingActionButton) findViewById(R.id.fab);
        logoImagesLayout = (LinearLayout) findViewById(R.id.logo_linearLayout);
    }

    /**
     *
     * Call click listeners on all the buttons
     *
     */
    private void initColorButtons() {
        Timber.d("------- Init Color Button Listeners START------");
        setButtonOnClickListener(color1Button);
        setButtonOnClickListener(color2Button);
        setButtonOnClickListener(color3Button);
        setButtonOnClickListener(color4Button);
        Timber.d("------- Init Color Button Listeners END------");
    }

    /**
     *
     * Change text color on button click
     *
     * @param button
     *
     */
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

    /**
     *
     * Change text color to input color
     *
     * @param color
     */
    private void setTextColor(int color) {
        snowFallTextView.setTextColor(color);
        temperatureTextView.setTextColor(color);
        elevationTextView.setTextColor(color);
        weatherSummaryTextView.setTextColor(color);
        locationTextView.setTextColor(color);
    }

    /**
     *
     * Get the base pressures for elevation calucations
     *
     */
    private void initSensorManager(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        distanceUnit = AltimatePrefs.getUnitPreference(MainActivity.this);

        float basePressure = AltimatePrefs.getBasePressure(MainActivity.this);
        if(basePressure != 0f) {
            myBasePressure = AltimatePrefs.getBasePressure(MainActivity.this);
            basePressureCoefficient = 1.0 / myBasePressure;
        }
    }

    /**
     *
     * Initialize printer helper
     *
     */
    private void initPrinterHepler(){
        photoPrinter = new PrintHelper(MainActivity.this);
    }

    /**
     *
     * Initialize and set title of Action Bar
     *
     */
    private void initActionBar(){
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chill Foundation");
    }

    /**
     *
     * Initialize Bouncing Animation
     *
     */
    private void initAnimations(){
        bounceRightToLeftAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate);
    }

    /**
     *
     * Load animations to text views
     *
     */
    private void loadAnimations(){
        snowFallTextView.startAnimation(bounceRightToLeftAnimation);
        temperatureTextView.startAnimation(bounceRightToLeftAnimation);
        elevationTextView.startAnimation(bounceRightToLeftAnimation);
        weatherSummaryTextView.startAnimation(bounceRightToLeftAnimation);
        locationTextView.startAnimation(bounceRightToLeftAnimation);
        logoImagesLayout.startAnimation(bounceRightToLeftAnimation);
    }



    //================================================================================
    // Address results
    //================================================================================


    /** MOVE TO ANOTHER LOCATION
     *
     * Start intent to fetch address
     *
     */
    protected void startFetchAddressIntentService() {
        Intent fetchAddressIntent = new Intent(this, FetchAddressIntentService.class);
        fetchAddressIntent.putExtra(Constants.RECEIVER, resultReceiver);
        fetchAddressIntent.putExtra(Constants.LOCATION_DATA_EXTRA, lastLocation);
        startService(fetchAddressIntent);
    }

    /** USE SINGLETON LIKE GOOGLE API CLIENT
     *
     * Instantiate receiver for address
     *
     */
    private void getReceiverAddress(){
        resultReceiver = new AddressResultReceiver(new Handler());
    }

    /**
     *
     * Handle results to address receiver
     *
     */
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

    /**
     *
     * Currently does nothing
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {

        if(CheckInternetConnection.isNetworkAvailable(MainActivity.this)){
            Timber.d("-------- OnConnected   network Available  START -------");
            Log.d(TAG, "Network Available");
            //getLatLongCoordinates();
            //checkLocationPermissions();
            Timber.d("-------- OnConnected   network Available  END -------");
        }else {
            Log.d(TAG, "Internet Not Connected");
//            if (lastLocation != null) {
//                Timber.d("-------- OnConnected   lastlocation != null  START -------");
//                Log.d(TAG, "lastlocation not null");
//                startFetchAddressIntentService();
//                callForecastApi();
//                Timber.d("-------- OnConnected   lastlocation != null  END -------");
//            } else {
//                Log.d(TAG, "lastlocation null");
//                checkLocationPermissions();
//            }
        }
    }

    /**
     *
     * Currently does nothing
     *
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     *
     * Currently do nothing
     *
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    //================================================================================
    // Toolbar Menu Methods
    //================================================================================

    /**
     *
     * Inflate menu with icons with new color
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        for(int iconIndex = 0; iconIndex < menu.size(); iconIndex++){
            Drawable drawable = menu.getItem(iconIndex).getIcon();
            if(drawable != null) {
                drawable.mutate();
                int menuIconColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
                drawable.setColorFilter(menuIconColor, PorterDuff.Mode.SRC_ATOP );
            }
        }

        return true;
    }

    /**
     *
     * Handle clicks to menu icons
     * 1) Take Photo
     * 2) Load from gallery
     * 3) Print screenshot
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_camera:
                Log.d(TAG, "Camera Clicked " + editedImageUri);
                verifyStoragePermissions(MainActivity.this);
                return true;
            case R.id.action_print:

                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                try {
                    
                    saveBitmap(takeScreenshot(findViewById(R.id.photo_container)));

                    Uri imagePathPrinterFileUri = getScreenshotFileUri();


                    Uri printerEditedImageUri = moveToContentProvider(imagePathPrinterFileUri);
                    String id = printerEditedImageUri.getLastPathSegment();

                    Log.d(TAG, "URI: Printer Clicked " + printerEditedImageUri);
                    Timber.d("URI: Printer " + MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Timber.d("URI: Printer ID" + id);

                    InputStream is = getContentResolver().openInputStream(printerEditedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    photoPrinter.printBitmap("Image", bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            case R.id.action_photo_library:
                Intent photoLibraryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(photoLibraryIntent, Constants.RESULT_LOAD_IMAGE);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //================================================================================
    // Sensor Methods
    //================================================================================

    /**
     *
     * Recalculate elevation on sensor change
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        final double current_millibars_of_pressure = event.values[0];
        final double adjust_pressure = current_millibars_of_pressure * basePressureCoefficient;

        /**implement simplified equation for pressure/altitude */
        final double altitude_ft = (1 - (Math.pow((adjust_pressure), 0.190284))) * 145366.45;


        double altitude_ft_calibrated = altitude_ft - altitude_ft_zero;
        long altitude_ft_round = Math.round(altitude_ft_calibrated);
        String altitude_string_ft = Long.toString(altitude_ft_round);

        Log.d(TAG, altitude_string_ft);
        Log.d(TAG, Double.toString(current_millibars_of_pressure));

        switch (distanceUnit) {
            case FEET:
                elevationTextView.setText(altitude_string_ft + " " + distanceUnit.getShortFormValue());
                break;
            case METERS:
                //1 meter = 3.28084 ft
                double altitude_m = 0.3047999902464 * altitude_ft_calibrated;
                long altitude_m_round = Math.round(altitude_m);
                String altitude_string_m = Long.toString(altitude_m_round);
                elevationTextView.setText(altitude_string_m + " " + distanceUnit.getShortFormValue());
                break;
        }

        Log.d(TAG, "expected altitude value " + altitude_string_ft + " millibars of pressure " + current_millibars_of_pressure);



    }

    /**
     *
     * Currently does nothing
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
