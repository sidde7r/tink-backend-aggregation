package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DigitalCardEntity {
    private String application;
    private String deviceId;
    private String deviceType;

    public String getApplication() {
        return application;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }
}
