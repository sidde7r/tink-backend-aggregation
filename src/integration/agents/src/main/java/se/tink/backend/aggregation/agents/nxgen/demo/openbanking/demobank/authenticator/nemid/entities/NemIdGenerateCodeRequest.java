package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdGenerateCodeRequest {
    private boolean pushEnabled;

    public NemIdGenerateCodeRequest setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
        return this;
    }
}
