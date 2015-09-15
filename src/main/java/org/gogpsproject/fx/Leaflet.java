package org.gogpsproject.fx;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.gogpsproject.ObservationSet;
import org.gogpsproject.RoverPosition;
import org.gogpsproject.RoverPositionObs;
import org.gogpsproject.fx.model.ConsoleStreamer;

import net.java.html.BrwsrCtx;
import net.java.html.geo.Position;
import net.java.html.leaflet.FeatureGroup;
import net.java.html.leaflet.ILayer;
import net.java.html.leaflet.Icon;
import net.java.html.leaflet.IconOptions;
import net.java.html.leaflet.LatLng;
import net.java.html.leaflet.LayerGroup;
import net.java.html.leaflet.Map;
import net.java.html.leaflet.MapOptions;
import net.java.html.leaflet.Marker;
import net.java.html.leaflet.MarkerOptions;
import net.java.html.leaflet.Point;
import net.java.html.leaflet.Polygon;
import net.java.html.leaflet.Popup;
import net.java.html.leaflet.PopupOptions;
import net.java.html.leaflet.TileLayer;
import net.java.html.leaflet.TileLayerOptions;
import net.java.html.leaflet.event.MouseEvent;
import net.java.html.leaflet.event.MouseListener;

public class Leaflet {
  static final Logger logger = Logger.getLogger(Leaflet.class.getName());

  static Leaflet instance;
  static BrwsrCtx ctx;
  static Map map;
  static GeoLocation geoLocation = new GeoLocation(true);
  static LatLng geoLocationLatLng = new LatLng(48.336614, 14.319305);
  LayerGroup markers;
  
  final DecimalFormat df = new DecimalFormat("0.0000");
  final DecimalFormat dopf = new DecimalFormat("0.0");
  final SimpleDateFormat sdfHeader = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");
  final TimeZone utc = TimeZone.getTimeZone("GMT Time");

  final IconOptions redIconOption;
  final Icon redIcon; 
  final MarkerOptions redMarkerOptions;
  final IconOptions greenIconOption;
  final Icon greenIcon; 
  final MarkerOptions greenMarkerOptions;
  final IconOptions greyIconOption;
  final Icon greyIcon; 
  final MarkerOptions greyMarkerOptions;

  private Leaflet(){
    sdfHeader.setTimeZone(utc);
    final Point size = new Point(10,10);
    //final IconOptions iconOption = new IconOptions("leaflet-0.7.2/images/marker-icon.png");
    redIconOption = new IconOptions("leaflet-0.7.2/images/SNP_2752125_en_v0.png").setIconSize(size);
    greenIconOption = new IconOptions("leaflet-0.7.2/images/SNP_2752129_en_v0.png").setIconSize(size);
    greyIconOption = new IconOptions("http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png").setIconSize(size);
                     
    redIcon = new Icon(redIconOption);
    greenIcon = new Icon(greenIconOption);
    greyIcon = new Icon(greyIconOption);

    redMarkerOptions = new MarkerOptions().setIcon(redIcon);
    greenMarkerOptions = new MarkerOptions().setIcon(greenIcon);
    greyMarkerOptions = new MarkerOptions().setIcon(greyIcon);
  }
  
  public static Leaflet get(){
    if( instance == null )
      instance = new Leaflet();
    return instance;
  }
  
  static public class GeoLocation extends net.java.html.geo.Position.Handle{

    protected GeoLocation(boolean oneTime) {
      super(oneTime);
    }

    @Override
    protected void onError(Exception arg0) throws Throwable {
      // TODO Auto-generated method stub
    }

    @Override
    protected void onLocation(Position pos ) throws Throwable {
      geoLocationLatLng = new LatLng( pos.getCoords().getLatitude(), pos.getCoords().getLongitude() );
      if( map != null ){
        map.setView(geoLocationLatLng, 20);
      }
    }
  }
  
  public void init( BrwsrCtx _ctx ){
    ctx = _ctx;

    if( geoLocation.isSupported() ){
      geoLocation.start();
    }

    // Create a map zoomed to Linz.
    MapOptions mapOptions = new MapOptions()
              .setCenter(geoLocationLatLng)
              .setZoom(15)
              .setLayers(new ILayer[] { /* duckLayer */ });
    map = new Map("map", mapOptions);
    
      TileLayerOptions tlo = new TileLayerOptions()
         .setAttribution("Map &copy; 1987-2014 <a href=\"http://developer.here.com\">HERE</a>")
         .setMaxZoom(20)
         .setSubdomains("1234");
      TileLayer layer = new TileLayer("http://{s}.aerial.maps.cit.api.here.com/maptile/2.1/maptile/newest/satellite.day/{z}/{x}/{y}/256/png8?app_id=DemoAppId01082013GAL&app_code=AJKnXv84fjrb0KIHawS0Tg", tlo);
      map.addLayer(layer);
      
      markers = new FeatureGroup(new ILayer[]{});
      markers.addTo(map);
  }

  public void clearMarkers() {
    ctx.execute(new Runnable(){
      @Override
      public void run() {
        markers.clearLayers();
      }
    });
  }

  public void addMarker(RoverPosition coord) {
    ctx.execute(new Runnable(){
      @Override
      public void run() {
        // it doesn't work String to int class cast ex
//        int z = map.getMaxZoom();
        LatLng ll = new LatLng( coord.getGeodeticLatitude(), coord.getGeodeticLongitude() );
        Marker m;
        
        if( coord.gethDop()>2 )
          m = new Marker( ll, redMarkerOptions );
        else
          m = new Marker( ll, greenMarkerOptions );
        
        m.addMouseListener(MouseEvent.Type.CLICK, new MouseListener() {
          @Override
          public void onEvent(MouseEvent ev) {
            
              PopupOptions popupOptions = new PopupOptions().setMaxWidth(400);
              Popup popup = new Popup(popupOptions);
              popup.setLatLng(ev.getLatLng());
              String ppStr = "Coord: " + df.format( coord.getGeodeticLatitude()) + ", " 
                                         + df.format( coord.getGeodeticLongitude()) + ", " 
                                         + df.format( coord.getGeodeticHeight())  
                                         + "<br/>hdop: " + dopf.format( coord.gethDop()) 
                                         + "<br/>computed Time: " + coord.getRefTime().getGpsWeek() + "." + coord.getRefTime().getGpsWeekSec()  + " " + sdfHeader.format(new Date(coord.getRefTime().getMsec())) ; 
              if( coord instanceof RoverPositionObs ){
                RoverPositionObs c2 = (RoverPositionObs)coord;
                ppStr += "<br/>recorded Time: " + c2.sampleTime.getGpsWeek() + "." + c2.sampleTime.getGpsWeekSec() + " " + sdfHeader.format(new Date(c2.sampleTime.getMsec())) + "<br/>"; 
//                        +"<br/>obs: " + c2.obs.toString()
                ppStr+= "<table><tr align=right><th>satId</th><th>Code</th><th>Doppler</th><th>SNR</th></tr>";
                for(int i=0;i<c2.obs.getNumSat();i++){
                  ObservationSet os = c2.obs.getSatByIdx(i);
                  ppStr += "<tr align=right>" + 
//                      "satType:"+ os.getSatType() +
                      "<td>" + os.getSatID() + "</td>" 
                      +"<td>"+ (long)(os.getCodeC(0)) + "</td>"
//                    +" cP:"+ df.format(os.getCodeP(0))
//                    +" Ph:"+fd(os.getPhaseCycles(0))
                    +"<td>"+(long)(os.getDoppler(0)) + "</td>"
                    +"<td>"+dopf.format(os.getSignalStrength(0)) + "</td>"
//                    +" LL:"+fd(os.getLossLockInd(0))
//                    +" LL2:"+fd(os.getLossLockInd(1))
                    + "</tr>";
                }
                ppStr+= "</table>";

                ppStr += "<br/>index: " + c2.index;
              }
              ppStr = ppStr.replace("/(\r\n|\n|\r)/g","<br />");
              popup.setContent(ppStr);
              popup.openOn(map);
          }
        });
        
        markers.addLayer(m);
        map.setView(ll, 20);
      }
    });
  }
  
  
}
