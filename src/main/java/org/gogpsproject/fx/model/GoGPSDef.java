package org.gogpsproject.fx.model;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.gogpsproject.GoGPS;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.ObservationsProducer;
import org.gogpsproject.StreamEventProducer;
import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.model.DynModel;
import org.gogpsproject.fx.model.GoGPSModel;
import org.gogpsproject.fx.model.Mode;
import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.SerialPortModel;
import org.gogpsproject.parser.rinex.RinexNavigation;
import org.gogpsproject.parser.rinex.RinexNavigationParser;
import org.gogpsproject.parser.rinex.RinexObservationParser;
import org.gogpsproject.parser.ublox.UBXSerialConnection;
import org.gogpsproject.producer.KmlProducer;
import org.gogpsproject.producer.TxtProducer;

import net.java.html.js.JavaScriptBody;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

/**
 * @author EZ
 * It defines GoGPSModel, the root of our model tree, it maps to a "goGPS" javascript object
 */
@Model(className = "GoGPSModel", targetId = "", properties = {
    @Property(name = "selectedRunMode", type = Mode.class),
    @Property(name = "selectedDynModel", type = DynModel.class),
    @Property(name = "observationProducers", type = Producer.class, array=true),
    @Property(name = "selectedObservationProducer", type = Producer.class),
    @Property(name = "navigationProducers", type = Producer.class, array=true),
    @Property(name = "selectedNavigationProducer", type = Producer.class),
    @Property(name = "masterProducers", type = Producer.class, array=true),
    @Property(name = "selectedMasterProducer", type = Producer.class),
    @Property(name = "outputFolder", type = String.class),
    @Property(name = "serialPortList", type = SerialPortModel.class, array = true),
    @Property(name = "satellites", type = SatelliteModel.class, array=true),
    @Property(name = "running", type = boolean.class)
    })
public final class GoGPSDef {

  @ComputedProperty
  public static List<Mode> runModes(){
    return Arrays.asList(new Mode[]{ Modes.standAlone, Modes.kalmanFilter, Modes.doubleDifferences });
  }

  @ComputedProperty
  public static List<DynModel> dynModels(){
    return Arrays.asList(new DynModel[]{ DynModels.staticm, DynModels.constantSpeed, DynModels.constantAcceleration });
  }

  @ComputedProperty
  public static List<Integer> speedOptions(){
    return  Arrays.asList(new Integer[]{9600, 115200} );
  }
  
  @ComputedProperty
  public static List<Integer> measurementRateOptions(){
    return  Arrays.asList(new Integer[]{1, 2, 5, 10});
  }

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

  /**
   * Called by onPageLoad(), it builds goGPSModel and its descendants
   * and populates it with default values
   * @param goGPSModel
   */
  @Function
  public static void init( GoGPSModel goGPSModel ){
    Modes.init();
    goGPSModel.setSelectedRunMode(Modes.standAlone);
    DynModels.init();
    goGPSModel.setSelectedDynModel(DynModels.staticm);
    Producers.init();
    goGPSModel.setOutputFolder("./out");
  }


  
  public static void cleanUp(GoGPSModel model) throws InterruptedException {
    List<SerialPortModel> ports = model.getSerialPortList();
    for (SerialPortModel port : ports) {
      if (port.isConnected()) {
        stop(model);
      }
    }
  }

  /**
   * This method scans for available serial ports. 
   * Not only that, it populates the list of available Observation and Navigation producers
   * @param model
   * @throws Exception
   */
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
      Producers.serialNavigationProducer.setSerialPort(ports.get(0));
      model.getNavigationProducers().add( Producers.serialNavigationProducer);
      model.getMasterProducers().add( Producers.serialMasterProducer );
    } else {
      Producers.serialObservationProducer.setSerialPort(null);
      Producers.serialNavigationProducer.setSerialPort(null);
      Producers.serialMasterProducer.setSerialPort(null);
    }
    if (ports.size() > 1) {
      Producers.serialMasterProducer.setSerialPort(ports.get(1));
    }
    
    model.getObservationProducers().add( Producers.rinexObservationProducer );
    model.getNavigationProducers().add( Producers.rinexNavigationProducer );
    model.getNavigationProducers().add( Producers.ftpNavigationProducer );
    model.getMasterProducers().add( Producers.rinexMasterProducer );
    
    model.setSelectedObservationProducer(model.getObservationProducers().get(0));
    model.setSelectedNavigationProducer(model.getNavigationProducers().get(0));
    model.setSelectedMasterProducer(model.getMasterProducers().get(0));
  }

  /* Some DukeScript example code, I'll keep it here for now */
  @JavaScriptBody(args = { "msg", "callback" }, javacall = true, body = "if (confirm(msg)) {\n"
      + "  callback.@java.lang.Runnable::run()();} " )
  public static native void confirmByUser(String msg, Runnable callback);

  @JavaScriptBody(args = {}, body = "var w = window,\n"
      + "    d = document,\n" + "    e = d.documentElement,\n"
      + "    g = d.getElementsByTagName('body')[0],\n"
      + "    x = w.innerWidth || e.clientWidth || g.clientWidth,\n"
      + "    y = w.innerHeight|| e.clientHeight|| g.clientHeight;\n" + "\n"
      + "return 'Screen size is ' + x + ' times ' + y;\n")
  static native String screenSize();
  
  @JavaScriptBody(args = { "msg" }, body = "alert(msg);")
  public static native void alert(String msg);

  
  /**
   * Creates a "goGPS" javascript object, for debugging from the Firebug command line
   */
  @JavaScriptBody(args = {}, body = ""
      + "ko.bindingHandlers.Model = {"
        + "init: function( element, valueAccessor, allBindingsAccessor, viewModel ){"
        + "goGPS = viewModel;" 
        + "}" 
      + "};")
  public static native void registerModel();

  public static boolean checkDir(String tempDir) {
    try {
        final File f = new File(tempDir);
        if (!f.exists() || !f.isDirectory()) {
        f.mkdir();
        if (!f.exists() || !f.isDirectory()) {
            return false;
        }
      } // end if
      return true;
    } catch (final Exception e) {
        e.printStackTrace();
    }
    return false;
  }

  @Function 
  static void start( GoGPSModel model ) throws Exception {
    
    //force dot as decimal separator
    Locale.setDefault(new Locale("en", "US"));

    if( model.isRunning() ) 
      stop( model );

    l.info("Start goGPS" );
    String outFolder = model.getOutputFolder();
    checkDir(outFolder);
    
    Producer observation = model.getSelectedObservationProducer(); 
    switch ( observation.getType() ){
      case Producers.SERIAL : {
          SerialPortModel port1 = observation.getSerialPort();
          ubxSerialConn1 = new UBXSerialConnection( port1.getName(), port1.getSpeed() );
  
          ubxSerialConn1.setMeasurementRate( port1.getMeasurementRate() );
          ubxSerialConn1.enableEphemeris(setEphemerisRate);
          ubxSerialConn1.enableIonoParam(setIonosphereRate);
          ubxSerialConn1.enableTimetag(enableTimetag);
          ubxSerialConn1.enableDebug(enableDebug);
          ubxSerialConn1.enableNmeaSentences(new ArrayList<String>());
  
          ubxSerialConn1.init();
          ConsoleStreamer listener = new ConsoleStreamer(model);
          ubxSerialConn1.addStreamEventListener(listener);
          roverIn = new ObservationsBuffer( ubxSerialConn1, "./roverOut.dat" );
        }
        break;
        case Producers.FILE: {
          roverIn = new RinexObservationParserStreamEventProducer(new File( observation.getFilename() ));
          ConsoleStreamer listener = new ConsoleStreamer(model);
          ((StreamEventProducer) roverIn).addStreamEventListener(listener);
        }
        break;
    }
    
    Producer navigation = model.getSelectedNavigationProducer();
    switch(navigation.getType()){
      case Producers.SERIAL:
        if( navigation.getSerialPort() == observation.getSerialPort() ) {
          navigationIn = (NavigationProducer) roverIn;
        } 
        else {
          SerialPortModel port2 = navigation.getSerialPort();
          ubxSerialConn2 = new UBXSerialConnection( port2.getName(), port2.getSpeed() );

          ubxSerialConn2.setMeasurementRate( port2.getMeasurementRate() );
          ubxSerialConn2.enableEphemeris(setEphemerisRate);
          ubxSerialConn2.enableIonoParam(setIonosphereRate);
          ubxSerialConn2.enableTimetag(enableTimetag);
          ubxSerialConn2.enableDebug(enableDebug);
          ubxSerialConn2.enableNmeaSentences(new ArrayList<String>());

          ubxSerialConn2.init();
          ConsoleStreamer listener = new ConsoleStreamer(model);
          ubxSerialConn2.addStreamEventListener(listener);
          navigationIn = new ObservationsBuffer( ubxSerialConn2, outFolder + "/navigationOut.dat" );
        }
       break;
      case Producers.FILE:
          navigationIn = new RinexNavigationParser(new File( navigation.getFilename() ));
//          navigationIn = new RinexNavigationStreamEventProducer(new File( navigation.getFilename() ));
//          ConsoleStreamer listener = new ConsoleStreamer(model);
//          ((StreamEventProducer) navigationIn).addStreamEventListener(listener);
        break;
      case Producers.FTP:
          navigationIn = new RinexNavigation( RinexNavigation.GARNER_NAVIGATION_AUTO );
        break;
    }
    
    GoGPS goGPS = new GoGPS(navigationIn, roverIn, null);
    goGPS.setDynamicModel( model.getSelectedDynModel().getValue() );

    String outPathTxt = outFolder + "/out.txt";
    String outPathKml = outFolder + "/out.kml";
    TxtProducer txt = new TxtProducer(outPathTxt);
//    ConsoleProducer console = new ConsoleProducer();
    double goodDopThreshold = 10;
    int timeSampleDelaySec = 30; // should be tuned according to the dataset; use '0' to disable timestamps in the KML 
    KmlProducer kml = new KmlProducer(outPathKml, goodDopThreshold, timeSampleDelaySec );
    goGPS.addPositionConsumerListener(txt);
    goGPS.addPositionConsumerListener(kml);
//    goGPS.addPositionConsumerListener(console);

    roverIn.init();
    if( navigationIn!=roverIn )
      navigationIn.init();
    
    model.setRunning(true);
    goGPS.runThreadMode( model.getSelectedRunMode().getValue() );
    
  }

  @Function 
  static void stop( GoGPSModel model ) throws InterruptedException {
    if( roverIn != null ){
      System.out.println("Stop Rover");
      roverIn.release( true, 10000 ); // release and close rover
      roverIn = null;
    }
    if( ubxSerialConn1 != null ){
      l.info("Stop UBX1");
      ubxSerialConn1.release( true, 10000 );
      ubxSerialConn1 = null;
    }
    if( navigationIn != roverIn  ){
      System.out.println("Stop Navigation");
      navigationIn.release( true, 10000 ); // release and close rover
      navigationIn = null;
    }
    if( ubxSerialConn2 != null ){
      l.info("Stop UBX2");
      ubxSerialConn2.release( true, 10000 );
      ubxSerialConn2 = null;
    }
    
    model.setRunning(false);
//    for (SerialPortModel port : ports) {
  }
}
