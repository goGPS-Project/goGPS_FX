package org.gogpsproject.fx.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gogpsproject.GoGPS;
import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.model.DynModel;
import org.gogpsproject.fx.model.DynModels;
import org.gogpsproject.fx.model.FTPModel;
import org.gogpsproject.fx.model.FTPSites;
import org.gogpsproject.fx.model.GoGPSDef;
import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.Producers;
import org.gogpsproject.fx.storage.StorageManager.Storage;

import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;

@Model(className = "Preferences", targetId ="", properties = {
    @Property(name = "version", type = String.class),
    @Property(name = "runMode",  type = int.class),
    @Property(name = "dynModel", type = int.class),
    @Property(name = "observationProducer", type = Producer.class),
    @Property(name = "navigationProducer",  type = Producer.class),
    @Property(name = "masterProducer", type = Producer.class),
    @Property(name = "outputFolder", type = String.class),
    
//    @Property(name = "producers", type = Producer.class, array=true),
    @Property(name = "serialObservationProducer", type = Producer.class),
    @Property(name = "rinexObservationProducer",  type = Producer.class),
    @Property(name = "serialNavigationProducer",  type = Producer.class),
    @Property(name = "rinexNavigationProducer",   type = Producer.class),
    @Property(name = "ftpNavigationProducer",     type = Producer.class),
    @Property(name = "serialMasterProducer",      type = Producer.class),
    @Property(name = "rinexMasterProducer",       type = Producer.class),
    
})
public class PreferencesDef {
 
  private static final Logger logger = Logger.getLogger(PreferencesDef.class.getName());

  public static Preferences init(){
    Preferences s = null;
    
    try {
      BrwsrCtx ctx = BrwsrCtx.findDefault(GoGPS_Fx.class);
      Storage storage = StorageManager.getStorage();
      String modelstr = storage.get("Preferences");
      if( !modelstr.equals("") &&  !modelstr.equals("undefined") ){
        InputStream is = new ByteArrayInputStream(modelstr.getBytes(StandardCharsets.UTF_8));
          s = Models.parse( ctx, Preferences.class, is );
        }
    }
    catch(Exception ex){
      logger.log(  Level.WARNING, ex.getMessage(), ex );
    }
    finally{
      if( s == null || !s.getVersion().equals( GoGPSDef.VERSION )){
        s = new Preferences();
        s.setVersion( GoGPSDef.VERSION );
//        s.getProducers().addAll( Producers.init() );
        s.setSerialObservationProducer( new Producer( Producers.SERIAL, "Serial (Ublox)", null, "", null ));
        s.setRinexObservationProducer( new Producer( Producers.FILE,    "Rinex Observation File", null, "./data/yamatogawa_rover.obs", null ));
        s.setSerialNavigationProducer( new Producer( Producers.SERIAL,  "Serial (Ublox)", null, "", null ));
        s.setRinexNavigationProducer( new Producer( Producers.FILE,     "Rinex Navigation File", null, "./data/yamatogawa_rover.nav", null ));
        s.setFtpNavigationProducer( new Producer( Producers.FTP,        "Rinex FTP", null, "", FTPSites.GarnerNavigationAuto));
        s.setSerialMasterProducer( new Producer( Producers.SERIAL,      "Serial (Ublox)", null, "", null ));
        s.setRinexMasterProducer( new Producer( Producers.FILE,         "Rinex Observation File", null, "./data/yamatogawa_master.obs", null ));
        
        s.setRunMode(GoGPS.RUN_MODE_STANDALONE);
        s.setDynModel(GoGPS.DYN_MODEL_STATIC);
        s.setOutputFolder( "./out" );
      }
    }
    return s;
  }
  
/*  @OnPropertyChange("s")
  public static void savePreferences( GoGPSModel model ){
    Storage storage = StorageManager.getStorage();
    storage.put("Preferences", model.getS().toString());
  }*/

}
