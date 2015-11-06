package org.gogpsproject.fx.model;

import java.io.File;
import java.util.Vector;

import org.gogpsproject.Coordinates;
import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.StreamEventProducer;
import org.gogpsproject.parser.rinex.RinexObservationParser;

public class RinexObservationParserStreamEventProducer implements ObservationsProducer, StreamEventProducer {

  private Vector<StreamEventListener> streamEventListeners = new Vector<StreamEventListener>();
  RinexObservationParser parser;
  
  public RinexObservationParserStreamEventProducer( RinexObservationParser parser ) {
    this.parser = parser;
  }

//  public RinexObservationParserStreamEventProducer(File fileObs,
//      Boolean[] multiConstellation) {
//    super(fileObs, multiConstellation);
//  }

  @Override
  public void init() throws Exception {
    parser.init();
  }

  @Override
  public void release(boolean waitForThread, long timeoutMs) throws InterruptedException {
    parser.release(waitForThread, timeoutMs);
  }

  @Override
  public Observations getCurrentObservations() {
    return parser.getCurrentObservations();
  }

  @Override
  public Coordinates getDefinedPosition() {
    return parser.getDefinedPosition();
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
    Observations o = parser.getNextObservations();
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
