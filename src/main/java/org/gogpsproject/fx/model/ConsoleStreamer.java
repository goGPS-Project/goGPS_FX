package org.gogpsproject.fx.model;

import java.util.logging.Logger;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.PositionConsumer;
import org.gogpsproject.RoverPosition;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.Leaflet;

import net.java.html.leaflet.Icon;
import net.java.html.leaflet.IconOptions;
import net.java.html.leaflet.LatLng;
import net.java.html.leaflet.Marker;
import net.java.html.leaflet.MarkerOptions;

public class ConsoleStreamer implements StreamEventListener, PositionConsumer{
 
  private static final Logger logger = Logger.getLogger(ConsoleStreamer.class.getName());

  GoGPSModel model;
  
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
    
    Leaflet.ctx.execute(new Runnable(){
      @Override
      public void run() {
        // it doesn't work String to int class cast ex
//        int z = Leaflet.map.getMaxZoom();

        Icon icon = new Icon(new IconOptions("leaflet-0.7.2/images/marker-icon.png"));
        LatLng ll = new LatLng( coord.getGeodeticLatitude(), coord.getGeodeticLongitude() );
        Marker m = new Marker(ll, new MarkerOptions().setIcon(icon));
        m.addTo(Leaflet.map);
        Leaflet.map.setView(ll, 20);
      }
    });
  }

  @Override
  public void event(int event) {
  }

}