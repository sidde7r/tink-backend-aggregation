package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {

    private String password;

    public PsuDataEntity(String password) {
        this.password = password;
    }
}
