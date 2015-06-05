package org.gogpsproject.model;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.IonoGps;
import org.gogpsproject.Observations;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "SerialPortModel", targetId="", properties = {
    @Property(name = "port", type = String.class ),
    @Property(name = "speed", type = int.class),
    @Property(name = "ports", type = String.class, array = true )
//    @Property(name = "rotating", type = boolean.class)
})
public class SerialPortModelDef {

  private static UBXSerialConnection ubxSerialConn;
  private String fileNameOutLog = null;
  private FileOutputStream fosOutLog = null;
  private DataOutputStream outLog = null;//new XMLEncoder(os);
  
  @Function 
  static void getPortList( SerialPortModel model ){
    model.setPort("undefined");
    List<String> ports = model.getPorts();
    ports.clear();
    ports.addAll( UBXSerialConnection.getPortList(true));
    if( ports.size()>0 )
      model.setPort( ports.get(0) );
  }
  
  public static class UBXTest implements StreamEventListener{

    @Override
    public void streamClosed() {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void addObservations(Observations o) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void addIonospheric(IonoGps iono) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void addEphemeris(EphGps eph) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public void setDefinedPosition(Coordinates definedPosition) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public Observations getCurrentObservations() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void pointToNextObservations() {
      // TODO Auto-generated method stub
    }
  }

  @Function 
  static void RunUBXxTest( SerialPortModel model ) throws InterruptedException {
    //force dot as decimal separator
    Locale.setDefault(new Locale("en", "US"));

    if( ubxSerialConn != null )
      ubxSerialConn.release( true, 1000 );
    
    ubxSerialConn = new UBXSerialConnection( model.getPort(), model.getSpeed() );
    try {
      ubxSerialConn.init();
//      ObservationsBuffer rover = new ObservationsBuffer();
//      rover.setStreamSource(ubxSerialConn);
  //
//      try {
//        rover.init();
//      } catch (Exception e) {
//        e.printStackTrace();
//      }
      UBXTest test = new UBXTest();
      ubxSerialConn.addStreamEventListener(test);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
