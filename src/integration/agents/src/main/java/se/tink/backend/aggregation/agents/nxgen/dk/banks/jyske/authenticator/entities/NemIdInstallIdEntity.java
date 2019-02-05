package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdInstallIdEntity {
    private String installId;

    public String getInstallId() {
        return installId;
    }
}
