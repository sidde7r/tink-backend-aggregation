package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BooliIntegrationConfiguration {
    @JsonProperty
    private String callerId;
    @JsonProperty
    private String privateKey;

    public String getCallerId() {
        return callerId;
    }

    public String getPrivateKey() {
        return privateKey;
    }
}
