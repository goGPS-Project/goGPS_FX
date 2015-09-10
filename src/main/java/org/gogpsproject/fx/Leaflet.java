package org.gogpsproject.fx;

import net.java.html.BrwsrCtx;
import net.java.html.geo.Position;
import net.java.html.leaflet.ILayer;
import net.java.html.leaflet.Icon;
import net.java.html.leaflet.IconOptions;
import net.java.html.leaflet.LatLng;
import net.java.html.leaflet.Map;
import net.java.html.leaflet.MapOptions;
import net.java.html.leaflet.Marker;
import net.java.html.leaflet.MarkerOptions;
import net.java.html.leaflet.Polygon;
import net.java.html.leaflet.Popup;
import net.java.html.leaflet.PopupOptions;
import net.java.html.leaflet.TileLayer;
import net.java.html.leaflet.TileLayerOptions;
import net.java.html.leaflet.event.MouseEvent;
import net.java.html.leaflet.event.MouseListener;

public class Leaflet {
  public static BrwsrCtx ctx;
  public static Map map;
         static GeoLocation geoLocation = new GeoLocation(true);
  public static LatLng geoLocationLatLng = new LatLng(48.336614, 14.319305);
  
  static{
    if( geoLocation.isSupported() ){
      geoLocation.start();
    }
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
        Leaflet.map.setView(geoLocationLatLng, 20);
      }
    }
  }
  
  public static void init( BrwsrCtx _ctx ){
    ctx = _ctx;
    
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
      
//      // Add a polygon. When you click on the polygon a popup shows up
//      Polygon polygonLayer = new Polygon(new LatLng[] {
//              new LatLng(48.335067, 14.320660),
//              new LatLng(48.337335, 14.323642),
//              new LatLng(48.335238, 14.328942),
//              new LatLng(48.333883, 14.327612)
//      });
//      polygonLayer.addMouseListener(MouseEvent.Type.CLICK, new MouseListener() {
//          @Override
//          public void onEvent(MouseEvent ev) {
//              PopupOptions popupOptions = new PopupOptions().setMaxWidth(400);
//              Popup popup = new Popup(popupOptions);
//              popup.setLatLng(ev.getLatLng());
//              popup.setContent("The Leaflet API for Java has been created here!");
//              popup.openOn(map);
//          }
//      });
//      map.addLayer(polygonLayer);
  }
  
  
}
