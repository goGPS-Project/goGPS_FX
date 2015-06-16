package org.gogpsproject.model;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Mode", targetId = "", properties = {
    @Property(name = "name", type = String.class )
})
public class Modes {
  
  public static Mode realTime = new Mode("Real-time");
  public static Mode postProcessing = new Mode("Post-processing");
  public static Mode navigation = new Mode("Navigation");
  public static Mode roverMonitor = new Mode("Rover Monitor");
  public static Mode masterMonitor = new Mode("Master Monitor");
  public static Mode roverMasterMonitor = new Mode("Rover and Master Monitor");
  public static Mode leastSquares = new Mode("Least squares");
  public static Mode kalmanFilter = new Mode("Kalman filter");
  public static Mode codeStandAlone = new Mode("Code stand-alone");
  public static Mode codeDD = new Mode("Code double difference");
  public static Mode codePhaseStandAlone = new Mode("Code and phase stand-alone");
  public static Mode codePhaseDD = new Mode("Code and phase double difference");

}
