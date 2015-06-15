package org.gogpsproject;
	
import java.io.PrintStream;
import java.util.logging.Logger;

import gnu.io.CommDriver;
import gnu.io.CommPortIdentifier;

import org.gogpsproject.model.ConsoleError;
import org.gogpsproject.model.ConsoleInfo;
import org.gogpsproject.model.GoGPSDef;
import org.gogpsproject.model.GoGPSModel;
import org.gogpsproject.model.SerialPortDef;

import net.java.html.boot.BrowserBuilder;
import net.java.html.js.JavaScriptBody;
import net.java.html.js.JavaScriptResource;
import net.java.html.json.Models;

public class GoGPS_Fx {
  private static final Logger logger = Logger.getLogger(GoGPS_Fx.class.getName());
  static GoGPSModel goGPSModel;

  public static void main(String... args) throws Exception {
    System.out.println("Library Path is " + System.getProperty("java.library.path"));
    System.out.println("rootdir is " + System.getProperty("user.dir"));

    String rootdir = System.getProperty("user.dir") + "/src/main/webapp";
    System.setProperty("browser.rootdir", rootdir ); 
    
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
      
      System.out.println("Info");
      System.err.println("Err");
      System.setOut(new PrintStream(new ConsoleInfo(), true));
      System.setErr(new PrintStream(new ConsoleError(), true));

      System.out.println("Info");
      System.err.println("Err");

      // test Serialio. If it fails, copy native library to root
      for( int i=0; i<2; i++ ){
        try {
          GoGPSDef.getPortList( goGPSModel );
          System.out.println("RXTX Ok");
          break;
        }
        catch (Throwable localThrowable){
          System.err.println(localThrowable + " thrown while loading " + "gnu.io.RXTXCommDriver");
          System.err.flush();
          if( i == 0 )
            SerialPortDef.copyRxTxLibToRoot();
        }
      }
  }    
  
}

