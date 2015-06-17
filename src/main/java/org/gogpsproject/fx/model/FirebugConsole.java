package org.gogpsproject.fx.model;

import java.io.OutputStream;

import net.java.html.BrwsrCtx;

public abstract class FirebugConsole extends OutputStream {
  protected final BrwsrCtx ctx;

  public FirebugConsole( BrwsrCtx ctx ){
    this.ctx = ctx;
  }
  abstract void logNative( String msg );

  void log(String msg) {
    ctx.execute(new Runnable(){
      @Override
      public void run() {
        logNative(msg);
      }
    });
  }

  StringBuilder sb = new StringBuilder();

  @Override
  public void write(int i) {
    sb.append((char)i);
  }

  @Override
  public void flush() {
    if( sb.length() >0 && !sb.toString().equals("\r\n"))
      log(sb.toString());
    sb = new StringBuilder();
  }
  
  public static class FirebugConsoleInfo extends FirebugConsole{
    public FirebugConsoleInfo(BrwsrCtx ctx) {
      super(ctx);
    }

    @net.java.html.js.JavaScriptBody(args = { "msg" }, body = ""
        + "Firebug.Console.log(msg);")
    public native void logNative( String msg );

  }
  
  public static class FirebugConsoleError extends FirebugConsole{
    public FirebugConsoleError(BrwsrCtx ctx) {
      super(ctx);
    }

    @net.java.html.js.JavaScriptBody(args = { "msg" }, body = ""
        + "Firebug.Console.error(msg);")
    public native void logNative( String msg );
  }
  
}