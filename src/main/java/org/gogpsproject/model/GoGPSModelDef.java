package org.gogpsproject.model;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "GoGPSModel", targetId = "", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "surname", type = String.class),
    @Property(name = "message", type = String.class),
    @Property(name = "rotating", type = boolean.class), 
    @Property(name = "SerialPort", type = SerialPortModel.class) 
    })
public final class GoGPSModelDef {

  // @net.java.html.js.JavaScriptBody(
  // args = {}, body =
  // "Sirtrack.loadFirebug();"
  // )
  // public static native void loadFirebug();

  // @net.java.html.js.JavaScriptBody(
  // args = {"msg"}, body =
  // "alert(msg);"
  // )
  @net.java.html.js.JavaScriptBody(args = { "msg" }, body = "alert(msg);")
  public static native void alert(String msg);

  // @Function static void popAlertJava(Sirtrack model) {
  // SirtrackModel.alert("fromJava");
  // }

  @net.java.html.js.JavaScriptBody(args = {}, body = "if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', 'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}")
  public static native void loadFirebug();

  @ComputedProperty
  static java.util.List<String> words(String message) {
    String[] arr = new String[6];
    String[] words = message == null ? new String[0] : message.split(" ", 6);
    for (int i = 0; i < 6; i++) {
      arr[i] = words.length > i ? words[i] : "!";
    }
    return java.util.Arrays.asList(arr);
  }

  @Function
  static void turnAnimationOn(GoGPSModel model) {
    model.setRotating(true);
  }

  @Function
  static void turnAnimationOff(final GoGPSModel model) {
    confirmByUser("Really turn off?", new Runnable() {
      @Override
      public void run() {
        model.setRotating(false);
      }
    });
  }

  @Function
  static void rotate5s(final GoGPSModel model) {
    model.setRotating(true);
    java.util.Timer timer = new java.util.Timer("Rotates a while");
    timer.schedule(new java.util.TimerTask() {
      @Override
      public void run() {
        model.setRotating(false);
      }
    }, 5000);
  }

  @Function
  static void showScreenSize(GoGPSModel model) {
    model.setMessage(screenSize());
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
      + "Model = viewModel;" + "}" + "};")
  public static native void registerModel();

}
