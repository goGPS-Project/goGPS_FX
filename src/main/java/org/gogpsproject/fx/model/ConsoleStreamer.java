package org.gogpsproject.fx.model;

import java.util.logging.Logger;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.PositionConsumer;
import org.gogpsproject.RoverPosition;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.fx.Leaflet;

public class ConsoleStreamer implements StreamEventListener, PositionConsumer{
 
  public static final Logger logger = Logger.getLogger(ConsoleStreamer.class.getName());

  GoGPSModel model;
  Leaflet L = Leaflet.get();
  
  public ConsoleStreamer( GoGPSModel model ){
    this.model = model;
  }
  
  @Override
  public void streamClosed() {
    System.out.println("streamClosed");
    try {
      GoGPSDef.stop(model);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  @Override
  public void addObservations( org.gogpsproject.Observations o) {
    model.getSatellites().clear();
    for( int i = 0; i<o.getNumSat(); i++ ){
      int satId = o.getSatID(i);
      ObservationSet os = o.getSatByID(satId);
      SatelliteModel sat = new SatelliteModel(satId, os.getSignalStrength(0), os.getCodeC(0), os.getDoppler(0), os.getCodeP(0) );
      model.getSatellites().add(sat);
    }
  }

  @Override
  public void addIonospheric(IonoGps iono) {
//    System.out.println( "Iono " + iono.toString());
  }

  @Override
  public void addEphemeris(EphGps eph) {
//    System.out.println("Eph " + eph.toString());
  }

  @Override
  public void setDefinedPosition(Coordinates definedPosition) {
    System.out.println("setDefinedPosition: " +  definedPosition );
  }

  @Override
  public org.gogpsproject.Observations getCurrentObservations() {
//    System.out.println("streamClosed");
    return null;
  }

  @Override
  public void pointToNextObservations() {
//    System.out.println("pointToNextObservations");
  }

  @Override
  public void addCoordinate(RoverPosition coord) {
    L.addMarker(coord);
  }

  @Override
  public void event(int event) {
    switch( event ){
    case 0: 
      L.clearMarkers();
//      Leaflet.ctx.execute(new Runnable(){
//        @Override
//        public void run() {
//          Leaflet.map.
//        }
//       });
    break;
    default:
//      logger.info( "ev " + event );
      break;
    }
  }

}