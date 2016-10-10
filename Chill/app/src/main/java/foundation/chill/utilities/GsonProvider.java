package foundation.chill.utilities;

import com.google.gson.Gson;

/**
 * Created by brendanweinstein on 5/16/15.
 */
public class GsonProvider {

  private static final Gson INSTANCE = new Gson();

  public static Gson get() {
    return INSTANCE;
  }

}
