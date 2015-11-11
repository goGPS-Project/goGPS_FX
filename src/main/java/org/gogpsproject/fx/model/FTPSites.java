package org.gogpsproject.fx.model;

import java.util.Arrays;
import java.util.List;

import org.gogpsproject.parser.rinex.RinexNavigation;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "FTPModel", targetId="", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "ftp", type = String.class)
})
public class FTPSites {
  
  /*
  public final static String GARNER_NAVIGATION_AUTO = "ftp://garner.ucsd.edu/pub/nav/${yyyy}/${ddd}/auto${ddd}0.${yy}n.Z";
  public final static String IGN_MULTI_NAVIGATION_DAILY = "ftp://igs.ign.fr/pub/igs/data/campaign/mgex/daily/rinex3/${yyyy}/${ddd}/brdm${ddd}0.${yy}p.Z";
  public final static String GARNER_NAVIGATION_ZIM2 = "ftp://garner.ucsd.edu/pub/nav/${yyyy}/${ddd}/zim2${ddd}0.${yy}n.Z";
  public final static String IGN_NAVIGATION_HOURLY_ZIM2 = "ftp://igs.ensg.ign.fr/pub/igs/data/hourly/${yyyy}/${ddd}/zim2${ddd}${h}.${yy}n.Z";
  public final static String NASA_NAVIGATION_DAILY = "ftp://cddis.gsfc.nasa.gov/pub/gps/data/daily/${yyyy}/${ddd}/${yy}n/brdc${ddd}0.${yy}n.Z";
   */
  public static FTPModel GarnerNavigationAuto;
  public static FTPModel NasaNavigationDaily;
  
  public static List<FTPModel> init(){
    GarnerNavigationAuto = new FTPModel( "Garner Navigation Auto", RinexNavigation.GARNER_NAVIGATION_AUTO );
    NasaNavigationDaily = new FTPModel( "Nasa Navigation Daily", RinexNavigation.NASA_NAVIGATION_DAILY );
    
    return Arrays.asList( 
          FTPSites.GarnerNavigationAuto,
          FTPSites.NasaNavigationDaily
    );
  }
  
  public static FTPModel get( String ftp ){
    switch(ftp){
      case RinexNavigation.NASA_NAVIGATION_DAILY:
        return NasaNavigationDaily;
       default:
         return GarnerNavigationAuto;
    }
  }
}

