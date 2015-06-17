package org.gogpsproject.model;

import java.util.Arrays;
import java.util.List;

import org.gogpsproject.GoGPS;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Mode", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "value", type = int.class )
})
public class Modes {
  public static Mode standAlone;
  public static Mode doubleDifferences;
  public static Mode kalmanFilter;

  public static List<Mode> get(){
    standAlone     = new Mode("Stand-alone",   GoGPS.RUN_MODE_STANDALONE);
    doubleDifferences         = new Mode("Double difference", GoGPS.RUN_MODE_DOUBLE_DIFF);
    kalmanFilter   = new Mode("Kalman filter", GoGPS.RUN_MODE_KALMAN_FILTER);
    
    return Arrays.asList(new Mode[]{ standAlone, kalmanFilter, doubleDifferences });
  }
}
