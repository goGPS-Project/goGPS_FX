package org.gogpsproject.model;

import java.io.OutputStream;

// @Function
// public static void setPort( GoGPSModel model, int index, SerialPortModel
// port ){
// l.info(port.toString());
// }
public abstract class FirebugConsole extends OutputStream {

  abstract void log( String msg );

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
}

