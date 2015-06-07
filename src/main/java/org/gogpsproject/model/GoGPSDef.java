package org.gogpsproject.model;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "GoGPSModel", targetId = "", properties = {
    @Property(name = "javaLibraryPath", type = String.class), 
    @Property(name = "SerialPort", type = SerialPortModel.class) 
})
public final class GoGPSDef {

  @net.java.html.js.JavaScriptBody(args = { "msg" }, body = "alert(msg);")
  public static native void alert(String msg);

  @net.java.html.js.JavaScriptBody(args = {}, body = "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}")
  public static native void loadFirebug();

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
      + "Model = viewModel;" + "}" + "};")
  public static native void registerModel();
  
//  @ComputedProperty 
//  public static String getLibPath( GoGPSModel model ){
//    return ( System.getProperty("java.library.path") );
//  }
  
  @Function 
  public static void getLibPath( GoGPSModel model ){
    model.setJavaLibraryParh( System.getProperty("java.library.path") );
  }
}




