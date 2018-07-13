package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceInfoKeyValue {
    private String key;
    private String value;

    public DeviceInfoKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
