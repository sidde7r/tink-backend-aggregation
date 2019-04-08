package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Device {
    String vendor;
    boolean registered;
    String name;
    String model;
    String osVersion;
    String os;

    public Device(
            String vendor,
            boolean registered,
            String name,
            String model,
            String osVersion,
            String os) {
        this.vendor = vendor;
        this.registered = registered;
        this.name = name;
        this.model = model;
        this.osVersion = osVersion;
        this.os = os;
    }
}
