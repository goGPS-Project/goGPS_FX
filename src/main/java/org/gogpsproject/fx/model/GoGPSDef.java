package org.gogpsproject.fx.model;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.gogpsproject.Coordinates;
import org.gogpsproject.EphGps;
import org.gogpsproject.GoGPS;
import org.gogpsproject.IonoGps;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.Observations;
import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.StreamEventListener;
import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.model.DynModel;
import org.gogpsproject.fx.model.GoGPSModel;
import org.gogpsproject.fx.model.Mode;
import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.SerialPortModel;
import org.gogpsproject.parser.rinex.RinexNavigation;
import org.gogpsproject.parser.rinex.RinexObservationParser;
import org.gogpsproject.parser.ublox.UBXSerialConnection;
import org.gogpsproject.producer.KmlProducer;
import org.gogpsproject.producer.TxtProducer;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "GoGPSModel", targetId = "", properties = {

    @Property(name = "runModes", type = Mode.class, array=true),
    @Property(name = "selectedRunMode", type = Mode.class),
    @Property(name = "dynModels", type = DynModel.class, array=true),
    @Property(name = "selectedDynModel", type = DynModel.class),
    @Property(name = "observationProducers", type = Producer.class, array=true),
    @Property(name = "selectedObservationProducer", type = Producer.class),
    @Property(name = "navigationProducers", type = Producer.class, array=true),
    @Property(name = "selectedNavigationProducer", type = Producer.class),
    @Property(name = "outputFolder", type = String.class),
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
        stop(model);
      }
    }
  }

  @Function
  public static void getPortList(GoGPSModel model) throws Exception {
    List<SerialPortModel> ports = model.getSerialPortList();

    if (model.isRunning())
      stop(model);
    ports.clear();

    for (String name : UBXSerialConnection.getPortList(true)) {
      SerialPortModel port = new SerialPortModel(name, 9600, 1, false);
      ports.add(port);
    }

    model.getObservationProducers().clear();
    model.getNavigationProducers().clear();

    if (ports.size() > 0) {
      Producers.serialObservationProducer.setSerialPort(ports.get(0));
      model.getObservationProducers().add( Producers.serialObservationProducer );
    } else
      Producers.serialObservationProducer.setSerialPort(null);
    
    if (ports.size() > 1) {
      Producers.serialNavigationProducer.setSerialPort(ports.get(1));
      model.getNavigationProducers().add( Producers.serialNavigationProducer);
    } else
      Producers.serialNavigationProducer.setSerialPort(null);
    
    model.getObservationProducers().add( Producers.rinexObservationProducer);
    model.getNavigationProducers().add( Producers.rinexNavigationProducer);
    
    model.setSelectedObservationProducer(model.getObservationProducers().get(0));
    model.setSelectedNavigationProducer(model.getNavigationProducers().get(0));
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
  static void start( GoGPSModel model ) throws Exception {
    l.info("Info");
    l.warning("warning");
    l.severe("Severe");
    
    //force dot as decimal separator
    Locale.setDefault(new Locale("en", "US"));

    if( model.isRunning() ) 
      stop( model );

    l.info("Start UBX test" );
    Producer observation = model.getSelectedObservationProducer(); 
    switch ( observation.getType() ){
      case Producers.SERIAL :
        SerialPortModel port1 = observation.getSerialPort();
        ubxSerialConn1 = new UBXSerialConnection( port1.getName(), port1.getSpeed() );

        ubxSerialConn1.setMeasurementRate( port1.getMeasurementRate() );
        ubxSerialConn1.enableEphemeris(setEphemerisRate);
        ubxSerialConn1.enableIonoParam(setIonosphereRate);
        ubxSerialConn1.enableTimetag(enableTimetag);
        ubxSerialConn1.enableDebug(enableDebug);
        ubxSerialConn1.enableNmeaSentences(new ArrayList<String>());

        ubxSerialConn1.init();
        ConsoleStreamer listener = new ConsoleStreamer();
        ubxSerialConn1.addStreamEventListener(listener);
        roverIn = new ObservationsBuffer( ubxSerialConn1, "./roverOut.dat" );
        
        break;
        case Producers.FILE:
          roverIn = new RinexObservationParser(new File( observation.getFilename() ));
        break;
    }
    
    Producer navigation = model.getSelectedNavigationProducer();
    switch(navigation.getType()){
      case Producers.SERIAL:
          // TODO
        navigationIn = (NavigationProducer) roverIn;
       break;
      case Producers.FILE:
        // TODO
        break;
      case Producers.FTP:
        navigationIn = new RinexNavigation( RinexNavigation.GARNER_NAVIGATION_AUTO );
        break;
    }
    
    GoGPS goGPS = new GoGPS(navigationIn, roverIn, null);
    goGPS.setDynamicModel( model.getSelectedDynModel().getValue() );
    goGPS.runThreadMode( model.getSelectedRunMode().getValue() );

    String outFolder = model.getOutputFolder();
    String outPathTxt = outFolder + "/out.txt";
    String outPathKml = outFolder + "/out.kml";
    TxtProducer txt = new TxtProducer(outPathTxt);
    double goodDopThreshold = 10;
    int timeSampleDelaySec = 30; // should be tuned according to the dataset; use '0' to disable timestamps in the KML 
    KmlProducer kml = new KmlProducer(outPathKml, goodDopThreshold, timeSampleDelaySec );
    goGPS.addPositionConsumerListener(txt);
    goGPS.addPositionConsumerListener(kml);

    roverIn.init();
    model.setRunning(true);
  }

  @Function 
  static void stop( GoGPSModel model ) throws InterruptedException {
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
//    for (SerialPortModel port : ports) {
  }
 
  public static class ConsoleStreamer implements StreamEventListener{

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
}
