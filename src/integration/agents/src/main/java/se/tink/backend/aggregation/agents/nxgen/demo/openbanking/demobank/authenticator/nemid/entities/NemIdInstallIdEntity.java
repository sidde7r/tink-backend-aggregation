package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.nemid.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdInstallIdEntity {
    private String installId;

    public String getInstallId() {
        return installId;
    }
}
