package org.gogpsproject.fx.model;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.ObservationSet;
import org.gogpsproject.StreamEventListener;

public class ConsoleStreamer implements StreamEventListener{
  
  GoGPSModel model;
  
  public ConsoleStreamer( GoGPSModel model ){
    this.model = model;
  }
  
  @Override
  public void streamClosed() {
    System.out.println("streamClosed");
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
    System.out.println( "Iono" + iono.toString());
  }

  @Override
  public void addEphemeris(EphGps eph) {
    System.out.println("Eph" + eph.toString());
  }

  @Override
  public void setDefinedPosition(Coordinates definedPosition) {
    System.out.println("setDefinedPosition");
  }

  @Override
  public org.gogpsproject.Observations getCurrentObservations() {
    System.out.println("streamClosed");
    return null;
  }

  @Override
  public void pointToNextObservations() {
    System.out.println("pointToNextObservations");
  }

}