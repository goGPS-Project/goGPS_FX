package org.gogpsproject.fx.model;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "SerialPortModel", targetId="", properties = {
    @Property(name = "name", type = String.class ),
    @Property(name = "friendlyName", type = String.class ),
    @Property(name = "speed", type = int.class),
    @Property(name = "measurementRate", type = int.class),
    @Property(name = "connected", type = boolean.class)
})
public class SerialPortDef {
}

