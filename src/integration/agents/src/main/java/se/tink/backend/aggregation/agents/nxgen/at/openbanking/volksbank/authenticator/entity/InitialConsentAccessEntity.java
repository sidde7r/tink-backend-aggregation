package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialConsentAccessEntity implements ConsentAccessEntity {
    private String allPsd2;

    public InitialConsentAccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
