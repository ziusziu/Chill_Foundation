package foundation.chill.model;

/**
 * Created by jeanweatherwax on 4/25/15.
 */
public enum DistanceUnit {

  FEET("ft"), METERS("m");

  String shortFormValue;

  DistanceUnit(String shortFormValue) {
    this.shortFormValue = shortFormValue;
  }

  public String getShortFormValue() {
    return shortFormValue;
  }

}
