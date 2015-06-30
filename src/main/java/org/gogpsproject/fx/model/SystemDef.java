package org.gogpsproject.fx.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.digitalinferno.win32.test.PrintDevices;
import com.digitalinferno.win32.test.PrintDevices.DeviceInformation;
import com.sun.jna.platform.win32.SetupApi.SP_DEVINFO_DATA;

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "OSSystem", targetId="", properties = {
})
public class SystemDef {

  private static final Logger l = Logger.getLogger(SystemDef.class.getName());

  // cached result of OS detection
  protected static OSType detectedOS;
  protected static PrintDevices pd = PrintDevices.getInstance();
  protected static List<DeviceInformation> infoObjects;

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
  

  /**
   * detect the operating system from the os.name System property and cache
   * the result
   * 
   * @returns - the operating system detected
   */
  @ComputedProperty
  public static OSType OSType(){
//  System.getProperties().list(System.out);
  // http://stackoverflow.com/questions/15240835/is-it-possible-to-detect-processor-architecture-in-java
//  sun.desktop=windows
//  sun.cpu.isalist=amd64
//  sun.arch.data.model=64
//System.getProperty ("os.arch");
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
  
  public static void setRxTxLibPath( OSSystem model ) throws Exception{
  //  System.getProperty("os.name");
    String dir = "";
    switch (model.getOSType()) {
        case Windows32: 
          dir = System.getProperty("user.dir") + "./libs/RXTX/win32";
          break;
        case Windows64: 
          dir = System.getProperty("user.dir") + "\\libs\\RXTX\\win64";
          break;
        case MacOS: 
          dir = System.getProperty("user.dir") + "./libs/RXTX/mac-10.5";
          break;
        case Linux32: 
          dir = System.getProperty("user.dir") + "./libs/RXTX/i686-pc-linux-gnu";
          break;
        case Linux64: 
          dir = System.getProperty("user.dir") + "./libs/RXTX/x86_64-unknown-linux-gnu";
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

  public static void copyRxTxLibToRoot( OSSystem model ) throws Exception{
  //  System.getProperty("os.name");
    String from = "";
    String to = "";
    switch( model.getOSType() ) {
        case Windows32: 
          from = /*System.getProperty("user.dir") + */ ".\\libs\\RXTX\\win32\\rxtxSerial.dll";
          break;
        case Windows64: 
          from = /*System.getProperty("user.dir") + */ ".\\libs\\RXTX\\win64\\rxtxSerial.dll";
          to = ".\\rxtxSerial.dll";
          break;
        case MacOS: 
          from = "./libs/RXTX/mac-10.5/librxtxSerial.jnilib";
          break;
        case Linux32: 
          from = "./libs/RXTX/i686-pc-linux-gnu/librxtxSerial.so";
          break;
        case Linux64: 
          from = "./libs/RXTX/x86_64-unknown-linux-gnu/librxtxSerial.so";
          break;
        case Other: 
          throw new Exception("RxTx doesn't support this system");
    }    
    try{
      Files.copy( new File(from).toPath(), new File(to).toPath() );
    }
    catch( java.nio.file.FileAlreadyExistsException e){};
  }

  public static void updateInfoObjects(){
    PrintDevices pd = PrintDevices.getInstance();
    // Try to retrieve all SP_DEVINFO_DATA references, for this windows machine
    List<SP_DEVINFO_DATA.ByReference> deviceDevInfoDataReferences = pd.getAllDevInfoDataReferences();    
    System.out.println("Found " + deviceDevInfoDataReferences.size() + " SP_DEVINFO_DATA references");
    // Next, using the found SP_DEVINFO_DATA references, get some value objects with info found in the registry
    infoObjects = pd.getAllDevInfoForDataFound(deviceDevInfoDataReferences);
  }
  
  public static String getFriendlyName( GoGPSModel model, String name ) {
    if( model.getSystem().getOSType() == OSType.Windows32 || model.getSystem().getOSType() == OSType.Windows64 ){

      for (DeviceInformation devInfo : infoObjects) {
        System.out.println(devInfo.toString());
        if( devInfo.getFriendlyName() != null && !devInfo.getFriendlyName().equals("") && devInfo.getFriendlyName().contains(name)){
          return devInfo.getManufacturer() + ": " + devInfo.getFriendlyName();
        }
      }
    }
    
    return name;
  }
}

