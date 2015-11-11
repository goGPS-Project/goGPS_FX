package org.gogpsproject.fx.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.model.DynModel;
import org.gogpsproject.fx.model.DynModels;
import org.gogpsproject.fx.model.FTPModel;
import org.gogpsproject.fx.model.FTPSites;
import org.gogpsproject.fx.model.GoGPSDef;
import org.gogpsproject.fx.model.GoGPSModel;
import org.gogpsproject.fx.model.Mode;
import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.Producers;
import org.gogpsproject.fx.model.RunModes;
import org.gogpsproject.fx.storage.StorageManager.Storage;

import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.OnPropertyChange;
import net.java.html.json.Property;

@Model(className = "State", targetId ="", properties = {
    @Property(name = "version", type = String.class),
    @Property(name = "runMode",  type = Integer.class),
    @Property(name = "dynModel", type = DynModel.class),
    
    @Property(name = "serialObservationProducer", type = Producer.class),
    @Property(name = "rinexObservationProducer", type = Producer.class),
    @Property(name = "serialNavigationProducer", type = Producer.class),
    @Property(name = "rinexNavigationProducer", type = Producer.class),
    @Property(name = "ftpNavigationProducer", type = Producer.class),
    @Property(name = "serialMasterProducer", type = Producer.class),
    @Property(name = "rinexMasterProducer", type = Producer.class),
    
    @Property(name = "observationProducer", type = Producer.class),
    @Property(name = "navigationProducer",  type = Producer.class),
    @Property(name = "navigationFTP",  type = FTPModel.class),
    @Property(name = "masterProducer", type = Producer.class),
    @Property(name = "outputFolder", type = String.class)
})
public class Preferences {
  
  public static State init(){
    State s = null;
    
    BrwsrCtx ctx = BrwsrCtx.findDefault(GoGPS_Fx.class);
    Storage storage = StorageManager.getStorage();
    String modelstr = storage.get("Preferences");
    if( !modelstr.equals("") &&  !modelstr.equals("undefined") ){
      InputStream is = new ByteArrayInputStream(modelstr.getBytes(StandardCharsets.UTF_8));
      try {
        s = Models.parse( ctx, State.class, is );
        // update default values to match singleton objects
        s.setDynModel( DynModels.get( s.getDynModel().getValue()) );
        s.setNavigationFTP( FTPSites.get(s.getNavigationFTP().getFtp()) );
      }
      catch(Exception ex){
        throw new RuntimeException(ex);
      }
    }
    if( s == null || !s.getVersion().equals( GoGPSDef.VERSION )){
      s = new State();
      s.setVersion( GoGPSDef.VERSION );
  //    goGPSModel.setOutputFolder( storage.get("outputFolder") );
      s.setOutputFolder( "./out" );
      s.setSerialObservationProducer( new Producer( Producers.SERIAL, "Serial (Ublox)", null, "" ));
      s.setRinexObservationProducer( new Producer( Producers.FILE,   "Rinex Observation File", null, "./data/yamatogawa_rover.obs") );
      s.setSerialNavigationProducer( new Producer( Producers.SERIAL, "Serial (Ublox)", null, "") );
      s.setRinexNavigationProducer( new Producer( Producers.FILE,   "Rinex Navigation File", null, "./data/yamatogawa_rover.nav") );
      s.setFtpNavigationProducer( new Producer( Producers.FTP,    "Rinex FTP", null, "" ) );
      s.setSerialMasterProducer( new Producer( Producers.SERIAL, "Serial (Ublox)", null, "") );
      s.setRinexMasterProducer( new Producer( Producers.FILE,   "Rinex Observation File", null, "./data/yamatogawa_master.obs") );
      s.setNavigationFTP(FTPSites.GarnerNavigationAuto);
      s.setRunMode(RunModes.standAlone.getValue());
      s.setDynModel(DynModels.staticm);
    }
    return s;
  }
  
/*  @OnPropertyChange("s")
  public static void savePreferences( GoGPSModel model ){
    Storage storage = StorageManager.getStorage();
    storage.put("Preferences", model.getS().toString());
  }*/

}
