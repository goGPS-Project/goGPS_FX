package org.gogpsproject.model;

import org.gogpsproject.GoGPS;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "DynModel", targetId = "", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "value", type = int.class )
})
public class DynModels {
  public static DynModel staticm               = new DynModel("Static",   GoGPS.DYN_MODEL_STATIC);
  public static DynModel constantSpeed         = new DynModel("Constant Speed",   GoGPS.DYN_MODEL_CONST_SPEED);
  public static DynModel constantAcceleration  = new DynModel("Constant Acceleration",   GoGPS.DYN_MODEL_CONST_ACCELERATION);
}
