package org.gogpsproject;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;

public class ConsoleStreamer implements StreamEventListener{

    @Override
    public void streamClosed() {
      System.out.println("streamClosed");
    }

    @Override
    public void addObservations(Observations o) {
      System.out.println("addObservations");
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
    public Observations getCurrentObservations() {
      System.out.println("streamClosed");
      return null;
    }

    @Override
    public void pointToNextObservations() {
      System.out.println("pointToNextObservations");
    }
  }