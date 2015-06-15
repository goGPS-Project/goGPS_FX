package org.gogpsproject.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.gogpsproject.GoGPS_Fx;
import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "GoGPSModel", targetId = "", properties = {
    @Property(name = "javaLibraryPath", type = String.class),
    @Property(name = "serialPort1", type = SerialPortModel.class),
    @Property(name = "serialPort2", type = SerialPortModel.class),
    @Property(name = "serialPortList", type = SerialPortModel.class, array = true),
    @Property(name = "speedOptions", type = int.class, array=true),
    })
public final class GoGPSDef {

  private static final Logger l = Logger.getLogger(GoGPS_Fx.class.getName());

  @net.java.html.js.JavaScriptBody(args = { "msg" }, body = "alert(msg);")
  public static native void alert(String msg);

  @net.java.html.js.JavaScriptBody(args = {}, body = "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}")
  public static native void loadFirebug();

  public static void cleanUp(GoGPSModel model) throws InterruptedException {
    List<SerialPortModel> ports = model.getSerialPortList();
    for (SerialPortModel port : ports) {
      if (port.isRunning()) {
        SerialPortDef.stopUBXxTest(port);
      }
    }
  }

  @Function
  public static void getPortList(GoGPSModel model) throws Exception {
    List<SerialPortModel> ports = model.getSerialPortList();

    for (SerialPortModel port : ports) {
      if (port.isRunning())
        SerialPortDef.stopUBXxTest(port);
    }
    ports.clear();

    for (String name : UBXSerialConnection.getPortList(true)) {
      SerialPortModel port = new SerialPortModel(name, 9600, false);
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

  // @ComputedProperty
  // public static String getLibPath( GoGPSModel model ){
  // return ( System.getProperty("java.library.path") );
  // }

  @Function
  public static void getLibPath(GoGPSModel model) {
    model.setJavaLibraryPath(System.getProperty("java.library.path"));
  }
}
