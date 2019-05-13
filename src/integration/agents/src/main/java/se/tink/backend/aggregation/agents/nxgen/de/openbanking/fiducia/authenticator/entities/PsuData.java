package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuData {
    private String password;

    public PsuData(String password) {
        this.password = password;
    }
}
