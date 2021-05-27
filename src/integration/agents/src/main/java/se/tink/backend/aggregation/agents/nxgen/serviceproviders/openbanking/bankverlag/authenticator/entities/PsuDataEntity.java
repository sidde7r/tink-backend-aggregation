package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PsuDataEntity {

    private String password;

    public PsuDataEntity(String password) {
        this.password = password;
    }
}
