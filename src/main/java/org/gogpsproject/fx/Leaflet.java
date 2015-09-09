package org.gogpsproject.fx;

import net.java.html.BrwsrCtx;
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
  
  public Leaflet() {
    // TODO Auto-generated constructor stub
  }
  
  public static Map map;
  
  public static void init( BrwsrCtx _ctx ){
    ctx = _ctx;
    
    // Create a map zoomed to Linz.
    MapOptions mapOptions = new MapOptions()
              .setCenter(new LatLng(48.336614, 14.319305))
              .setZoom(15)
              .setLayers(new ILayer[] { /* duckLayer */ });
    map = new Map("map", mapOptions);
    
//      // add a tile layer to the map
      TileLayerOptions tlo = new TileLayerOptions();
      tlo.setAttribution("Map data &copy; <a href='http://www.thunderforest.com/opencyclemap/'>OpenCycleMap</a> contributors, "
              + "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
              + "Imagery © <a href='http://www.thunderforest.com/'>Thunderforest</a>");
      tlo.setMaxZoom(18);
      TileLayer layer = new TileLayer("http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png", tlo);
      map.addLayer(layer);
      
      // Set a marker with a user defined icon
//      Icon icon = new Icon(new IconOptions("leaflet-0.7.2/images/marker-icon.png"));
//      Marker m = new Marker(new LatLng(48.336614, 14.33), new MarkerOptions().setIcon(icon));
//      m.addTo(map);
      
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
