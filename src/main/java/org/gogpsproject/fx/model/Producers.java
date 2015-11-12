package org.gogpsproject.fx.model;

import java.util.Arrays;
import java.util.List;

import org.gogpsproject.fx.model.SerialPortModel;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "Producer", targetId = "", properties = {
    @Property(name = "type", type = String.class ),
    @Property(name = "name", type = String.class ),
    @Property(name = "serialPort", type = SerialPortModel.class),
    @Property(name = "filename", type = String.class ),
    @Property(name = "ftpSite",  type = FTPSite.class)
})
public class Producers {
  
  public static final String SERIAL = "serial";
  public static final String FILE   = "file";
  public static final String FTP    = "ftp";

//  public static List<Producer> init(){
//    return Arrays.asList( 
//      new Producer( Producers.SERIAL, "Serial (Ublox)", null, "", null ),
//      new Producer( Producers.FILE,   "Rinex Observation File", null, "./data/yamatogawa_rover.obs", null ),
//      new Producer( Producers.SERIAL, "Serial (Ublox)", null, "", null ),
//      new Producer( Producers.FILE,   "Rinex Navigation File", null, "./data/yamatogawa_rover.nav", null ),
//      new Producer( Producers.FTP,    "Rinex FTP", null, "", FTPSites.GarnerNavigationAuto),
//      new Producer( Producers.SERIAL, "Serial (Ublox)", null, "", null ),
//      new Producer( Producers.FILE,   "Rinex Observation File", null, "./data/yamatogawa_master.obs", null )
//    );
//  }
}

