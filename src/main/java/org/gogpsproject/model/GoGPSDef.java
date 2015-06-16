package org.gogpsproject.model;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.gogpsproject.GoGPS;
import org.gogpsproject.GoGPS_Fx;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.model.SerialPortDef.UBXTest;
import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "GoGPSModel", targetId = "", properties = {

    @Property(name = "runModes", type = Mode.class, array=true),
    @Property(name = "selectedRunMode", type = Mode.class),
    @Property(name = "dynModels", type = DynModel.class, array=true),
    @Property(name = "selectedDynModel", type = DynModel.class),
    
    @Property(name = "serialPort1", type = SerialPortModel.class),
    @Property(name = "serialPort2", type = SerialPortModel.class),
    @Property(name = "serialPortList", type = SerialPortModel.class, array = true),
    @Property(name = "speedOptions", type = int.class, array=true),
    @Property(name = "measurementRateOptions", type = int.class, array=true),
    @Property(name = "running", type = boolean.class)
    })
public final class GoGPSDef {

  private static final Logger l = Logger.getLogger(GoGPS_Fx.class.getName());

  static ObservationsProducer roverIn;
  static NavigationProducer   navigationIn;
  static ObservationsProducer masterIn;
  
  private static UBXSerialConnection ubxSerialConn1;
  private static UBXSerialConnection ubxSerialConn2;
  
  private static int setEphemerisRate = 10;
  private static int setIonosphereRate = 60;
  private static boolean enableTimetag = true;
  private static Boolean enableDebug = true;

  private String fileNameOutLog = null;
  private FileOutputStream fosOutLog = null;
  private DataOutputStream outLog = null;//new XMLEncoder(os);


  @net.java.html.js.JavaScriptBody(args = { "msg" }, body = "alert(msg);")
  public static native void alert(String msg);

  @net.java.html.js.JavaScriptBody(args = {}, body = "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}")
  public static native void loadFirebug();

  public static void cleanUp(GoGPSModel model) throws InterruptedException {
    List<SerialPortModel> ports = model.getSerialPortList();
    for (SerialPortModel port : ports) {
      if (port.isConnected()) {
        stopUBXxTest(model);
      }
    }
  }

  @Function
  public static void getPortList(GoGPSModel model) throws Exception {
    List<SerialPortModel> ports = model.getSerialPortList();

    for (SerialPortModel port : ports) {
      if (model.isRunning())
        stopUBXxTest(model);
    }
    ports.clear();

    for (String name : UBXSerialConnection.getPortList(true)) {
      SerialPortModel port = new SerialPortModel(name, 9600, 1, false);
      ports.add(port);
    }
    if (ports.size() > 0) {
      model.setSerialPort1(ports.get(0));
    } else
      model.setSerialPort1(null);
    if (ports.size() > 1) {
      model.setSerialPort2(ports.get(1));
    } else
      model.setSerialPort2(null);
  }

  /** Shows direct interaction with JavaScript */
  @net.java.html.js.JavaScriptBody(args = { "msg", "callback" }, javacall = true, body = "if (confirm(msg)) {\n"
      + "  callback.@java.lang.Runnable::run()();\n" + "}\n")
  static native void confirmByUser(String msg, Runnable callback);

  @net.java.html.js.JavaScriptBody(args = {}, body = "var w = window,\n"
      + "    d = document,\n" + "    e = d.documentElement,\n"
      + "    g = d.getElementsByTagName('body')[0],\n"
      + "    x = w.innerWidth || e.clientWidth || g.clientWidth,\n"
      + "    y = w.innerHeight|| e.clientHeight|| g.clientHeight;\n" + "\n"
      + "return 'Screen size is ' + x + ' times ' + y;\n")
  static native String screenSize();

  @net.java.html.js.JavaScriptBody(args = {}, body = "ko.bindingHandlers.Model = {"
      + "init: function( element, valueAccessor, allBindingsAccessor, viewModel ){"
      + "Model = viewModel;" + "}" 
      + "};")
  public static native void registerModel();

  @Function 
  static void startUBXxTest( GoGPSModel model ) throws Exception {
    System.out.println("Info");
    System.err.println("Err");
    
    //force dot as decimal separator
    Locale.setDefault(new Locale("en", "US"));

    if( model.isRunning() ) 
      stopUBXxTest( model );

    l.info("Start UBX test" );

    SerialPortModel port1 = model.getSerialPort1();
    
    ubxSerialConn1 = new UBXSerialConnection( port1.getName(), port1.getSpeed() );

    UBXTest test = new UBXTest();
    ubxSerialConn1.setMeasurementRate( port1.getMeasurementRate() );
    ubxSerialConn1.enableEphemeris(setEphemerisRate);
    ubxSerialConn1.enableIonoParam(setIonosphereRate);
    ubxSerialConn1.enableTimetag(enableTimetag);
    ubxSerialConn1.enableDebug(enableDebug);
    ubxSerialConn1.enableNmeaSentences(new ArrayList<String>());

    ubxSerialConn1.init();
    ubxSerialConn1.addStreamEventListener(test);
    
    roverIn = new ObservationsBuffer( ubxSerialConn1, "./roverOut.dat" );
    navigationIn = (NavigationProducer) roverIn;
    roverIn.init();
 
    GoGPS goGPSstandalone = new GoGPS(navigationIn, roverIn, null);
    goGPSstandalone.setDynamicModel( model.getSelectedDynModel().getValue() );
    
    goGPSstandalone.runThreadMode( model.getSelectedRunMode().getValue() );
    
    model.setRunning(true);
  }

  @Function 
  static void stopUBXxTest( GoGPSModel model ) throws InterruptedException {
    if( roverIn != null ){
      System.out.println("Stop Rover");
      roverIn.release( true, 10000 ); // release and close rover
      roverIn = null;
    }
    if( ubxSerialConn1 != null ){
      l.info("Stop UBX");
      ubxSerialConn1.release( true, 10000 );
      ubxSerialConn1 = null;
    }
    model.setRunning(false);
  }
  
}
