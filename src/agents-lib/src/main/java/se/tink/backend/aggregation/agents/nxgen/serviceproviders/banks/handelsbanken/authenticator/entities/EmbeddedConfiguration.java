package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbeddedConfiguration {

    @JsonProperty("analytics-config")
    private HandelsbankenAnalyticsConfiguration configuration;

    @JsonIgnore
    public HandelsbankenClearingNumber getClearingNumber() {
        return configuration.getShbClearingNo();
    }
}
