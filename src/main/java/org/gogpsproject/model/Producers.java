package org.gogpsproject.model;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "ProducerM", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "serialPort", type = SerialPortModel.class)
})
public class Producers {
  public static ProducerM serialObservationProducer = new ProducerM("serial", null);
  public static ProducerM rinexObservationProducer = new ProducerM("Rinex Observation File", null);
  public static ProducerM rinexNavigationProducer  = new ProducerM("Rinex Navigation File", null);
  public static ProducerM serialNavigationProducer = new ProducerM("serial", null);
}

