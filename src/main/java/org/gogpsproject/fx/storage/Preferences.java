package org.gogpsproject.fx.storage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.gogpsproject.fx.GoGPS_Fx;
import org.gogpsproject.fx.model.DynModel;
import org.gogpsproject.fx.model.DynModels;
import org.gogpsproject.fx.model.FTPModel;
import org.gogpsproject.fx.model.FTPSites;
import org.gogpsproject.fx.model.GoGPSModel;
import org.gogpsproject.fx.model.Mode;
import org.gogpsproject.fx.model.Producer;
import org.gogpsproject.fx.model.RunModes;
import org.gogpsproject.fx.storage.StorageManager.Storage;

import net.java.html.BrwsrCtx;
import net.java.html.json.Model;
import net.java.html.json.Models;
import net.java.html.json.Property;

@Model(className = "State", targetId ="", properties = {
    @Property(name = "selectedRunMode", type = Mode.class),
    @Property(name = "selectedDynModel", type = DynModel.class),
    @Property(name = "selectedObservationProducer", type = Producer.class),
    @Property(name = "selectedNavigationProducer", type = Producer.class),
    @Property(name = "selectedNavigationFTP", type = FTPModel.class),
    @Property(name = "selectedMasterProducer", type = Producer.class),
    @Property(name = "outputFolder", type = String.class)
})
public class Preferences {
  public static State init(){
    State s;
    BrwsrCtx ctx = BrwsrCtx.findDefault(GoGPS_Fx.class);
    Storage storage = StorageManager.getStorage();
    String modelstr = storage.get("Preferences");
    if( !modelstr.equals("") &&  !modelstr.equals("undefined") ){
      InputStream is = new ByteArrayInputStream(modelstr.getBytes(StandardCharsets.UTF_8));
      try {
        s = Models.parse( ctx, State.class, is );
      }
      catch(Exception ex){
        throw new RuntimeException(ex);
      }
    }
    else {
      s = new State();
      s.setSelectedNavigationFTP(FTPSites.GarnerNavigationAuto);
      s.setSelectedRunMode(RunModes.standAlone);
      s.setSelectedDynModel(DynModels.staticm);
  //    goGPSModel.setOutputFolder( storage.get("outputFolder") );
      s.setOutputFolder( "./out" );
    }
    return s;
  }
}
