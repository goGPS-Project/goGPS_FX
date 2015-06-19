package org.gogpsproject.fx.model;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import org.gogpsproject.Observations;
import org.gogpsproject.SatellitePosition;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
import org.gogpsproject.parser.rinex.RinexNavigationParser;

public class RinexNavigationStreamEventProducer extends RinexNavigationParser implements StreamEventProducer {

  private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();

  public RinexNavigationStreamEventProducer(File fileNav) {
    super(fileNav);
  }

  public RinexNavigationStreamEventProducer(InputStream is, File cache) {
    super(is, cache);
  }

  @Override
  public void addStreamEventListener(StreamEventListener streamEventListener) {
    if(streamEventListener==null) return;
    if(!streamEventListeners.contains(streamEventListener))
      this.streamEventListeners.add(streamEventListener);
  }

  @Override
  public Vector<StreamEventListener> getStreamEventListeners() {
    return (Vector<StreamEventListener>)streamEventListeners.clone();
  }

  @Override
  public void removeStreamEventListener(StreamEventListener streamEventListener) {
    if(streamEventListener==null) return;
    if(streamEventListeners.contains(streamEventListener))
      this.streamEventListeners.remove(streamEventListener);
  }
  
  public SatellitePosition getGpsSatPosition(Observations o, int satID, char satType, double receiverClockError){
    if(streamEventListeners!=null && o!=null){
      for(StreamEventListener sel:streamEventListeners){
        Observations oc = (Observations)o.clone();
        sel.addObservations(oc);
      }
    }
    if( o==null )
      for(StreamEventListener sel:streamEventListeners){
        sel.streamClosed();
      }
    return super.getGpsSatPosition(o, satID, satType, receiverClockError );
  }

}

