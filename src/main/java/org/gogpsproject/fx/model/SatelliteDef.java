package org.gogpsproject.fx.model;

import net.java.html.json.Model;
import net.java.html.json.Property;

@Model(className = "SatelliteModel", targetId = "", properties = {
    @Property(name = "id", type = int.class ),
    @Property(name = "SNR", type = float.class),
    @Property(name = "range", type = double.class),
    @Property(name = "doppler", type = float.class),
    @Property(name = "phase", type = double.class)
    })
public class SatelliteDef {
}

