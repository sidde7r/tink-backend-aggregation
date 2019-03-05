package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ActivationResponse {
    private String seed;

    private String configuration;

    private String activationData;

    public String getSeed() {
        return seed;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getActivationData() {
        return activationData;
    }
}
