package foundation.chill.utilities;

import android.Manifest;

/**
 * Created by samsiu on 8/2/16.
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME =
            "foundation.chill";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME +
            ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
            ".LOCATION_DATA_EXTRA";


    public static final int PERMISSION_ACCESS_COARSE_LOCATION = 22;
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static final int TAKE_PICTURE = 1;
    public static final int GET_EDIT_PICTURE = 2;

    public static final int RESULT_LOAD_IMAGE = 3;
    public static final int RESULT_GPS_NOT_CONNECT = 1000;

}