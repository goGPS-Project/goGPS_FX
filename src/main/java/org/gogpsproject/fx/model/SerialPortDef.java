package org.gogpsproject.fx.model;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Logger;

import org.gogpsproject.GoGPS;
import org.gogpsproject.NavigationProducer;
import org.gogpsproject.ObservationsBuffer;
import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.parser.ublox.UBXSerialConnection;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "SerialPortModel", targetId="", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "speed", type = int.class),
    @Property(name = "measurementRate", type = int.class),
    @Property(name = "connected", type = boolean.class),
})
public class SerialPortDef {

  private static final Logger l = Logger.getLogger(SerialPortDef.class.getName());

  public static enum OSType {
    Windows32, Windows64, MacOS, Linux32, Linux64, Other
  };

  // cached result of OS detection
  protected static OSType detectedOS;

  /**
   * detect the operating system from the os.name System property and cache
   * the result
   * 
   * @returns - the operating system detected
   */
  public static OSType getOperatingSystemType() {
//    System.getProperties().list(System.out);
    // http://stackoverflow.com/questions/15240835/is-it-possible-to-detect-processor-architecture-in-java
//    sun.desktop=windows
//    sun.cpu.isalist=amd64
//    sun.arch.data.model=64
// System.getProperty ("os.arch");
    if (detectedOS == null) {
      String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
      System.out.println("os.name = " + OS );
      String osArch = System.getProperty("os.arch", "generic").toLowerCase(Locale.ENGLISH);
      System.out.println("os.arch = " + osArch );
      
      if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
        detectedOS = OSType.MacOS;
      } else if (OS.indexOf("win") >= 0) {
        if( osArch.equals("amd64"))
          detectedOS = OSType.Windows64;
        else
          detectedOS = OSType.Windows32;
      } else if (OS.indexOf("nux") >= 0) {
        if( osArch.equals("amd64"))
          detectedOS = OSType.Linux64;
        else
          detectedOS = OSType.Linux32;
      } else {
        detectedOS = OSType.Other;
      }
    }
    System.out.println("detectedOS = " + detectedOS.name() );
    return detectedOS;
  }
  
  public static void addDir(String s) throws IOException {
    try {
        // This enables the java.library.path to be modified at runtime
        // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
        Field field = ClassLoader.class.getDeclaredField("usr_paths");
        field.setAccessible(true);
        String[] paths = (String[])field.get(null);
        for (int i = 0; i < paths.length; i++) {
            if (s.equals(paths[i])) {
                return;
            }
        }
        String[] tmp = new String[paths.length+1];
        System.arraycopy(paths,0,tmp,0,paths.length);
        tmp[paths.length] = s;
        field.set(null,tmp);
        System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
    } catch (IllegalAccessException e) {
        throw new IOException("Failed to get permissions to set library path");
    } catch (NoSuchFieldException e) {
        throw new IOException("Failed to get field handle to set library path");
    }
  }
  
  public static void setRxTxLibPath() throws Exception{
  //  System.getProperty("os.name");
    OSType ostype= getOperatingSystemType();
    String dir = "";
    switch (ostype) {
        case Windows32: 
          dir = "./libs/RXTX/win32";
          break;
        case Windows64: 
          dir = System.getProperty("user.dir") + "\\libs\\RXTX\\win64";
          break;
        case MacOS: 
          dir = "./libs/RXTX/mac-10.5";
          break;
        case Linux32: 
          dir = "./libs/RXTX/i686-pc-linux-gnu";
          break;
        case Linux64: 
          dir = "./libs/RXTX/x86_64-unknown-linux-gnu";
          break;
        case Other: 
          throw new Exception("RxTx doesn't support this system");
    }    
    System.out.println("Library Path is " + System.getProperty("java.library.path"));
    addDir( dir );
    System.out.println("Library Path is " + System.getProperty("java.library.path"));
  }
  
  public static void copyFile( File from, File to ) throws IOException {
    Files.copy( from.toPath(), to.toPath() );
  }

  public static void copyRxTxLibToRoot() throws Exception{
  //  System.getProperty("os.name");
    OSType ostype= getOperatingSystemType();
    String from = "";
    String to = "";
    switch (ostype) {
        case Windows32: 
          from = "./libs/RXTX/win32";
          break;
        case Windows64: 
          from = /*System.getProperty("user.dir") + */ ".\\libs\\RXTX\\win64\\rxtxSerial.dll";
          to = ".\\rxtxSerial.dll";
          break;
        case MacOS: 
          from = "./libs/RXTX/mac-10.5";
          break;
        case Linux32: 
          from = "./libs/RXTX/i686-pc-linux-gnu";
          break;
        case Linux64: 
          from = "./libs/RXTX/x86_64-unknown-linux-gnu";
          break;
        case Other: 
          throw new Exception("RxTx doesn't support this system");
    }    
    try{
      Files.copy( new File(from).toPath(), new File(to).toPath() );
    }
    catch( java.nio.file.FileAlreadyExistsException e){};
  }

}

