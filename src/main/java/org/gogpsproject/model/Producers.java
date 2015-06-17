package org.gogpsproject.model;

import java.util.Arrays;
import java.util.List;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "ProducerM", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "serialPort", type = SerialPortModel.class),
    @Property(name = "filename", type = String.class ),
})
public class Producers {
  public static ProducerM serialObservationProducer;
  public static ProducerM rinexObservationProducer;
  public static ProducerM serialNavigationProducer;
  public static ProducerM rinexNavigationProducer;
  
  public static void init(){
    serialObservationProducer = new ProducerM("Serial", null);
    rinexObservationProducer = new ProducerM("Rinex Observation File", null);
    serialNavigationProducer = new ProducerM("Serial", null);
    rinexNavigationProducer  = new ProducerM("Rinex Navigation File", null);
  }
}

