package org.gogpsproject.model;

public class FirebugConsoleInfo extends FirebugConsole{
  @net.java.html.js.JavaScriptBody(args = { "msg" }, body = ""
      + "Firebug.Console.log(msg);")
  public native void log( String msg );
}