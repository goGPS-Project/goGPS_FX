package org.gogpsproject.fx.model;

import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.SerialPortModel;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Producer", targetId = "", properties = {
    @Property(name = "type", type = String.class ),
    @Property(name = "name", type = String.class ),
    @Property(name = "serialPort", type = SerialPortModel.class),
    @Property(name = "filename", type = String.class ),
})
public class Producers {
  
  public static final String SERIAL = "serial";
  public static final String FILE   = "file";
  public static final String FTP    = "ftp";

  public static Producer serialObservationProducer;
  public static Producer rinexObservationProducer;
  public static Producer serialNavigationProducer;
  public static Producer rinexNavigationProducer;
  
  public static void init(){
    serialObservationProducer = new Producer( SERIAL, "Serial (Ublox)", null, "" );
    rinexObservationProducer  = new Producer( FILE,   "Rinex Observation File", null, "./data/UB110350.15o");
    serialNavigationProducer  = new Producer( SERIAL, "Serial", null, "");
    rinexNavigationProducer   = new Producer( FTP,    "Rinex FTP", null, "" );
  }
}

