package org.gogpsproject;
	
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Logger;

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

  public abstract static class FirebugConsole extends OutputStream {

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

  public static class FirebugConsoleInfo extends FirebugConsole{
    @net.java.html.js.JavaScriptBody(args = { "msg" }, body = ""
        + "Firebug.Console.log(msg);")
    public native void log( String msg );
  }
  
  public static class FirebugConsoleError extends FirebugConsole{
    @net.java.html.js.JavaScriptBody(args = { "msg" }, body = ""
        + "Firebug.Console.error(msg);")
    public native void log( String msg );
  }
  
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
      goGPSModel.getSpeedOptions().addAll( Arrays.asList(new Integer[]{9600, 115200}));
      goGPSModel.getMeasurementRateOptions().addAll( Arrays.asList(new Integer[]{1, 2, 5, 10}));
      Models.toRaw(goGPSModel);
      GoGPSDef.registerModel();
      goGPSModel.applyBindings();
      
      System.setOut(new PrintStream(new FirebugConsoleInfo(), true));
      System.setErr(new PrintStream(new FirebugConsoleError(), true));

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
          if( i == 0 ){
            SerialPortDef.copyRxTxLibToRoot();
            GoGPSDef.alert("RXTX lib copied to root, you might have to restart the app");
          }
        }
      }
    }    
}

