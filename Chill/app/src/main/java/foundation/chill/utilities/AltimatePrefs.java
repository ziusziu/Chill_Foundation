package foundation.chill.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import foundation.chill.model.DistanceUnit;

/**
 * Created by jeanweatherwax on 4/25/15.
 */
public class AltimatePrefs {

  private static final String SHARED_PREFS_FILE_NAME = "altimate_prefs";

  private static final String KEY_UNIT_PREFERENCE = "unit_preference";
  private static final String KEY_BASE_PRESSURE = "base_pressure";
  private static final String KEY_LOCATION = "location";

  public static DistanceUnit getUnitPreference(Context context) {
    SharedPreferences prefs = getSharedPreferences(context);
    String val = prefs.getString(KEY_UNIT_PREFERENCE, "FEET");
    DistanceUnit unit = DistanceUnit.valueOf(val);
    return unit;
  }

  public static void setUnitPreference(Context context, String unitPreference) {
    SharedPreferences.Editor editor = getEditor(context);
    editor.putString(KEY_UNIT_PREFERENCE, unitPreference);
    editor.commit();
  }

  public static void setLocation(Context context, Location location) {
    SharedPreferences.Editor editor = getEditor(context);
    String serializedLocation = GsonProvider.get().toJson(location);
    editor.putString(KEY_LOCATION, serializedLocation);
    editor.commit();
  }

  public static Location getLocation(Context context) {
    SharedPreferences prefs = getSharedPreferences(context);
    String serializedLocation = prefs.getString(KEY_LOCATION, null);
    Location location = GsonProvider.get().fromJson(serializedLocation, Location.class);
    return location;
  }

  public static void setBasePressure(Context context, Float basePressure) {
    SharedPreferences.Editor editor = getEditor(context);
    editor.putFloat(KEY_BASE_PRESSURE, basePressure);
    editor.commit();
  }

  public static Float getBasePressure(Context context) {
    SharedPreferences prefs = getSharedPreferences(context);
    Float basePressure = prefs.getFloat(KEY_BASE_PRESSURE, 0f);
    return basePressure;
  }


  /**
   * Utility method for getting an Editor instance
   */
  private static SharedPreferences.Editor getEditor(Context context) {
    return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE).edit();
  }

  /**
   * Utility method for getting a SharedPreferences instance
   */
  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
  }

}
