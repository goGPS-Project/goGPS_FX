package org.gogpsproject.fx.model;

import java.io.File;
import java.util.Vector;

import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
import org.gogpsproject.parser.rinex.RinexObservationParser;

public class RinexObservationParserStreamEventProducer extends RinexObservationParser implements StreamEventProducer {

  private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();

  public RinexObservationParserStreamEventProducer(File fileObs) {
    super(fileObs);
  }

  public RinexObservationParserStreamEventProducer(File fileObs,
      Boolean[] multiConstellation) {
    super(fileObs, multiConstellation);
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
  
  @Override
  public Observations getNextObservations() {
    Observations o = super.getNextObservations();
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

    return o;
  }
}
