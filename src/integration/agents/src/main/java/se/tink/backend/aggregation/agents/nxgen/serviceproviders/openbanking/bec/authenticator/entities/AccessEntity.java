package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessEntity {

    private String allPsd2;

    public String getAllPsd2() {
        return allPsd2;
    }

    public AccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
