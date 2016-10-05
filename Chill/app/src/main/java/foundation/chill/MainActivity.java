package foundation.chill;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
    LinearLayout logoImagesLayout;

    Animation bounceRightToLeftAnimation;

    String addressOutput;

    ImageView photoImageView;
    Uri imageUri;
    Uri editedImageUri;

    Toolbar toolbar;
    ActionBar actionBar;

    PrintHelper photoPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        photoPrinter = new PrintHelper(MainActivity.this);

        initializeViews();
        initActionBar();
        initColorButtons();

        setImageViewClickListener();
        setFabClickListenter();

        setGoogleApiClient();
        checkLocationPermissions();
        getReceiverAddress();

        callForecastApi();

        initAnimations();
        loadAnimations();

    }

    private void initActionBar(){
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Chill Foundation");
    }

    private void initAnimations(){
        bounceRightToLeftAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate);
    }

    private void loadAnimations(){
        snowFallTextView.startAnimation(bounceRightToLeftAnimation);
        temperatureTextView.startAnimation(bounceRightToLeftAnimation);
        elevationTextView.startAnimation(bounceRightToLeftAnimation);
        locationTextView.startAnimation(bounceRightToLeftAnimation);
        locationHyphenTextView.startAnimation(bounceRightToLeftAnimation);
        locationDetailTextView.startAnimation(bounceRightToLeftAnimation);
        logoImagesLayout.startAnimation(bounceRightToLeftAnimation);
    }


    private void setImageViewClickListener(){

        Bitmap bitmap = ((BitmapDrawable) photoImageView.getDrawable()).getBitmap();
        editedImageUri = getImageUri(getApplicationContext(), bitmap);

        photoImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "Photot Clicked" + editedImageUri);
                Intent imageEditorIntent = new AdobeImageIntent.Builder(getApplicationContext()).setData(editedImageUri).build();
                startActivityForResult(imageEditorIntent, Constants.GET_EDIT_PICTURE);
                //verifyStoragePermissions(MainActivity.this);
                return false;
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
                Log.d(TAG, "Send Clicked " + editedImageUri);

                saveBitmap(takeScreenshot(view));
                Uri imagePathFileUri = getScreenshotFileUri();

                Log.d(TAG, "Share Image uri " + imagePathFileUri);
                //UtilityFunction.sendTweet(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postTumblr(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                UtilityFunction.postInstagram(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postSnapChat(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);
                //UtilityFunction.postPinterest(MainActivity.this, "#Chill#ChillFoundation", imagePathFileUri);

            }
        });
    }



    public Bitmap takeScreenshot(View view) {
        View rootView = view.getRootView().findViewById(R.id.photo_container);
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

    public Uri getScreenshotFileUri(){
        File pix = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imagePath1 = new File(pix, "screenshot.jpg");
        return Uri.fromFile(imagePath1);
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
                    Log.d(TAG, "Picture Take " + editedImageUri);

                    editedImageUri = moveToContentProvider(editedImageUri);

                    Log.d(TAG, "Picture Take moved to content provider " + editedImageUri);
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

        }

    }


    private Uri moveToContentProvider(Uri uri){
        try{
            return Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), uri.getPath(), null, null));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return uri;
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
        photoImageView = (ImageView) findViewById(R.id.photo_imageView);
        shareFAB = (FloatingActionButton) findViewById(R.id.fab);
        logoImagesLayout = (LinearLayout) findViewById(R.id.logo_linearLayout);
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

                    Log.d(TAG, "Printer Clicked " + printerEditedImageUri);

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
}
