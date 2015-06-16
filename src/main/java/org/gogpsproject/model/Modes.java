package org.gogpsproject.model;

import org.gogpsproject.GoGPS;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Mode", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "value", type = int.class )
})
public class Modes {
  public static Mode standAlone     = new Mode("Stand-alone",   GoGPS.RUN_MODE_STANDALONE);
  public static Mode doubleDifferences         = new Mode("Double difference", GoGPS.RUN_MODE_DOUBLE_DIFF);
  public static Mode kalmanFilter   = new Mode("Kalman filter", GoGPS.RUN_MODE_KALMAN_FILTER);
}
