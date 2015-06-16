package org.gogpsproject.model;

import org.gogpsproject.GoGPS;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Mode", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "value", type = int.class )
})
public class Modes {
  
//  public static Mode realTime = new Mode("Real-time");
//  public static Mode postProcessing = new Mode("Post-processing");
//  public static Mode navigation = new Mode("Navigation");
//  public static Mode roverMonitor = new Mode("Rover Monitor");
//  public static Mode masterMonitor = new Mode("Master Monitor");
//  public static Mode roverMasterMonitor = new Mode("Rover and Master Monitor");
//  public static Mode leastSquares = new Mode("Least squares");

  public static Mode standAlone     = new Mode("Stand-alone",   GoGPS.RUN_MODE_STANDALONE);
  public static Mode doubleDifferences         = new Mode("Code double difference", GoGPS.RUN_MODE_DOUBLE_DIFF);
  public static Mode kalmanFilter   = new Mode("Kalman filter", GoGPS.RUN_MODE_KALMAN_FILTER);
  
//  public static Mode codePhaseStandAlone = new Mode("Code and phase stand-alone");
//  public static Mode codePhaseDD = new Mode("Code and phase double difference");

}
