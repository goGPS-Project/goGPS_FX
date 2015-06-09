package org.gogpsproject;
	
import org.gogpsproject.model.GoGPSDef;
import org.gogpsproject.model.GoGPSModel;
import org.gogpsproject.model.SerialPortDef;

import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Models;


public class GoGPS_Fx {
  static GoGPSModel goGPSModel;

  public static void main(String... args) throws Exception {
    String rootdir = System.getProperty("user.dir") + "/src/main/webapp";
    System.setProperty("browser.rootdir", rootdir ); 

    SerialPortDef.setRxTxLibPath();

    BrowserBuilder.newBrowser().
                   loadPage("pages/index.html").
                   loadClass(GoGPS_Fx.class).
                   invoke("onPageLoad").
                   showAndWait();
    System.exit(0);
  }
  
  public static void onPageLoad() throws Exception {
      if( goGPSModel != null ){
        GoGPSDef.cleanUp(goGPSModel);
      }
      goGPSModel = new GoGPSModel();
      Models.toRaw(goGPSModel);
      GoGPSDef.registerModel();
      goGPSModel.applyBindings();
      GoGPSDef.getPortList( goGPSModel );
  }    
  
}

