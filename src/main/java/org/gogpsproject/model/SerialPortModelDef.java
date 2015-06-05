package org.gogpsproject.model;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "SerialPortModel", targetId="", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "ports", type = String.class, array = true )
//    @Property(name = "message", type = String.class),
//    @Property(name = "rotating", type = boolean.class)
})
public class SerialPortModelDef {

  public static int speed = 9600;
  private static UBXSerialConnection ubxSerialConn;
  private String fileNameOutLog = null;
  private FileOutputStream fosOutLog = null;
  private DataOutputStream outLog = null;//new XMLEncoder(os);
  
  @Function 
  static void getPortList( SerialPortModel model ){
    List<String> ports = model.getPorts();
    ports.clear();
    ports.addAll( UBXSerialConnection.getPortList(true));
  }
  
  public void UBXTest() {
    //force dot as decimal separator
    Locale.setDefault(new Locale("en", "US"));

    Vector<String> ports = UBXSerialConnection.getPortList(false);
    if (ports.size() > 0) {
      System.out.println("the following serial ports have been detected:");
    } else {
      System.out.println("sorry, no serial ports were found on your computer\n");
      System.exit(0);
    }
    String port = null;
    for (int i = 0; i < ports.size(); ++i) {
      System.out.println("    " + Integer.toString(i + 1) + ":  "+ ports.elementAt(i));
    }

    port = ports.elementAt(0);

    ubxSerialConn = new UBXSerialConnection(port, speed);
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
      //TestUBX test = new TestUBX();
      //ubxSerialConn.addStreamEventListener(test);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
}
