package org.gogpsproject;
	
import org.gogpsproject.model.GoGPSDef;
import org.gogpsproject.model.GoGPSModel;
import org.gogpsproject.model.SerialPortDef;
import org.gogpsproject.model.SerialPortModel;

import net.java.html.boot.BrowserBuilder;
import net.java.html.json.Models;


public class GoGPS_Fx {
  static GoGPSModel goGPSModel;
  static SerialPortModel serialPort; 

  public static void main(String... args) throws Exception {
    String rootdir = System.getProperty("user.dir") + "/src/main/webapp";
    System.setProperty("browser.rootdir", rootdir ); 

    SerialPortDef.setRxTxLibPath();

    goGPSModel = new GoGPSModel();
    serialPort = goGPSModel.getSerialPort();
    serialPort.setPort("undefined");
    serialPort.setSpeed(9600);
    
    BrowserBuilder.newBrowser().
                   loadPage("pages/index.html").
                   loadClass(GoGPS_Fx.class).
                   invoke("onPageLoad", args).
                   showAndWait();
    System.exit(0);
  }
  
  public static void onPageLoad() throws Exception {
      Models.toRaw(goGPSModel);
      GoGPSDef.registerModel();
      goGPSModel.applyBindings();
      SerialPortDef.getPortList( goGPSModel.getSerialPort() );
  }    
}

