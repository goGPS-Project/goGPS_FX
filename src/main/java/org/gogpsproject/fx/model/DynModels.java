package org.gogpsproject.fx.model;

import java.util.Arrays;
import java.util.List;

import org.gogpsproject.GoGPS;
import org.gogpsproject.fx.model.DynModel;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "DynModel", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "value", type = int.class )
})
public class DynModels {
  public static DynModel staticm;
  public static DynModel constantSpeed;
  public static DynModel constantAcceleration;
  
  public static List<DynModel> init(){
    staticm               = new DynModel("Static",   GoGPS.DYN_MODEL_STATIC);
    constantSpeed         = new DynModel("Constant Speed",   GoGPS.DYN_MODEL_CONST_SPEED);
    constantAcceleration  = new DynModel("Constant Acceleration",   GoGPS.DYN_MODEL_CONST_ACCELERATION);
    
    return Arrays.asList( 
          DynModels.staticm, 
          DynModels.constantSpeed, 
          DynModels.constantAcceleration );
  }
}
